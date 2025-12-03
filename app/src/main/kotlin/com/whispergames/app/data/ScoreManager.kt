package com.whispergames.app.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages high scores using SharedPreferences
 * Stores scores per game type
 */
class ScoreManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "whisper_games_scores"
        private const val KEY_HIGH_SCORE_PREFIX = "high_score_"
        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_TOTAL_SCORE = "total_score"
        private const val KEY_LAST_PLAYED = "last_played_"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get high score for a specific game type
     */
    fun getHighScore(gameType: String): Int {
        return prefs.getInt(KEY_HIGH_SCORE_PREFIX + gameType, 0)
    }

    /**
     * Save score if it's a new high score
     * Returns true if it's a new record
     */
    fun saveScore(gameType: String, score: Int): Boolean {
        val currentHighScore = getHighScore(gameType)
        val isNewRecord = score > currentHighScore

        if (isNewRecord) {
            prefs.edit()
                .putInt(KEY_HIGH_SCORE_PREFIX + gameType, score)
                .apply()
        }

        // Always update last played score
        prefs.edit()
            .putInt(KEY_LAST_PLAYED + gameType, score)
            .apply()

        // Increment games played counter
        incrementGamesPlayed()

        // Add to total score
        addToTotalScore(score)

        return isNewRecord
    }

    /**
     * Get last played score for a game
     */
    fun getLastPlayedScore(gameType: String): Int {
        return prefs.getInt(KEY_LAST_PLAYED + gameType, 0)
    }

    /**
     * Get total number of games played
     */
    fun getGamesPlayed(): Int {
        return prefs.getInt(KEY_GAMES_PLAYED, 0)
    }

    /**
     * Increment games played counter
     */
    private fun incrementGamesPlayed() {
        val current = getGamesPlayed()
        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, current + 1)
            .apply()
    }

    /**
     * Get total cumulative score across all games
     */
    fun getTotalScore(): Int {
        return prefs.getInt(KEY_TOTAL_SCORE, 0)
    }

    /**
     * Add score to total
     */
    private fun addToTotalScore(score: Int) {
        val current = getTotalScore()
        prefs.edit()
            .putInt(KEY_TOTAL_SCORE, current + score)
            .apply()
    }

    /**
     * Get all high scores as a map
     */
    fun getAllHighScores(): Map<String, Int> {
        return mapOf(
            "whisper_line" to getHighScore("whisper_line"),
            "dead_silence" to getHighScore("dead_silence"),
            "blow_balloon" to getHighScore("blow_balloon"),
            "clap_catch" to getHighScore("clap_catch")
        )
    }

    /**
     * Reset all scores (for testing or settings)
     */
    fun resetAllScores() {
        prefs.edit().clear().apply()
    }

    /**
     * Reset score for specific game
     */
    fun resetGameScore(gameType: String) {
        prefs.edit()
            .remove(KEY_HIGH_SCORE_PREFIX + gameType)
            .remove(KEY_LAST_PLAYED + gameType)
            .apply()
    }
}
