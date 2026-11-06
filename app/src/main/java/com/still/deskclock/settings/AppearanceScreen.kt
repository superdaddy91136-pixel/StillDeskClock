package com.still.deskclock.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.still.deskclock.ui.components.LargeTimeDisplay
import com.still.deskclock.ui.theme.StillColors

@Composable
fun AppearanceScreen(modifier: Modifier = Modifier) {
    val prefs = LocalAppearancePreferences.current
    val currentTypography = LocalTimerTypography.current
    val currentCardStyle = LocalTimerCardStyle.current

    Column(modifier = modifier.fillMaxSize()) {
        // Live Preview Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(StillColors.MainBackground)
        ) {
            LargeTimeDisplay(
                mainTimeText = "25:00",
                accessibilityLabel = "Live preview of appearance",
                modifier = Modifier.fillMaxSize(),
                typographyStyle = currentTypography,
                cardStyle = currentCardStyle,
                topRowContent = {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.titleMedium,
                        color = StillColors.SecondaryText
                    )
                }
            )
        }

        // Selection Grids Section
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader("Timer Text Style")
            }
            items(TimerTypography.entries) { typography ->
                TypographyOptionCard(
                    typography = typography,
                    isSelected = typography == currentTypography,
                    onClick = { prefs.setTypography(typography) }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader("Timer Card Style")
            }
            items(TimerCardStyle.entries) { style ->
                CardStyleOptionCard(
                    cardStyle = style,
                    isSelected = style == currentCardStyle,
                    onClick = { prefs.setCardStyle(style) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = StillColors.MainDigits,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun TypographyOptionCard(
    typography: TimerTypography,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) StillColors.MainDigits else StillColors.PageIndicator
    val backgroundColor = if (isSelected) StillColors.DisplayPanel else StillColors.MainBackground

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "25:00",
                style = AppearanceStyleMappers.getTextStyleFor(typography).copy(
                    fontSize = MaterialTheme.typography.displayLarge.fontSize
                )
            )
            Text(
                text = typography.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = StillColors.SecondaryText,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CardStyleOptionCard(
    cardStyle: TimerCardStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) StillColors.MainDigits else StillColors.PageIndicator

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // We need a miniature version of the actual card style
        // We'll wrap a small LargeTimeDisplay mock to show the card effect accurately.
        val (cardModifier, _) = AppearanceStyleMappers.getCardModifier(
            cardStyle = cardStyle,
            isLandscape = false,
            horizontalMargin = 8.dp,
            verticalMargin = 8.dp,
            defaultCornerRadius = 12.dp
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StillColors.MainBackground) // Base background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(cardModifier)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                 Text(
                    text = cardStyle.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StillColors.MainDigits
                )
            }
        }
    }
}
