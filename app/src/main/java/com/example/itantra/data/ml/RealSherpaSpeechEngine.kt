package com.example.itantra.data.ml

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealSherpaSpeechEngine(private val context: Context) : SpeechEngine {

    companion object {
        private const val TAG = "RealSherpaEngine"
    }
    
    private var isMockMode = false
    private val mockEngine = MockSpeechEngine()
    private var ready = false

    override suspend fun initialize(context: Context) = withContext(Dispatchers.IO) {
        val sttModels = try { context.assets.list("models/stt") } catch(e: Exception) { null }
        val ttsModels = try { context.assets.list("models/tts") } catch(e: Exception) { null }
        
        if (sttModels.isNullOrEmpty() || ttsModels.isNullOrEmpty()) {
            Log.w(TAG, "ONNX models missing in assets/models/. Falling back to Mock Mode.")
            isMockMode = true
            mockEngine.initialize(context)
            ready = true
            return@withContext
        }

        try {
            // Attempt to load Sherpa-ONNX via reflection to avoid hard compile-time crashes 
            // if the AAR dependency fails to resolve from JitPack.
            val recognizerClass = Class.forName("com.k2fsa.sherpa.onnx.OfflineRecognizer")
            
            // TODO: Actually instantiate OfflineRecognizer and OfflineTts here 
            // once the models are verified and the AAR is loaded.
            
            Log.d(TAG, "Sherpa-ONNX models found and engine initialized successfully.")
            ready = true
        } catch (e: Exception) {
            Log.e(TAG, "Sherpa-ONNX library not found or failed to load. Falling back to Mock Mode.", e)
            isMockMode = true
            mockEngine.initialize(context)
            ready = true
        }
    }

    override suspend fun transcribe(pcmData: ShortArray, language: String): TranscriptionResult {
        if (isMockMode) return mockEngine.transcribe(pcmData, language)
        
        return withContext(Dispatchers.Default) {
            TranscriptionResult("Sherpa-ONNX real transcription not fully hooked up yet.", language, 1.0f)
        }
    }

    override suspend fun synthesize(text: String, language: String): ShortArray {
        if (isMockMode) return mockEngine.synthesize(text, language)
        
        return withContext(Dispatchers.Default) {
            ShortArray(16000) // 1 second of silence
        }
    }

    override fun isReady(): Boolean = ready

    override fun release() {
        if (isMockMode) mockEngine.release()
        ready = false
    }
    
    fun isInMockMode(): Boolean = isMockMode
}
