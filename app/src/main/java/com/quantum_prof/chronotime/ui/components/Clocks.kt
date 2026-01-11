package com.quantum_prof.chronotime.ui.components

import android.R.attr.typeface
import android.annotation.SuppressLint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.Font
import com.quantum_prof.chronotime.R
import com.quantum_prof.chronotime.ui.utils.AutoScalingText
import androidx.core.content.res.ResourcesCompat.getFont
import com.kyant.backdrop.Backdrop
import kotlin.shr
import kotlin.toString
import android.R.attr.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth

// --- Components ---

/**
 * HexClock - Displays time as a hex color code
 * RESPONSIVE: Uses AutoScalingText to ensure hex code fits on one line
 * on all screen sizes from Galaxy Z Flip outer screen to tablets.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun HexClock(time: Calendar,
             backdrop: Backdrop
) {
    val r = time.get(Calendar.HOUR_OF_DAY)
    val g = time.get(Calendar.MINUTE)
    val b = time.get(Calendar.SECOND)

    val hexString = String.format("#%02d%02d%02d", r, g, b)

    val bgColor = try {
        Color(android.graphics.Color.parseColor(hexString))
    } catch (e: Exception) {
        Color.Black
    }
    
    // Get screen width for responsive sizing
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isCompactScreen = screenWidth < 360.dp
    val exoFontFamily = FontFamily(Font(R.font.exo2_bold))


    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

            AutoScalingText(
                text = hexString,
                modifier = Modifier.wrapContentWidth(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = exoFontFamily,
                maxFontSize = if (isCompactScreen) 40.sp else 60.sp,
                minFontSize = 16.sp,
                scalingFactor = 0.12f,
                textAlign = TextAlign.Right // Properly centers text glyphs within the bounds
            )

        }
    }

/**
 * BerlinClock - The famous Berlin Mengenlehreuhr
 * RESPONSIVE: Blocks maintain aspect ratio and shrink to fit screen width.
 * On small screens (< 360dp), spacing is reduced.
 */
