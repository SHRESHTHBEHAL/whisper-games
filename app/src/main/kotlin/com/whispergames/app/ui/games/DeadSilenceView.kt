package com.whispergames.app.ui.games

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.shreshth.whispergames.R
import kotlin.math.sin

/**
 * Custom view for Dead Silence game visualization
 * Shows stealth-themed volume detector with dark aesthetic
 */
class DeadSilenceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val silenceRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.argb(80, 0, 255, 0)
    }

    private val volumeCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(150, 0, 255, 0)
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val thresholdCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.RED
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val warningPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.RED
        alpha = 0
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 72f
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        color = Color.argb(200, 255, 255, 255)
        textAlign = Paint.Align.CENTER
    }

    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.argb(50, 0, 255, 0)
    }

    var currentVolume: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var threshold: Float = 0.05f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var isGameOver: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var isSilent: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private var animationFrame = 0f

    init {
        // Start animation
        post(object : Runnable {
            override fun run() {
                animationFrame += 0.05f
                if (animationFrame > 360f) animationFrame = 0f
                invalidate()
                postDelayed(this, 16) // ~60 FPS
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2
        val maxRadius = minOf(width, height) / 2 - 50f

        // Draw dark background
        canvas.drawRect(0f, 0f, width, height, backgroundPaint)

        // Draw animated ripples (sonar effect)
        if (isSilent) {
            for (i in 0..2) {
                val rippleRadius = (animationFrame + i * 60f) % 180f + 50f
                ripplePaint.alpha = (255 - (rippleRadius / 230f * 255)).toInt().coerceIn(0, 255)
                canvas.drawCircle(centerX, centerY, rippleRadius, ripplePaint)
            }
        }

        // Draw silence rings (concentric circles)
        for (i in 1..5) {
            val radius = maxRadius * (i / 5f)
            silenceRingPaint.alpha = (100 - i * 15).coerceIn(20, 100)
            canvas.drawCircle(centerX, centerY, radius, silenceRingPaint)
        }

        // Draw threshold danger circle
        val thresholdRadius = maxRadius * (threshold * 10f).coerceAtMost(0.9f)
        canvas.drawCircle(centerX, centerY, thresholdRadius, thresholdCirclePaint)

        // Draw "SAFE ZONE" text
        smallTextPaint.alpha = 80
        canvas.drawText(
            "SAFE ZONE",
            centerX,
            centerY - thresholdRadius - 20,
            smallTextPaint
        )
        smallTextPaint.alpha = 200

        // Draw volume indicator (pulsing circle)
        if (currentVolume > 0.001f) {
            val volumeRadius = maxRadius * (currentVolume * 2f).coerceAtMost(1f)

            // Determine color based on threshold
            val volumeColor = if (currentVolume > threshold) {
                Color.RED
            } else if (currentVolume > threshold * 0.7f) {
                Color.YELLOW
            } else {
                Color.GREEN
            }

            // Add pulsing effect
            val pulse = sin(animationFrame * 0.1f) * 10f + volumeRadius

            // Draw glow
            val gradientRadius = pulse + 30f
            glowPaint.shader = RadialGradient(
                centerX, centerY, gradientRadius,
                intArrayOf(
                    Color.argb(
                        100,
                        Color.red(volumeColor),
                        Color.green(volumeColor),
                        Color.blue(volumeColor)
                    ),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(centerX, centerY, gradientRadius, glowPaint)

            // Draw main volume circle
            volumeCirclePaint.color = Color.argb(
                200,
                Color.red(volumeColor),
                Color.green(volumeColor),
                Color.blue(volumeColor)
            )
            canvas.drawCircle(centerX, centerY, pulse, volumeCirclePaint)
        }

        // Draw volume percentage in center
        textPaint.textSize = 48f
        val volumePercent = (currentVolume * 100).toInt()
        val volumeColor = if (currentVolume > threshold) Color.RED else Color.GREEN
        textPaint.color = volumeColor
        canvas.drawText(
            "$volumePercent%",
            centerX,
            centerY + 15,
            textPaint
        )

        // Draw status text
        textPaint.textSize = 32f
        textPaint.color = Color.WHITE
        canvas.drawText(
            if (isSilent) "SILENT" else "DETECTED",
            centerX,
            centerY + 60,
            textPaint
        )

        // Draw game over overlay
        if (isGameOver) {
            // Pulsing red overlay
            val pulseAlpha = (sin(animationFrame * 0.2f) * 50 + 100).toInt()
            warningPaint.alpha = pulseAlpha
            canvas.drawRect(0f, 0f, width, height, warningPaint)

            // Game over text with shadow
            textPaint.textSize = 64f
            textPaint.color = Color.BLACK
            canvas.drawText("DETECTED!", centerX + 3, centerY + 3, textPaint)
            textPaint.color = Color.RED
            canvas.drawText("DETECTED!", centerX, centerY, textPaint)

            smallTextPaint.color = Color.WHITE
            canvas.drawText(
                "You made a sound!",
                centerX,
                centerY + 80,
                smallTextPaint
            )
        }
    }
}
