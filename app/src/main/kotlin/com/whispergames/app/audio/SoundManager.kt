package com.whispergames.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.preference.PreferenceManager

class SoundManager private constructor(private val context: Context) {

    private var soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    private val vibrator: Vibrator

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }

        const val SOUND_CLICK = "click"
        const val SOUND_SUCCESS = "success"
        const val SOUND_FAIL = "fail"
        const val SOUND_POP = "pop"
        const val SOUND_CLAP = "clap"
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun playSound(soundKey: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val soundEnabled = prefs.getBoolean("sound_effects", true)

        if (soundEnabled) {
            soundMap[soundKey]?.let { soundId ->
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    fun vibrate(durationMs: Long = 50) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val vibrationEnabled = prefs.getBoolean("vibration", true)

        if (vibrationEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        }
    }

    fun vibratePattern(pattern: LongArray) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val vibrationEnabled = prefs.getBoolean("vibration", true)

        if (vibrationEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }

    fun release() {
        soundPool.release()
    }

    fun vibrateClick() = vibrate(30)
    fun vibrateSuccess() = vibratePattern(longArrayOf(0, 50, 50, 100))
    fun vibrateFail() = vibratePattern(longArrayOf(0, 100, 50, 100, 50, 100))
}

