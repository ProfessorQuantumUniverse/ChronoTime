package com.quantum_prof.chronotime.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.*
import kotlin.random.Random

/**
 * Mid-Layer Floating Particles that visualize time
 * Creates ethereal, slowly moving shapes that represent temporal flow
 */

data class TimeParticle(
    val id: Int,
    var x: Float,
    var y: Float,
    var size: Float,
    var alpha: Float,
    var speed: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    val shape: ParticleShape,
    val color: Color
)

enum class ParticleShape {
    CIRCLE, RING, TRIANGLE, HEXAGON, DIAMOND, LINE, DOT_CLUSTER
}

@Composable
fun ParticleField(
    modifier: Modifier = Modifier,
    time: Calendar,
    particleCount: Int = 30,
    baseColor: Color = Color(0xFF00F0FF)
) {
    val second = time.get(Calendar.SECOND)
    val minute = time.get(Calendar.MINUTE)
    val hour = time.get(Calendar.HOUR_OF_DAY)

    // Particle colors based on time
    val timeColors = remember(hour, minute) {
        listOf(
            Color(0xFF00F0FF).copy(alpha = 0.6f), // Neon Blue
            Color(0xFFFF006E).copy(alpha = 0.4f), // Neon Pink
            Color(0xFF7B2FFF).copy(alpha = 0.5f), // Purple
            Color(0xFF00FF88).copy(alpha = 0.3f), // Green
        )
    }

    var particles by remember { mutableStateOf<List<TimeParticle>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize(1080, 1920)) }

    // Initialize particles
    LaunchedEffect(Unit) {
        particles = List(particleCount) { index ->
            createParticle(index, canvasSize, timeColors.random())
        }
    }

    // Animate particles
    LaunchedEffect(Unit) {
        while (true) {
            particles = particles.map { particle ->
                particle.copy(
                    y = particle.y - particle.speed,
                    rotation = particle.rotation + particle.rotationSpeed,
                    alpha = (sin(particle.y / 200f) * 0.3f + 0.4f).coerceIn(0.1f, 0.7f)
                ).let {
                    // Reset particle when it goes off screen
                    if (it.y < -100) {
                        createParticle(it.id, canvasSize, timeColors.random())
                    } else it
                }
            }
            delay(16) // ~60fps
        }
    }

    // Time pulse effect - particles pulse every second
    val pulseScale by animateFloatAsState(
        targetValue = if (second % 2 == 0) 1.1f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "pulse"
    )

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        canvasSize = IntSize(size.width.toInt(), size.height.toInt())

        particles.forEach { particle ->
            drawParticle(particle, pulseScale)
        }
    }
}

private fun createParticle(
    id: Int,
    size: IntSize,
    color: Color
): TimeParticle {
    return TimeParticle(
        id = id,
        x = Random.nextFloat() * size.width,
        y = size.height + Random.nextFloat() * 200,
        size = Random.nextFloat() * 40 + 10,
        alpha = Random.nextFloat() * 0.5f + 0.1f,
        speed = Random.nextFloat() * 1.5f + 0.3f,
        rotation = Random.nextFloat() * 360,
        rotationSpeed = (Random.nextFloat() - 0.5f) * 2,
        shape = ParticleShape.entries.toTypedArray().random(),
        color = color
    )
}

