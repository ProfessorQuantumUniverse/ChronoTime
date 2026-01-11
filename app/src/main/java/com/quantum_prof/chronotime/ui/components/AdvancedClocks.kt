package com.quantum_prof.chronotime.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale
import kotlin.math.*

/**
 * Synästhesie-Uhr
 * Jede Ziffer hat eine feste Farbe - ein buntes Mosaik das sich jede Sekunde neu zusammensetzt
 */

// Synesthetic color mappings for digits 0-9
private val digitColors = listOf(
    Color(0xFFFFFFFF), // 0 - White (void, empty)
    Color(0xFF00BFFF), // 1 - Deep Sky Blue (single, start)
    Color(0xFFFFD700), // 2 - Gold (duality, warmth)
    Color(0xFFFF6B6B), // 3 - Coral Red (triangle, fire)
    Color(0xFF4ECDC4), // 4 - Turquoise (square, stability)
    Color(0xFF9B59B6), // 5 - Purple (center, mystical)
    Color(0xFFFF8C00), // 6 - Dark Orange (hexagon, energy)
    Color(0xFF2ECC71), // 7 - Emerald (luck, nature)
    Color(0xFF3498DB), // 8 - Bright Blue (infinity, flow)
    Color(0xFFE74C3C), // 9 - Red (completion, intensity)
)

@Composable
fun SynesthesiaClock(time: Calendar) {
    val hour = time.get(Calendar.HOUR_OF_DAY)
    val minute = time.get(Calendar.MINUTE)
    val second = time.get(Calendar.SECOND)

    // Extract all digits
    val digits = listOf(
        hour / 10, hour % 10,
        minute / 10, minute % 10,
        second / 10, second % 10
    )

    // Animate colors for smooth transitions
    val animatedColors = digits.mapIndexed { index, digit ->
        animateColorAsState(
            targetValue = digitColors[digit],
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            label = "color$index"
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mosaic visualization
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .padding(16.dp)
        ) {
            val cellSize = size.width / 6
            val centerY = size.height / 2

            // Draw colored mosaic blocks
            digits.forEachIndexed { index, digit ->
                val x = index * cellSize
                val color = animatedColors[index].value

                // Main block with glow
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.9f),
                            color.copy(alpha = 0.6f)
                        )
                    ),
                    topLeft = Offset(x + 4, centerY - cellSize / 2 + 4),
                    size = Size(cellSize - 8, cellSize - 8),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
                )

                // Glow effect
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(x + cellSize / 2, centerY),
                        radius = cellSize
                    ),
                    topLeft = Offset(x - cellSize / 4, centerY - cellSize),
                    size = Size(cellSize * 1.5f, cellSize * 2),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f)
                )
            }

            // Draw separator dots
            listOf(2, 4).forEach { sepIndex ->
                val x = sepIndex * cellSize
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 4f,
                    center = Offset(x, centerY - 15)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 4f,
                    center = Offset(x, centerY + 15)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time text with colored digits
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            digits.forEachIndexed { index, digit ->
                Text(
                    text = digit.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedColors[index].value,
                        shadow = Shadow(
                            color = animatedColors[index].value.copy(alpha = 0.5f),
                            blurRadius = 15f
                        )
                    )
                )
                if (index == 1 || index == 3) {
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SYNÄSTHESIE",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 4.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        )
    }
}

/**
 * Fluid Hourglass
 * Der Bildschirm füllt sich langsam mit digitaler Flüssigkeit entsprechend der Tageszeit
 */
