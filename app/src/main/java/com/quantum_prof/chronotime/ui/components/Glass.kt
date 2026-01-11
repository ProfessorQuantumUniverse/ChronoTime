package com.quantum_prof.chronotime.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quantum_prof.chronotime.ui.theme.GlassBorder
import com.quantum_prof.chronotime.ui.theme.GlassWhite
import kotlin.random.Random

/**
 * Number of noise points for the glass texture effect.
 * 350 points provides good visual density while maintaining performance.
 * On Android 13+, the AGSL shader is used instead for better quality.
 * Note: This is only used as a fallback on older devices.
 */
private const val GLASS_NOISE_POINT_COUNT = 350

/**
 * Pre-computed noise pattern for glass texture using FloatArray for memory efficiency.
 * Each point is stored as 3 consecutive floats: xRatio, yRatio, alpha.
 * Total size: 350 * 3 = 1050 floats
 * Pre-computed at app startup to avoid allocation during rendering.
 */
private val GLASS_NOISE_PATTERN: FloatArray by lazy {
    val random = Random(42) // Fixed seed for consistent pattern
    FloatArray(GLASS_NOISE_POINT_COUNT * 3) { index ->
        random.nextFloat()
    }
}

/**
 * AGSL Shader for animated grain/noise overlay
 * Creates realistic frosted glass texture with animated grain
 */
private val GRAIN_NOISE_SHADER: String by lazy {
    """
    uniform float2 resolution;
    uniform float time;
    uniform float intensity;
    
    // High quality noise function
    float hash(float2 p) {
        float3 p3 = fract(float3(p.xyx) * 0.1031);
        p3 += dot(p3, p3.yzx + 33.33);
        return fract((p3.x + p3.y) * p3.z);
    }
    
    float noise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        float2 u = f * f * (3.0 - 2.0 * f);
        
        float a = hash(i);
        float b = hash(i + float2(1.0, 0.0));
        float c = hash(i + float2(0.0, 1.0));
        float d = hash(i + float2(1.0, 1.0));
        
        return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
    }
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;
        
        // Multi-frequency animated noise for organic grain
        float n1 = noise(uv * 200.0 + time * 10.0);
        float n2 = noise(uv * 400.0 - time * 15.0);
        float n3 = noise(uv * 100.0 + time * 5.0);
        
        // Combine noise layers
        float grain = (n1 * 0.5 + n2 * 0.3 + n3 * 0.2) * intensity;
        
        // Very subtle grain effect
        return half4(grain, grain, grain, grain * 0.03);
    }
    """.trimIndent()
}

