package com.whispergames.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.shreshth.whispergames.R
import com.shreshth.whispergames.audio.MicVolumeEngine
import com.whispergames.app.ui.games.WhisperLineView
import com.whispergames.app.ui.games.DeadSilenceView
import com.whispergames.app.ui.games.BlowBalloonView
import com.whispergames.app.ui.games.ClapCatchView
import com.whispergames.app.ui.GameOverActivity
import kotlin.math.roundToInt

class GameActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GAME_TYPE = "GAME_TYPE"
        const val GAME_TYPE_WHISPER_LINE = "whisper_line"
        const val GAME_TYPE_DEAD_SILENCE = "dead_silence"
        const val GAME_TYPE_BLOW_BALLOON = "blow_balloon"
        const val GAME_TYPE_CLAP_CATCH = "clap_catch"

        private const val GAME_DURATION_WHISPER = 10000L
        private const val GAME_DURATION_SILENCE = 10000L
        private const val GAME_DURATION_BALLOON = 30000L
        private const val GAME_DURATION_CLAP_CATCH = 30000L
        private const val UPDATE_INTERVAL = 16L

        private const val THRESHOLD_WHISPER_LINE = 0.3f
        private const val THRESHOLD_DEAD_SILENCE = 0.05f

        private const val BALLOON_GROWTH_MULTIPLIER = 0.015f
        private const val MAX_BALLOON_SIZE = 1.0f

        private const val CLAP_THRESHOLD = 0.6f
        private const val POINTS_PER_HIT = 100
        private const val TARGET_SPEED = 3000L
    }

    private lateinit var micEngine: MicVolumeEngine

    private lateinit var tvTimer: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvVolumePercent: TextView
    private lateinit var volumeIndicator: ProgressBar
    private var whisperLineView: WhisperLineView? = null
    private var deadSilenceView: DeadSilenceView? = null
    private var blowBalloonView: BlowBalloonView? = null
    private var clapCatchView: ClapCatchView? = null
    private var tvBalloonSize: TextView? = null
    private var tvHits: TextView? = null
    private var tvMisses: TextView? = null
    private var tvAccuracy: TextView? = null

    private var gameType: String = GAME_TYPE_WHISPER_LINE
    private var volumeThreshold: Float = THRESHOLD_WHISPER_LINE
    private var gameDuration: Long = GAME_DURATION_WHISPER
    private var isGameRunning = false
    private var gameStartTime: Long = 0
    private var timeRemaining: Float = 10.0f
    private var currentScore: Int = 0

    private var balloonSize: Float = 0f

    private var hits: Int = 0
    private var misses: Int = 0
    private var previousClapDetected: Boolean = false

    private val updateHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get game type from intent
        gameType = intent.getStringExtra(EXTRA_GAME_TYPE) ?: GAME_TYPE_WHISPER_LINE

        // Set threshold and layout based on game type
        when (gameType) {
            GAME_TYPE_WHISPER_LINE -> {
                volumeThreshold = THRESHOLD_WHISPER_LINE
                gameDuration = GAME_DURATION_WHISPER
                setContentView(R.layout.activity_game_whisper_line)
            }

            GAME_TYPE_DEAD_SILENCE -> {
                volumeThreshold = THRESHOLD_DEAD_SILENCE
                gameDuration = GAME_DURATION_SILENCE
                setContentView(R.layout.activity_game_dead_silence)
            }

            GAME_TYPE_BLOW_BALLOON -> {
                gameDuration = GAME_DURATION_BALLOON
                setContentView(R.layout.activity_game_blow_balloon)
            }

            GAME_TYPE_CLAP_CATCH -> {
                gameDuration = GAME_DURATION_CLAP_CATCH
                setContentView(R.layout.activity_game_clap_catch)
            }

            else -> {
                volumeThreshold = THRESHOLD_WHISPER_LINE
                gameDuration = GAME_DURATION_WHISPER
                setContentView(R.layout.activity_game_whisper_line)
            }
        }

        initializeUI()
        initializeMicEngine()
    }

    private fun initializeUI() {
        tvTimer = findViewById(R.id.tvTimer)
        tvScore = findViewById(R.id.tvScore)
        tvVolumePercent = findViewById(R.id.tvVolumePercent)
        volumeIndicator = findViewById(R.id.volumeIndicator)

        // Initialize game-specific views
        when (gameType) {
            GAME_TYPE_WHISPER_LINE -> {
                whisperLineView = findViewById(R.id.whisperLineView)
                whisperLineView?.threshold = volumeThreshold
            }

            GAME_TYPE_DEAD_SILENCE -> {
                deadSilenceView = findViewById(R.id.deadSilenceView)
                deadSilenceView?.threshold = volumeThreshold
            }

            GAME_TYPE_BLOW_BALLOON -> {
                blowBalloonView = findViewById(R.id.blowBalloonView)
                blowBalloonView?.maxBalloonSize = MAX_BALLOON_SIZE
                tvBalloonSize = findViewById(R.id.tvBalloonSize)
            }

            GAME_TYPE_CLAP_CATCH -> {
                clapCatchView = findViewById(R.id.clapCatchView)
                tvHits = findViewById(R.id.tvHits)
                tvMisses = findViewById(R.id.tvMisses)
                tvAccuracy = findViewById(R.id.tvAccuracy)

                // Start target movement
                clapCatchView?.post {
                    clapCatchView?.startTargetMovement(TARGET_SPEED)
                }
            }
        }

        updateTimerDisplay()
        updateScoreDisplay()
        updateVolumeDisplay(0f)
    }

    private fun initializeMicEngine() {
        // Check if microphone permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this,
                "Microphone permission required to play",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        // Initialize microphone engine
        micEngine = MicVolumeEngine()

        // Start the engine
        val started = micEngine.start()
        if (started) {
            startGame()
        } else {
            Toast.makeText(
                this,
                "Failed to initialize microphone",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun startGame() {
        isGameRunning = true
        gameStartTime = System.currentTimeMillis()
        startGameLoop()
    }

    private fun startGameLoop() {
        updateHandler.post(object : Runnable {
            override fun run() {
                if (isGameRunning && !isFinishing) {
                    updateGame()
                    updateHandler.postDelayed(this, UPDATE_INTERVAL)
                }
            }
        })
    }

    private fun updateGame() {
        // Calculate time remaining
        val elapsed = System.currentTimeMillis() - gameStartTime
        timeRemaining = ((gameDuration - elapsed) / 1000f).coerceAtLeast(0f)

        // Get normalized volume (0.0 to 1.0)
        val volume = micEngine.getNormalizedVolume()

        // Update UI
        updateTimerDisplay()
        updateVolumeDisplay(volume)

        // Game-specific logic
        when (gameType) {
            GAME_TYPE_WHISPER_LINE -> {
                whisperLineView?.currentVolume = volume

                // Check win condition (survived 10 seconds)
                if (elapsed >= gameDuration) {
                    onGameWon()
                    return
                }

                // Check fail condition (volume exceeded threshold)
                if (volume > volumeThreshold) {
                    onGameLost()
                    return
                }

                // Update score (milliseconds survived)
                currentScore = elapsed.toInt()
            }

            GAME_TYPE_DEAD_SILENCE -> {
                deadSilenceView?.currentVolume = volume
                deadSilenceView?.isSilent = volume <= volumeThreshold

                // Check win condition (survived 10 seconds)
                if (elapsed >= gameDuration) {
                    onGameWon()
                    return
                }

                // Check fail condition (volume exceeded threshold)
                if (volume > volumeThreshold) {
                    onGameLost()
                    return
                }

                // Update score (milliseconds survived)
                currentScore = elapsed.toInt()
            }

            GAME_TYPE_BLOW_BALLOON -> {
                // Grow balloon based on volume
                balloonSize += volume * BALLOON_GROWTH_MULTIPLIER
                balloonSize = balloonSize.coerceAtLeast(0f)

                blowBalloonView?.currentVolume = volume
                blowBalloonView?.balloonSize = balloonSize

                // Update balloon size display
                val sizePercent =
                    ((balloonSize / MAX_BALLOON_SIZE) * 100).roundToInt().coerceAtMost(100)
                tvBalloonSize?.text = "$sizePercent%"

                // Check pop condition (balloon too big)
                if (balloonSize >= MAX_BALLOON_SIZE) {
                    onBalloonPop()
                    return
                }

                // Check win condition (time's up without popping)
                if (elapsed >= gameDuration) {
                    onGameWon()
                    return
                }

                // Score = balloon size percentage (0-100) + time bonus
                val sizeScore = (balloonSize / MAX_BALLOON_SIZE * 5000).toInt()
                val timeBonus = ((gameDuration - elapsed) / 100).toInt()
                currentScore = sizeScore + timeBonus
            }

            GAME_TYPE_CLAP_CATCH -> {
                clapCatchView?.currentVolume = volume

                // Detect clap (spike in volume)
                val clapDetected = clapCatchView?.detectClap(volume) ?: false

                // Check if clap was detected and it's a new clap (not held)
                if (clapDetected && !previousClapDetected) {
                    // Check if target is in hit zone
                    val isInHitZone = clapCatchView?.isTargetInHitZone() ?: false

                    if (isInHitZone) {
                        // HIT!
                        hits++
                        currentScore += POINTS_PER_HIT
                        clapCatchView?.showHitEffect()
                    } else {
                        // MISS!
                        misses++
                        if (clapCatchView?.isTargetPastHitZone() == true) {
                            clapCatchView?.showTooSlowEffect()
                        } else {
                            clapCatchView?.showMissEffect()
                        }
                    }

                    // Update stats display
                    updateClapCatchStats()
                }

                previousClapDetected = clapDetected

                // Check win condition (time's up)
                if (elapsed >= gameDuration) {
                    onGameWon()
                    return
                }
            }
        }

        updateScoreDisplay()
    }

    private fun updateTimerDisplay() {
        tvTimer.text = String.format("%.1fs", timeRemaining)

        val color = when {
            timeRemaining > 5.0f -> ContextCompat.getColor(this, R.color.accent_light)
            timeRemaining > 3.0f -> ContextCompat.getColor(this, R.color.game_warning)
            else -> ContextCompat.getColor(this, R.color.game_danger)
        }
        tvTimer.setTextColor(color)
    }

    private fun updateScoreDisplay() {
        when (gameType) {
            GAME_TYPE_BLOW_BALLOON, GAME_TYPE_CLAP_CATCH -> {
                // Show points score
                tvScore.text = "$currentScore"
            }

            else -> {
                // Show time survived for other games
                val seconds = currentScore / 1000
                val millis = (currentScore % 1000) / 100
                tvScore.text = "Score: $seconds.${millis}s"
            }
        }
    }

    private fun updateClapCatchStats() {
        tvHits?.text = hits.toString()
        tvMisses?.text = misses.toString()

        val total = hits + misses
        val accuracy = if (total > 0) {
            (hits.toFloat() / total * 100).roundToInt()
        } else {
            0
        }
        tvAccuracy?.text = "$accuracy%"
    }

    private fun updateVolumeDisplay(volume: Float) {
        val volumePercent = (volume * 100).roundToInt()
        tvVolumePercent.text = "$volumePercent%"
        volumeIndicator.progress = volumePercent

        val color = when {
            volume > volumeThreshold -> ContextCompat.getColor(this, R.color.game_danger)
            volume > volumeThreshold * 0.8f -> ContextCompat.getColor(this, R.color.game_warning)
            else -> ContextCompat.getColor(this, R.color.game_success)
        }
        volumeIndicator.progressTintList = android.content.res.ColorStateList.valueOf(color)
    }

    private fun onGameWon() {
        isGameRunning = false

        // Calculate final score
        when (gameType) {
            GAME_TYPE_BLOW_BALLOON -> {
                // Bonus for winning without popping
                currentScore += 2000
            }

            else -> {
                // Full time survived
                currentScore = gameDuration.toInt()
            }
        }

        endGame(currentScore)
    }

    private fun onBalloonPop() {
        isGameRunning = false
        blowBalloonView?.isPopped = true

        // Show pop animation briefly before ending
        updateHandler.postDelayed({
            endGame(currentScore)
        }, 2000)
    }

    private fun onGameLost() {
        isGameRunning = false

        // Update game-specific views
        when (gameType) {
            GAME_TYPE_WHISPER_LINE -> {
                whisperLineView?.isGameOver = true
            }

            GAME_TYPE_DEAD_SILENCE -> {
                deadSilenceView?.isGameOver = true
            }

            GAME_TYPE_BLOW_BALLOON -> {
                blowBalloonView?.isPopped = true
            }

            GAME_TYPE_CLAP_CATCH -> {
                clapCatchView?.stopTargetMovement()
            }
        }

        // Show fail message briefly before ending
        updateHandler.postDelayed({
            endGame(currentScore)
        }, 1500)
    }

    private fun endGame(score: Int) {
        updateHandler.removeCallbacksAndMessages(null)

        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("SCORE", score)
        intent.putExtra("GAME_TYPE", gameType)
        intent.putExtra("MAX_SCORE", gameDuration.toInt())
        startActivity(intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        if (::micEngine.isInitialized) {
            micEngine.stop()
        }
        isGameRunning = false
        updateHandler.removeCallbacksAndMessages(null)

        // Stop target movement for Clap Catch
        if (gameType == GAME_TYPE_CLAP_CATCH) {
            clapCatchView?.stopTargetMovement()
        }
    }

    override fun onResume() {
        super.onResume()
        // Don't restart if we paused during game - it would be unfair
        // Game will just end when returning
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::micEngine.isInitialized) {
            micEngine.release()
        }
        updateHandler.removeCallbacksAndMessages(null)
    }
}
