package com.whispergames.app.ui.games

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.shreshth.whispergames.R
import kotlin.math.sin
import kotlin.random.Random

/**
 * Custom view for Blow Balloon game visualization
 * Shows an inflating balloon that grows with volume
 */
class BlowBalloonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val balloonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val balloonOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.argb(100, 0, 0, 0)
    }

    private val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(150, 255, 255, 255)
    }

    private val stringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.argb(200, 150, 150, 150)
    }

    private val warningCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.RED
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 48f
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Balloon state
    var balloonSize: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1.5f)
            invalidate()
        }

    var maxBalloonSize: Float = 1.0f
    var currentVolume: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var isPopped: Boolean = false
        set(value) {
            field = value
            if (value) {
                generatePopParticles()
            }
            invalidate()
        }

    // Balloon colors (can cycle through)
    private val balloonColors = listOf(
        Color.rgb(255, 100, 100), // Red
        Color.rgb(100, 150, 255), // Blue
        Color.rgb(255, 200, 100), // Orange
        Color.rgb(150, 100, 255), // Purple
        Color.rgb(100, 255, 150)  // Green
    )
    private var currentBalloonColor = balloonColors.random()

    // Pop particles
    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        var color: Int
    )

    private val particles = mutableListOf<Particle>()
    private var animationFrame = 0f

    init {
        // Start animation loop
        post(object : Runnable {
            override fun run() {
                animationFrame += 0.1f
                if (animationFrame > 360f) animationFrame = 0f

                // Update particles
                if (isPopped) {
                    updateParticles()
                }

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

        if (isPopped) {
            // Draw pop particles
            drawPopAnimation(canvas)

            // Draw "POP!" text
            textPaint.textSize = 72f
            textPaint.color = Color.RED
            canvas.drawText("POP!", centerX, centerY, textPaint)
        } else {
            // Draw warning circle at max size
            val maxRadius = minOf(width, height) * 0.4f
            canvas.drawCircle(centerX, centerY, maxRadius, warningCirclePaint)

            // Calculate balloon radius based on size (0.0 to 1.0+)
            val baseRadius = minOf(width, height) * 0.15f
            val currentRadius = baseRadius + (baseRadius * balloonSize * 2f)

            // Add breathing animation
            val breathe = sin(animationFrame * 0.2f) * 5f
            val animatedRadius = currentRadius + breathe

            // Draw balloon string
            drawBalloonString(canvas, centerX, centerY, animatedRadius)

            // Draw balloon body
            drawBalloon(canvas, centerX, centerY - animatedRadius * 0.1f, animatedRadius)

            // Draw warning if near max
            if (balloonSize > maxBalloonSize * 0.8f) {
                val alpha =
                    ((balloonSize - maxBalloonSize * 0.8f) / (maxBalloonSize * 0.2f) * 255).toInt()
                textPaint.alpha = alpha.coerceIn(0, 200)
                textPaint.textSize = 36f
                textPaint.color = Color.RED
                canvas.drawText("CAREFUL!", centerX, centerY - currentRadius - 50f, textPaint)
                textPaint.alpha = 255
            }

            // Draw size indicator
            drawSizeIndicator(canvas, width, height)
        }
    }

    private fun drawBalloon(canvas: Canvas, x: Float, y: Float, radius: Float) {
        val path = Path()

        // Create balloon shape (oval with bottom tip)
        val topY = y - radius
        val bottomY = y + radius * 0.8f
        val leftX = x - radius * 0.85f
        val rightX = x + radius * 0.85f

        // Top curve
        path.moveTo(x, topY)
        path.cubicTo(
            leftX, topY + radius * 0.3f,
            leftX, bottomY - radius * 0.3f,
            x - radius * 0.2f, bottomY
        )

        // Bottom tip
        path.lineTo(x, bottomY + radius * 0.2f)
        path.lineTo(x + radius * 0.2f, bottomY)

        // Right side
        path.cubicTo(
            rightX, bottomY - radius * 0.3f,
            rightX, topY + radius * 0.3f,
            x, topY
        )

        // Change color based on size
        val dangerLevel = (balloonSize / maxBalloonSize).coerceIn(0f, 1f)
        balloonPaint.color = when {
            dangerLevel > 0.9f -> Color.RED
            dangerLevel > 0.7f -> Color.rgb(255, 150, 0) // Orange
            else -> currentBalloonColor
        }

        // Draw balloon
        canvas.drawPath(path, balloonPaint)
        canvas.drawPath(path, balloonOutlinePaint)

        // Draw shine effect
        val shineRadius = radius * 0.3f
        shinePaint.shader = RadialGradient(
            x - radius * 0.3f,
            y - radius * 0.3f,
            shineRadius,
            Color.WHITE,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(x - radius * 0.3f, y - radius * 0.3f, shineRadius, shinePaint)
    }

    private fun drawBalloonString(canvas: Canvas, x: Float, y: Float, radius: Float) {
        val stringStartY = y + radius
        val stringEndY = height.toFloat() - 50f

        val path = Path()
        path.moveTo(x, stringStartY)

        // Wavy string
        val segments = 10
        val segmentHeight = (stringEndY - stringStartY) / segments
        for (i in 1..segments) {
            val currentY = stringStartY + i * segmentHeight
            val wave = sin(animationFrame * 0.1f + i * 0.5f) * 5f
            path.lineTo(x + wave, currentY)
        }

        canvas.drawPath(path, stringPaint)
    }

    private fun drawSizeIndicator(canvas: Canvas, width: Float, height: Float) {
        val barWidth = 40f
        val barHeight = height * 0.6f
        val barX = width - 80f
        val barY = (height - barHeight) / 2

        // Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.GRAY
        }
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, bgPaint)

        // Fill
        val fillHeight = barHeight * (balloonSize / maxBalloonSize).coerceIn(0f, 1f)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = when {
                balloonSize > maxBalloonSize * 0.9f -> Color.RED
                balloonSize > maxBalloonSize * 0.7f -> Color.rgb(255, 200, 0)
                else -> Color.rgb(100, 200, 255)
            }
        }
        canvas.drawRect(
            barX,
            barY + barHeight - fillHeight,
            barX + barWidth,
            barY + barHeight,
            fillPaint
        )

        // Max line
        val maxLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.RED
        }
        canvas.drawLine(barX - 10f, barY, barX + barWidth + 10f, barY, maxLinePaint)
    }

    private fun generatePopParticles() {
        particles.clear()
        val centerX = width / 2f
        val centerY = height / 2f

        // Generate particles in all directions
        for (i in 0 until 30) {
            val angle = (i / 30f) * Math.PI * 2
            val speed = Random.nextFloat() * 10f + 5f
            particles.add(
                Particle(
                    x = centerX,
                    y = centerY,
                    vx = (Math.cos(angle) * speed).toFloat(),
                    vy = (Math.sin(angle) * speed).toFloat(),
                    life = 1f,
                    color = currentBalloonColor
                )
            )
        }
    }

    private fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.x += particle.vx
            particle.y += particle.vy
            particle.vy += 0.5f // Gravity
            particle.life -= 0.02f

            if (particle.life <= 0) {
                iterator.remove()
            }
        }
    }

    private fun drawPopAnimation(canvas: Canvas) {
        for (particle in particles) {
            particlePaint.color = Color.argb(
                (particle.life * 255).toInt(),
                Color.red(particle.color),
                Color.green(particle.color),
                Color.blue(particle.color)
            )
            canvas.drawCircle(particle.x, particle.y, 8f, particlePaint)
        }
    }

    fun resetBalloon() {
        balloonSize = 0f
        isPopped = false
        particles.clear()
        currentBalloonColor = balloonColors.random()
        invalidate()
    }
}
