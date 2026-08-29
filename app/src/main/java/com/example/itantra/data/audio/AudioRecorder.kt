package com.example.itantra.data.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import kotlin.coroutines.coroutineContext
import kotlin.math.sqrt

class AudioRecorder(private val context: Context) {

    companion object {
        private const val TAG = "AudioRecorder"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var isRecording = false

    fun recordAudioFrames(): Flow<AudioFrame> = flow {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "RECORD_AUDIO permission not granted")
            return@flow
        }

        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val bufferSize = maxOf(minBufferSize, SAMPLE_RATE / 10) // 100ms buffer

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord failed to initialize")
            return@flow
        }

        val buffer = ShortArray(bufferSize / 2) // Short is 2 bytes
        
        isRecording = true
        audioRecord.startRecording()

        try {
            while (isRecording && coroutineContext.isActive) {
                val readResult = audioRecord.read(buffer, 0, buffer.size)
                if (readResult > 0) {
                    val samples = buffer.copyOf(readResult)
                    val peakLevel = calculatePeakLevel(samples)
                    emit(AudioFrame(samples, peakLevel))
                }
                yield()
            }
        } finally {
            isRecording = false
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord.stop()
                audioRecord.release()
            }
        }
    }.flowOn(Dispatchers.IO)

    fun stopRecording() {
        isRecording = false
    }

    private fun calculatePeakLevel(samples: ShortArray): Float {
        var sum = 0.0f
        for (sample in samples) {
            val normalized = sample / 32768f
            sum += normalized * normalized
        }
        val rms = sqrt(sum / samples.size.coerceAtLeast(1))
        return minOf(rms * 5f, 1.0f) // Scale for visualizer
    }
}
