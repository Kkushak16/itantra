package com.example.itantra.data.ml

import android.content.Context

interface SpeechEngine {
    suspend fun initialize(context: Context)
    suspend fun transcribe(pcmData: ShortArray, language: String): TranscriptionResult
    suspend fun synthesize(text: String, language: String): ShortArray
    fun isReady(): Boolean
    fun release()
}

data class TranscriptionResult(
    val text: String,
    val language: String,
    val confidence: Float
)
