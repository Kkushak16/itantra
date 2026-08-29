package com.example.itantra.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VoicePacket(
    val senderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String,
    val text: String,
    val isEmergency: Boolean = false,
    val confidence: Float = 1.0f
)
