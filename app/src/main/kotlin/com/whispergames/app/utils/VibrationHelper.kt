package com.whispergames.app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Helper class for haptic feedback that respects user settings
 */
class VibrationHelper(private val context: Context) {

    private val settingsManager = SettingsManager(context)

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Short vibration for button clicks (10ms)
     */
    fun vibrateClick() {
        if (!settingsManager.isVibrationEnabled()) return
        vibrate(10)
    }

    /**
     * Medium vibration for success (50ms)
     */
    fun vibrateSuccess() {
        if (!settingsManager.isVibrationEnabled()) return
        vibrate(50)
    }

    /**
     * Long vibration for errors/game over (100ms)
     */
    fun vibrateError() {
        if (!settingsManager.isVibrationEnabled()) return
        vibrate(100)
    }

    /**
     * Custom vibration duration
     */
    fun vibrate(durationMs: Long) {
        if (!settingsManager.isVibrationEnabled()) return

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(durationMs)
            }
        }
    }

    /**
     * Pattern vibration (e.g., for achievements)
     * pattern: array of [wait, vibrate, wait, vibrate, ...]
     */
    fun vibratePattern(pattern: LongArray) {
        if (!settingsManager.isVibrationEnabled()) return

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(pattern, -1)
            }
        }
    }
}
