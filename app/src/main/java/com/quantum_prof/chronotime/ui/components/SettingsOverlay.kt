package com.quantum_prof.chronotime.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

/**
 * Settings Overlay - Glass Bottom Sheet with app settings
 */
@Composable
fun SettingsOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    hapticEnabled: Boolean,
    onHapticToggle: (Boolean) -> Unit,
    soundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    selectedTheme: Int,
    onThemeSelect: (Int) -> Unit,
    backdrop: Backdrop
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            val bottomSheetBackdrop = rememberLayerBackdrop()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp) },
                        effects = {
                            vibrancy()
                            blur(4.dp.toPx())
                            lens(24.dp.toPx(), 48.dp.toPx(), true)
                        },
                        exportedBackdrop = bottomSheetBackdrop,
                        onDrawSurface = { drawRect(Color.White.copy(alpha = 0.65f)) }
                    )
                    .padding(24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars), // Handle safe area
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Handle indicator
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "EINSTELLUNGEN",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 6.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = Color.Black.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Haptic Toggle
                SettingsToggleRow(
                    title = "HAPTIK",
                    subtitle = "Vibrationen bei Interaktion",
                    checked = hapticEnabled,
                    onCheckedChange = onHapticToggle,
                    importedBackdrop = bottomSheetBackdrop
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sound Toggle
                SettingsToggleRow(
                    title = "AMBIENT SOUND",
                    subtitle = "Generative Klanglandschaft",
                    checked = soundEnabled,
                    onCheckedChange = onSoundToggle,
                    importedBackdrop = bottomSheetBackdrop
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Theme Selection
                Text(
                    text = "FARBSCHEMA",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemeOption(
                        colors = listOf(Color(0xFF00F0FF), Color(0xFFFF006E)),
                        selected = selectedTheme == 0,
                        onClick = { onThemeSelect(0) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOption(
                        colors = listOf(Color(0xFF7B2FFF), Color(0xFF00FF88)),
                        selected = selectedTheme == 1,
                        onClick = { onThemeSelect(1) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOption(
                        colors = listOf(Color(0xFFFFB347), Color(0xFFFF6B6B)),
                        selected = selectedTheme == 2,
                        onClick = { onThemeSelect(2) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOption(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFF888888)),
                        selected = selectedTheme == 3,
                        onClick = { onThemeSelect(3) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Version info
                Text(
                    text = "ChronoTime v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.4f)
                )
                Text(
                    text = "Time in Motion",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = Color.Black.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    importedBackdrop: Backdrop
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            // Nested glass effect for rows using the sheet's backdrop
            .drawBackdrop(
                backdrop = importedBackdrop,
                shape = { RoundedCornerShape(16.dp) },
                shadow = null,
                effects = {
                    vibrancy()
                    blur(2.dp.toPx()) // Slightly less blur for inner elements
                },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.4f)) }
            )
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black.copy(alpha = 0.8f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.5f)
            )
        }

        // Custom Toggle
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (checked) Brush.horizontalGradient(
                        listOf(Color(0xFF00F0FF), Color(0xFF00FF88))
                    ) else Brush.horizontalGradient(
                        listOf(Color(0xFF555555), Color(0xFF777777))
                    )
                )
                .padding(3.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun ThemeOption(
    colors: List<Color>,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(colors))
            .then(
                if (selected) Modifier.border(
                    2.dp,
                    Color.Black.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

/**
 * Focus Mode Indicator - Minimal dot that shows app is in focus mode
 */
@Composable
fun FocusModeIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "focusPulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF00F0FF).copy(alpha = alpha))
        )
    }
}

/**
 * Swipe Hint Animation - Shows users they can swipe
 */
@Composable
fun SwipeHintOverlay(
    showVertical: Boolean = true,
    showHorizontal: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "swipeHint")

    val verticalOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "verticalOffset"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Vertical swipe hint (top)
        if (showVertical) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
                    .offset(y = verticalOffset.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "▲",
                    color = Color.White.copy(alpha = alpha),
                    fontSize = 24.sp
                )
                Text(
                    text = "WISCHEN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = alpha * 0.7f)
                )
            }
        }

        // Vertical swipe hint (bottom)
        if (showVertical) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .offset(y = -verticalOffset.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "WISCHEN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = alpha * 0.7f)
                )
                Text(
                    text = "▼",
                    color = Color.White.copy(alpha = alpha),
                    fontSize = 24.sp
                )
            }
        }
    }
}
