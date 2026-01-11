package com.quantum_prof.chronotime.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.annotation.SuppressLint
import java.util.Locale
import kotlin.math.abs

import com.quantum_prof.chronotime.ui.components.*
import com.quantum_prof.chronotime.ui.utils.rememberTilt
import com.quantum_prof.chronotime.ui.theme.DeepBackground
import java.util.Calendar

/**
 * FLUX: Open Chronology - Time in Motion
 * A living, breathing clock app with Deep Glass aesthetic
 * Premium TikTok-style vertical navigation with physics-based animations
 */
@SuppressLint("MissingPermission", "ObsoleteSdkInt")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen() {
    val currentTime = rememberCurrentTime()
    
    // Responsive design - detect screen size
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isCompactScreen = screenWidth < 360.dp || screenHeight < 640.dp
    val isLargeScreen = screenWidth > 600.dp
    
    // Responsive scaling factors
    val scaleFactor = when {
        isCompactScreen -> 0.85f
        isLargeScreen -> 1.15f
        else -> 1f
    }
    
    // Enhanced pager with snap fling behavior for premium feel
    val pagerState = rememberPagerState(pageCount = { 9 }) // 9 clock modes

    // Easter Egg State - 1337 Mode
    var leetMode by remember { mutableStateOf(false) }

    // Parallax from device tilt (reduced on smaller screens)
    val tilt = rememberTilt()
    val parallaxMultiplier = if (isCompactScreen) 15f else 25f
    val parallaxX = (tilt.roll * parallaxMultiplier).dp
    val parallaxY = (tilt.pitch * parallaxMultiplier).dp

    // Current clock mode
    val currentMode = pagerState.currentPage

    // Focus Mode State - hide all UI elements
    var focusMode by remember { mutableStateOf(false) }

    // Settings Overlay State
    var showSettings by remember { mutableStateOf(false) }
    var hapticEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf(0) }

    // First launch hint state
    var showSwipeHint by remember { mutableStateOf(true) }

    // Hide swipe hint after first interaction
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage > 0) {
            showSwipeHint = false
        }
    }

    // Clock configuration (horizontal swipe) with animated transition
    // Each clock mode has its own distinct configuration state
    val clockConfigs = remember { mutableStateOf(IntArray(9) { 0 }) }
    val maxConfigs = 3 // Different configurations per clock
    
    // Helper function to get/set config for a specific clock mode
    fun getClockConfig(mode: Int): Int = clockConfigs.value[mode]
    fun setClockConfig(mode: Int, value: Int) {
        val newConfigs = clockConfigs.value.copyOf()
        newConfigs[mode] = value.coerceIn(0, maxConfigs - 1)
        clockConfigs.value = newConfigs
    }
    
    // Current config for the active clock (for UI indicators)
    val currentClockConfig = clockConfigs.value.getOrElse(currentMode) { 0 }

    // Vibration setup
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // Leet Mode Theme
    val LeetTypography = Typography(
        displayLarge = TextStyle(fontFamily = FontFamily.Monospace, color = Color.Green, fontWeight = FontWeight.Bold, fontSize = (80 * scaleFactor).sp),
        displayMedium = TextStyle(fontFamily = FontFamily.Monospace, color = Color.Green, fontWeight = FontWeight.Medium, fontSize = (57 * scaleFactor).sp),
        titleMedium = TextStyle(fontFamily = FontFamily.Monospace, color = Color.Green, fontWeight = FontWeight.Medium, fontSize = (16 * scaleFactor).sp, letterSpacing = 3.sp),
        labelMedium = TextStyle(fontFamily = FontFamily.Monospace, color = Color.Green, fontWeight = FontWeight.Medium, fontSize = (12 * scaleFactor).sp, letterSpacing = 3.sp)
    )

    val LeetColorScheme = remember {
        darkColorScheme(
            background = Color.Black,
            surface = Color.Black,
            onSurface = Color.Green,
            primary = Color.Green
        )
    }

    // Per-second tick haptic
    val currentSecond = currentTime.get(Calendar.SECOND)

    // Pulsating animation synced to seconds with physics
    val pulseAlpha by animateFloatAsState(
        targetValue = if (currentSecond % 2 == 0) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pulse"
    )

    // Card scale animation for morphing effect with premium spring physics
    val cardScale by animateFloatAsState(
        targetValue = if (pagerState.isScrollInProgress) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    // Tick haptic every second (CLOCK_TICK feel)
    LaunchedEffect(currentSecond, hapticEnabled) {
        if (hapticEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(5)
            }
        }
    }

    // Page change haptic (gear-like ratcheting feel for premium UX)
    LaunchedEffect(pagerState, hapticEnabled) {
        snapshotFlow { pagerState.currentPage }.collect { _ ->
            // Note: Each clock now maintains its own config state, no reset needed
            // Heavy haptic on page settle - feels like clicking through a physical gear
            if (hapticEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
        }
    }
    
    // Scroll progress haptic (light clicks during scroll for gear feel)
    // Uses threshold crossing detection to avoid excessive vibration
    var lastHapticThreshold by remember { mutableStateOf(0f) }
    LaunchedEffect(pagerState, hapticEnabled) {
        snapshotFlow { pagerState.currentPageOffsetFraction }.collect { offset ->
            val absOffset = abs(offset)
            // Define threshold points for gear-like haptic feedback
            val thresholdPoints = listOf(0.15f, 0.35f, 0.55f, 0.75f)
            
            // Find which threshold we've crossed
            val currentThreshold = thresholdPoints.find { threshold ->
                absOffset >= threshold - 0.05f && absOffset <= threshold + 0.05f
            }
            
            // Only trigger haptic when crossing a new threshold
            if (hapticEnabled && currentThreshold != null && currentThreshold != lastHapticThreshold) {
                lastHapticThreshold = currentThreshold
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                }
            }
            
            // Reset threshold when scroll completes
            if (absOffset < 0.05f) {
                lastHapticThreshold = 0f
            }
        }
    }

    // Dynamic colors based on current mode
    val modeColors = remember(currentMode) {
        getModeColors(currentMode)
    }

    val animatedPrimaryColor by animateColorAsState(
        targetValue = modeColors.primary,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "primaryColor"
    )

    MaterialTheme(
        typography = if (leetMode) LeetTypography else MaterialTheme.typography,
        colorScheme = if (leetMode) LeetColorScheme else MaterialTheme.colorScheme
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (leetMode) Color.Black else DeepBackground)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            // Double tap opens settings when in focus mode
                            if (focusMode) {
                                showSettings = true
                                if (hapticEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                }
                            }
                        },
                        onLongPress = {
                            focusMode = !focusMode
                            if (hapticEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                            }
                        },
                        onTap = {
                            // Easter Egg: Tap at 13:37 to enable leet mode
                            val h = currentTime.get(Calendar.HOUR_OF_DAY)
                            val m = currentTime.get(Calendar.MINUTE)
                            if (h == 13 && m == 37) {
                                leetMode = !leetMode
                                if (hapticEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                                }
                            }
                        }
                    )
                }
                // Note: Horizontal drag handling moved to per-page level for independent clock configs
        ) {
            // === LAYER 1: BACKGROUND ===
            // Dynamic mesh gradient background
            if (!leetMode) {
                // Mode-specific background
                when (currentMode) {
                    1 -> { // Hex Mode - Color IS the time
                        val r = currentTime.get(Calendar.HOUR_OF_DAY)
                        val g = currentTime.get(Calendar.MINUTE)
                        val b = currentTime.get(Calendar.SECOND)
                        val hexColor = Color(red = r / 24f, green = g / 60f, blue = b / 60f)
                        Box(
                            Modifier
                                .fillMaxSize()
                                .offset(x = parallaxX, y = parallaxY)
                                .background(hexColor.copy(alpha = 0.7f * pulseAlpha))
                        )
                    }
                    7 -> { // Solar Dial - Sky gradient
                        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
                        val skyColors = when {
                            hour < 6 -> listOf(Color(0xFF0a0a15), Color(0xFF1a1a2e))
                            hour < 8 -> listOf(Color(0xFF4A90D9), Color(0xFFFFB347))
                            hour < 17 -> listOf(Color(0xFF4A90D9), Color(0xFF87CEEB))
                            hour < 19 -> listOf(Color(0xFFFF8C42), Color(0xFF4169E1))
                            else -> listOf(Color(0xFF0a0a15), Color(0xFF1a1a2e))
                        }
                        Box(
                            Modifier
                                .fillMaxSize()
                                .offset(x = parallaxX, y = parallaxY)
                                .background(Brush.verticalGradient(skyColors.map { it.copy(alpha = pulseAlpha) }))
                        )
                    }
                    else -> {
                        // Living mesh gradient
                        MeshGradientBackground(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = parallaxX, y = parallaxY)
                                .alpha(pulseAlpha),
                            time = currentTime,
                            dynamicColors = modeColors.gradientColors
                        )
                    }
                }
            } else {
                // Leet Mode: Pure Black with Matrix-style effect
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
                // Matrix Rain Effect
                MatrixRainEffect(
                    modifier = Modifier.fillMaxSize(),
                    density = 40
                )
            }

            // === LAYER 2: MID-LAYER PARTICLES ===
            if (!leetMode && !focusMode) {
                ParticleField(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = parallaxX / 2, y = parallaxY / 2),
                    time = currentTime,
                    particleCount = 25,
                    baseColor = animatedPrimaryColor
                )
            }

            // === LAYER 3: GLASS REFLECTION (Tilt-responsive) ===
            if (!leetMode) {
                GlassReflection(
                    modifier = Modifier.fillMaxSize(),
                    tiltX = tilt.roll,
                    tiltY = tilt.pitch
                )
            }

            // === LAYER 4: CLOCK INTERFACES (Vertical Pager - TikTok style with snap fling) ===
            // Enhanced pager with premium snap fling behavior - STICKY/ELASTIC physics
            // Animation constants for elastic feel
            val elasticDampingRatio = 0.6f // Lower damping = more bouncy/elastic
            val elasticStiffness = 200f // Lower stiffness = more stretchy feel
            
            val flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = spring(
                    dampingRatio = elasticDampingRatio,
                    stiffness = elasticStiffness
                )
            )
            
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 0.dp,
                beyondViewportPageCount = 1,
                flingBehavior = flingBehavior
            ) { page ->
                // Page transition effects with liquid dissolve feel
                val pageOffset = pagerState.currentPageOffsetFraction
                
                // Get this page's specific config
                val pageConfig = getClockConfig(page)
                
                // Calculate elastic stretch factor - page stretches slightly before snapping
                val stretchFactor = 1f + (abs(pageOffset) * 0.08f) // Subtle stretch
                val elasticScale = if (pageOffset != 0f) {
                    1f - (abs(pageOffset) * 0.12f) // Shrink as it moves away
                } else {
                    1f
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        // Horizontal drag for clock configuration - per page
                        .pointerInput(page, hapticEnabled) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (abs(dragAmount) > 30) {
                                    val newConfig = (getClockConfig(page) + if (dragAmount > 0) 1 else -1)
                                    setClockConfig(page, newConfig)
                                    if (hapticEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                                    }
                                }
                            }
                        }
                        .graphicsLayer {
                            // ENHANCED: Morphing scale with elastic stretch effect
                            val scale = elasticScale * cardScale
                            scaleX = scale
                            // Y scale gets slightly stretched during swipe for "sticky" feel
                            scaleY = scale * (1f + abs(pageOffset) * 0.03f)

                            // Fade effect with easing
                            alpha = 1f - abs(pageOffset) * 0.6f

                            // 3D rotation for depth perception
                            rotationX = pageOffset * -8f
                            
                            // Slight Y translation for parallax depth
                            translationY = pageOffset * 50f
                            
                            // Camera distance for 3D effect
                            cameraDistance = 12f * density
                        }
                ) {
                    // Select appropriate glass card style per clock
                    val glassContent: @Composable () -> Unit = {
                        ClockContent(
                            page = page,
                            time = currentTime,
                            config = pageConfig,
                            tiltX = tilt.roll,
                            tiltY = tilt.pitch,
                            leetMode = leetMode,
                            scaleFactor = scaleFactor
                        )
                    }

                    // Different card styles for different clocks
                    when (page) {
                        5 -> { // Unix - Terminal style
                            NeonGlassCard(
                                modifier = Modifier.offset(x = -parallaxX / 2, y = -parallaxY / 2),
                                neonColor = Color.Green
                            ) { glassContent() }
                        }
                        4 -> { // Swatch - Retro style
                            NeonGlassCard(
                                modifier = Modifier.offset(x = -parallaxX / 2, y = -parallaxY / 2),
                                neonColor = Color(0xFF00F260)
                            ) { glassContent() }
                        }
                        else -> {
                            // Use LiquidGlassCard for premium liquid glass effect
                            // with blur that extends beyond card bounds (no clipping)
                            LiquidGlassCard(
                                modifier = Modifier.offset(x = -parallaxX / 2, y = -parallaxY / 2),
                                glowColor = animatedPrimaryColor,
                                blurRadius = 24f,
                                enableLens = true
                            ) { glassContent() }
                        }
                    }
                }
            }
            
            // === LAYER 4.5: FOREGROUND PARTICLES (pass in front of glass - sharp, fast) ===
            if (!leetMode && !focusMode) {
                ForegroundParticleField(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = -parallaxX / 3, y = -parallaxY / 3), // Inverse parallax for depth
                    time = currentTime,
                    particleCount = 10,
                    baseColor = animatedPrimaryColor
                )
            }

            // === LAYER 5: UI OVERLAYS (Hidden in Focus Mode) ===

            // Title at top
            AnimatedVisibility(
                visible = !focusMode,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (leetMode) "ROOT@CHRONOTIME:~#" else "CHRONOTIME",
                        style = MaterialTheme.typography.titleMedium.copy(
                            letterSpacing = 6.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = if (leetMode) Color.Green else Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "TIME IN MOTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 4.sp
                        ),
                        color = if (leetMode) Color.Green.copy(alpha = 0.5f) else animatedPrimaryColor.copy(alpha = 0.6f)
                    )
                }
            }

            // Mode indicator at bottom
            AnimatedVisibility(
                visible = !focusMode,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Page indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        repeat(9) { index ->
                            val isSelected = index == currentMode
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 10.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) animatedPrimaryColor
                                        else Color.White.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    // Mode name
                    Text(
                        text = getModeName(currentMode),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (leetMode) Color.Green else Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Mode description
                    Text(
                        text = getModeTagline(currentMode),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (leetMode) Color.Green.copy(alpha = 0.5f) else animatedPrimaryColor.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )

                    // Config indicator
                    if (maxConfigs > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            repeat(maxConfigs) { index ->
                                Box(
                                    modifier = Modifier
                                        .width(if (index == currentClockConfig) 20.dp else 8.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (index == currentClockConfig) animatedPrimaryColor.copy(alpha = 0.8f)
                                            else Color.White.copy(alpha = 0.2f)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Swipe hints (corners)
            AnimatedVisibility(
                visible = !focusMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = "◀",
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 20.sp
                )
            }

            AnimatedVisibility(
                visible = !focusMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = "▶",
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 20.sp
                )
            }

            // === LAYER 6: FOCUS MODE INDICATOR ===
            FocusModeIndicator(
                visible = focusMode,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 24.dp)
            )

            // === LAYER 7: SWIPE HINT FOR FIRST-TIME USERS ===
            AnimatedVisibility(
                visible = showSwipeHint && !focusMode,
                enter = fadeIn(tween(1000, delayMillis = 2000)),
                exit = fadeOut()
            ) {
                SwipeHintOverlay(
                    showVertical = true,
                    showHorizontal = false
                )
            }

            // === LAYER 8: SETTINGS OVERLAY ===
            SettingsOverlay(
                visible = showSettings,
                onDismiss = { showSettings = false },
                hapticEnabled = hapticEnabled,
                onHapticToggle = { hapticEnabled = it },
                soundEnabled = soundEnabled,
                onSoundToggle = { soundEnabled = it },
                selectedTheme = selectedTheme,
                onThemeSelect = { selectedTheme = it }
            )
        }
    }
}

