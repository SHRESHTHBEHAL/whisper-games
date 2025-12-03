package com.whispergames.app.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shreshth.whispergames.R
import com.whispergames.app.audio.MicVolumeEngine
import com.whispergames.app.utils.PermissionHelper

/**
 * Test activity to demonstrate MicVolumeEngine usage
 * This activity shows real-time volume readings and engine status
 */
class MicTestActivity : AppCompatActivity() {

    private lateinit var micEngine: MicVolumeEngine
    private lateinit var tvStatus: TextView
    private lateinit var tvVolume: TextView
    private lateinit var tvAmplitude: TextView
    private lateinit var volumeBar: ProgressBar
    private lateinit var btnToggle: Button

    private val updateHandler = Handler(Looper.getMainLooper())
    private var isMonitoring = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mic_test)

        initializeUI()
        micEngine = MicVolumeEngine()

        // Check permission
        if (!PermissionHelper.isMicrophonePermissionGranted(this)) {
            Toast.makeText(
                this,
                "Microphone permission required",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }
    }

    private fun initializeUI() {
        tvStatus = findViewById(R.id.tvStatus)
        tvVolume = findViewById(R.id.tvVolume)
        tvAmplitude = findViewById(R.id.tvAmplitude)
        volumeBar = findViewById(R.id.volumeBar)
        btnToggle = findViewById(R.id.btnToggle)

        btnToggle.setOnClickListener {
            if (isMonitoring) {
                stopMonitoring()
            } else {
                startMonitoring()
            }
        }

        updateStatus("Ready to start", Color.GRAY)
    }

    private fun startMonitoring() {
        val started = micEngine.start()
        if (started) {
            isMonitoring = true
            btnToggle.text = "Stop Monitoring"
            updateStatus("Monitoring active", Color.GREEN)
            startUpdateLoop()
        } else {
            updateStatus("Failed to start", Color.RED)
            Toast.makeText(this, "Failed to initialize microphone", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopMonitoring() {
        micEngine.stop()
        isMonitoring = false
        btnToggle.text = "Start Monitoring"
        updateStatus("Monitoring stopped", Color.GRAY)
        updateHandler.removeCallbacksAndMessages(null)

        // Reset displays
        tvVolume.text = "Volume: 0.00"
        tvAmplitude.text = "Amplitude: 0.0"
        volumeBar.progress = 0
    }

    private fun startUpdateLoop() {
        updateHandler.post(object : Runnable {
            override fun run() {
                if (isMonitoring && !isFinishing) {
                    updateReadings()
                    updateHandler.postDelayed(this, 50) // Update at 20 Hz
                }
            }
        })
    }

    private fun updateReadings() {
        val volume = micEngine.getNormalizedVolume()
        val amplitude = micEngine.getRawAmplitude()

        tvVolume.text = String.format("Volume: %.2f", volume)
        tvAmplitude.text = String.format("Amplitude: %.1f", amplitude)
        volumeBar.progress = (volume * 100).toInt()

        // Visual feedback based on volume level
        val color = when {
            volume < 0.1f -> Color.GREEN
            volume < 0.5f -> Color.YELLOW
            volume < 0.8f -> Color.rgb(255, 165, 0) // Orange
            else -> Color.RED
        }
        volumeBar.progressTintList = android.content.res.ColorStateList.valueOf(color)
    }

    private fun updateStatus(message: String, color: Int) {
        tvStatus.text = "Status: $message"
        tvStatus.setTextColor(color)
    }

    override fun onPause() {
        super.onPause()
        if (isMonitoring) {
            stopMonitoring()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        micEngine.release()
        updateHandler.removeCallbacksAndMessages(null)
    }
}
