package com.whispergames.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.shreshth.whispergames.R
import com.whispergames.app.data.ScoreManager
import com.whispergames.app.ui.adapters.GameCarouselAdapter
import com.whispergames.app.ui.models.GameInfo

class HomeActivity : AppCompatActivity() {

    private lateinit var adView: AdView
    private lateinit var gameCarousel: ViewPager2
    private lateinit var pageIndicator: LinearLayout
    private lateinit var scoreManager: ScoreManager

    private val games = mutableListOf<GameInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_enhanced)

        scoreManager = ScoreManager(this)

        initializeGames()
        setupCarousel()
        setupQuickActions()
        updateStats()
        initializeAds()
    }

    private fun initializeGames() {
        games.clear()
        val highScores = scoreManager.getAllHighScores()
        games.add(
            GameInfo(
                id = "whisper_line",
                name = getString(R.string.game_whisper_line),
                description = "Stay under the volume threshold",
                icon = "🎤",
                difficulty = "⭐⭐",
                backgroundColor = "#BFDBFE",
                highScore = formatScore(highScores["whisper_line"] ?: 0, "whisper_line")
            )
        )
        games.add(
            GameInfo(
                id = "dead_silence",
                name = getString(R.string.game_dead_silence),
                description = "Maintain absolute silence",
                icon = "🤫",
                difficulty = "⭐⭐⭐⭐⭐",
                backgroundColor = "#4C1D95",
                highScore = formatScore(highScores["dead_silence"] ?: 0, "dead_silence")
            )
        )
        games.add(
            GameInfo(
                id = "blow_balloon",
                name = getString(R.string.game_blow_balloon),
                description = "Inflate without popping",
                icon = "🎈",
                difficulty = "⭐⭐⭐",
                backgroundColor = "#FBCFE8",
                highScore = formatScore(highScores["blow_balloon"] ?: 0, "blow_balloon")
            )
        )
        games.add(
            GameInfo(
                id = "clap_catch",
                name = getString(R.string.game_clap_catch),
                description = "Clap to hit moving targets",
                icon = "👏",
                difficulty = "⭐⭐⭐⭐",
                backgroundColor = "#A7F3D0",
                highScore = formatScore(highScores["clap_catch"] ?: 0, "clap_catch")
            )
        )
    }

    private fun formatScore(score: Int, gameType: String): String {
        return when (gameType) {
            "whisper_line", "dead_silence" -> {
                if (score == 0) return "—"
                val seconds = score / 1000
                val millis = (score % 1000) / 100
                "$seconds.${millis}s"
            }
            else -> {
                if (score == 0) return "—"
                score.toString()
            }
        }
    }

    private fun setupCarousel() {
        gameCarousel = findViewById(R.id.gameCarousel)
        pageIndicator = findViewById(R.id.pageIndicator)
        val adapter = GameCarouselAdapter(games) { gameInfo ->
            launchGame(gameInfo.id)
        }
        gameCarousel.adapter = adapter
        gameCarousel.offscreenPageLimit = 1
        gameCarousel.setPageTransformer { page, position ->
            page.apply {
                val scaleFactor = 0.85f
                val scale = 1 - kotlin.math.abs(position) * (1 - scaleFactor)
                scaleX = scale
                scaleY = scale
                alpha = 0.5f + (1 - kotlin.math.abs(position)) * 0.5f
            }
        }
        setupPageIndicator(games.size)
        gameCarousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position)
            }
        })
    }

    private fun setupPageIndicator(count: Int) {
        pageIndicator.removeAllViews()
        for (i in 0 until count) {
            val dot = View(this)
            val params = LinearLayout.LayoutParams(24, 24)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params
            dot.setBackgroundResource(R.drawable.indicator_dot)
            pageIndicator.addView(dot)
        }
    }

    private fun updatePageIndicator(position: Int) {
        for (i in 0 until pageIndicator.childCount) {
            val dot = pageIndicator.getChildAt(i)
            dot.alpha = if (i == position) 1.0f else 0.4f
            dot.scaleX = if (i == position) 1.2f else 1.0f
            dot.scaleY = if (i == position) 1.2f else 1.0f
        }
    }

    private fun setupQuickActions() {
        val leaderboardCard = findViewById<CardView>(R.id.cardLeaderboard)
        val settingsCard = findViewById<CardView>(R.id.cardSettings)

        leaderboardCard.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }

        settingsCard.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }

        leaderboardCard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        settingsCard.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun updateStats() {
        val gamesPlayed = scoreManager.getGamesPlayed()
        val totalScore = scoreManager.getTotalScore()
        findViewById<TextView>(R.id.tvGamesPlayed).text = "🎯 $gamesPlayed Games"
        findViewById<TextView>(R.id.tvTotalScore).text = "⭐ Total: $totalScore"
    }

    private fun launchGame(gameType: String) {
        val intent = Intent(this, InstructionsActivity::class.java)
        intent.putExtra(InstructionsActivity.EXTRA_GAME_TYPE, gameType)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun initializeAds() {
        MobileAds.initialize(this) {}
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
        initializeGames()
        gameCarousel.adapter?.notifyDataSetChanged()
        updateStats()
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}

