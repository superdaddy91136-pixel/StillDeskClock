package com.still.deskclock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Exact starting color palette specified for Still — Minimalist OLED Desk Clock.
 */
object StillColors {
    val MainBackground = Color(0xFF000000)      // "#000000" — pure black
    val DisplayPanel = Color(0xFF151515)        // "#151515" — very dark charcoal
    val MainDigits = Color(0xFFF2F2F2)          // "#F2F2F2" — soft white
    val SecondaryText = Color(0xFFA0A0A0)       // "#A0A0A0" — secondary text
    val SecondaryControls = Color(0xFF242424)   // "#242424" — secondary controls
    val PageIndicator = Color(0xFF555555)       // "#555555" — subtle page indicators
}

private val StillColorScheme = darkColorScheme(
    background = StillColors.MainBackground,
    surface = StillColors.DisplayPanel,
    onBackground = StillColors.MainDigits,
    onSurface = StillColors.MainDigits,
    primary = StillColors.MainDigits,
    onPrimary = StillColors.MainBackground,
    secondary = StillColors.SecondaryControls,
    onSecondary = StillColors.MainDigits,
    tertiary = StillColors.SecondaryText
)

/**
 * Clean system sans-serif typography with tabular numerals ("tnum") enabled
 * so digits maintain uniform width and never shift horizontally when time changes.
 */
val StillTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        color = StillColors.MainDigits,
        fontFeatureSettings = "tnum",
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        letterSpacing = (-2.0).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        color = StillColors.SecondaryText,
        fontFeatureSettings = "tnum",
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        color = StillColors.SecondaryText,
        fontFeatureSettings = "tnum",
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        color = StillColors.MainDigits,
        fontFeatureSettings = "tnum",
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        letterSpacing = 0.3.sp
    )
)

@Composable
fun StillDeskClockTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StillColorScheme,
        typography = StillTypography,
        content = content
    )
}
