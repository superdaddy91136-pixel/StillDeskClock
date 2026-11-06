package com.still.deskclock.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp

@Composable
fun FlipTimeDisplay(
    timeParts: List<String>,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    topRowContent: @Composable (RowScope.() -> Unit)? = null,
    bottomRowContent: @Composable (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Pure pitch-black background
            .padding(
                horizontal = if (isLandscape) 28.dp else 20.dp,
                vertical = if (isLandscape) 20.dp else 24.dp
            )
            .semantics { contentDescription = accessibilityLabel },
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Row
        if (topRowContent != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                content = topRowContent
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth())
        }

        // Flip Clock Center
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val groupCount = timeParts.size
            val totalDigits = timeParts.sumOf { it.length }

            val digitHeight = if (isLandscape) {
                // Determine bounding height based on available width or height
                val wBased = (maxWidth - 48.dp) / (totalDigits * 0.75f + (groupCount - 1) * 0.3f)
                val hBased = maxHeight * 0.8f
                androidx.compose.ui.unit.min(wBased, hBased)
            } else {
                val maxDigitsInRow = timeParts.maxOf { it.length }
                val wBased = (maxWidth - 32.dp) / (maxDigitsInRow * 0.75f)
                val hBased = (maxHeight - 64.dp) / (groupCount * 1.1f)
                androidx.compose.ui.unit.min(wBased, hBased)
            }

            val digitWidth = digitHeight * 0.75f
            val fontSize = (digitHeight.value * 0.70f).sp

            if (isLandscape) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    timeParts.forEachIndexed { index, part ->
                        FlipDigitGroup(part, digitWidth, digitHeight, fontSize)
                        if (index < timeParts.lastIndex) {
                            ColonDivider(digitHeight)
                        }
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    timeParts.forEachIndexed { index, part ->
                        FlipDigitGroup(part, digitWidth, digitHeight, fontSize)
                    }
                }
            }
        }

        // Bottom Row
        if (bottomRowContent != null) {
            bottomRowContent()
        } else {
            Box(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ColonDivider(height: Dp) {
    Column(
        modifier = Modifier.height(height),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(height * 0.1f).clip(CircleShape).background(Color.White))
        Box(modifier = Modifier.size(height * 0.1f).clip(CircleShape).background(Color.White))
    }
}

@Composable
fun FlipDigitGroup(text: String, width: Dp, height: Dp, fontSize: TextUnit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        text.forEach { char ->
            AnimatedFlipDigit(digit = char, width = width, height = height, fontSize = fontSize)
        }
    }
}

@Composable
fun AnimatedFlipDigit(digit: Char, width: Dp, height: Dp, fontSize: TextUnit) {
    var oldDigit by remember { mutableStateOf(digit) }
    var newDigit by remember { mutableStateOf(digit) }
    val flipAnim = remember { Animatable(0f) }

    LaunchedEffect(digit) {
        if (digit != newDigit) {
            oldDigit = newDigit
            newDigit = digit
            flipAnim.snapTo(0f)
            flipAnim.animateTo(
                targetValue = 180f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
            oldDigit = newDigit
            flipAnim.snapTo(0f)
        }
    }

    val rotation = flipAnim.value
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
    ) {
        // Back Top
        DigitHalfPanel(newDigit, isTop = true, width = width, height = height, fontSize = fontSize)
        // Back Bottom
        DigitHalfPanel(oldDigit, isTop = false, width = width, height = height, fontSize = fontSize)

        // Flap
        if (rotation <= 90f) {
            DigitHalfPanel(
                digit = oldDigit,
                isTop = true,
                width = width,
                height = height,
                fontSize = fontSize,
                modifier = Modifier.graphicsLayer {
                    rotationX = rotation
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    cameraDistance = 12f * density.density
                }
            )
        } else {
            DigitHalfPanel(
                digit = newDigit,
                isTop = false,
                width = width,
                height = height,
                fontSize = fontSize,
                modifier = Modifier.graphicsLayer {
                    rotationX = rotation - 180f
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    cameraDistance = 12f * density.density
                }
            )
        }

        // Horizontal middle split
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.Black.copy(alpha = 0.8f))
        )
    }
}

@Composable
fun DigitHalfPanel(
    digit: Char,
    isTop: Boolean,
    width: Dp,
    height: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(
                object : Shape {
                    override fun createOutline(
                        size: Size,
                        layoutDirection: LayoutDirection,
                        density: Density
                    ) = Outline.Rectangle(
                        Rect(
                            left = 0f,
                            top = if (isTop) 0f else size.height / 2f,
                            right = size.width,
                            bottom = if (isTop) size.height / 2f else size.height
                        )
                    )
                }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        if (isTop) listOf(Color(0xFF3A3A3A), Color(0xFF262626))
                        else listOf(Color(0xFF262626), Color(0xFF141414))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(2.dp, Color(0xFF151515), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = digit.toString(),
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                style = TextStyle(
                    fontFeatureSettings = "tnum",
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                modifier = Modifier.offset(y = if (isTop) 0.dp else 0.dp)
            )
        }
    }
}
