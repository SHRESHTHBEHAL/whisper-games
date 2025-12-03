package com.whispergames.app.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages app settings using SharedPreferences
 */
class SettingsManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "whisper_games_settings"

        // Setting keys
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"

        // Default values
        private const val DEFAULT_SOUND = true
        private const val DEFAULT_VIBRATION = true
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if sound effects are enabled
     */
    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_ENABLED, DEFAULT_SOUND)
    }

    /**
     * Enable or disable sound effects
     */
    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    /**
     * Check if vibration is enabled
     */
    fun isVibrationEnabled(): Boolean {
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, DEFAULT_VIBRATION)
    }

    /**
     * Enable or disable vibration
     */
    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }

    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
}
