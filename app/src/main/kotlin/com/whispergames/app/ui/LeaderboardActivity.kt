package com.whispergames.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.shreshth.whispergames.R
import com.whispergames.app.data.ScoreManager

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var scoreManager: ScoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        scoreManager = ScoreManager(this)

        setupButtons()
        loadScores()
        displayStats()
    }

    private fun loadScores() {
        val highScores = scoreManager.getAllHighScores()

        // Whisper Line
        setupGameCard(
            R.id.cardWhisperLine,
            R.id.tvWhisperLineScore,
            getString(R.string.game_whisper_line),
            highScores["whisper_line"] ?: 0,
            GameActivity.GAME_TYPE_WHISPER_LINE
        )

        // Dead Silence
        setupGameCard(
            R.id.cardDeadSilence,
            R.id.tvDeadSilenceScore,
            getString(R.string.game_dead_silence),
            highScores["dead_silence"] ?: 0,
            GameActivity.GAME_TYPE_DEAD_SILENCE
        )

        // Blow Balloon
        setupGameCard(
            R.id.cardBlowBalloon,
            R.id.tvBlowBalloonScore,
            getString(R.string.game_blow_balloon),
            highScores["blow_balloon"] ?: 0,
            GameActivity.GAME_TYPE_BLOW_BALLOON
        )

        // Clap Catch
        setupGameCard(
            R.id.cardClapCatch,
            R.id.tvClapCatchScore,
            getString(R.string.game_clap_catch),
            highScores["clap_catch"] ?: 0,
            GameActivity.GAME_TYPE_CLAP_CATCH
        )
    }

    private fun setupGameCard(
        cardId: Int,
        scoreTextId: Int,
        gameName: String,
        score: Int,
        gameType: String
    ) {
        val scoreText = findViewById<TextView>(scoreTextId)
        scoreText.text = formatScore(score, gameType)

        // Optional: Make card clickable to launch game
        findViewById<CardView>(cardId)?.setOnClickListener {
            // Could launch game here if desired
        }
    }

    private fun formatScore(score: Int, gameType: String): String {
        if (score == 0) return "—"

        return when (gameType) {
            GameActivity.GAME_TYPE_WHISPER_LINE,
            GameActivity.GAME_TYPE_DEAD_SILENCE -> {
                // Time-based format
                val seconds = score / 1000
                val millis = (score % 1000) / 100
                "$seconds.${millis}s"
            }

            else -> {
                // Points-based format
                score.toString()
            }
        }
    }

    private fun displayStats() {
        val gamesPlayed = scoreManager.getGamesPlayed()
        val totalScore = scoreManager.getTotalScore()

        findViewById<TextView>(R.id.tvGamesPlayed).text = "$gamesPlayed"
        findViewById<TextView>(R.id.tvTotalScore).text = "$totalScore"
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Reset button (optional - for testing)
        findViewById<Button>(R.id.btnReset)?.setOnClickListener {
            // Show confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reset All Scores?")
                .setMessage("This will delete all high scores and statistics. This action cannot be undone.")
                .setPositiveButton("Reset") { _, _ ->
                    scoreManager.resetAllScores()
                    loadScores()
                    displayStats()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
