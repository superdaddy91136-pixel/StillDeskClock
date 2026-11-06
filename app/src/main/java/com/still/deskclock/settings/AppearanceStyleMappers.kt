package com.still.deskclock.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.still.deskclock.ui.theme.StillColors

object AppearanceStyleMappers {

    fun getTextStyleFor(typography: TimerTypography): TextStyle {
        val baseStyle = TextStyle(
            color = StillColors.MainDigits,
            fontFeatureSettings = "tnum",
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )

        return when (typography) {
            TimerTypography.CLASSIC_BOLD -> baseStyle.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.0).sp
            )
            TimerTypography.APPLE_STANDBY -> baseStyle.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                letterSpacing = (-2.0).sp
            )
            TimerTypography.MINIMAL -> baseStyle.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Thin,
                letterSpacing = 0.sp
            )
            TimerTypography.MODERN_ROUNDED -> baseStyle.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
            TimerTypography.DIGITAL_CLOCK -> baseStyle.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1.5).sp
            )
            TimerTypography.CONDENSED -> baseStyle.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-3.5).sp // Emulate condensed look
            )
            TimerTypography.MONOSPACED -> baseStyle.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp
            )
            TimerTypography.ELEGANT -> baseStyle.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
        }
    }

    // Returns a Pair of (PanelModifier, PanelCornerRadius) so the LargeTimeDisplay can apply it correctly
    fun getCardModifier(
        cardStyle: TimerCardStyle,
        isLandscape: Boolean,
        horizontalMargin: Dp,
        verticalMargin: Dp,
        defaultCornerRadius: Dp
    ): Pair<Modifier, Dp> {
        var modifier = Modifier.padding(horizontal = horizontalMargin, vertical = verticalMargin)
        var cornerRadius = defaultCornerRadius

        when (cardStyle) {
            TimerCardStyle.SOLID -> {
                modifier = modifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(StillColors.DisplayPanel)
            }
            TimerCardStyle.GLASS -> {
                modifier = modifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(Color.White.copy(alpha = 0.08f))
            }
            TimerCardStyle.GRADIENT -> {
                modifier = modifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF222222), Color(0xFF080808))
                        )
                    )
            }
            TimerCardStyle.MINIMAL_BORDER -> {
                modifier = modifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(cornerRadius))
                    .background(Color.Transparent)
            }
            TimerCardStyle.ELEVATED -> {
                modifier = modifier
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(cornerRadius))
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(StillColors.DisplayPanel)
            }
            TimerCardStyle.NEUMORPHIC -> {
                // Simulate neumorphic with subtle gradients/borders
                modifier = modifier
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = Color.Black, spotColor = Color.Black)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(Color(0xFF181818))
                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(cornerRadius))
            }
            TimerCardStyle.OUTLINED_GLASS -> {
                modifier = modifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(cornerRadius))
            }
            TimerCardStyle.FLOATING -> {
                // Floating card: larger margins, darker shadow, slightly smaller radius
                val floatMarginH = horizontalMargin + 16.dp
                val floatMarginV = verticalMargin + 16.dp
                cornerRadius = defaultCornerRadius - 4.dp
                modifier = Modifier
                    .padding(horizontal = floatMarginH, vertical = floatMarginV)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = Color.Black, spotColor = Color.Black)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(StillColors.DisplayPanel)
            }
            TimerCardStyle.FLAT_MINIMAL -> {
                // No background, transparent
                modifier = modifier.background(Color.Transparent)
            }
            TimerCardStyle.LIQUID_GLASS -> {
                // Soft radial gradient with a bit of color hint
                modifier = modifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1A1F26), // subtle blue/cyan hint
                                Color(0xFF0F0F12)
                            )
                        )
                    )
                    .border(0.5.dp, Brush.linearGradient(listOf(Color.White.copy(0.1f), Color.Transparent)), RoundedCornerShape(cornerRadius))
            }
        }
        return Pair(modifier, cornerRadius)
    }
}
