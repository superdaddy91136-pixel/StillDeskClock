package com.still.deskclock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.still.deskclock.ui.theme.StillColors

/**
 * Unobtrusive "#242424" secondary control pill button with accessible touch target size (>= 48dp height)
 * and clean typography.
 */
@Composable
fun ControlPillButton(
    text: String,
    accessibilityDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isEmphasized: Boolean = false,
    isCompact: Boolean = false,
    icon: (@Composable () -> Unit)? = null
) {
    val backgroundColor: Color = StillColors.SecondaryControls
    val textColor: Color = if (isEmphasized) StillColors.MainDigits else StillColors.SecondaryText
    val minHeight = if (isCompact) 36.dp else 48.dp
    val minWidth = if (isCompact) 48.dp else 72.dp
    val horizontalPad = if (isCompact) 12.dp else 18.dp
    val verticalPad = if (isCompact) 6.dp else 10.dp

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .alpha(if (enabled) 1f else 0.38f)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = horizontalPad, vertical = verticalPad)
            .semantics {
                contentDescription = accessibilityDescription
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            icon()
        } else {
            Text(
                text = text,
                style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelLarge,
                color = textColor,
                maxLines = 1
            )
        }
    }
}
