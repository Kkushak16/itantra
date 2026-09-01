package com.example.itantra.data.ml

import android.content.Context
import android.util.Log
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealSherpaSpeechEngine(private val context: Context) : SpeechEngine {

    companion object {
        private const val TAG = "RealSherpaEngine"
    }
    
    private var isMockMode = false
    private val mockEngine = MockSpeechEngine()
    private var ready = false
    
    private var recognizer: OfflineRecognizer? = null
    private val ttsEngines = mutableMapOf<String, OfflineTts>()

    private fun assetExists(context: Context, path: String): Boolean {
        return try {
            context.assets.open(path).use { true }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun initialize(context: Context) = withContext(Dispatchers.IO) {
        // Check if the core STT and TTS models exist
        val sttEncoderExists = assetExists(context, "models/stt/whisper/tiny-encoder.int8.onnx")
        val sttDecoderExists = assetExists(context, "models/stt/whisper/tiny-decoder.int8.onnx")
        val sttTokensExists = assetExists(context, "models/stt/whisper/tiny-tokens.txt")
        val enTtsExists = assetExists(context, "models/tts/en/en_US-amy-low.onnx")
        val enTtsTokensExists = assetExists(context, "models/tts/en/tokens.txt")
        
        val allRequiredModelsExist = sttEncoderExists && sttDecoderExists && sttTokensExists && enTtsExists && enTtsTokensExists

        if (!allRequiredModelsExist) {
            Log.w(TAG, "Core ONNX models missing in assets/models/. Falling back to Mock Mode. (Encoder: $sttEncoderExists, Decoder: $sttDecoderExists, STT Tokens: $sttTokensExists, EN TTS: $enTtsExists, EN TTS Tokens: $enTtsTokensExists)")
            isMockMode = true
            mockEngine.initialize(context)
            ready = true
            return@withContext
        }

        try {
            // 1. Initialize Whisper STT (OfflineRecognizer)
            val config = OfflineRecognizerConfig(
                featConfig = FeatureConfig(
                    sampleRate = 16000,
                    featureDim = 80
                ),
                modelConfig = OfflineModelConfig(
                    whisper = OfflineWhisperModelConfig(
                        encoder = "models/stt/whisper/tiny-encoder.int8.onnx",
                        decoder = "models/stt/whisper/tiny-decoder.int8.onnx",
                        language = "en",
                        task = "transcribe"
                    ),
                    tokens = "models/stt/whisper/tiny-tokens.txt",
                    numThreads = 2,
                    modelType = "whisper"
                )
            )
            recognizer = OfflineRecognizer(context.assets, config)
            Log.d(TAG, "STT (Whisper) initialized successfully.")
            
            // 2. Initialize English TTS
            val enTtsConfig = OfflineTtsConfig(
                model = OfflineTtsModelConfig(
                    vits = OfflineTtsVitsModelConfig(
                        model = "models/tts/en/en_US-amy-low.onnx",
                        tokens = "models/tts/en/tokens.txt",
                        dataDir = "models/tts/en/espeak-ng-data"
                    ),
                    numThreads = 2
                )
            )
            ttsEngines["en"] = OfflineTts(context.assets, enTtsConfig)
            Log.d(TAG, "English TTS initialized successfully.")
            
            // 3. Initialize Hindi TTS (optional — skip gracefully if missing)
            val hiTtsExists = assetExists(context, "models/tts/hi/hi_IN-priyamvada-medium.onnx")
            if (hiTtsExists) {
                val hiTtsConfig = OfflineTtsConfig(
                    model = OfflineTtsModelConfig(
                        vits = OfflineTtsVitsModelConfig(
                            model = "models/tts/hi/hi_IN-priyamvada-medium.onnx",
                            tokens = "models/tts/hi/tokens.txt",
                            dataDir = "models/tts/hi/espeak-ng-data"
                        ),
                        numThreads = 2
                    )
                )
                ttsEngines["hi"] = OfflineTts(context.assets, hiTtsConfig)
                Log.d(TAG, "Hindi TTS initialized successfully.")
            } else {
                Log.w(TAG, "Hindi TTS model not found — skipping. Hindi will fall back to English TTS.")
            }
            
            Log.d(TAG, "Sherpa-ONNX engine initialized. STT=OK, TTS(en)=OK, TTS(hi)=${if (hiTtsExists) "OK" else "SKIPPED"}")
            ready = true
        } catch (e: Throwable) {
            Log.e(TAG, "Sherpa-ONNX library failed to load. Falling back to Mock Mode.", e)
            isMockMode = true
            mockEngine.initialize(context)
            ready = true
        }
    }

    override suspend fun transcribe(pcmData: ShortArray, language: String): TranscriptionResult {
        if (isMockMode) return mockEngine.transcribe(pcmData, language)
        
        return withContext(Dispatchers.Default) {
            try {
                val floatSamples = FloatArray(pcmData.size) { pcmData[it] / 32768.0f }
                val stream = recognizer?.createStream() ?: return@withContext TranscriptionResult("", language, 0.0f)
                
                stream.acceptWaveform(floatSamples, 16000)
                recognizer?.decode(stream)
                val result = recognizer?.getResult(stream)
                val text = result?.text ?: ""
                stream.release()
                
                TranscriptionResult(text, language, 1.0f)
            } catch (e: Exception) {
                Log.e(TAG, "Transcription error", e)
                TranscriptionResult("", language, 0.0f)
            }
        }
    }

    override suspend fun synthesize(text: String, language: String): ShortArray {
        if (isMockMode) return mockEngine.synthesize(text, language)
        
        return withContext(Dispatchers.Default) {
            try {
                val tts = ttsEngines[language] ?: ttsEngines["en"] ?: return@withContext ShortArray(0)
                val audio = tts.generate(text = text, sid = 0, speed = 1.0f)
                val samples = audio.samples
                ShortArray(samples.size) { (samples[it] * 32767.0f).toInt().toShort() }
            } catch (e: Exception) {
                Log.e(TAG, "Synthesis error", e)
                ShortArray(0)
            }
        }
    }

    override fun isReady(): Boolean = ready

    override fun release() {
        if (isMockMode) {
            mockEngine.release()
        } else {
            recognizer?.release()
            ttsEngines.values.forEach { it.release() }
        }
        ready = false
    }
    
    fun isInMockMode(): Boolean = isMockMode
}
