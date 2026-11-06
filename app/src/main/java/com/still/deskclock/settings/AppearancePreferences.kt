package com.still.deskclock.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TimerTypography(val displayName: String) {
    CLASSIC_BOLD("Classic Bold"),
    APPLE_STANDBY("Apple StandBy"),
    MINIMAL("Minimal"),
    MODERN_ROUNDED("Modern Rounded"),
    DIGITAL_CLOCK("Digital Clock"),
    CONDENSED("Condensed"),
    MONOSPACED("Monospaced"),
    ELEGANT("Elegant")
}

enum class TimerCardStyle(val displayName: String) {
    GLASS("Glass"),
    SOLID("Solid"),
    GRADIENT("Gradient"),
    MINIMAL_BORDER("Minimal Border"),
    ELEVATED("Elevated"),
    NEUMORPHIC("Neumorphic"),
    OUTLINED_GLASS("Outlined Glass"),
    FLOATING("Floating"),
    FLAT_MINIMAL("Flat Minimal"),
    LIQUID_GLASS("Liquid Glass")
}

val LocalTimerTypography = compositionLocalOf<TimerTypography> {
    error("No TimerTypography provided")
}

val LocalTimerCardStyle = compositionLocalOf<TimerCardStyle> {
    error("No TimerCardStyle provided")
}

val LocalAppearancePreferences = compositionLocalOf<AppearancePreferences> {
    error("No AppearancePreferences provided")
}

class AppearancePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("appearance_prefs", Context.MODE_PRIVATE)

    private val _typographyFlow = MutableStateFlow(
        TimerTypography.valueOf(prefs.getString("typography", TimerTypography.APPLE_STANDBY.name) ?: TimerTypography.APPLE_STANDBY.name)
    )
    val typographyFlow: StateFlow<TimerTypography> = _typographyFlow.asStateFlow()

    private val _cardStyleFlow = MutableStateFlow(
        TimerCardStyle.valueOf(prefs.getString("card_style", TimerCardStyle.SOLID.name) ?: TimerCardStyle.SOLID.name)
    )
    val cardStyleFlow: StateFlow<TimerCardStyle> = _cardStyleFlow.asStateFlow()

    fun setTypography(typography: TimerTypography) {
        prefs.edit().putString("typography", typography.name).apply()
        _typographyFlow.value = typography
    }

    fun setCardStyle(cardStyle: TimerCardStyle) {
        prefs.edit().putString("card_style", cardStyle.name).apply()
        _cardStyleFlow.value = cardStyle
    }
}
