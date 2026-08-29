package com.example.itantra.data.ml

import android.content.Context
import kotlinx.coroutines.delay
import kotlin.math.sin

class MockSpeechEngine : SpeechEngine {

    private var ready = false

    override suspend fun initialize(context: Context) {
        delay(500) // Simulate loading models
        ready = true
    }

    override suspend fun transcribe(pcmData: ShortArray, language: String): TranscriptionResult {
        delay(300) // Simulate STT inference
        
        // Provide mock transcriptions based on language
        val text = when (language) {
            "hi" -> "बाढ़ का पानी बढ़ रहा है, तुरंत सुरक्षित स्थान पर जाएं।"
            "gu" -> "પૂરનું પાણી વધી રહ્યું છે, તરત જ સુરક્ષિત જગ્યાએ જાઓ."
            "en" -> "Flood waters are rising, please move to a safe location immediately."
            else -> "Simulated transcription in $language"
        }
        
        return TranscriptionResult(
            text = text,
            language = language,
            confidence = 0.95f
        )
    }

    override suspend fun synthesize(text: String, language: String): ShortArray {
        delay(400) // Simulate TTS inference

        // Generate a 1-second 440Hz sine wave (A4 note) as mock audio
        val sampleRate = 16000
        val durationSeconds = 1
        val numSamples = sampleRate * durationSeconds
        val samples = ShortArray(numSamples)
        val frequency = 440.0
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val v = sin(2.0 * Math.PI * frequency * t)
            samples[i] = (v * Short.MAX_VALUE).toInt().toShort()
        }
        
        return samples
    }

    override fun isReady(): Boolean = ready

    override fun release() {
        ready = false
    }
}
