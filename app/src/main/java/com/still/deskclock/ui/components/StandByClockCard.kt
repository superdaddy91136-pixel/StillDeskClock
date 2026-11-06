package com.still.deskclock.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium Apple StandBy-inspired Split-Flap Clock Card with Responsive Sizing and 3D Mechanical Flip Animation.
 *
 * - Dark charcoal background `#101010` with large rounded corners (~11% of card width).
 * - Responsive font sizing bounded by both height and width to prevent side clipping or overlapping.
 * - Center oversized split-flap digits in light grey `#B0B0B0`.
 * - Full-width horizontal split hinge line `#000000` (~6dp thickness) across the exact middle (50%).
 * - Premium 3D mechanical rotation flip animation with dynamic lighting/shading overlays.
 */
@Composable
fun StandByClockCard(
    digitsText: String,
    topLabel: String = "",
    bottomLeftLabel: String? = null,
    bottomRightLabel: String? = null,
    modifier: Modifier = Modifier,
    topContent: @Composable (() -> Unit)? = null
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 11))
            .background(Color(0xFF101010))
    ) {
        val cardWidth = this.maxWidth
        val cardHeight = this.maxHeight

        val topPadding = cardHeight * 0.04f
        val bottomPadding = cardHeight * 0.04f
        val sidePadding = cardWidth * 0.06f

        val digitCount = digitsText.length.coerceAtLeast(1)
        val digitSpacing = if (digitCount > 1) 8.dp else 0.dp
        val availableWidthForDigits = (cardWidth - (sidePadding * 2) - (digitSpacing * (digitCount - 1))).coerceAtLeast(1.dp)
        val widthPerDigit = availableWidthForDigits / digitCount

        // Responsive font sizing bounded by both height and width to prevent clipping when controls appear/disappear
        val targetDigitHeight = cardHeight * 0.65f
        val heightBasedSize = targetDigitHeight.value * 0.82f * 1.5f
        val widthBasedSize = widthPerDigit.value * 0.95f * 1.5f
        val fontSize = minOf(heightBasedSize, widthBasedSize).sp

        // Top Header Label or Custom Top Content
        if (topContent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = topPadding)
            ) {
                topContent()
            }
        } else if (topLabel.isNotEmpty()) {
            Text(
                text = topLabel,
                style = TextStyle(
                    fontSize = (cardHeight.value * 0.08f).coerceIn(16f, 26f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB0B0B0),
                    fontFamily = FontFamily.SansSerif
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = topPadding)
            )
        }

        // Center Oversized Split-Flap Digits
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sidePadding),
            contentAlignment = Alignment.Center
        ) {
            StandByDigitsGroup(
                digitsText = digitsText,
                fontSize = fontSize
            )
        }

        // Horizontal split line extending across the complete card
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFF000000))
        )

        // Bottom Left Label (AM / PM)
        if (!bottomLeftLabel.isNullOrEmpty()) {
            Text(
                text = bottomLeftLabel,
                style = TextStyle(
                    fontSize = (cardHeight.value * 0.09f).coerceIn(18f, 30f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB0B0B0),
                    fontFamily = FontFamily.SansSerif
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = sidePadding, bottom = bottomPadding)
            )
        }

        // Bottom Right Label (Secondary Hour / Seconds)
        if (!bottomRightLabel.isNullOrEmpty()) {
            Text(
                text = bottomRightLabel,
                style = TextStyle(
                    fontSize = (cardHeight.value * 0.09f).coerceIn(18f, 30f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB0B0B0),
                    fontFamily = FontFamily.SansSerif
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = sidePadding, bottom = bottomPadding)
            )
        }
    }
}

@Composable
private fun StandByDigitsGroup(
    digitsText: String,
    fontSize: TextUnit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        digitsText.forEach { char ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.82f),
                contentAlignment = Alignment.Center
            ) {
                StandByFlipDigit(
                    digit = char,
                    fontSize = fontSize
                )
            }
        }
    }
}

@Composable
private fun StandByFlipDigit(
    digit: Char,
    fontSize: TextUnit
) {
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
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
            )
            oldDigit = newDigit
            flipAnim.snapTo(0f)
        }
    }

    val rotation = flipAnim.value
    val density = LocalDensity.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Static Top: Always shows newDigit top half
        DigitHalfView(digit = newDigit, isTop = true, fontSize = fontSize)

        // 2. Static Bottom: Shows oldDigit bottom half during first half, then newDigit bottom half
        DigitHalfView(
            digit = if (rotation <= 90f) oldDigit else newDigit,
            isTop = false,
            fontSize = fontSize
        )

        // 3. Animated Flipping Leaf with 3D Rotation and Realistic Shading
        if (rotation <= 90f) {
            val progress = rotation / 90f
            DigitHalfView(
                digit = oldDigit,
                isTop = true,
                fontSize = fontSize,
                modifier = Modifier.graphicsLayer {
                    rotationX = rotation
                    transformOrigin = TransformOrigin(0.5f, 1.0f)
                    cameraDistance = 16f * density.density
                    shadowElevation = 10.dp.toPx()
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = progress * 0.5f))
                )
            }
        } else {
            val progress = (rotation - 90f) / 90f
            DigitHalfView(
                digit = newDigit,
                isTop = false,
                fontSize = fontSize,
                modifier = Modifier.graphicsLayer {
                    rotationX = -(180f - rotation)
                    transformOrigin = TransformOrigin(0.5f, 0.0f)
                    cameraDistance = 16f * density.density
                    shadowElevation = 10.dp.toPx()
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (1f - progress) * 0.5f))
                )
            }
        }
    }
}

@Composable
private fun DigitHalfView(
    digit: Char,
    isTop: Boolean,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
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
        Text(
            text = digit.toString(),
            color = Color(0xFFB0B0B0),
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            style = TextStyle(
                fontFeatureSettings = "tnum",
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            modifier = Modifier.align(Alignment.Center)
        )
        content?.invoke()
    }
}
