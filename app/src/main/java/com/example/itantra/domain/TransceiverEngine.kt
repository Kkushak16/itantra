package com.example.itantra.domain

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.itantra.data.audio.AudioFrame
import com.example.itantra.data.audio.AudioPlayer
import com.example.itantra.data.audio.AudioRecorder
import com.example.itantra.data.ml.MockSpeechEngine
import com.example.itantra.data.ml.RealSherpaSpeechEngine
import com.example.itantra.data.ml.SpeechEngine
import com.example.itantra.data.model.SupportedLanguage
import com.example.itantra.data.model.VoicePacket
import com.example.itantra.data.network.P2PTransceiver
import com.example.itantra.data.network.SocketTransceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Central orchestrator that wires the full transceiver pipeline:
 *
 *   OUTGOING: Record → STT → VoicePacket → P2P Send
 *   INCOMING: P2P Receive → VoicePacket → TTS → Playback
 *
 * This class manages the lifecycle of all components and maintains
 * the complete message history as observable state for the UI.
 */
class TransceiverEngine(private val context: Context) {

    companion object {
        private const val TAG = "TransceiverEngine"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Components
    val recorder = AudioRecorder(context)
    val player = AudioPlayer(context)
    val speechEngine: SpeechEngine = RealSherpaSpeechEngine(context)
    val transceiver: P2PTransceiver = SocketTransceiver()

    // State
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntry>>(emptyList())
    val messages: StateFlow<List<MessageEntry>> = _messages.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(SupportedLanguage.HINDI)
    val selectedLanguage: StateFlow<SupportedLanguage> = _selectedLanguage.asStateFlow()

    private val _isEmergencyMode = MutableStateFlow(false)
    val isEmergencyMode: StateFlow<Boolean> = _isEmergencyMode.asStateFlow()

    private val _senderId = MutableStateFlow("Unit-${Build.MODEL.take(8)}")
    val senderId: StateFlow<String> = _senderId.asStateFlow()

    private val _isMockModeEngine = MutableStateFlow(true)
    val isMockModeEngine: StateFlow<Boolean> = _isMockModeEngine.asStateFlow()

    // Recording state
    private var recordingJob: Job? = null
    private val recordedFrames = mutableListOf<ShortArray>()

    // Receiver listener
    private var receiverJob: Job? = null

    /**
     * Initialize all engine components.
     * Call once during app startup.
     */
    fun initialize() {
        scope.launch {
            try {
                speechEngine.initialize(context)
                _isMockModeEngine.value = (speechEngine as? RealSherpaSpeechEngine)?.isInMockMode() ?: true
                Log.d(TAG, "TransceiverEngine initialized (MockMode=${_isMockModeEngine.value})")
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to initialize speech engine: ${e.message}", e)
            }
        }

        // Listen for incoming packets
        receiverJob = scope.launch {
            transceiver.incomingPackets.collect { packet ->
                handleIncomingPacket(packet)
            }
        }
    }

    /**
     * Start Push-to-Talk recording.
     * Captures audio frames and accumulates them for STT processing on release.
     */
    fun startRecording() {
        if (_recordingState.value != RecordingState.Idle) return

        recordedFrames.clear()
        _recordingState.value = RecordingState.Recording

        recordingJob = scope.launch {
            recorder.recordAudioFrames().collect { frame: AudioFrame ->
                recordedFrames.add(frame.samples)
                _audioLevel.value = frame.peakLevel
            }
        }

        Log.d(TAG, "PTT recording started")
    }

    /**
     * Stop Push-to-Talk recording and process the captured audio.
     * Pipeline: Stop Recording → STT → Transmit
     */
    fun stopRecordingAndTransmit() {
        recorder.stopRecording()
        recordingJob?.cancel()
        _audioLevel.value = 0f

        if (recordedFrames.isEmpty()) {
            _recordingState.value = RecordingState.Idle
            Log.d(TAG, "No audio frames recorded, skipping")
            return
        }

        _recordingState.value = RecordingState.Transcribing
        Log.d(TAG, "PTT released: ${recordedFrames.size} frames captured, transcribing...")

        scope.launch {
            try {
                // Concatenate all frames
                val totalSamples = recordedFrames.sumOf { it.size }
                val allSamples = ShortArray(totalSamples)
                var offset = 0
                for (frame in recordedFrames) {
                    frame.copyInto(allSamples, offset)
                    offset += frame.size
                }

                // STT
                val language = _selectedLanguage.value.isoCode
                val result = speechEngine.transcribe(allSamples, language)

                if (result.text.isBlank()) {
                    Log.d(TAG, "STT returned empty text, skipping transmission")
                    _recordingState.value = RecordingState.Idle
                    return@launch
                }

                // Create voice packet
                val packet = VoicePacket(
                    senderId = _senderId.value,
                    language = language,
                    text = result.text,
                    isEmergency = _isEmergencyMode.value,
                    confidence = result.confidence
                )

                // Add to message history
                val entry = MessageEntry(
                    packet = packet,
                    direction = Direction.OUTGOING,
                    pcmAudio = allSamples
                )
                _messages.update { it + entry }

                // Transmit
                _recordingState.value = RecordingState.Transmitting
                try {
                    transceiver.sendPacket(packet)
                    Log.d(TAG, "Packet transmitted: \"${packet.text.take(30)}...\"")
                } catch (e: Exception) {
                    Log.e(TAG, "Transmission failed: ${e.message}")
                    // Message is still in history even if send fails
                }

                _recordingState.value = RecordingState.Idle
            } catch (e: Exception) {
                Log.e(TAG, "Processing pipeline error: ${e.message}", e)
                _recordingState.value = RecordingState.Idle
            }
        }
    }

    /**
     * Handle an incoming voice packet from a peer.
     * Pipeline: Receive → TTS → Playback
     */
    private suspend fun handleIncomingPacket(packet: VoicePacket) {
        Log.d(TAG, "Incoming: [${packet.senderId}] (${packet.language}) \"${packet.text.take(40)}...\"" +
                if (packet.isEmergency) " [EMERGENCY]" else "")

        // TTS
        _playbackState.value = PlaybackState.Synthesizing(packet.language)
        val audio = try {
            speechEngine.synthesize(packet.text, packet.language)
        } catch (e: Exception) {
            Log.e(TAG, "TTS failed: ${e.message}", e)
            ShortArray(0)
        }

        // Add to message history
        val entry = MessageEntry(
            packet = packet,
            direction = Direction.INCOMING,
            pcmAudio = audio
        )
        _messages.update { it + entry }

        // Playback
        if (audio.isNotEmpty()) {
            _playbackState.value = PlaybackState.Playing(entry.id)
            player.play(audio, isEmergency = packet.isEmergency)
        }

        _playbackState.value = PlaybackState.Idle
    }

    /**
     * Replay audio for a specific message.
     */
    fun replayMessage(messageId: String) {
        val entry = _messages.value.find { it.id == messageId } ?: return
        val audio = entry.pcmAudio ?: return

        scope.launch {
            _playbackState.value = PlaybackState.Playing(messageId)
            player.play(audio, isEmergency = entry.packet.isEmergency)
            _playbackState.value = PlaybackState.Idle
        }
    }

    // --- Configuration ---

    fun setLanguage(language: SupportedLanguage) {
        _selectedLanguage.value = language
        Log.d(TAG, "Language changed to: ${language.displayName}")
    }

    fun toggleEmergencyMode() {
        _isEmergencyMode.update { !it }
        Log.d(TAG, "Emergency mode: ${_isEmergencyMode.value}")
    }

    fun setEmergencyMode(enabled: Boolean) {
        _isEmergencyMode.value = enabled
    }

    fun setSenderId(id: String) {
        _senderId.value = id
    }

    // --- Network ---

    fun startServer(port: Int = 8888) {
        scope.launch {
            transceiver.startServer(port, _senderId.value)
        }
    }

    fun connectToHost(address: String, port: Int = 8888) {
        scope.launch {
            transceiver.connectToHost(address, port)
        }
    }

    fun startScanning() {
        scope.launch {
            transceiver.startScanning()
        }
    }

    fun disconnectNetwork() {
        transceiver.disconnect()
    }

    /**
     * Release all resources.
     */
    fun release() {
        recorder.stopRecording()
        recordingJob?.cancel()
        receiverJob?.cancel()
        transceiver.disconnect()
        speechEngine.release()
        Log.d(TAG, "TransceiverEngine released")
    }
}
