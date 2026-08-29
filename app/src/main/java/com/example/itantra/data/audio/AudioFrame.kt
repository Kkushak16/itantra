package com.example.itantra.data.audio

data class AudioFrame(
    val samples: ShortArray,
    val peakLevel: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioFrame

        if (!samples.contentEquals(other.samples)) return false
        if (peakLevel != other.peakLevel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = samples.contentHashCode()
        result = 31 * result + peakLevel.hashCode()
        return result
    }
}