@Composable
fun FluidHourglass(
    time: Calendar,
    tiltX: Float = 0f,
    tiltY: Float = 0f
) {
    val hour = time.get(Calendar.HOUR_OF_DAY)
    val minute = time.get(Calendar.MINUTE)
    val second = time.get(Calendar.SECOND)

    // Calculate progress through the day (0.0 to 1.0)
    val totalSeconds = hour * 3600 + minute * 60 + second
    val dayProgress = totalSeconds / 86400f

    // Fluid wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "fluid")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    // Secondary wave
    val waveOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )

    // Gyroscope-based wave distortion
    val tiltInfluence = (tiltX * 30).coerceIn(-50f, 50f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(260.dp, 320.dp)
                .padding(8.dp)
        ) {
            val fluidHeight = size.height * dayProgress
            val fluidTop = size.height - fluidHeight

            // Hourglass container outline
            val containerPath = Path().apply {
                moveTo(size.width * 0.1f, 0f)
                lineTo(size.width * 0.9f, 0f)
                lineTo(size.width * 0.55f, size.height * 0.45f)
                lineTo(size.width * 0.55f, size.height * 0.55f)
                lineTo(size.width * 0.9f, size.height)
                lineTo(size.width * 0.1f, size.height)
                lineTo(size.width * 0.45f, size.height * 0.55f)
                lineTo(size.width * 0.45f, size.height * 0.45f)
                close()
            }

            // Clip to hourglass shape
            clipPath(containerPath) {
                // Fluid gradient
                val fluidBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00D2FF).copy(alpha = 0.3f),
                        Color(0xFF3A7BD5).copy(alpha = 0.6f),
                        Color(0xFF00D2FF).copy(alpha = 0.8f),
                        Color(0xFF3A7BD5)
                    ),
                    startY = fluidTop,
                    endY = size.height
                )

                // Draw fluid with wave effect
                val fluidPath = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(0f, fluidTop)

                    // Create wave pattern
                    var x = 0f
                    while (x <= size.width) {
                        val y = fluidTop +
                            sin(waveOffset + x * 0.03f) * 8 +
                            sin(waveOffset2 + x * 0.05f) * 5 +
                            tiltInfluence * sin(x * 0.02f)
                        lineTo(x, y)
                        x += 5f
                    }

                    lineTo(size.width, size.height)
                    close()
                }

                drawPath(fluidPath, fluidBrush)

                // Bubble effects
                val bubbleCount = 15
                repeat(bubbleCount) { i ->
                    val bubbleX = (size.width * (i.toFloat() / bubbleCount)) + sin(waveOffset + i) * 20
                    val bubbleY = fluidTop + (size.height - fluidTop) * ((i * 7 + second) % 100) / 100f
                    val bubbleSize = 4f + (i % 5) * 2

                    if (bubbleY > fluidTop) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            radius = bubbleSize,
                            center = Offset(bubbleX, bubbleY)
                        )
                    }
                }
            }

            // Glass container with reflection
            drawPath(
                containerPath,
                color = Color.White.copy(alpha = 0.3f),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )

            // Glass reflection highlight
            val reflectionPath = Path().apply {
                moveTo(size.width * 0.15f, size.height * 0.05f)
                lineTo(size.width * 0.25f, size.height * 0.05f)
                lineTo(size.width * 0.5f, size.height * 0.4f)
                lineTo(size.width * 0.48f, size.height * 0.4f)
                close()
            }
            drawPath(reflectionPath, Color.White.copy(alpha = 0.15f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress text
        Text(
            text = "${(dayProgress * 100).toInt()}%",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Light,
                color = Color(0xFF00D2FF)
            )
        )

        Text(
            text = "DES TAGES VERFLOSSEN",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 3.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Time remaining
        val remainingHours = 23 - hour
        val remainingMinutes = 59 - minute
        Text(
            text = String.format(Locale.getDefault(), "%02d:%02d verbleibend", remainingHours, remainingMinutes),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.4f),
                fontFamily = FontFamily.Monospace
            )
        )
    }
}

/**
 * Solar Dial
 * Visualisiert den Sonnenstand und zeigt Golden Hour, Blue Hour
 */
