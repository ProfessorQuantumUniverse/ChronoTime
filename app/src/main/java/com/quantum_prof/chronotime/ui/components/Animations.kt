package com.quantum_prof.chronotime.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntSize
import kotlin.math.*
import kotlin.random.Random

/**
 * Particle Morph Animation
 * Creates a beautiful particle explosion/implosion effect during transitions
 */
data class MorphParticle(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val size: Float,
    val color: Color,
    val delay: Float // 0-1 staggered delay
)

@Composable
fun ParticleMorphTransition(
    active: Boolean,
    particleCount: Int = 50,
    baseColor: Color = Color(0xFF00F0FF),
    modifier: Modifier = Modifier
) {
    var particles by remember { mutableStateOf<List<MorphParticle>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize(1080, 1920)) }

    // Animation progress
    val progress by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "morphProgress"
    )

    // Generate particles when animation starts
    LaunchedEffect(active) {
        if (active && particles.isEmpty()) {
            val centerX = canvasSize.width / 2f
            val centerY = canvasSize.height / 2f

            particles = List(particleCount) { index ->
                val angle = Random.nextFloat() * 2 * PI
                val distance = Random.nextFloat() * 300 + 100

                MorphParticle(
                    startX = centerX,
                    startY = centerY,
                    endX = centerX + (cos(angle) * distance).toFloat(),
                    endY = centerY + (sin(angle) * distance).toFloat(),
                    size = Random.nextFloat() * 8 + 2,
                    color = baseColor.copy(alpha = Random.nextFloat() * 0.5f + 0.3f),
                    delay = index.toFloat() / particleCount * 0.3f
                )
            }
        } else if (!active) {
            particles = emptyList()
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        canvasSize = IntSize(size.width.toInt(), size.height.toInt())

        particles.forEach { particle ->
            val adjustedProgress = ((progress - particle.delay) / (1 - particle.delay)).coerceIn(0f, 1f)
            val eased = FastOutSlowInEasing.transform(adjustedProgress)

            val currentX = lerp(particle.startX, particle.endX, eased)
            val currentY = lerp(particle.startY, particle.endY, eased)
            val currentAlpha = (1 - eased) * particle.color.alpha

            drawCircle(
                color = particle.color.copy(alpha = currentAlpha),
                radius = particle.size * (1 + eased * 0.5f),
                center = Offset(currentX, currentY)
            )
        }
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

/**
 * Ripple Effect from touch point
 */
@Composable
fun TouchRipple(
    touchPoint: Offset?,
    color: Color = Color(0xFF00F0FF),
    modifier: Modifier = Modifier
) {
    var lastTouchPoint by remember { mutableStateOf<Offset?>(null) }
    var animationTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(touchPoint) {
        if (touchPoint != null) {
            lastTouchPoint = touchPoint
            animationTrigger++
        }
    }

    val rippleScale by animateFloatAsState(
        targetValue = if (lastTouchPoint != null) 3f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        finishedListener = { lastTouchPoint = null },
        label = "rippleScale"
    )

    val rippleAlpha by animateFloatAsState(
        targetValue = if (lastTouchPoint != null) 0f else 0.5f,
        animationSpec = tween(600),
        label = "rippleAlpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        lastTouchPoint?.let { point ->
            drawCircle(
                color = color.copy(alpha = rippleAlpha),
                radius = 50f * rippleScale,
                center = point,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = rippleAlpha * 0.3f),
                        Color.Transparent
                    ),
                    center = point,
                    radius = 50f * rippleScale
                ),
                radius = 50f * rippleScale,
                center = point
            )
        }
    }
}

/**
 * Digit Morph Animation - for transitioning between numbers
 */
@Composable
fun DigitMorphCanvas(
    fromDigit: Int,
    toDigit: Int,
    progress: Float,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    // Simple morph: fade out old, fade in new with scale
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Old digit fading out
        val oldAlpha = (1 - progress).coerceIn(0f, 1f)
        val oldScale = 1f + progress * 0.2f

        // New digit fading in
        val newAlpha = progress.coerceIn(0f, 1f)
        val newScale = 0.8f + progress * 0.2f

        // Draw particles representing digit transformation
        val particleCount = 20
        repeat(particleCount) { i ->
            val angle = (i.toFloat() / particleCount) * 2 * PI
            val distance = 30f * (1 - progress) + 10f

            drawCircle(
                color = color.copy(alpha = oldAlpha * 0.5f),
                radius = 3f,
                center = Offset(
                    centerX + (cos(angle) * distance * oldScale).toFloat(),
                    centerY + (sin(angle) * distance * oldScale).toFloat()
                )
            )
        }
    }
}

/**
 * Breathing Glow Effect - subtle pulsing glow around elements
 */
@Composable
fun BreathingGlow(
    color: Color = Color(0xFF00F0FF),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathingGlow")

    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 50f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = glowAlpha),
                    color.copy(alpha = glowAlpha * 0.5f),
                    Color.Transparent
                ),
                center = center,
                radius = glowRadius * 2
            ),
            radius = glowRadius * 2,
            center = center
        )
    }
}

/**
 * Matrix Rain Effect for leet mode
 */
@Composable
fun MatrixRainEffect(
    modifier: Modifier = Modifier,
    density: Int = 30
) {
    data class MatrixDrop(
        val x: Float,
        var y: Float,
        val speed: Float,
        val char: Char,
        val alpha: Float
    )

    var drops by remember { mutableStateOf<List<MatrixDrop>>(emptyList()) }
    var screenHeight by remember { mutableStateOf(1920f) }
    var screenWidth by remember { mutableStateOf(1080f) }

    val chars = "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン0123456789"

    LaunchedEffect(Unit) {
        drops = List(density) {
            MatrixDrop(
                x = Random.nextFloat() * screenWidth,
                y = Random.nextFloat() * screenHeight,
                speed = Random.nextFloat() * 5 + 2,
                char = chars.random(),
                alpha = Random.nextFloat() * 0.5f + 0.2f
            )
        }

        while (true) {
            kotlinx.coroutines.delay(50)
            drops = drops.map { drop ->
                val newY = drop.y + drop.speed
                if (newY > screenHeight) {
                    drop.copy(
                        y = -20f,
                        x = Random.nextFloat() * screenWidth,
                        char = chars.random()
                    )
                } else {
                    drop.copy(y = newY)
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        screenHeight = size.height
        screenWidth = size.width

        drops.forEach { drop ->
            // We can't draw text in Canvas easily, so use circles as rain drops
            drawCircle(
                color = Color.Green.copy(alpha = drop.alpha),
                radius = 3f,
                center = Offset(drop.x, drop.y)
            )
            // Trail
            repeat(5) { i ->
                drawCircle(
                    color = Color.Green.copy(alpha = drop.alpha * (1 - i * 0.2f)),
                    radius = 2f,
                    center = Offset(drop.x, drop.y - i * 15)
                )
            }
        }
    }
}

