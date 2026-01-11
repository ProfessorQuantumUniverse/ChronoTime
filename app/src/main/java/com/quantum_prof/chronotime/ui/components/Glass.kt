package com.quantum_prof.chronotime.ui.components

import android.graphics.RenderEffect
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
 * 200 points provides a good balance between visual density and performance.
 * Fewer points would look too sparse, while more would impact rendering performance.
 */
private const val GLASS_NOISE_POINT_COUNT = 200

/**
 * Pre-computed noise pattern for glass texture using FloatArray for memory efficiency.
 * Each point is stored as 3 consecutive floats: xRatio, yRatio, alpha.
 * Total size: 200 * 3 = 600 floats
 */
private val GLASS_NOISE_PATTERN: FloatArray by lazy {
    val random = Random(42) // Fixed seed for consistent pattern
    FloatArray(GLASS_NOISE_POINT_COUNT * 3) { index ->
        random.nextFloat()
    }
}

/**
 * Enhanced Deep Glass Card with Superellipse shape, RenderEffect blur, 
 * white noise texture overlay, and advanced "Frosted Glass" effects
 * for the "Deep Glass" aesthetic.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 30.dp,
    shape: RoundedCornerShape = RoundedCornerShape(32.dp),
    glowColor: Color = Color(0xFF00F0FF),
    enableGlow: Boolean = true,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glassGlow")

    // Subtle pulsing glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
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
        targetValue = 1.005f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
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
            // Outer glow (ambient shadow)
            .shadow(
                elevation = 24.dp,
                shape = shape,
                spotColor = if (enableGlow) glowColor.copy(alpha = glowAlpha) else Color.Black.copy(alpha = 0.3f),
                ambientColor = if (enableGlow) glowColor.copy(alpha = glowAlpha * 0.5f) else Color.Black.copy(alpha = 0.1f)
            )
            .clip(shape)
            // Multi-layer glass effect with depth
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.06f),
                        Color.White.copy(alpha = 0.12f)
                    )
                )
            )
            // White noise texture overlay for glass grain (using pre-computed pattern)
            .drawWithContent {
                drawContent()
                // Draw subtle noise texture from pre-computed FloatArray
                for (i in 0 until GLASS_NOISE_POINT_COUNT) {
                    val baseIndex = i * 3
                    val xRatio = GLASS_NOISE_PATTERN[baseIndex]
                    val yRatio = GLASS_NOISE_PATTERN[baseIndex + 1]
                    val alpha = GLASS_NOISE_PATTERN[baseIndex + 2]
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.015f),
                        radius = 0.5f,
                        center = Offset(size.width * xRatio, size.height * yRatio)
                    )
                }
            }
            // Inner shadow and reflections for depth
            .drawBehind {
                // Top edge highlight (bevel effect)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 50f
                    )
                )

                // Bottom edge subtle shadow (inner)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.05f)
                        ),
                        startY = size.height - 30f,
                        endY = size.height
                    )
                )

                // Moving light reflection (caustic effect)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        start = Offset(reflectionOffset, 0f),
                        end = Offset(reflectionOffset + 150f, size.height)
                    )
                )
            }
            // 10% opacity white border stroke (as specified)
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.25f)
                        )
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
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
            .shadow(
                elevation = 30.dp,
                shape = shape,
                spotColor = neonColor.copy(alpha = glowIntensity),
                ambientColor = neonColor.copy(alpha = glowIntensity * 0.3f)
            )
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.7f))
            .border(
                BorderStroke(
                    2.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            neonColor.copy(alpha = 0.8f),
                            neonColor.copy(alpha = 0.4f),
                            neonColor.copy(alpha = 0.8f)
                        )
                    )
                ),
                shape
            )
            .padding(20.dp)
    ) {
        content()
    }
}

