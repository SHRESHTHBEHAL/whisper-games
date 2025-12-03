package com.whispergames.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.shreshth.whispergames.R

class InstructionsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GAME_TYPE = "GAME_TYPE"
    }

    private lateinit var gameType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

        gameType = intent.getStringExtra(EXTRA_GAME_TYPE) ?: GameActivity.GAME_TYPE_WHISPER_LINE

        setupUI()
        setupButtons()
    }

    private fun setupUI() {
        val tvGameTitle = findViewById<TextView>(R.id.tvGameTitle)
        val tvInstructions = findViewById<TextView>(R.id.tvInstructions)

        when (gameType) {
            GameActivity.GAME_TYPE_WHISPER_LINE -> {
                tvGameTitle.text = getString(R.string.game_whisper_line)
                tvInstructions.text = "Keep your voice below the threshold line for as long as possible. Stay quiet to survive!"
            }
            GameActivity.GAME_TYPE_DEAD_SILENCE -> {
                tvGameTitle.text = getString(R.string.game_dead_silence)
                tvInstructions.text = "Maintain absolute silence! Any sound will end your game. How long can you stay silent?"
            }
            GameActivity.GAME_TYPE_BLOW_BALLOON -> {
                tvGameTitle.text = getString(R.string.game_blow_balloon)
                tvInstructions.text = "Blow into your microphone to inflate the balloon. Fill it up before time runs out!"
            }
            GameActivity.GAME_TYPE_CLAP_CATCH -> {
                tvGameTitle.text = getString(R.string.game_clap_catch)
                tvInstructions.text = "Clap to catch falling targets! Time your claps perfectly to maximize your score."
            }
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnStartGame).setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra(GameActivity.EXTRA_GAME_TYPE, gameType)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}

