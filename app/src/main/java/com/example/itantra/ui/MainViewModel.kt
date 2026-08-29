package com.example.itantra.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.itantra.data.model.SupportedLanguage
import com.example.itantra.domain.ConnectionState
import com.example.itantra.domain.MessageEntry
import com.example.itantra.domain.PlaybackState
import com.example.itantra.domain.RecordingState
import com.example.itantra.domain.TransceiverEngine
import com.example.itantra.data.network.PeerInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel bridging TransceiverEngine to Jetpack Compose.
 * Exposes all engine state as StateFlows for the MainScreen.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    val engine = TransceiverEngine(application.applicationContext)

    // State
    val messages: StateFlow<List<MessageEntry>> = engine.messages
    val recordingState: StateFlow<RecordingState> = engine.recordingState
    val playbackState: StateFlow<PlaybackState> = engine.playbackState
    val audioLevel: StateFlow<Float> = engine.audioLevel
    val selectedLanguage: StateFlow<SupportedLanguage> = engine.selectedLanguage
    val isEmergencyMode: StateFlow<Boolean> = engine.isEmergencyMode
    val senderId: StateFlow<String> = engine.senderId

    val connectionState: StateFlow<ConnectionState> = engine.transceiver.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionState.Disconnected)

    val discoveredPeers: StateFlow<List<PeerInfo>> = engine.transceiver.discoveredPeers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isMockMode: StateFlow<Boolean> = engine.isMockModeEngine

    init {
        engine.initialize()
    }

    // --- Actions ---

    fun startRecording() = engine.startRecording()

    fun stopRecordingAndTransmit() = engine.stopRecordingAndTransmit()

    fun selectLanguage(language: SupportedLanguage) = engine.setLanguage(language)

    fun toggleEmergency() = engine.toggleEmergencyMode()

    fun replayMessage(messageId: String) = engine.replayMessage(messageId)

    fun startServer() = engine.startServer()

    fun connectToHost(address: String) = engine.connectToHost(address)


    fun startScanning() = engine.startScanning()

    fun stopScanning() = engine.transceiver.stopScanning()

    fun disconnect() = engine.disconnectNetwork()

    override fun onCleared() {
        super.onCleared()
        engine.release()
    }
}
