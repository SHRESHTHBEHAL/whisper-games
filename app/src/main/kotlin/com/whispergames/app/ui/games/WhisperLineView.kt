package com.whispergames.app.ui.games

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.shreshth.whispergames.R

/**
 * Custom view for Whisper Line game visualization
 * Shows volume bar and threshold line
 */
class WhisperLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val volumePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.primary)
    }

    private val thresholdPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = context.getColor(R.color.game_danger)
    }

    private val thresholdDashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
        alpha = 150
    }

    private val safePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.game_success)
        alpha = 50
    }

    private val dangerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.game_danger)
        alpha = 50
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 48f
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    private val warningPath = Path()

    var currentVolume: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var threshold: Float = 0.3f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var isGameOver: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Calculate threshold line position (from bottom)
        val thresholdY = height - (height * threshold)

        // Draw safe zone (below threshold)
        canvas.drawRect(0f, thresholdY, width, height, safePaint)

        // Draw danger zone (above threshold)
        canvas.drawRect(0f, 0f, width, thresholdY, dangerPaint)

        // Draw threshold line
        canvas.drawLine(0f, thresholdY, width, thresholdY, thresholdPaint)

        // Draw dashed line effect
        val dashLength = 20f
        val gapLength = 15f
        var x = 0f
        while (x < width) {
            canvas.drawLine(x, thresholdY - 4, x + dashLength, thresholdY - 4, thresholdDashPaint)
            x += dashLength + gapLength
        }

        // Draw threshold label
        canvas.drawText(
            "THRESHOLD",
            width / 2,
            thresholdY - 20,
            smallTextPaint
        )

        // Draw volume bar
        if (currentVolume > 0) {
            val volumeHeight = height * currentVolume
            val volumeY = height - volumeHeight

            // Change color based on threshold proximity
            volumePaint.color = when {
                currentVolume > threshold -> context.getColor(R.color.game_danger)
                currentVolume > threshold * 0.8f -> context.getColor(R.color.game_warning)
                else -> context.getColor(R.color.game_success)
            }

            // Draw volume bar from bottom
            canvas.drawRect(0f, volumeY, width, height, volumePaint)
        }

        // Draw "SAFE ZONE" text in the middle of safe area
        val safeZoneMiddleY = thresholdY + (height - thresholdY) / 2
        smallTextPaint.alpha = 100
        canvas.drawText("SAFE ZONE", width / 2, safeZoneMiddleY, smallTextPaint)
        smallTextPaint.alpha = 255

        // Draw game over overlay
        if (isGameOver) {
            // Semi-transparent red overlay
            val overlayPaint = Paint().apply {
                color = Color.RED
                alpha = 150
            }
            canvas.drawRect(0f, 0f, width, height, overlayPaint)

            // Game over text
            canvas.drawText(
                "TOO LOUD!",
                width / 2,
                height / 2,
                textPaint
            )
        }
    }
}
