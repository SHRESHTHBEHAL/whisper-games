package com.shreshth.whispergames

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.whispergames.app.ads.AdManager

/**
 * Application class for Whisper Games
 * Initializes global services
 */
class WhisperGamesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {
            // Initialization complete
        }

        // Initialize Ad Manager
        _adManager = AdManager(this)
        _adManager.preloadAds()
    }

    companion object {
        private lateinit var instance: WhisperGamesApp
        private lateinit var _adManager: AdManager

        val adManager: AdManager
            get() = _adManager
    }
}
