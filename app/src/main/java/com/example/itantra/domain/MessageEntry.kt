package com.example.itantra.domain

import com.example.itantra.data.model.VoicePacket
import java.util.UUID

/**
 * Direction of a message relative to this device.
 */
enum class Direction {
    OUTGOING,
    INCOMING
}

/**
 * A single message entry in the communication log.
 * Contains the original voice packet, its direction,
 * and optional PCM audio data for replay functionality.
 */
data class MessageEntry(
    val id: String = UUID.randomUUID().toString(),
    val packet: VoicePacket,
    val direction: Direction,
    val pcmAudio: ShortArray? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageEntry) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
