package com.quantum_prof.chronotime.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.*
import kotlin.random.Random

/**
 * AGSL Shader source for animated mesh gradient background
 * Creates a living, pulsating gradient that responds to time
 * ENHANCED: More vibrant colors, deeper saturation for premium glass blur
 * Lazy initialized to avoid allocation on class load
 */
private val MESH_GRADIENT_SHADER: String by lazy {
    """
    uniform float2 resolution;
    uniform float time;
    uniform float3 color1;
    uniform float3 color2;
    uniform float3 color3;
    uniform float breathScale;
    
    // Simplex noise helper functions
    float3 mod289(float3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
    float2 mod289(float2 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
    float3 permute(float3 x) { return mod289(((x * 34.0) + 1.0) * x); }
    
    float snoise(float2 v) {
        const float4 C = float4(0.211324865405187, 0.366025403784439,
                               -0.577350269189626, 0.024390243902439);
        float2 i  = floor(v + dot(v, C.yy));
        float2 x0 = v -   i + dot(i, C.xx);
        float2 i1;
        i1 = (x0.x > x0.y) ? float2(1.0, 0.0) : float2(0.0, 1.0);
        float4 x12 = x0.xyxy + C.xxzz;
        x12.xy -= i1;
        i = mod289(i);
        float3 p = permute(permute(i.y + float3(0.0, i1.y, 1.0)) + i.x + float3(0.0, i1.x, 1.0));
        float3 m = max(0.5 - float3(dot(x0, x0), dot(x12.xy, x12.xy), dot(x12.zw, x12.zw)), 0.0);
        m = m * m;
        m = m * m;
        float3 x = 2.0 * fract(p * C.www) - 1.0;
        float3 h = abs(x) - 0.5;
        float3 ox = floor(x + 0.5);
        float3 a0 = x - ox;
        m *= 1.79284291400159 - 0.85373472095314 * (a0 * a0 + h * h);
        float3 g;
        g.x = a0.x * x0.x + h.x * x0.y;
        g.yz = a0.yz * x12.xz + h.yz * x12.yw;
        return 130.0 * dot(m, g);
    }
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;
        
        // Animated noise for organic movement - ENHANCED amplitude
        float noise1 = snoise(uv * 2.0 + time * 0.12) * 0.5 + 0.5;
        float noise2 = snoise(uv * 3.0 - time * 0.18) * 0.5 + 0.5;
        float noise3 = snoise(uv * 1.5 + time * 0.1) * 0.5 + 0.5;
        float noise4 = snoise(uv * 4.0 + time * 0.08) * 0.5 + 0.5;
        
        // Radial mesh points with breathing animation - MORE DYNAMIC
        float2 center1 = float2(0.15, 0.25) + float2(sin(time * 0.35), cos(time * 0.25)) * 0.15 * breathScale;
        float2 center2 = float2(0.85, 0.75) + float2(cos(time * 0.3), sin(time * 0.4)) * 0.15 * breathScale;
        float2 center3 = float2(0.5, 0.5) + float2(sin(time * 0.25), cos(time * 0.35)) * 0.08 * breathScale;
        float2 center4 = float2(0.7, 0.2) + float2(cos(time * 0.2), sin(time * 0.3)) * 0.1 * breathScale;
        
        float d1 = distance(uv, center1);
        float d2 = distance(uv, center2);
        float d3 = distance(uv, center3);
        float d4 = distance(uv, center4);
        
        // Smooth color blending with noise modulation - ENHANCED vibrancy
        float w1 = smoothstep(1.2, 0.0, d1 * 1.3) * (0.6 + noise1 * 0.4);
        float w2 = smoothstep(1.2, 0.0, d2 * 1.3) * (0.6 + noise2 * 0.4);
        float w3 = smoothstep(1.0, 0.0, d3 * 1.4) * (0.7 + noise3 * 0.3);
        float w4 = smoothstep(0.8, 0.0, d4 * 1.5) * (0.5 + noise4 * 0.5);
        
        float total = w1 + w2 + w3 + w4 + 0.001;
        w1 /= total;
        w2 /= total;
        w3 /= total;
        w4 /= total;
        
        // More vibrant color mixing with accent color
        float3 color = color1 * w1 + color2 * w2 + color3 * w3;
        
        // Add subtle color accent based on fourth weight
        float3 accentColor = mix(color1, color2, 0.5) * 1.3;
        color = mix(color, accentColor, w4 * 0.3);
        
        // Boost saturation for vibrancy
        float gray = dot(color, float3(0.299, 0.587, 0.114));
        color = mix(float3(gray, gray, gray), color, 1.25);
        
        // Add subtle vignette with softer falloff
        float vignette = 1.0 - smoothstep(0.5, 1.1, length(uv - 0.5));
        color *= 0.75 + vignette * 0.25;
        
        // Slight brightness boost for glass blur to look better
        color *= 1.15;
        
        return half4(color, 1.0);
    }
    """.trimIndent()
}

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