private fun DrawScope.drawParticle(particle: TimeParticle, pulseScale: Float) {
    val adjustedSize = particle.size * pulseScale
    val center = Offset(particle.x, particle.y)

    when (particle.shape) {
        ParticleShape.CIRCLE -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        particle.color.copy(alpha = particle.alpha),
                        particle.color.copy(alpha = 0f)
                    ),
                    center = center,
                    radius = adjustedSize
                ),
                radius = adjustedSize,
                center = center
            )
        }
        ParticleShape.RING -> {
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha * 0.8f),
                radius = adjustedSize,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha * 0.3f),
                radius = adjustedSize * 0.6f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }
        ParticleShape.TRIANGLE -> {
            rotate(particle.rotation, center) {
                val path = Path().apply {
                    moveTo(center.x, center.y - adjustedSize)
                    lineTo(center.x - adjustedSize * 0.866f, center.y + adjustedSize * 0.5f)
                    lineTo(center.x + adjustedSize * 0.866f, center.y + adjustedSize * 0.5f)
                    close()
                }
                drawPath(path, particle.color.copy(alpha = particle.alpha * 0.5f))
            }
        }
        ParticleShape.HEXAGON -> {
            rotate(particle.rotation, center) {
                val path = Path().apply {
                    for (i in 0..5) {
                        val angle = Math.toRadians((60.0 * i) - 30)
                        val x = center.x + adjustedSize * cos(angle).toFloat()
                        val y = center.y + adjustedSize * sin(angle).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(path, particle.color.copy(alpha = particle.alpha * 0.4f))
            }
        }
        ParticleShape.DIAMOND -> {
            rotate(particle.rotation, center) {
                val path = Path().apply {
                    moveTo(center.x, center.y - adjustedSize)
                    lineTo(center.x + adjustedSize * 0.6f, center.y)
                    lineTo(center.x, center.y + adjustedSize)
                    lineTo(center.x - adjustedSize * 0.6f, center.y)
                    close()
                }
                drawPath(path, particle.color.copy(alpha = particle.alpha * 0.5f))
            }
        }
        ParticleShape.LINE -> {
            rotate(particle.rotation, center) {
                drawLine(
                    color = particle.color.copy(alpha = particle.alpha),
                    start = Offset(center.x - adjustedSize, center.y),
                    end = Offset(center.x + adjustedSize, center.y),
                    strokeWidth = 2f
                )
            }
        }
        ParticleShape.DOT_CLUSTER -> {
            for (i in 0..4) {
                val angle = Math.toRadians((72.0 * i) + particle.rotation)
                val dotOffset = Offset(
                    center.x + (adjustedSize * 0.5f * cos(angle)).toFloat(),
                    center.y + (adjustedSize * 0.5f * sin(angle)).toFloat()
                )
                drawCircle(
                    color = particle.color.copy(alpha = particle.alpha),
                    radius = 3f,
                    center = dotOffset
                )
            }
        }
    }
}

/**
 * Time-reactive background mesh gradient
 * Creates a living, breathing background that responds to time
 */
@Composable
fun MeshGradientBackground(
    modifier: Modifier = Modifier,
    time: Calendar,
    dynamicColors: List<Color> = listOf(
        Color(0xFF0F2027),
        Color(0xFF203A43),
        Color(0xFF2C5364)
    )
) {
    val second = time.get(Calendar.SECOND)
    val millisecond = time.get(Calendar.MILLISECOND)

    // Breathing animation
    val breathPhase = (second + millisecond / 1000f) / 60f * 2 * PI
    val breathScale by animateFloatAsState(
        targetValue = 0.8f + sin(breathPhase.toFloat()) * 0.2f,
        animationSpec = tween(1000),
        label = "breath"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "mesh")

    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Multiple gradient layers for mesh effect
        val gradients = listOf(
            Brush.radialGradient(
                colors = listOf(
                    dynamicColors.getOrElse(0) { Color(0xFF0F2027) }.copy(alpha = 0.8f * breathScale),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.2f + offsetX, size.height * 0.3f + offsetY),
                radius = size.maxDimension * 0.8f
            ),
            Brush.radialGradient(
                colors = listOf(
                    dynamicColors.getOrElse(1) { Color(0xFF203A43) }.copy(alpha = 0.6f * breathScale),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.8f - offsetX, size.height * 0.7f - offsetY),
                radius = size.maxDimension * 0.7f
            ),
            Brush.radialGradient(
                colors = listOf(
                    dynamicColors.getOrElse(2) { Color(0xFF2C5364) }.copy(alpha = 0.5f * breathScale),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.5f),
                radius = size.maxDimension * 0.6f
            )
        )

        gradients.forEach { brush ->
            drawRect(brush = brush)
        }
    }
}

/**
 * Light reflection effect that moves with device tilt
 */
@Composable
fun GlassReflection(
    modifier: Modifier = Modifier,
    tiltX: Float,
    tiltY: Float
) {
    val offsetX = tiltX * 200
    val offsetY = tiltY * 200

    Canvas(modifier = modifier.fillMaxSize()) {
        // Primary reflection streak
        val reflectionBrush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.1f),
                Color.White.copy(alpha = 0.05f),
                Color.Transparent
            ),
            start = Offset(offsetX - 200, offsetY - 400),
            end = Offset(size.width + offsetX + 200, size.height + offsetY + 400)
        )

        drawRect(brush = reflectionBrush)

        // Secondary subtle reflection
        val secondaryBrush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.3f + offsetX, size.height * 0.2f + offsetY),
            radius = 300f
        )

        drawRect(brush = secondaryBrush)
    }
}

