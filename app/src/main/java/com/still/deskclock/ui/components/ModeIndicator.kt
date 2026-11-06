package com.still.deskclock.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.still.deskclock.ui.theme.StillColors

/**
 * Three small, subtle page indicators near the bottom of the screen.
 * Indicates which mode (Clock, Countdown Timer, Stopwatch) is currently selected.
 */
@Composable
fun ModeIndicator(
    pageCount: Int,
    currentPage: Int,
    modeTitles: List<String>,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until pageCount) {
            val isSelected = index == currentPage
            val indicatorWidth by animateDpAsState(
                targetValue = if (isSelected) 18.dp else 6.dp,
                animationSpec = tween(durationMillis = 220),
                label = "IndicatorWidth"
            )
            val indicatorColor by animateColorAsState(
                targetValue = if (isSelected) StillColors.SecondaryText else StillColors.PageIndicator,
                animationSpec = tween(durationMillis = 220),
                label = "IndicatorColor"
            )
            val label = modeTitles.getOrElse(index) { "Mode ${index + 1}" }

            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 32.dp, minHeight = 24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectPage(index) }
                    )
                    .semantics {
                        contentDescription = label
                        role = Role.Tab
                        selected = isSelected
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(indicatorWidth)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
        }
    }
}