/**
 * Centralized clock content renderer with responsive scaling
 */
@Composable
private fun ClockContent(
    page: Int,
    time: Calendar,
    config: Int,
    tiltX: Float,
    tiltY: Float,
    leetMode: Boolean,
    scaleFactor: Float = 1f
) {
    // Wrap content in a responsive container
    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scaleFactor
            scaleY = scaleFactor
        },
        contentAlignment = Alignment.Center
    ) {
        when (page) {
            0 -> ModernTime(time, config, scaleFactor)
            1 -> HexClock(time)
            2 -> BerlinClock(time)
            3 -> BinaryClock(time)
            4 -> SwatchBeatClock(time)
            5 -> UnixClock(time)
            6 -> SynesthesiaClock(time)
            7 -> SolarDial(time)
            8 -> FluidHourglass(time, tiltX, tiltY)
        }
    }
}

/**
 * Enhanced Modern Time display with variable configurations
 * Supports variable font weight that gets "heavier" as seconds progress
 * ENHANCED: Drop shadows for text readability and Bold font weights
 */
@Composable
fun ModernTime(time: Calendar, config: Int = 0, scaleFactor: Float = 1f) {
    val hour = time.get(Calendar.HOUR_OF_DAY)
    val minute = time.get(Calendar.MINUTE)
    val second = time.get(Calendar.SECOND)
    val millisecond = time.get(Calendar.MILLISECOND)

    // Variable font weight based on time progression
    // Seconds get "heavier" as they progress (simulating variable font weight)
    val secondWeight by animateFloatAsState(
        targetValue = 400f + (second / 60f) * 500f, // Range from Normal (400) to Black (900)
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "secondWeight"
    )
    
    // Minutes affect hour display weight
    val minuteWeight by animateFloatAsState(
        targetValue = 500f + (minute / 60f) * 400f, // Range from Medium (500) to Black (900)
        animationSpec = tween(1000),
        label = "minuteWeight"
    )
    
    // Drop shadow for text readability
    val textShadow = androidx.compose.ui.graphics.Shadow(
        color = Color.Black.copy(alpha = 0.35f),
        blurRadius = 14f,
        offset = Offset(2f, 4f)
    )
    
    // Secondary lighter shadow for depth
    val textShadowLight = androidx.compose.ui.graphics.Shadow(
        color = Color.Black.copy(alpha = 0.2f),
        blurRadius = 20f,
        offset = Offset(0f, 6f)
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (config) {
            0 -> {
                // Default: Large time with seconds below - ENHANCED with shadows
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (80 * scaleFactor).sp,
                        letterSpacing = (-2).sp,
                        shadow = textShadow
                    )
                )
                // Seconds with variable weight and shadow
                Text(
                    text = String.format(Locale.getDefault(), "%02d", second),
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = (57 * scaleFactor).sp,
                        shadow = textShadowLight
                    )
                )
            }
            1 -> {
                // Config 1: Full time inline - ENHANCED with shadows
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (56 * scaleFactor).sp,
                        shadow = textShadow
                    )
                )
                // Milliseconds progress bar with glow
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .width((200 * scaleFactor).dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(millisecond / 1000f)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00F0FF),
                                        Color(0xFFFF006E)
                                    )
                                )
                            )
                    )
                }
            }
            2 -> {
                // Config 2: 12-hour format with AM/PM - ENHANCED
                val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                val amPm = if (hour < 12) "AM" else "PM"

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%d:%02d", displayHour, minute),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (72 * scaleFactor).sp,
                            shadow = textShadow
                        )
                    )
                    Column(
                        modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d", second),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                shadow = textShadowLight
                            )
                        )
                        Text(
                            text = amPm,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F0FF),
                                shadow = textShadowLight
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mode-specific color palettes - ENHANCED for vibrant backgrounds
 */