@Composable
fun BerlinClock(time: Calendar) {
    val hours = time.get(Calendar.HOUR_OF_DAY)
    val minutes = time.get(Calendar.MINUTE)
    val seconds = time.get(Calendar.SECOND)
    
    // Responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isCompactScreen = screenWidth < 360.dp
    
    // Responsive spacing - reduced on small screens
    val rowSpacing = if (isCompactScreen) 4.dp else 8.dp
    val lampSpacing = if (isCompactScreen) 3.dp else 6.dp
    val lampHeight = if (isCompactScreen) 36.dp else 50.dp
    val secondsLampSize = if (isCompactScreen) 44.dp else 60.dp
    val padding = if (isCompactScreen) 8.dp else 16.dp
    
    // Scale factor for very small screens
    val scaleFactor = when {
        screenWidth < 280.dp -> 0.75f
        screenWidth < 320.dp -> 0.85f
        screenWidth < 360.dp -> 0.95f
        else -> 1f
    }

    // Track previous 5-hour count for thud animation
    // Use -1 as sentinel value to avoid triggering on initial render
    val current5HourCount = hours / 5
    var previous5HourCount by remember { mutableStateOf(-1) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // Thud animation scale
    val thudScale = remember { Animatable(1f) }

    // Haptics for Berlin Clock (Every 5 minutes)
    val context = LocalContext.current
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Thud animation when 5-hour block fills (only after initial render)
    LaunchedEffect(current5HourCount) {
        if (!isInitialized) {
            // First render - just set the initial state
            previous5HourCount = current5HourCount
            isInitialized = true
            return@LaunchedEffect
        }
        
        if (current5HourCount != previous5HourCount && current5HourCount > 0) {
            // Trigger thud animation
            thudScale.animateTo(
                targetValue = 1.15f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            thudScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            // Heavy haptic feedback for thud
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
        previous5HourCount = current5HourCount
    }

    LaunchedEffect(minutes) {
        if (minutes % 5 == 0 && seconds == 0) {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                 vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
             } else {
                 @Suppress("DEPRECATION")
                 vibrator.vibrate(100)
             }
        }
    }

    // Apply overall scale for very small screens
    Column(
        modifier = Modifier
            // FIX: Begrenze die Breite, damit die GlassCard im MainScreen Ränder hat
            // Berlin Uhr braucht etwas mehr Platz als Text, daher 0.9f / 0.8f
            .fillMaxWidth(if (isCompactScreen) 0.95f else 0.85f)
            .padding(padding)
            .scale(scaleFactor),
        verticalArrangement = Arrangement.spacedBy(rowSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Seconds Lamp with neon glow
        NeonBerlinLamp(
            active = seconds % 2 == 0, 
            color = Color.Yellow, 
            shape = CircleShape, 
            size = secondsLampSize
        )

        // 5 Hours Row with thud animation
        Row(
            horizontalArrangement = Arrangement.spacedBy(lampSpacing),
            modifier = Modifier.graphicsLayer {
                scaleX = thudScale.value
                scaleY = thudScale.value
            }
        ) {
            repeat(4) { i ->
                NeonBerlinLamp(
                    active = hours / 5 > i, 
                    color = Color.Red, 
                    modifier = Modifier.weight(1f).height(lampHeight)
                )
            }
        }

        // 1 Hour Row
        Row(horizontalArrangement = Arrangement.spacedBy(lampSpacing)) {
            repeat(4) { i ->
                NeonBerlinLamp(
                    active = hours % 5 > i, 
                    color = Color.Red, 
                    modifier = Modifier.weight(1f).height(lampHeight)
                )
            }
        }

        // 5 Minutes Row
        Row(horizontalArrangement = Arrangement.spacedBy(lampSpacing)) {
            repeat(11) { i ->
                val isRed = (i + 1) % 3 == 0
                val c = if (isRed) Color.Red else Color.Yellow
                NeonBerlinLamp(
                    active = minutes / 5 > i, 
                    color = c, 
                    modifier = Modifier.weight(1f).height(lampHeight)
                )
            }
        }

        // 1 Minute Row
        Row(horizontalArrangement = Arrangement.spacedBy(lampSpacing)) {
            repeat(4) { i ->
                NeonBerlinLamp(
                    active = minutes % 5 > i, 
                    color = Color.Yellow, 
                    modifier = Modifier.weight(1f).height(lampHeight)
                )
            }
        }
    }
}

/**
 * Enhanced Berlin Lamp with Neon Glow Effect
 */
@Composable
fun NeonBerlinLamp(
    active: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    size: androidx.compose.ui.unit.Dp? = null
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (active) 1f else 0.08f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "alpha"
    )
    
    val glowIntensity by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glow"
    )

    val mod = if (size != null) modifier.size(size) else modifier

    Box(
        modifier = mod
            // Outer neon glow
            .shadow(
                elevation = if (active) 20.dp else 0.dp,
                shape = shape,
                spotColor = color.copy(alpha = 0.8f * glowIntensity),
                ambientColor = color.copy(alpha = 0.4f * glowIntensity)
            )
            // Inner neon glow layer
            .drawBehind {
                if (active) {
                    // Multiple glow layers for neon effect
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.6f * glowIntensity),
                                color.copy(alpha = 0.3f * glowIntensity),
                                Color.Transparent
                            ),
                            center = Offset(this.size.width / 2, this.size.height / 2),
                            radius = this.size.maxDimension
                        )
                    )
                }
            }
            .background(color.copy(alpha = animatedAlpha), shape)
            .border(2.dp, Color.White.copy(alpha = 0.2f + 0.3f * glowIntensity), shape)
    ) {
        // Glass highlight reflection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f * animatedAlpha),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(100f, 100f)
                    ),
                    shape
                )
        )
    }
}

// Keep old BerlinLamp for backward compatibility
@Composable
fun BerlinLamp(
    active: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    size: androidx.compose.ui.unit.Dp? = null
) {
    NeonBerlinLamp(active, color, modifier, shape, size)
}

/**
 * BinaryClock - Displays time as binary numbers
 * RESPONSIVE: On small screens (< 360dp), reduces gap between dots from 16dp to 4dp.
 * The entire layout scales down based on screen width using Modifier.scale().
 */
/**
 * BinaryClock - Displays time as binary numbers
 * FIXED: Uses weights for even columns and fixed aspect ratios to prevent jitter/squishing.
 * Removed general scaling in favor of responsive layout constraints.
 */