/**
 * Foreground Particle Field - Small, fast particles that pass IN FRONT of the glass
 * Creates depth perception by having sharp particles in front of the blurred glass
 */
@Composable
fun ForegroundParticleField(
    modifier: Modifier = Modifier,
    time: Calendar,
    particleCount: Int = 12,
    baseColor: Color = Color(0xFF00F0FF)
) {
    val second = time.get(Calendar.SECOND)
    
    // Brighter, more visible colors for foreground particles
    val foregroundColors = remember {
        listOf(
            Color.White.copy(alpha = 0.7f),
            Color(0xFF00F0FF).copy(alpha = 0.6f),
            Color(0xFFFF006E).copy(alpha = 0.5f),
        )
    }
    
    var particles by remember { mutableStateOf<List<TimeParticle>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize(1080, 1920)) }
    
    // Initialize smaller, faster foreground particles
    LaunchedEffect(Unit) {
        particles = List(particleCount) { index ->
            createForegroundParticle(index, canvasSize, foregroundColors.random())
        }
    }
    
    // Animate particles - faster movement for foreground
    LaunchedEffect(Unit) {
        while (true) {
            particles = particles.map { particle ->
                particle.copy(
                    y = particle.y - particle.speed,
                    x = particle.x + sin(particle.y / 100f).toFloat() * 0.5f, // Slight horizontal drift
                    rotation = particle.rotation + particle.rotationSpeed,
                    alpha = (particle.alpha * 0.998f).coerceIn(0.2f, 0.8f) // Slow fade
                ).let {
                    // Reset particle when it goes off screen
                    if (it.y < -50) {
                        createForegroundParticle(it.id, canvasSize, foregroundColors.random())
                    } else it
                }
            }
            delay(16) // ~60fps
        }
    }
    
    // Subtle pulse synced to seconds
    val pulseScale by animateFloatAsState(
        targetValue = if (second % 2 == 0) 1.15f else 1f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "fgPulse"
    )
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        canvasSize = IntSize(size.width.toInt(), size.height.toInt())
        
        particles.forEach { particle ->
            drawForegroundParticle(particle, pulseScale)
        }
    }
}

/**
 * Create smaller, faster particles for the foreground layer
 */
private fun createForegroundParticle(
    id: Int,
    size: IntSize,
    color: Color
): TimeParticle {
    return TimeParticle(
        id = id,
        x = Random.nextFloat() * size.width,
        y = size.height + Random.nextFloat() * 100,
        size = Random.nextFloat() * 8 + 2, // Much smaller (2-10px)
        alpha = Random.nextFloat() * 0.4f + 0.4f, // Brighter (0.4-0.8)
        speed = Random.nextFloat() * 4f + 2f, // Faster (2-6px per frame)
        rotation = Random.nextFloat() * 360,
        rotationSpeed = (Random.nextFloat() - 0.5f) * 4, // Faster rotation
        shape = listOf(ParticleShape.CIRCLE, ParticleShape.DOT_CLUSTER, ParticleShape.DIAMOND).random(),
        color = color
    )
}

/**
 * Draw foreground particles with sharper, more defined appearance
 */
