package com.still.deskclock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Floating 5-Icon Bottom Navigation Capsule.
 *
 * - Width: ~75% of screen width.
 * - Height: ~6.7% of screen height (~54dp).
 * - Background: `#101010` with rounded capsule corners (~28dp).
 * - 5 icon buttons inside rounded square containers (`#1C1C1C` unselected, `#2A2A2A` selected).
 * - Icons:
 *   1. Settings / Customization (maps to page 3)
 *   2. Clock Home (maps to page 1)
 *   3. Stopwatch (maps to page 0)
 *   4. Countdown Timer (maps to page 2)
 *   5. Appearance / Themes (maps to page 3)
 */
@Composable
fun ClockBottomNavigation(
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val navTargetMap = listOf(3, 1, 0, 2, 3)
    val contentDescriptions = listOf(
        "Settings",
        "Clock Home",
        "Stopwatch",
        "Countdown Timer",
        "Appearance"
    )

    Box(
        modifier = modifier
            .fillMaxWidth(0.75f)
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Color(0xFF101010))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (index in 0..4) {
                val targetPage = navTargetMap[index]
                val isSelected = when (index) {
                    0, 4 -> currentPage == 3
                    1 -> currentPage == 1
                    2 -> currentPage == 0
                    3 -> currentPage == 2
                    else -> false
                }

                val containerColor = if (isSelected) Color(0xFF262626) else Color(0xFF1C1C1C)
                val iconColor = if (isSelected) Color(0xFFFFFFFF) else Color(0xFFB0B0B0)

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(containerColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectPage(targetPage) }
                        )
                        .semantics {
                            contentDescription = contentDescriptions[index]
                            role = Role.Tab
                            selected = isSelected
                        },
                    contentAlignment = Alignment.Center
                ) {
                    NavIconCanvas(iconType = index, color = iconColor)
                }
            }
        }
    }
}

/**
 * Vertical 5-Icon Navigation Rail for Landscape Mode.
 *
 * - Height: ~75% of screen height.
 * - Width: ~54dp.
 * - Background: `#101010` with rounded capsule corners (`27.dp`).
 * - 5 icon buttons stacked vertically.
 */
@Composable
fun ClockNavigationRail(
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val navTargetMap = listOf(3, 1, 0, 2, 3)
    val contentDescriptions = listOf(
        "Settings",
        "Clock Home",
        "Stopwatch",
        "Countdown Timer",
        "Appearance"
    )

    Box(
        modifier = modifier
            .width(54.dp)
            .fillMaxHeight(0.75f)
            .clip(RoundedCornerShape(27.dp))
            .background(Color(0xFF101010))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (index in 0..4) {
                val targetPage = navTargetMap[index]
                val isSelected = when (index) {
                    0, 4 -> currentPage == 3
                    1 -> currentPage == 1
                    2 -> currentPage == 0
                    3 -> currentPage == 2
                    else -> false
                }

                val containerColor = if (isSelected) Color(0xFF262626) else Color(0xFF1C1C1C)
                val iconColor = if (isSelected) Color(0xFFFFFFFF) else Color(0xFFB0B0B0)

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(containerColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectPage(targetPage) }
                        )
                        .semantics {
                            contentDescription = contentDescriptions[index]
                            role = Role.Tab
                            selected = isSelected
                        },
                    contentAlignment = Alignment.Center
                ) {
                    NavIconCanvas(iconType = index, color = iconColor)
                }
            }
        }
    }
}

@Composable
private fun NavIconCanvas(iconType: Int, color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()

        when (iconType) {
            0 -> {
                // Settings (Gear)
                drawCircle(color = color, radius = w * 0.28f, center = center, style = Stroke(strokeWidth))
                drawCircle(color = color, radius = w * 0.1f, center = center)
                for (i in 0..3) {
                    val angle = (i * 45) * (Math.PI / 180.0)
                    val dx = (Math.cos(angle) * w * 0.38).toFloat()
                    val dy = (Math.sin(angle) * h * 0.38).toFloat()
                    drawCircle(color = color, radius = 2.5.dp.toPx(), center = Offset(center.x + dx, center.y + dy))
                }
            }
            1 -> {
                // Home
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.48f)
                    lineTo(w * 0.85f, h * 0.85f)
                    lineTo(w * 0.15f, h * 0.85f)
                    lineTo(w * 0.15f, h * 0.48f)
                    close()
                }
                drawPath(path = path, color = color, style = Stroke(strokeWidth))
            }
            2 -> {
                // Stopwatch
                drawCircle(color = color, radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.55f), style = Stroke(strokeWidth))
                drawLine(color = color, start = Offset(w * 0.5f, h * 0.05f), end = Offset(w * 0.5f, h * 0.18f), strokeWidth = strokeWidth * 1.2f)
                drawLine(color = color, start = Offset(w * 0.5f, h * 0.55f), end = Offset(w * 0.65f, h * 0.35f), strokeWidth = strokeWidth)
            }
            3 -> {
                // World Clock / Countdown (Hourglass)
                val stroke = Stroke(width = strokeWidth)
                val path = Path().apply {
                    moveTo(w * 0.2f, h * 0.15f)
                    lineTo(w * 0.8f, h * 0.15f)
                    lineTo(w * 0.5f, h * 0.5f)
                    lineTo(w * 0.8f, h * 0.85f)
                    lineTo(w * 0.2f, h * 0.85f)
                    lineTo(w * 0.5f, h * 0.5f)
                    close()
                }
                drawPath(path = path, color = color, style = stroke)
            }
            4 -> {
                // Appearance / Themes (Palette)
                drawCircle(color = color, radius = w * 0.4f, center = center, style = Stroke(strokeWidth))
                drawCircle(color = color, radius = 2.dp.toPx(), center = Offset(w * 0.35f, h * 0.35f))
                drawCircle(color = color, radius = 2.dp.toPx(), center = Offset(w * 0.65f, h * 0.35f))
                drawCircle(color = color, radius = 2.dp.toPx(), center = Offset(w * 0.35f, h * 0.65f))
            }
        }
    }
}
