package com.whispergames.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.shreshth.whispergames.R
import com.whispergames.app.ads.AdManager
import com.whispergames.app.data.ScoreManager

class GameOverActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SCORE = "SCORE"
        const val EXTRA_GAME_TYPE = "GAME_TYPE"
        const val EXTRA_MAX_SCORE = "MAX_SCORE"
    }

    private lateinit var scoreManager: ScoreManager
    private lateinit var adManager: AdManager

    private var currentScore: Int = 0
    private var gameType: String = ""
    private var maxScore: Int = 10000
    private var isNewHighScore = false

    // UI Components
    private lateinit var tvGameTitle: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvHighScore: TextView
    private lateinit var btnRetry: Button
    private lateinit var btnHome: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        // Initialize managers
        scoreManager = ScoreManager(this)
        adManager = AdManager(this)

        // Get data from intent
        currentScore = intent.getIntExtra(EXTRA_SCORE, 0)
        gameType = intent.getStringExtra(EXTRA_GAME_TYPE) ?: ""
        maxScore = intent.getIntExtra(EXTRA_MAX_SCORE, 10000)

        initializeUI()
        displayResults()
        handleAdDisplay()
        setupButtons()
    }

    private fun initializeUI() {
        tvGameTitle = findViewById(R.id.tvTitle)
        tvScore = findViewById(R.id.tvScore)
        tvHighScore = findViewById(R.id.tvHighScore)
        btnRetry = findViewById(R.id.btnRetry)
        btnHome = findViewById(R.id.btnHome)
    }

    private fun displayResults() {
        // Set game title
        val gameName = when (gameType) {
            GameActivity.GAME_TYPE_WHISPER_LINE -> getString(R.string.game_whisper_line)
            GameActivity.GAME_TYPE_DEAD_SILENCE -> getString(R.string.game_dead_silence)
            GameActivity.GAME_TYPE_BLOW_BALLOON -> getString(R.string.game_blow_balloon)
            GameActivity.GAME_TYPE_CLAP_CATCH -> getString(R.string.game_clap_catch)
            else -> "Game Over"
        }
        tvGameTitle.text = gameName

        // Display current score
        tvScore.text = formatScore(currentScore, gameType)

        // Get and display high score
        val highScore = scoreManager.getHighScore(gameType)
        tvHighScore.text = formatScore(highScore, gameType)

        // Check if new high score
        isNewHighScore = scoreManager.saveScore(gameType, currentScore)

        // Increment ad counter
        adManager.incrementGameCounter()
    }

    private fun formatScore(score: Int, gameType: String): String {
        return when (gameType) {
            GameActivity.GAME_TYPE_WHISPER_LINE,
            GameActivity.GAME_TYPE_DEAD_SILENCE -> {
                // Time-based games: format as seconds
                val seconds = score / 1000
                val millis = (score % 1000) / 100
                "$seconds.${millis}s"
            }

            else -> {
                // Points-based games
                score.toString()
            }
        }
    }

    private fun handleAdDisplay() {
        // Delay to allow ad preparation
        Handler(Looper.getMainLooper()).postDelayed({
            // Try to show interstitial ad
            adManager.showInterstitialIfReady(this) {
                // Ad dismissed or not shown - continue normally
            }
        }, 500) // Small delay for better UX
    }

    private fun setupButtons() {
        btnRetry.setOnClickListener {
            retryGame()
        }

        btnHome.setOnClickListener {
            goHome()
        }
    }

    private fun retryGame() {
        val intent = Intent(this, InstructionsActivity::class.java)
        intent.putExtra(InstructionsActivity.EXTRA_GAME_TYPE, gameType)
        startActivity(intent)
        finish()
    }

    private fun goHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevent back button - must use Home button
        super.onBackPressed()
        goHome()
    }
}
