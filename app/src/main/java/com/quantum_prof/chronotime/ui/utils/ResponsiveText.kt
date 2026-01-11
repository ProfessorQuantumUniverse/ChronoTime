package com.quantum_prof.chronotime.ui.utils

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
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
 * AutoScalingText - Text that dynamically scales to fit available width
 * 
 * Key Features:
 * - Text NEVER wraps to a second line
 * - Automatically scales down font size to fit
 * - Uses BoxWithConstraints to measure available width
 * - Calculates font size dynamically: fontSize = availableWidth * scalingFactor
 * 
 * @param text The text to display
 * @param modifier Modifier for the text composable
 * @param style Base text style (will be scaled)
 * @param maxFontSize Maximum font size (won't scale larger than this)
 * @param minFontSize Minimum font size (won't scale smaller than this)
 * @param scalingFactor Factor to calculate font size from width
 * @param color Text color
 * @param fontWeight Font weight
 * @param fontFamily Font family
 * @param textAlign Text alignment
 */
@Composable
fun AutoScalingText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    maxFontSize: TextUnit = 120.sp,
    minFontSize: TextUnit = 12.sp,
    scalingFactor: Float = 0.15f,
    color: Color = style.color,
    fontWeight: FontWeight? = style.fontWeight,
    fontFamily: FontFamily? = style.fontFamily,
    textAlign: TextAlign? = null
) {
    var shouldDraw by remember { mutableStateOf(false) }
    var scaledFontSize by remember(text, maxFontSize) { mutableStateOf(maxFontSize) }
    
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val maxWidthPx = constraints.maxWidth.toFloat()
        
        // Calculate initial font size based on available width
        val calculatedFontSize = with(density) {
            val pixelSize = (maxWidthPx * scalingFactor / text.length.coerceAtLeast(1)).coerceIn(
                minFontSize.toPx(),
                maxFontSize.toPx()
            )
            pixelSize.sp
        }
        
        // Initialize scaledFontSize with calculated value to avoid visual flicker
        val initialFontSize = if (calculatedFontSize.value < maxFontSize.value) calculatedFontSize else maxFontSize
        
        // Reset shouldDraw and font size when text changes
        if (!shouldDraw && scaledFontSize == maxFontSize && initialFontSize.value < maxFontSize.value) {
            scaledFontSize = initialFontSize
        }
        
        Text(
            text = text,
            color = color,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            textAlign = textAlign,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            style = style.copy(fontSize = scaledFontSize),
            modifier = Modifier.drawWithContent {
                if (shouldDraw) {
                    drawContent()
                }
            },
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.didOverflowWidth) {
                    // Text overflowed, reduce font size
                    val newSize = (scaledFontSize.value * 0.9f).sp
                    if (newSize >= minFontSize) {
                        scaledFontSize = newSize
                    } else {
                        scaledFontSize = minFontSize
                        shouldDraw = true
                    }
                } else {
                    shouldDraw = true
                }
            }
        )
    }
}

/**
 * ResponsiveScaleBox - Scales content down to fit available width
 * Useful for entire layouts that need to shrink on small screens
 * 
 * @param targetWidth The ideal width for the content
 * @param minScale Minimum scale factor (won't scale smaller than this)
 * @param modifier Modifier for the container
 * @param content The content to scale
 */
@Composable
fun ResponsiveScaleBox(
    targetWidth: Dp = 400.dp,
    minScale: Float = 0.5f,
    modifier: Modifier = Modifier,
    content: @Composable (scaleFactor: Float) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val availableWidth = maxWidth
        val scaleFactor = (availableWidth / targetWidth).coerceIn(minScale, 1f)
        content(scaleFactor)
    }
}

/**
 * Calculate responsive spacing based on screen width
 * 
 * @param baseSpacing The spacing value for standard screens (360dp width)
 * @param screenWidth Current screen width
 * @return Scaled spacing value
 */
fun responsiveSpacing(baseSpacing: Dp, screenWidth: Dp): Dp {
    val scaleFactor = (screenWidth / 360.dp).coerceIn(0.7f, 1.5f)
    return baseSpacing * scaleFactor
}