data class ModeColors(
    val primary: Color,
    val secondary: Color,
    val gradientColors: List<Color>
)

private fun getModeColors(mode: Int): ModeColors {
    return when (mode) {
        0 -> ModeColors( // Modern - Enhanced vibrancy
            Color(0xFF00F0FF),
            Color(0xFFFF006E),
            listOf(Color(0xFF0F2A35), Color(0xFF1E4A5A), Color(0xFF2C6E7E))
        )
        1 -> ModeColors( // Hex
            Color(0xFFFFFFFF),
            Color(0xFFCCCCCC),
            listOf(Color(0xFF152535), Color(0xFF253545), Color(0xFF355565))
        )
        2 -> ModeColors( // Berlin - Warmer, more vibrant
            Color(0xFFFF6B6B),
            Color(0xFFFFE66D),
            listOf(Color(0xFF2a1a3e), Color(0xFF26315e), Color(0xFF1f4580))
        )
        3 -> ModeColors( // Binary - Deeper contrast
            Color(0xFF00E5FF),
            Color(0xFF00FF88),
            listOf(Color(0xFF0a0a12), Color(0xFF1a2a3e), Color(0xFF0f1f2a))
        )
        4 -> ModeColors( // Swatch - More energetic
            Color(0xFF00F260),
            Color(0xFF0575E6),
            listOf(Color(0xFF0F2A35), Color(0xFF254555), Color(0xFF3C6575))
        )
        5 -> ModeColors( // Unix - Terminal green glow
            Color(0xFF00FF00),
            Color(0xFF00AA00),
            listOf(Color(0xFF000508), Color(0xFF0a1510), Color(0xFF001810))
        )
        6 -> ModeColors( // Synesthesia - Rich purples
            Color(0xFFFF6B6B),
            Color(0xFF9B59B6),
            listOf(Color(0xFF2a1a3e), Color(0xFF3d2d5a), Color(0xFF2a1a3e))
        )
        7 -> ModeColors( // Solar - Sky blues enhanced
            Color(0xFFFFB347),
            Color(0xFF4A90D9),
            listOf(Color(0xFF1a4a6c), Color(0xFF5A9AE9), Color(0xFF97DEFF))
        )
        8 -> ModeColors( // Fluid Hourglass - Ocean depth
            Color(0xFF00D2FF),
            Color(0xFF3A7BD5),
            listOf(Color(0xFF0F2A35), Color(0xFF254A5A), Color(0xFF3C6A7A))
        )
        else -> ModeColors(
            Color(0xFF00F0FF),
            Color(0xFFFF006E),
            listOf(Color(0xFF0F2A35), Color(0xFF1E4A5A), Color(0xFF2C6E7E))
        )
    }
}

/**
 * Mode names
 */
fun getModeName(index: Int): String {
    return when (index) {
        0 -> "STANDARD"
        1 -> "HEXADECIMAL"
        2 -> "BERLIN UHR"
        3 -> "BINARY"
        4 -> "SWATCH .BEATS"
        5 -> "UNIX EPOCH"
        6 -> "SYNÄSTHESIE"
        7 -> "SOLAR DIAL"
        8 -> "FLUID HOURGLASS"
        else -> ""
    }
}

/**
 * Mode taglines
 */
fun getModeTagline(index: Int): String {
    return when (index) {
        0 -> "Zeit in Bewegung"
        1 -> "Die Farbe der Zeit"
        2 -> "Mengenlehre seit 1975"
        3 -> "True Nerd Mode"
        4 -> "Keine Zeitzonen. @beats."
        5 -> "Sekunden seit 1970"
        6 -> "Jede Ziffer hat ihre Farbe"
        7 -> "Folge der Sonne"
        8 -> "Der Tag fließt dahin"
        else -> ""
    }
}
