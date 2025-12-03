package com.whispergames.app.ui.games

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.shreshth.whispergames.R
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * Custom view for Clap Catch game visualization
 * Shows moving targets and hit zone with clap detection
 */
class ClapCatchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.rgb(255, 100, 100)
    }

    private val targetOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
    }

    private val hitZonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(80, 0, 255, 0)
    }

    private val hitZoneOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.rgb(0, 200, 0)
        pathEffect = DashPathEffect(floatArrayOf(15f, 15f), 0f)
    }

    private val clapIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.YELLOW
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 48f
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val missTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 36f
        color = Color.RED
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    // Target state
    var targetX: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private var targetRadius: Float = 60f
    private var targetSpeed: Float = 1.0f
    private var targetDirection: Int = 1 // 1 = right, -1 = left

    // Hit zone (center of screen)
    private val hitZoneWidth: Float = 200f

    // Clap detection
    var currentVolume: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    private var previousVolume: Float = 0f
    private var clapDetected: Boolean = false
    private var clapAnimationFrame: Int = 0
    private var clapCooldown: Int = 0

    // Feedback
    data class HitEffect(
        var x: Float,
        var y: Float,
        var alpha: Int = 255,
        var scale: Float = 1f
    )

    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        var color: Int
    )

    private val hitEffects = mutableListOf<HitEffect>()
    private val particles = mutableListOf<Particle>()
    private var missText: String? = null
    private var missTextAlpha: Int = 0

    private var animationFrame = 0f
    private var isAnimating = true

    // Target animator
    private var targetAnimator: ValueAnimator? = null

    init {
        startAnimation()
    }

    fun startTargetMovement(duration: Long = 3000L) {
        // Reset position
        targetX = 0f
        targetDirection = 1

        // Cancel existing animator
        targetAnimator?.cancel()

        // Create animator for smooth movement
        targetAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()

            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                // Move target from left to right
                val screenWidth = width.toFloat()
                targetX = screenWidth * fraction

                // Check if target reached the end
                if (targetX >= screenWidth - targetRadius) {
                    // Restart from left
                    targetX = targetRadius
                }

                invalidate()
            }

            start()
        }
    }

    fun stopTargetMovement() {
        targetAnimator?.cancel()
        targetAnimator = null
    }

    fun detectClap(volume: Float): Boolean {
        if (clapCooldown > 0) {
            return false
        }

        // Lowered threshold to 0.4 (was 0.7) and spike to 0.2 (was 0.4)
        // This makes it much more sensitive to claps
        val isSpike = volume > 0.4f && (volume - previousVolume) > 0.2f
        previousVolume = volume

        if (isSpike) {
            clapDetected = true
            clapAnimationFrame = 30 // Show for 30 frames
            clapCooldown = 15 // Cooldown for 15 frames (approx 250ms)
            return true
        }

        return false
    }

    fun isTargetInHitZone(): Boolean {
        val centerX = width / 2f
        val hitZoneLeft = centerX - hitZoneWidth / 2
        val hitZoneRight = centerX + hitZoneWidth / 2

        // Allow a slightly larger hit window (add target radius)
        return targetX >= (hitZoneLeft - targetRadius/2) && targetX <= (hitZoneRight + targetRadius/2)
    }

    fun isTargetPastHitZone(): Boolean {
        val centerX = width / 2f
        val hitZoneRight = centerX + hitZoneWidth / 2
        return targetX > hitZoneRight
    }

    fun showHitEffect() {
        val centerY = height / 2f
        hitEffects.add(HitEffect(targetX, centerY))
        generateHitParticles(targetX, centerY, Color.GREEN)
        missText = "NICE!"
        missTextAlpha = 255
        missTextPaint.color = Color.GREEN
    }

    fun showMissEffect() {
        missText = "TOO EARLY!"
        missTextAlpha = 255
        missTextPaint.color = Color.RED
        val centerY = height / 2f
        generateHitParticles(targetX, centerY, Color.RED)
    }

    fun showTooSlowEffect() {
        missText = "TOO SLOW!"
        missTextAlpha = 255
        missTextPaint.color = Color.RED
        val centerY = height / 2f
        generateHitParticles(targetX, centerY, Color.GRAY)
    }

    private fun generateHitParticles(x: Float, y: Float, color: Int) {
        for (i in 0 until 20) {
            val angle = Random.nextFloat() * Math.PI * 2
            val speed = Random.nextFloat() * 8f + 4f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = (Math.cos(angle) * speed).toFloat(),
                    vy = (Math.sin(angle) * speed).toFloat(),
                    life = 1f,
                    color = color
                )
            )
        }
    }

    private fun startAnimation() {
        post(object : Runnable {
            override fun run() {
                if (isAnimating) {
                    animationFrame += 0.1f
                    if (animationFrame > 360f) animationFrame = 0f

                    // Update effects
                    updateEffects()

                    // Decay clap indicator
                    if (clapAnimationFrame > 0) {
                        clapAnimationFrame--
                    }

                    if (clapCooldown > 0) {
                        clapCooldown--
                    }

                    // Decay miss text
                    if (missTextAlpha > 0) {
                        missTextAlpha -= 5
                        if (missTextAlpha < 0) {
                            missTextAlpha = 0
                            missText = null
                        }
                    }

                    invalidate()
                    postDelayed(this, 16) // ~60 FPS
                }
            }
        })
    }

    private fun updateEffects() {
        // Update hit effects
        val hitIterator = hitEffects.iterator()
        while (hitIterator.hasNext()) {
            val effect = hitIterator.next()
            effect.alpha -= 10
            effect.scale += 0.1f
            if (effect.alpha <= 0) {
                hitIterator.remove()
            }
        }

        // Update particles
        val particleIterator = particles.iterator()
        while (particleIterator.hasNext()) {
            val particle = particleIterator.next()
            particle.x += particle.vx
            particle.y += particle.vy
            particle.vy += 0.3f // Gravity
            particle.life -= 0.02f

            if (particle.life <= 0) {
                particleIterator.remove()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2

        // Draw hit zone
        val hitZoneLeft = centerX - hitZoneWidth / 2
        val hitZoneRight = centerX + hitZoneWidth / 2
        canvas.drawRect(hitZoneLeft, 0f, hitZoneRight, height, hitZonePaint)
        canvas.drawLine(hitZoneLeft, 0f, hitZoneLeft, height, hitZoneOutlinePaint)
        canvas.drawLine(hitZoneRight, 0f, hitZoneRight, height, hitZoneOutlinePaint)

        // Draw "HIT ZONE" text
        textPaint.textSize = 32f
        textPaint.alpha = 100
        canvas.drawText("HIT ZONE", centerX, 50f, textPaint)
        textPaint.alpha = 255

        // Draw target
        drawTarget(canvas, targetX, centerY)

        // Draw hit effects
        for (effect in hitEffects) {
            val effectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
                color = Color.GREEN
                alpha = effect.alpha
            }
            canvas.drawCircle(effect.x, effect.y, targetRadius * effect.scale, effectPaint)
        }

        // Draw particles
        for (particle in particles) {
            particlePaint.color = Color.argb(
                (particle.life * 255).toInt(),
                Color.red(particle.color),
                Color.green(particle.color),
                Color.blue(particle.color)
            )
            canvas.drawCircle(particle.x, particle.y, 6f, particlePaint)
        }

        // Draw miss text
        if (missText != null && missTextAlpha > 0) {
            missTextPaint.alpha = missTextAlpha
            canvas.drawText(missText!!, centerX, centerY - 100f, missTextPaint)
        }

        // Draw clap indicator
        if (clapAnimationFrame > 0) {
            val alpha = (clapAnimationFrame / 30f * 255).toInt()
            clapIndicatorPaint.alpha = alpha
            canvas.drawCircle(centerX, height - 100f, 50f, clapIndicatorPaint)

            textPaint.textSize = 36f
            textPaint.alpha = alpha
            canvas.drawText("CLAP!", centerX, height - 90f, textPaint)
            textPaint.alpha = 255
        }

        // Draw instructions at bottom
        textPaint.textSize = 20f
        textPaint.alpha = 150
        canvas.drawText(" Clap when target is in green zone!", centerX, height - 30f, textPaint)
        textPaint.alpha = 255
    }

    private fun drawTarget(canvas: Canvas, x: Float, y: Float) {
        // Draw target with pulsing animation
        val pulse = sin(animationFrame * 0.2f) * 5f
        val animatedRadius = targetRadius + pulse

        // Outer ring
        targetPaint.color = Color.rgb(255, 100, 100)
        canvas.drawCircle(x, y, animatedRadius, targetPaint)

        // Middle ring
        targetPaint.color = Color.WHITE
        canvas.drawCircle(x, y, animatedRadius * 0.7f, targetPaint)

        // Center
        targetPaint.color = Color.rgb(255, 0, 0)
        canvas.drawCircle(x, y, animatedRadius * 0.4f, targetPaint)

        // Outline
        canvas.drawCircle(x, y, animatedRadius, targetOutlinePaint)

        // Draw target direction indicator
        val arrowLength = 30f
        val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = Color.WHITE
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(
            x + targetRadius * 1.5f,
            y,
            x + targetRadius * 1.5f + arrowLength,
            y,
            arrowPaint
        )
        // Arrow head
        canvas.drawLine(
            x + targetRadius * 1.5f + arrowLength,
            y,
            x + targetRadius * 1.5f + arrowLength - 10f,
            y - 10f,
            arrowPaint
        )
        canvas.drawLine(
            x + targetRadius * 1.5f + arrowLength,
            y,
            x + targetRadius * 1.5f + arrowLength - 10f,
            y + 10f,
            arrowPaint
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAnimating = false
        stopTargetMovement()
    }

    fun reset() {
        targetX = 0f
        currentVolume = 0f
        previousVolume = 0f
        clapDetected = false
        clapAnimationFrame = 0
        hitEffects.clear()
        particles.clear()
        missText = null
        missTextAlpha = 0
        invalidate()
    }
}