@Composable
fun BinaryClock(time: Calendar) {
    val h = time.get(Calendar.HOUR_OF_DAY)
    val m = time.get(Calendar.MINUTE)
    val s = time.get(Calendar.SECOND)

    var showCheat by remember { mutableStateOf<Int?>(null) } // Index of column clicked

    // Responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isCompactScreen = screenWidth < 360.dp

    // Variables defined for layout logic
    val padding = if (isCompactScreen) 8.dp else 16.dp
    val dotSpacing = if (isCompactScreen) 4.dp else 8.dp
    val separatorWidth = if (isCompactScreen) 8.dp else 16.dp

    // Columns: H1 (2 bits), H2 (4 bits), M1 (3 bits), M2 (4 bits), S1 (3 bits), S2 (4 bits)
    val digits = listOf(h / 10, h % 10, m / 10, m % 10, s / 10, s % 10)
    val bitCounts = listOf(2, 4, 3, 4, 3, 4)

    // Outer Container controlling the max width relative to parent (LiquidGlass)
    Column(
        modifier = Modifier
            .fillMaxWidth(if (isCompactScreen) 0.98f else 0.9f) // Slight inset, not edge-to-edge
            .padding(vertical = padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            // Ensure columns are spaced appropriately, but weights inside columns handle the rest
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            digits.forEachIndexed { index, digit ->
                // Use Weight to treat all digit columns equally -> Fixes unequal dot sizes
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showCheat = if (showCheat == index) null else index },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dotSpacing)
                ) {
                    // Cheat Text (Decimal Number)
                    AnimatedVisibility(visible = showCheat == index) {
                        Text(
                            text = digit.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Render Bits
                    for (bit in (bitCounts[index] - 1) downTo 0) {
                        val isActive = ((digit shr bit) and 1) == 1

                        // Wrapper Box maintaining Aspect Ratio -> Fixes Oval Dots
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f), // Forces Square -> Circle
                            contentAlignment = Alignment.Center
                        ) {
                            GlowingOrb(isActive = isActive, isCompact = isCompactScreen)
                        }
                    }
                }

                // Fixed Width Separators between HH-MM and MM-SS
                if (index == 1 || index == 3) {
                    Spacer(modifier = Modifier.width(separatorWidth))
                } else if (index < 5) {
                    // Small spacer between digits of same unit
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

/**
 * A stable Glowing Orb that does not change layout size based on state
 */
@Composable
fun GlowingOrb(isActive: Boolean, isCompact: Boolean = false) {
    val activeColor = Color(0xFF00F0FF)
    val inactiveColor = Color(0xFF1A1A2E)

    val animatedColor by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        animationSpec = tween(300),
        label = "color"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.6f else 0f,
        label = "glow"
    )

    // Inner Dot Size relative to the Box container
    // We strictly layout the shadow INSIDE this box to prevent jitter
    Box(
        modifier = Modifier.fillMaxSize(0.85f), // Leave room for shadow
        contentAlignment = Alignment.Center
    ) {
        // Shadow (Glow) Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(if (isActive) 1f else 0.8f) // Scale shadow only inside allocated space
                .shadow(
                    elevation = if (isActive) 15.dp else 0.dp,
                    shape = CircleShape,
                    spotColor = activeColor.copy(alpha = glowAlpha),
                    ambientColor = activeColor.copy(alpha = glowAlpha * 0.5f)
                )
        )

        // The actual Dot
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedColor, CircleShape)
                .border(
                    width = 1.dp,
                    color = if (isActive) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )
    }
}



// Keep old BinaryDot for backward compatibility
@Composable
fun BinaryDot(isActive: Boolean) {
    GlowingOrb(isActive)
}

@Composable
fun SwatchBeatClock(time: Calendar) {
    /*
    Swatch Internet Time is calculated by:
    1. Convert time to UTC+1 (Biel Mean Time - BMT)
    2. Calculate seconds past midnight BMT.
    3. Divide by 86.4
    */
    val utcOne = TimeZone.getTimeZone("GMT+1")
    val cal = Calendar.getInstance(utcOne)
    cal.timeInMillis = time.timeInMillis

    val secondsPassed = (cal.get(Calendar.HOUR_OF_DAY) * 3600) +
                        (cal.get(Calendar.MINUTE) * 60) +
                        cal.get(Calendar.SECOND) +
                        (cal.get(Calendar.MILLISECOND) / 1000f)

    val beats = secondsPassed / 86.4f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format("@%03.0f", beats), // Huge bold number, usually integer part is main
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 90.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
        )
        Text(
            text = ".beats",
            style = MaterialTheme.typography.headlineMedium.copy(color = Color(0xFFAAAAAA))
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Retro-futuristic Loading Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(30.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        ) {
            // Segmented progress
            val segments = 20
            val fill = (beats / 1000f).coerceIn(0f, 1f)
            val filledSegments = (fill * segments).toInt()

            Row(
                modifier = Modifier.fillMaxSize().padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                 repeat(segments) { i ->
                     val active = i < filledSegments
                     val boxModifier = if (active) {
                         Modifier.background(
                             Brush.verticalGradient(
                                 listOf(Color(0xFF00F260), Color(0xFF0575E6))
                             )
                         )
                     } else {
                         Modifier.background(Color.White.copy(alpha = 0.05f))
                     }

                     Box(
                         modifier = Modifier
                             .weight(1f)
                             .fillMaxSize()
                             .then(boxModifier)
                     )
                 }
            }
        }
    }
}

@Composable
fun UnixClock(time: Calendar) {
    val timestamp = time.timeInMillis / 1000

    // Matrix / Terminal Style
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EPOCH_TIMESTAMP",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = Color.Green.copy(alpha = 0.7f),
                letterSpacing = 4.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$timestamp",
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = Color.Green,
                fontWeight = FontWeight.Bold,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Green,
                    blurRadius = 20f
                )
            )
        )
    }
}
