package com.whispergames.app.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.log10

class MicVolumeEngine {

    companion object {
        private const val TAG = "MicVolumeEngine"
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        ) * 2
        private const val MAX_AMPLITUDE = 32767.0
        private const val SMOOTHING_FACTOR = 0.3f
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile
    private var currentVolume: Float = 0.0f

    @Volatile
    private var currentAmplitude: Double = 0.0

    fun start(): Boolean {
        if (isRecording) {
            Log.w(TAG, "Engine already running")
            return true
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                audioRecord?.release()
                audioRecord = null
                return false
            }

            audioRecord?.startRecording()
            isRecording = true
            startProcessing()

            Log.d(TAG, "Engine started")
            return true

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission not granted", e)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start", e)
            audioRecord?.release()
            audioRecord = null
            return false
        }
    }

    fun stop() {
        if (!isRecording) {
            return
        }

        isRecording = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            currentVolume = 0.0f
            currentAmplitude = 0.0
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping", e)
        }
    }

    fun getNormalizedVolume(): Float {
        return currentVolume.coerceIn(0.0f, 1.0f)
    }

    fun getRawAmplitude(): Double {
        return currentAmplitude
    }

    fun isRunning(): Boolean {
        return isRecording
    }

    private fun startProcessing() {
        recordingJob = coroutineScope.launch {
            val buffer = ShortArray(BUFFER_SIZE)

            while (isRecording && isActive) {
                try {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                    if (readSize > 0) {
                        val amplitude = calculateAmplitude(buffer, readSize)
                        currentAmplitude = amplitude

                        val normalizedValue = normalizeAmplitude(amplitude)
                        currentVolume = smoothValue(currentVolume, normalizedValue)
                    }

                    delay(10)

                } catch (e: Exception) {
                    if (isRecording) {
                        Log.e(TAG, "Error processing", e)
                    }
                }
            }
        }
    }

    private fun calculateAmplitude(buffer: ShortArray, readSize: Int): Double {
        var sum = 0.0
        for (i in 0 until readSize) {
            val sample = buffer[i].toDouble()
            sum += sample * sample
        }
        return kotlin.math.sqrt(sum / readSize)
    }

    private fun normalizeAmplitude(amplitude: Double): Float {
        val normalized = if (amplitude > 1.0) {
            val db = 20 * log10(amplitude / MAX_AMPLITUDE)
            ((db + 60) / 60).coerceIn(0.0, 1.0)
        } else {
            0.0
        }
        return normalized.toFloat()
    }

    private fun smoothValue(current: Float, target: Float): Float {
        return current + (target - current) * SMOOTHING_FACTOR
    }

    fun release() {
        stop()
        coroutineScope.cancel()
    }
}