@Composable
fun SolarDial(time: Calendar) {
    val hour = time.get(Calendar.HOUR_OF_DAY)
    val minute = time.get(Calendar.MINUTE)

    // Simplified sun position calculation (actual would need location)
    val timeDecimal = hour + minute / 60f
    val sunAngle = ((timeDecimal - 6) / 12f * 180).coerceIn(0f, 180f)

    // Determine current phase
    val phase = when {
        hour < 5 -> SolarPhase.NIGHT
        hour < 6 -> SolarPhase.BLUE_HOUR_DAWN
        hour < 7 -> SolarPhase.GOLDEN_HOUR_DAWN
        hour < 17 -> SolarPhase.DAY
        hour < 18 -> SolarPhase.GOLDEN_HOUR_DUSK
        hour < 19 -> SolarPhase.BLUE_HOUR_DUSK
        else -> SolarPhase.NIGHT
    }

    val phaseColor by animateColorAsState(
        targetValue = phase.color,
        animationSpec = tween(1000),
        label = "phaseColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .padding(16.dp)
        ) {
            val centerX = size.width / 2
            val centerY = size.height * 0.7f
            val arcRadius = size.width * 0.45f

            // Sky gradient background
            val skyGradient = Brush.verticalGradient(
                colors = listOf(
                    phase.skyTop,
                    phase.skyBottom
                )
            )
            drawRoundRect(
                brush = skyGradient,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f)
            )

            // Horizon line
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 2f
            )

            // Sun arc path (dashed)
            val arcPath = Path().apply {
                addArc(
                    oval = androidx.compose.ui.geometry.Rect(
                        left = centerX - arcRadius,
                        top = centerY - arcRadius,
                        right = centerX + arcRadius,
                        bottom = centerY + arcRadius
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f
                )
            }
            drawPath(
                arcPath,
                color = Color.White.copy(alpha = 0.15f),
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            )

            // Sun position
            val sunRad = Math.toRadians(180.0 - sunAngle.toDouble())
            val sunX = centerX + (arcRadius * cos(sunRad)).toFloat()
            val sunY = centerY - (arcRadius * sin(sunRad)).toFloat()

            // Sun glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        phase.sunColor.copy(alpha = 0.6f),
                        phase.sunColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(sunX, sunY),
                    radius = 80f
                ),
                radius = 80f,
                center = Offset(sunX, sunY)
            )

            // Sun
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        phase.sunColor
                    ),
                    center = Offset(sunX, sunY)
                ),
                radius = 25f,
                center = Offset(sunX, sunY)
            )

            // Time markers on arc
            listOf(6 to "6", 9 to "9", 12 to "12", 15 to "15", 18 to "18").forEach { (h, label) ->
                val markerAngle = Math.toRadians(180.0 - ((h - 6) / 12.0 * 180))
                val markerX = centerX + ((arcRadius + 25) * cos(markerAngle)).toFloat()
                val markerY = centerY - ((arcRadius + 25) * sin(markerAngle)).toFloat()

                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 3f,
                    center = Offset(
                        centerX + (arcRadius * cos(markerAngle)).toFloat(),
                        centerY - (arcRadius * sin(markerAngle)).toFloat()
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phase name
        Text(
            text = phase.displayName,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Light,
                color = phaseColor
            )
        )

        Text(
            text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = phase.description,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.5f)
            )
        )
    }
}

enum class SolarPhase(
    val displayName: String,
    val description: String,
    val color: Color,
    val sunColor: Color,
    val skyTop: Color,
    val skyBottom: Color
) {
    NIGHT(
        "Nacht",
        "Die Sterne regieren",
        Color(0xFF1a1a2e),
        Color(0xFFCCCCCC),
        Color(0xFF0a0a15),
        Color(0xFF1a1a2e)
    ),
    BLUE_HOUR_DAWN(
        "Blaue Stunde",
        "Magisches Licht vor Sonnenaufgang",
        Color(0xFF4A90D9),
        Color(0xFFFFE4B5),
        Color(0xFF1a3a5c),
        Color(0xFF4A90D9)
    ),
    GOLDEN_HOUR_DAWN(
        "Goldene Stunde",
        "Perfektes Licht zum Fotografieren",
        Color(0xFFFFB347),
        Color(0xFFFFD700),
        Color(0xFF4A90D9),
        Color(0xFFFFB347)
    ),
    DAY(
        "Tag",
        "Die Sonne steht hoch",
        Color(0xFF87CEEB),
        Color(0xFFFFFF00),
        Color(0xFF4A90D9),
        Color(0xFF87CEEB)
    ),
    GOLDEN_HOUR_DUSK(
        "Goldene Stunde",
        "Das magische Abendlicht",
        Color(0xFFFF8C42),
        Color(0xFFFF6B35),
        Color(0xFFFF8C42),
        Color(0xFFFFB347)
    ),
    BLUE_HOUR_DUSK(
        "Blaue Stunde",
        "Der Übergang zur Nacht",
        Color(0xFF4169E1),
        Color(0xFFFFB6C1),
        Color(0xFF4169E1),
        Color(0xFF1a1a4e)
    )
}