private fun DrawScope.drawForegroundParticle(particle: TimeParticle, pulseScale: Float) {
    val adjustedSize = particle.size * pulseScale
    val center = Offset(particle.x, particle.y)
    
    when (particle.shape) {
        ParticleShape.CIRCLE -> {
            // Sharp circle with soft glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = particle.alpha * 0.9f),
                        particle.color.copy(alpha = particle.alpha * 0.6f),
                        particle.color.copy(alpha = 0f)
                    ),
                    center = center,
                    radius = adjustedSize * 2
                ),
                radius = adjustedSize * 2,
                center = center
            )
            // Bright core
            drawCircle(
                color = Color.White.copy(alpha = particle.alpha),
                radius = adjustedSize * 0.5f,
                center = center
            )
        }
        ParticleShape.DOT_CLUSTER -> {
            // Sparkle effect
            for (i in 0..2) {
                val angle = Math.toRadians((120.0 * i) + particle.rotation)
                val dotOffset = Offset(
                    center.x + (adjustedSize * cos(angle)).toFloat(),
                    center.y + (adjustedSize * sin(angle)).toFloat()
                )
                drawCircle(
                    color = Color.White.copy(alpha = particle.alpha * 0.8f),
                    radius = 1.5f,
                    center = dotOffset
                )
            }
            // Center dot
            drawCircle(
                color = Color.White.copy(alpha = particle.alpha),
                radius = 2f,
                center = center
            )
        }
        ParticleShape.DIAMOND -> {
            // Small sharp diamond
            rotate(particle.rotation, center) {
                val path = Path().apply {
                    moveTo(center.x, center.y - adjustedSize)
                    lineTo(center.x + adjustedSize * 0.5f, center.y)
                    lineTo(center.x, center.y + adjustedSize)
                    lineTo(center.x - adjustedSize * 0.5f, center.y)
                    close()
                }
                drawPath(path, Color.White.copy(alpha = particle.alpha * 0.7f))
            }
        }
        else -> {
            // Default to bright dot
            drawCircle(
                color = Color.White.copy(alpha = particle.alpha),
                radius = adjustedSize,
                center = center
            )
        }
    }
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
 * AGSL Shader-based animated mesh gradient background
 * Creates a living, breathing background that responds to time and dominant color
 * Falls back to Canvas-based implementation on older devices
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

    // Breathing animation synced to time
    val breathPhase = (second + millisecond / 1000f) / 60f * 2 * PI
    val breathScale by animateFloatAsState(
        targetValue = 0.8f + sin(breathPhase.toFloat()) * 0.2f,
        animationSpec = tween(1000),
        label = "breath"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "mesh")

    // Continuous time animation for shader
    val animatedTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

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

    // Use AGSL shader on Android 13+ for beautiful mesh gradients
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(MESH_GRADIENT_SHADER) }
        
        Canvas(modifier = modifier.fillMaxSize()) {
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("time", animatedTime)
            shader.setFloatUniform("breathScale", breathScale)
            
            // Extract RGB from colors
            val c1 = dynamicColors.getOrElse(0) { Color(0xFF0F2027) }
            val c2 = dynamicColors.getOrElse(1) { Color(0xFF203A43) }
            val c3 = dynamicColors.getOrElse(2) { Color(0xFF2C5364) }
            
            shader.setFloatUniform("color1", c1.red, c1.green, c1.blue)
            shader.setFloatUniform("color2", c2.red, c2.green, c2.blue)
            shader.setFloatUniform("color3", c3.red, c3.green, c3.blue)
            
            drawRect(brush = ShaderBrush(shader))
        }
    } else {
        // Fallback: Canvas-based mesh gradient for older devices
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
}

/**
 * Light reflection effect that moves with device tilt
 * Creates caustic-like reflections on the glass surfaces
 */
@Composable
fun GlassReflection(
    modifier: Modifier = Modifier,
    tiltX: Float,
    tiltY: Float
) {
    val offsetX = tiltX * 200
    val offsetY = tiltY * 200

    val infiniteTransition = rememberInfiniteTransition(label = "reflection")
    
    // Subtle shimmer animation
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Primary reflection streak (responds to tilt)
        val reflectionBrush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.03f + shimmer * 0.02f),
                Color.White.copy(alpha = 0.08f + shimmer * 0.04f),
                Color.White.copy(alpha = 0.03f + shimmer * 0.02f),
                Color.Transparent
            ),
            start = Offset(offsetX - 200, offsetY - 400),
            end = Offset(size.width + offsetX + 200, size.height + offsetY + 400)
        )

        drawRect(brush = reflectionBrush)

        // Secondary subtle reflection (more diffuse)
        val secondaryBrush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.06f + shimmer * 0.02f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.3f + offsetX, size.height * 0.2f + offsetY),
            radius = 300f
        )

        drawRect(brush = secondaryBrush)

        // Tertiary accent reflection on opposite corner
        val tertiaryBrush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.04f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.7f - offsetX * 0.5f, size.height * 0.8f - offsetY * 0.5f),
            radius = 200f
        )

        drawRect(brush = tertiaryBrush)
    }
}

