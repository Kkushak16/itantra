package com.example.itantra.domain

/**
 * Represents the P2P connection lifecycle.
 */
sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Listening : ConnectionState
    data object Discovering : ConnectionState
    data class Connecting(val peerName: String) : ConnectionState
    data class Connected(val peerName: String, val peerAddress: String) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

/**
 * Represents the microphone/STT recording lifecycle.
 */
sealed interface RecordingState {
    data object Idle : RecordingState
    data object Recording : RecordingState
    data object Transcribing : RecordingState
    data object Transmitting : RecordingState
}

/**
 * Represents the TTS/audio playback lifecycle.
 */
sealed interface PlaybackState {
    data object Idle : PlaybackState
    data class Synthesizing(val language: String) : PlaybackState
    data class Playing(val messageId: String) : PlaybackState
}
