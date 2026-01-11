package com.quantum_prof.chronotime.ui.components

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quantum_prof.chronotime.ui.theme.GlassBorder
import com.quantum_prof.chronotime.ui.theme.GlassWhite

/**
 * Enhanced Deep Glass Card with Superellipse shape and advanced effects
 * "Frosted Glass" that feels like etched glass floating above the background
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 30.dp,
    shape: RoundedCornerShape = RoundedCornerShape(32.dp), // More rounded for superellipse feel
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

    Box(
        modifier = modifier
            // Outer glow (ambient shadow)
            .shadow(
                elevation = 20.dp,
                shape = shape,
                spotColor = if (enableGlow) glowColor.copy(alpha = glowAlpha) else Color.Black.copy(alpha = 0.3f),
                ambientColor = if (enableGlow) glowColor.copy(alpha = glowAlpha * 0.5f) else Color.Black.copy(alpha = 0.1f)
            )
            .clip(shape)
            // Multi-layer glass effect
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.05f),
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            )
            // Inner shadow for depth
            .drawBehind {
                // Top edge highlight
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 30f
                    )
                )

                // Moving light reflection
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        start = Offset(reflectionOffset, 0f),
                        end = Offset(reflectionOffset + 150f, size.height)
                    )
                )
            }
            // Glowing border with gradient
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.3f)
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

