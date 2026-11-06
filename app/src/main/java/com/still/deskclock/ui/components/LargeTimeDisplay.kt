package com.still.deskclock.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.still.deskclock.ui.theme.StillColors
import kotlin.math.min

import com.still.deskclock.settings.TimerCardStyle
import com.still.deskclock.settings.TimerTypography
import com.still.deskclock.settings.AppearanceStyleMappers

/**
 * Reusable display panel and oversized digit renderer for Still — Desk Clock.
 *
 * - Surrounded by a pure-black margin ("#000000") so pure black covers the majority of the screen.
 * - Single subtle dark-charcoal display region ("#151515") behind the main digits.
 * - Dynamically scales the oversized digits ("#F2F2F2") so they occupy ~78–84% of the usable
 *   display width in landscape orientation and reflow gracefully in portrait without clipping.
 */
@Composable
fun LargeTimeDisplay(
    mainTimeText: String,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    typographyStyle: TimerTypography = TimerTypography.APPLE_STANDBY,
    cardStyle: TimerCardStyle = TimerCardStyle.SOLID,
    topRowContent: @Composable (RowScope.() -> Unit)? = null,
    bottomRowContent: @Composable (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Pure-black outer margin around the subtle #151515 charcoal panel
    val horizontalMargin = if (isLandscape) 28.dp else 18.dp
    val verticalMargin = if (isLandscape) 20.dp else 32.dp
    val basePanelCornerRadius = if (isLandscape) 24.dp else 20.dp

    val (cardModifier, finalCornerRadius) = AppearanceStyleMappers.getCardModifier(
        cardStyle = cardStyle,
        isLandscape = isLandscape,
        horizontalMargin = horizontalMargin,
        verticalMargin = verticalMargin,
        defaultCornerRadius = basePanelCornerRadius
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(cardModifier)
            .padding(
                horizontal = if (isLandscape) 28.dp else 20.dp,
                vertical = if (isLandscape) 20.dp else 24.dp
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar inside the display panel (e.g., Date on left, Weekday on right)
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

            // Dominant centered digits scaled dynamically to 78-84% of panel width without clipping
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val availableWidthDp = maxWidth.value
                val availableHeightDp = maxHeight.value

                // Calculate character width ratio for tabular sans-serif numerals (Apple StandBy style: extra-bold & massive width).
                // 5-char strings ("02 43", "30:00") fill ~88-94% width;
                // 8-char strings scale down proportionally so they never clip.
                val charCount = mainTimeText.length.coerceAtLeast(5)
                val targetWidthFraction = if (isLandscape) 0.88f else 0.94f
                val avgCharWidthFactor = 0.54f

                val widthBasedSp = (availableWidthDp * targetWidthFraction) / (charCount * avgCharWidthFactor)
                val heightBasedSp = availableHeightDp * (if (isLandscape) 0.82f else 0.56f)
                val computedFontSizeSp = min(widthBasedSp, heightBasedSp).coerceIn(36f, 260f)

                // Compensate for system fontScale so accessibility font settings don't clip the clock
                val adjustedSp = (computedFontSizeSp / density.fontScale.coerceAtLeast(0.85f))
                    .coerceIn(36f, 260f)

                val baseTextStyle = AppearanceStyleMappers.getTextStyleFor(typographyStyle)

                Text(
                    text = mainTimeText,
                    style = baseTextStyle.copy(
                        fontSize = adjustedSp.sp,
                        lineHeight = adjustedSp.sp
                    ),
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics {
                        contentDescription = accessibilityLabel
                    }
                )
            }

            // Optional unobtrusive bottom area (controls for Timer/Stopwatch; empty on idle Clock)
            if (bottomRowContent != null) {
                bottomRowContent()
            } else {
                Box(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
