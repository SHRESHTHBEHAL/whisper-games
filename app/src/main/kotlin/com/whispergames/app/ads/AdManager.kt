package com.whispergames.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Manages AdMob ads throughout the app
 * Handles interstitial and rewarded ads
 */
class AdManager(private val context: Context) {

    companion object {
        private const val TAG = "AdManager"

        // Test Ad Unit IDs (replace with real ones for production)
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

        // Ad frequency settings
        private const val GAMES_BETWEEN_ADS = 3 // Show ad every 3 games
        private const val PREFS_NAME = "ad_prefs"
        private const val KEY_GAMES_SINCE_AD = "games_since_last_ad"
        private const val KEY_HARD_MODE_UNLOCKED = "hard_mode_unlocked"
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isLoadingInterstitial = false
    private var isLoadingRewarded = false

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Load interstitial ad for later display
     */
    fun loadInterstitialAd() {
        if (isLoadingInterstitial || interstitialAd != null) {
            Log.d(TAG, "Ad already loaded or loading")
            return
        }

        isLoadingInterstitial = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoadingInterstitial = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                    isLoadingInterstitial = false
                }
            }
        )
    }

    /**
     * Show interstitial ad if available and conditions are met
     * Returns true if ad was shown or attempted
     */
    fun showInterstitialIfReady(
        activity: Activity,
        onAdDismissed: () -> Unit
    ): Boolean {
        if (!shouldShowAd()) {
            Log.d(TAG, "Not time to show ad yet")
            onAdDismissed()
            return false
        }

        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interstitial ad not ready")
            onAdDismissed()
            // Preload for next time
            loadInterstitialAd()
            return false
        }

        // Set up callbacks
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed")
                interstitialAd = null
                resetAdCounter()
                loadInterstitialAd() // Preload next ad
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Ad failed to show: ${error.message}")
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed")
                interstitialAd = null
            }
        }

        // Show the ad
        ad.show(activity)
        return true
    }

    /**
     * Increment game counter and check if ad should be shown
     */
    fun incrementGameCounter() {
        val current = prefs.getInt(KEY_GAMES_SINCE_AD, 0)
        prefs.edit()
            .putInt(KEY_GAMES_SINCE_AD, current + 1)
            .apply()
        Log.d(TAG, "Games since last ad: ${current + 1}")
    }

    /**
     * Check if enough games have been played to show ad
     */
    private fun shouldShowAd(): Boolean {
        val gamesSinceAd = prefs.getInt(KEY_GAMES_SINCE_AD, 0)
        return gamesSinceAd >= GAMES_BETWEEN_ADS
    }

    /**
     * Reset ad counter after showing ad
     */
    private fun resetAdCounter() {
        prefs.edit()
            .putInt(KEY_GAMES_SINCE_AD, 0)
            .apply()
        Log.d(TAG, "Ad counter reset")
    }

    /**
     * Get games remaining until next ad
     */
    fun getGamesUntilNextAd(): Int {
        val gamesSinceAd = prefs.getInt(KEY_GAMES_SINCE_AD, 0)
        return (GAMES_BETWEEN_ADS - gamesSinceAd).coerceAtLeast(0)
    }

    /**
     * Check if interstitial ad is loaded and ready
     */
    fun isInterstitialReady(): Boolean {
        return interstitialAd != null
    }

    /**
     * Preload interstitial ad on app start
     */
    fun preloadAds() {
        loadInterstitialAd()
        loadRewardedAd()
    }

    // ============= REWARDED ADS =============

    /**
     * Load rewarded ad for unlocking features
     */
    fun loadRewardedAd() {
        if (isLoadingRewarded || rewardedAd != null) {
            Log.d(TAG, "Rewarded ad already loaded or loading")
            return
        }

        isLoadingRewarded = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                    isLoadingRewarded = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${error.message}")
                    rewardedAd = null
                    isLoadingRewarded = false
                }
            }
        )
    }

    /**
     * Show rewarded ad to unlock hard mode
     */
    fun showRewardedAdForHardMode(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            Log.d(TAG, "Rewarded ad not ready")
            onAdFailed()
            loadRewardedAd() // Try loading for next time
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed")
                rewardedAd = null
                loadRewardedAd() // Preload next
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Rewarded ad failed to show: ${error.message}")
                rewardedAd = null
                onAdFailed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed")
                rewardedAd = null
            }
        }

        // Show ad and give reward
        ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            unlockHardMode()
            onRewardEarned()
        })
    }

    /**
     * Check if rewarded ad is loaded
     */
    fun isRewardedAdReady(): Boolean {
        return rewardedAd != null
    }

    /**
     * Unlock hard mode permanently
     */
    private fun unlockHardMode() {
        prefs.edit()
            .putBoolean(KEY_HARD_MODE_UNLOCKED, true)
            .apply()
        Log.d(TAG, "Hard mode unlocked!")
    }

    /**
     * Check if hard mode is unlocked
     */
    fun isHardModeUnlocked(): Boolean {
        return prefs.getBoolean(KEY_HARD_MODE_UNLOCKED, false)
    }

    /**
     * Reset hard mode (for testing)
     */
    fun resetHardMode() {
        prefs.edit()
            .putBoolean(KEY_HARD_MODE_UNLOCKED, false)
            .apply()
    }
}
