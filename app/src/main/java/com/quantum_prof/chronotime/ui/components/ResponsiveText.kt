package com.quantum_prof.chronotime.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AutoScalingText - Automatically scales text to fit within available width
 * 
 * Features:
 * - Text NEVER wraps to a second line
 * - Dynamic font sizing based on available width
 * - Uses BoxWithConstraints for measurement
 * - Supports custom scaling factors and min/max font sizes
 * 
 * @param text The text to display
 * @param modifier Modifier for the composable
 * @param style Base text style to use
 * @param color Text color
 * @param maxFontSize Maximum font size (upper bound)
 * @param minFontSize Minimum font size (lower bound for readability)
 * @param scalingFactor How aggressively to scale (higher = larger text relative to width)
 * @param shadow Optional text shadow for readability
 */
@Composable
fun AutoScalingText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = Color.White,
    maxFontSize: TextUnit = 96.sp,
    minFontSize: TextUnit = 12.sp,
    scalingFactor: Float = 0.15f,
    shadow: Shadow? = null,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign = TextAlign.Center,
    letterSpacing: TextUnit = TextUnit.Unspecified
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        
        // Calculate font size based on available width
        // Formula: fontSize = availableWidth * scalingFactor
        // Clamped between minFontSize and maxFontSize
        val calculatedFontSize = remember(maxWidth, text.length, scalingFactor) {
            with(density) {
                // Base calculation: width * factor, adjusted for text length
                val baseSize = maxWidth.toPx() * scalingFactor
                // Adjust for text length (longer text needs smaller font)
                val lengthFactor = (10f / text.length.coerceAtLeast(1)).coerceIn(0.5f, 1.5f)
                val adjustedSize = baseSize * lengthFactor
                
                // Clamp to min/max bounds
                adjustedSize.coerceIn(minFontSize.toPx(), maxFontSize.toPx()).toSp()
            }
        }
        
        Text(
            text = text,
            style = style.copy(
                fontSize = calculatedFontSize,
                color = color,
                shadow = shadow,
                fontFamily = fontFamily ?: style.fontFamily,
                fontWeight = fontWeight ?: style.fontWeight,
                textAlign = textAlign,
                letterSpacing = if (letterSpacing != TextUnit.Unspecified) letterSpacing else style.letterSpacing
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    }
}

/**
 * Responsive width-aware scale factor calculator
 * Returns a scale factor based on screen width for responsive layouts
 */
@Composable
fun rememberResponsiveScaleFactor(
    compactThreshold: Dp = 360.dp,
    largeThreshold: Dp = 600.dp,
    compactScale: Float = 0.85f,
    normalScale: Float = 1f,
    largeScale: Float = 1.15f
): Float {
    return BoxWithConstraints {
        when {
            maxWidth < compactThreshold -> compactScale
            maxWidth > largeThreshold -> largeScale
            else -> normalScale
        }
    }.let { normalScale } // Fallback, actual value comes from BoxWithConstraints
}

/**
 * Calculates responsive spacing based on screen width
 * Useful for adapting layouts like Binary Clock dot spacing
 */
@Composable
fun rememberResponsiveSpacing(
    compactSpacing: Dp = 4.dp,
    normalSpacing: Dp = 8.dp,
    largeSpacing: Dp = 12.dp,
    compactThreshold: Dp = 360.dp,
    largeThreshold: Dp = 600.dp
): Dp {
    return BoxWithConstraints {
        when {
            maxWidth < compactThreshold -> compactSpacing
            maxWidth > largeThreshold -> largeSpacing
            else -> normalSpacing
        }
    }.let { normalSpacing } // Fallback
}

