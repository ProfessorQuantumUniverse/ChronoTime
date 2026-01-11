package com.quantum_prof.chronotime.ui.components

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

// --- Components ---

@Composable
fun HexClock(time: Calendar) {
    val r = time.get(Calendar.HOUR_OF_DAY)
    val g = time.get(Calendar.MINUTE)
    val b = time.get(Calendar.SECOND)

    val hexString = String.format("#%02X%02X%02X", r, g, b)
    // Minimalist: Text IS the color. Background handled by parent.
    // val color = Color(red = r / 24f, green = g / 60f, blue = b / 60f) // Removed unused variable

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = hexString,
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.Monospace,
                color = Color.White, // Text should be readable against the colored background
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    blurRadius = 10f
                )
            )
        )
    }
}

@Composable
fun BerlinClock(time: Calendar) {
    val hours = time.get(Calendar.HOUR_OF_DAY)
    val minutes = time.get(Calendar.MINUTE)
    val seconds = time.get(Calendar.SECOND)

    // Haptics for Berlin Clock (Every 5 minutes)
    val context = LocalContext.current
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Seconds Lamp
        BerlinLamp(active = seconds % 2 == 0, color = Color.Yellow, shape = CircleShape, size = 60.dp)

        // 5 Hours Row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { i ->
                BerlinLamp(active = hours / 5 > i, color = Color.Red, modifier = Modifier.weight(1f).height(50.dp))
            }
        }

        // 1 Hour Row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { i ->
                BerlinLamp(active = hours % 5 > i, color = Color.Red, modifier = Modifier.weight(1f).height(50.dp))
            }
        }

        // 5 Minutes Row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(11) { i ->
                val isRed = (i + 1) % 3 == 0
                val c = if (isRed) Color.Red else Color.Yellow
                BerlinLamp(active = minutes / 5 > i, color = c, modifier = Modifier.weight(1f).height(50.dp))
            }
        }

        // 1 Minute Row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { i ->
                BerlinLamp(active = minutes % 5 > i, color = Color.Yellow, modifier = Modifier.weight(1f).height(50.dp))
            }
        }
    }
}

@Composable
fun BerlinLamp(
    active: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    size: androidx.compose.ui.unit.Dp? = null
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (active) 1f else 0.1f, // Dimmer when off for contrast
        label = "alpha"
    )
    val glowRadius by animateFloatAsState(
        targetValue = if (active) 10.dp.value else 0f,
        label = "glow"
    )

    val mod = if (size != null) modifier.size(size) else modifier

    Box(
        modifier = mod
            .shadow(
                elevation = if(active) 10.dp else 0.dp,
                shape = shape,
                spotColor = color
            )
            .background(color.copy(alpha = animatedAlpha), shape)
            .border(2.dp, Color.White.copy(alpha = 0.3f), shape)
    ) {
        // "Glass" reflection
         Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha=0.4f), Color.Transparent),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(100f, 100f)
                    ),
                    shape
                )
        )
    }
}

@Composable
fun BinaryClock(time: Calendar) {
    val h = time.get(Calendar.HOUR_OF_DAY)
    val m = time.get(Calendar.MINUTE)
    val s = time.get(Calendar.SECOND)

    var showCheat by remember { mutableStateOf<Int?>(null) } // Index of column clicked

    // Columns: H1 (2 bits), H2 (4 bits), M1 (3 bits), M2 (4 bits), S1 (3 bits), S2 (4 bits)
    val digits = listOf(h / 10, h % 10, m / 10, m % 10, s / 10, s % 10)
    val bitCounts = listOf(2, 4, 3, 4, 3, 4)

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        digits.forEachIndexed { index, digit ->
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                   showCheat = if (showCheat == index) null else index
                }
            ) {
                // Cheating Mode (Dezimalzahl)
                AnimatedVisibility(visible = showCheat == index) {
                     Text(
                        text = digit.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }

                for (bit in (bitCounts[index] - 1) downTo 0) {
                    val isActive = ((digit shr bit) and 1) == 1
                    BinaryDot(isActive = isActive)
                }
            }
            if (index == 1 || index == 3) {
                 Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Composable
fun BinaryDot(isActive: Boolean) {
    val color by animateColorAsState(
        targetValue = if (isActive) Color(0xFF00E5FF) else Color(0xFF00E5FF).copy(alpha = 0.1f),
        label = "color"
    )
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 0.8f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(36.dp * scale)
            .shadow(if (isActive) 15.dp else 0.dp, CircleShape, spotColor = color)
            .background(
                Brush.radialGradient(
                    colors = listOf(color, color.copy(alpha = 0.5f), Color.Transparent)
                ),
                CircleShape
            )
    )
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
