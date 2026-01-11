package com.quantum_prof.chronotime.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.backdrops.layerBackdrop

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
    FloatArray(GLASS_NOISE_POINT_COUNT * 3) { _ ->
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
 * - Semi-transparent glass overlay (content remains sharp and readable)
 * - Animated AGSL grain shader for frosted glass texture (Android 13+)
 * - Specular gradient border (light hitting top-left edge)
 * - Inner glow at top, inner shadow at bottom for 3D volume
 * 
 * Note: The glassmorphism effect is achieved through semi-transparency and grain
 * texture - NOT through blur. Background elements (particles, gradients) show 
 * through the translucent surface, creating a frosted glass appearance.
 * The content (text, clocks) remains razor-sharp and highly readable.
 */
@Composable
fun GlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(32.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val cardBackdrop = rememberLayerBackdrop()
    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(24f.dp.toPx(), 48f.dp.toPx(), true)
                },
                exportedBackdrop = cardBackdrop,
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
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
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(24f.dp.toPx(), 48f.dp.toPx(), true)
                },
                onDrawSurface = { drawRect(Color.Black.copy(alpha = 0.3f)) }
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
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    neonColor: Color = Color(0xFF00FF88),
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    content: @Composable BoxScope.() -> Unit
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

    Box(
        modifier = modifier
            .scale(pulseScale)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(8.dp.toPx())
                    lens(32.dp.toPx(), 64.dp.toPx(), true)
                },
                onDrawSurface = {
                    drawRect(neonColor.copy(alpha = 0.1f))
                    drawRect(Color.White.copy(alpha = 0.2f))
                }
            )
            .border(2.dp, neonColor.copy(alpha = 0.5f), shape)
            .padding(24.dp)
    ) {
        content()
    }
}

/**
 * Liquid Glass Card using Kyant Backdrop Library
 * 
 * Implements the "Backdrop" technique for liquid glass effect:
 * - Renders background content
 * - Applies blur effect without clipping at card bounds
 * - Creates fluid, water-like glass appearance
 * 
 * The blur extends naturally beyond card edges for realistic glass look.
 * Uses vibrancy and blur effects from the backdrop library.
 * 
 * @param backdrop The Backdrop instance from parent (created via rememberLayerBackdrop)
 * @param modifier Modifier for the card
 * @param shape Card shape (rounded corners)
 * @param glowColor Color for the outer glow effect
 * @param enableGlow Whether to show ambient glow
 * @param content The content to display inside the card
 */
@Composable
fun LiquidGlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(32.dp),
    glowColor: Color = Color(0xFF00F0FF),
    enableGlow: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquidGlass")
    
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
    
    Box(
        modifier = modifier
            // Scale breathing effect
            .graphicsLayer {
                scaleX = breathScale
                scaleY = breathScale
                // CRITICAL: clip = false allows blur to extend beyond bounds
                clip = false
            }
            // Outer glow (ambient shadow) - enhanced depth
            .shadow(
                elevation = 32.dp,
                shape = shape,
                spotColor = if (enableGlow) glowColor.copy(alpha = glowAlpha) else Color.Black.copy(alpha = 0.4f),
                ambientColor = if (enableGlow) glowColor.copy(alpha = glowAlpha * 0.5f) else Color.Black.copy(alpha = 0.15f)
            )
            // Apply backdrop blur effect - this is the key to liquid glass
            // The blur does NOT clip at card bounds - it extends naturally
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(24f.dp.toPx(), 48f.dp.toPx(), true)
                },
                /*onDrawSurface = {
                    // Semi-transparent glass surface
                    drawRect(Color.White.copy(alpha = 0.15f))
                } */
            )
            // Animated grain noise texture overlay
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
                    // Fallback: Pre-computed noise pattern
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
            /* Inner shadow and reflections for depth
            .drawBehind {
                // Top edge highlight - Inner white glow
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
                
                /* Bottom edge subtle shadow
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
                
                / Moving light reflection (caustic effect)
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
                ) */
            } */
            /* Specular border
            .border(
                BorderStroke(
                    0.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.08f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
                shape
            ) */
            .padding(24.dp),
        content = content
    )
}

/**
 * Creates a Liquid Glass Scaffold - the parent container that provides
 * the backdrop source for child LiquidGlassCard components.
 * 
 * Usage:
 * ```
 * LiquidGlassScaffold(
 *     backgroundColor = DeepBackground,
 *     backgroundContent = { 
 *         // Your animated background (particles, gradients, etc.)
 *         MeshGradientBackground()
 *     }
 * ) { backdrop ->
 *     // Your glass cards that use the backdrop
 *     LiquidGlassCard(backdrop = backdrop) {
 *         Text("Content")
 *     }
 * }
 * ```
 */
@Composable
fun LiquidGlassScaffold(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF050510),
    backgroundContent: @Composable () -> Unit = {},
    content: @Composable BoxScope.(backdrop: Backdrop) -> Unit
) {
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .layerBackdrop(backdrop)
    ) {
        // Background layer (becomes the backdrop source)
        backgroundContent()
        
        // Foreground content with backdrop available
        content(backdrop)
    }
}