/**
 * Enhanced Deep Glass Card with "Hyper-Real" Glass Effect
 * Implements:
 * - High-radius background blur (35dp default, configurable for performance)
 * - Animated AGSL grain shader for frosted glass texture (Android 13+)
 * - Specular gradient border (light hitting top-left edge)
 * - Inner glow at top, inner shadow at bottom for 3D volume
 * 
 * Performance note: Higher blur radius increases GPU load. On lower-end devices,
 * consider reducing blurRadius to 25-30dp for smoother performance.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 35.dp, // Configurable for performance tuning
    shape: RoundedCornerShape = RoundedCornerShape(32.dp),
    glowColor: Color = Color(0xFF00F0FF),
    enableGlow: Boolean = true,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glassGlow")

    // Subtle pulsing glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    // Light reflection movement
    val reflectionOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reflection"
    )

    // Breathing scale for organic feel
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.003f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    
    // Animated grain time for AGSL shader
    val grainTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grainTime"
    )

    BoxWithConstraints(
        modifier = modifier
            // Scale breathing effect
            .graphicsLayer {
                scaleX = breathScale
                scaleY = breathScale
            }
            // Apply RenderEffect blur on Android 12+ for true frosted glass
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        renderEffect = RenderEffect
                            .createBlurEffect(
                                blurRadius.value,
                                blurRadius.value,
                                Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                    }
                } else {
                    Modifier.blur(blurRadius / 2)
                }
            )
            // Outer glow (ambient shadow) - enhanced depth
            .shadow(
                elevation = 32.dp,
                shape = shape,
                spotColor = if (enableGlow) glowColor.copy(alpha = glowAlpha) else Color.Black.copy(alpha = 0.4f),
                ambientColor = if (enableGlow) glowColor.copy(alpha = glowAlpha * 0.5f) else Color.Black.copy(alpha = 0.15f)
            )
            .clip(shape)
            // Multi-layer glass effect with depth - Enhanced opacity gradient
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.14f)
                    )
                )
            )
            // Animated grain noise texture overlay (AGSL on Android 13+, fallback on older)
            .drawWithContent {
                drawContent()
                
                // Use AGSL shader for animated grain on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val shader = RuntimeShader(GRAIN_NOISE_SHADER)
                    shader.setFloatUniform("resolution", size.width, size.height)
                    shader.setFloatUniform("time", grainTime)
                    shader.setFloatUniform("intensity", 1.0f)
                    drawRect(brush = ShaderBrush(shader))
                } else {
                    // Fallback: Pre-computed noise pattern with higher density
                    for (i in 0 until GLASS_NOISE_POINT_COUNT) {
                        val baseIndex = i * 3
                        val xRatio = GLASS_NOISE_PATTERN[baseIndex]
                        val yRatio = GLASS_NOISE_PATTERN[baseIndex + 1]
                        val alpha = GLASS_NOISE_PATTERN[baseIndex + 2]
                        drawCircle(
                            color = Color.White.copy(alpha = alpha * 0.025f),
                            radius = 0.8f,
                            center = Offset(size.width * xRatio, size.height * yRatio)
                        )
                    }
                }
            }
            // Inner shadow and reflections for depth - Enhanced 3D volume
            .drawBehind {
                // Top edge highlight - Inner white glow (simulates light from above)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 80f
                    )
                )

                // Bottom edge subtle shadow (inner) - Creates 3D depth
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.03f),
                            Color.Black.copy(alpha = 0.08f)
                        ),
                        startY = size.height - 60f,
                        endY = size.height
                    )
                )
                
                // Left edge inner glow (simulates light from top-left)
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 40f
                    )
                )
                
                // Right edge inner shadow
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.05f)
                        ),
                        startX = size.width - 40f,
                        endX = size.width
                    )
                )

                // Moving light reflection (caustic effect) - Enhanced
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        start = Offset(reflectionOffset, 0f),
                        end = Offset(reflectionOffset + 180f, size.height)
                    )
                )
            }
            // Specular border - Gradient from White to Transparent (light hitting top-left)
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f),  // Top-left - brightest (light source)
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.08f),  // Bottom-right - darkest
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
                shape
            )
            .padding(24.dp)
    ) {
        content()
    }
}

/**
 * Minimalist Glass Card variant for specific clock modes
 */
@Composable
fun MinimalGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.2f))
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.3f))
            .border(
                BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
                shape
            )
            .padding(16.dp)
    ) {
        content()
    }
}

/**
 * Neon Glass Card with intense glow for special modes
 * Enhanced with grain texture and specular border
 */
@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    neonColor: Color = Color(0xFF00FF88),
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neonPulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Animated grain for texture
    val grainTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grainTime"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
            .shadow(
                elevation = 35.dp,
                shape = shape,
                spotColor = neonColor.copy(alpha = glowIntensity),
                ambientColor = neonColor.copy(alpha = glowIntensity * 0.4f)
            )
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.75f))
            // Add grain noise texture
            .drawWithContent {
                drawContent()
                // Subtle grain for texture
                for (i in 0 until GLASS_NOISE_POINT_COUNT / 2) {
                    val baseIndex = i * 3
                    val xRatio = GLASS_NOISE_PATTERN[baseIndex]
                    val yRatio = GLASS_NOISE_PATTERN[baseIndex + 1]
                    val alpha = GLASS_NOISE_PATTERN[baseIndex + 2]
                    drawCircle(
                        color = neonColor.copy(alpha = alpha * 0.015f),
                        radius = 0.6f,
                        center = Offset(size.width * xRatio, size.height * yRatio)
                    )
                }
            }
            // Inner glow effects
            .drawBehind {
                // Top inner glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            neonColor.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 50f
                    )
                )
                // Bottom inner shadow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f)
                        ),
                        startY = size.height - 40f,
                        endY = size.height
                    )
                )
            }
            .border(
                BorderStroke(
                    2.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            neonColor.copy(alpha = 0.9f),
                            neonColor.copy(alpha = 0.5f),
                            neonColor.copy(alpha = 0.3f),
                            neonColor.copy(alpha = 0.6f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
                shape
            )
            .padding(20.dp)
    ) {
        content()
    }
}

