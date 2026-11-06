package com.still.deskclock.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.still.deskclock.R
import com.still.deskclock.ui.components.ControlPillButton
import com.still.deskclock.ui.components.StandByClockCard
import com.still.deskclock.ui.components.icons.HourglassIcon
import com.still.deskclock.ui.components.icons.PauseIcon
import com.still.deskclock.ui.components.icons.PlayIcon
import com.still.deskclock.ui.components.icons.ResetIcon
import com.still.deskclock.ui.theme.StillColors
import com.still.deskclock.util.TimeFormatters
import com.still.deskclock.viewmodel.CountdownTimerUiState
import com.still.deskclock.viewmodel.CountdownTimerViewModel
import com.still.deskclock.viewmodel.TimerStatus
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Mode Two — Countdown Timer (Minimalist Redesign).
 *
 * - Pure black background `#000000`.
 * - No status text (`RUNNING`, `PAUSED`) or `SECONDS` labels inside boxes.
 * - Duration selection button moved to right-side control column.
 * - Icon-only controls with auto-hiding behavior and expandable flip-clock cards.
 */
@Composable
fun CountdownTimerScreen(
    viewModel: CountdownTimerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showDurationDialog by remember { mutableStateOf(false) }

    val timeParts = remember(state.remainingMs, state.configuredDurationMs) {
        TimeFormatters.getCountdownParts(
            remainingMs = state.remainingMs,
            configuredDurationMs = state.configuredDurationMs
        )
    }

    val totalCfgMin = (state.configuredDurationMs / 60_000L).toInt()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isRunning = state.status == TimerStatus.RUNNING

    var isControlsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(3500L)
            isControlsVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { isControlsVisible = !isControlsVisible }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Square StandBy Cards Area (Left / Main) with smooth size animation
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing)),
                    contentAlignment = Alignment.Center
                ) {
                    val count = timeParts.size
                    val spacing = 16.dp
                    val availableWidth = this.maxWidth
                    val availableHeight = this.maxHeight

                    val maxBoxWidth = (availableWidth - spacing * (count - 1)) / count
                    val boxSize = min(maxBoxWidth, availableHeight).coerceAtLeast(0.dp)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        timeParts.forEach { part ->
                            StandByClockCard(
                                digitsText = part,
                                topLabel = "",
                                bottomLeftLabel = null,
                                bottomRightLabel = null,
                                modifier = Modifier.size(boxSize)
                            )
                        }
                    }
                }

                // Vertical Side Controls Column (Auto-Hiding)
                AnimatedVisibility(
                    visible = isControlsVisible,
                    enter = fadeIn(tween(300, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it },
                    exit = fadeOut(tween(300, easing = FastOutSlowInEasing)) +
                            slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { it }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable(enabled = false, onClick = {}),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Start / Pause / Resume (Primary)
                        CircularIconButton(
                            onClick = {
                                when (state.status) {
                                    TimerStatus.READY, TimerStatus.COMPLETED -> viewModel.start()
                                    TimerStatus.RUNNING -> viewModel.pause()
                                    TimerStatus.PAUSED -> viewModel.resume()
                                }
                            },
                            accessibilityDescription = "Toggle timer playback",
                            isPrimary = true
                        ) {
                            when (state.status) {
                                TimerStatus.RUNNING -> PauseIcon()
                                else -> PlayIcon()
                            }
                        }

                        // Reset (Secondary)
                        CircularIconButton(
                            onClick = { viewModel.reset() },
                            accessibilityDescription = "Reset timer",
                            enabled = state.status != TimerStatus.READY || state.remainingMs != state.configuredDurationMs,
                            isPrimary = false
                        ) {
                            ResetIcon()
                        }

                        // Duration Selection Button (Secondary)
                        CircularIconButton(
                            onClick = { showDurationDialog = true },
                            accessibilityDescription = "Set duration",
                            enabled = !isRunning,
                            isPrimary = false
                        ) {
                            HourglassIcon()
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing)),
                    contentAlignment = Alignment.Center
                ) {
                    val count = timeParts.size
                    val spacing = 14.dp
                    val availableWidth = this.maxWidth
                    val availableHeight = this.maxHeight

                    val maxBoxHeight = (availableHeight - spacing * (count - 1)) / count
                    val boxSize = min(maxBoxHeight, availableWidth).coerceAtLeast(0.dp)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        timeParts.forEach { part ->
                            StandByClockCard(
                                digitsText = part,
                                topLabel = "",
                                bottomLeftLabel = null,
                                bottomRightLabel = null,
                                modifier = Modifier.size(boxSize)
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isControlsVisible,
                    enter = fadeIn(tween(300, easing = FastOutSlowInEasing)) +
                            slideInVertically(tween(300, easing = FastOutSlowInEasing)) { it },
                    exit = fadeOut(tween(300, easing = FastOutSlowInEasing)) +
                            slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable(enabled = false, onClick = {}),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Start / Pause / Resume (Primary)
                        CircularIconButton(
                            onClick = {
                                when (state.status) {
                                    TimerStatus.READY, TimerStatus.COMPLETED -> viewModel.start()
                                    TimerStatus.RUNNING -> viewModel.pause()
                                    TimerStatus.PAUSED -> viewModel.resume()
                                }
                            },
                            accessibilityDescription = "Toggle timer playback",
                            isPrimary = true
                        ) {
                            when (state.status) {
                                TimerStatus.RUNNING -> PauseIcon()
                                else -> PlayIcon()
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Reset (Secondary)
                        CircularIconButton(
                            onClick = { viewModel.reset() },
                            accessibilityDescription = "Reset timer",
                            enabled = state.status != TimerStatus.READY || state.remainingMs != state.configuredDurationMs,
                            isPrimary = false
                        ) {
                            ResetIcon()
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Duration Selection Button (Secondary)
                        CircularIconButton(
                            onClick = { showDurationDialog = true },
                            accessibilityDescription = "Set duration",
                            enabled = !isRunning,
                            isPrimary = false
                        ) {
                            HourglassIcon()
                        }
                    }
                }
            }
        }
    }

    if (showDurationDialog) {
        TimerDurationDialog(
            initialMinutes = totalCfgMin.toFloat().coerceIn(1f, 180f),
            onDismiss = { showDurationDialog = false },
            onConfirm = { minutes ->
                viewModel.setDurationMinutes(minutes)
                showDurationDialog = false
            }
        )
    }
}

@Composable
private fun CircularIconButton(
    onClick: () -> Unit,
    accessibilityDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    content: @Composable () -> Unit
) {
    val background = if (isPrimary) Color(0xFF262626) else Color(0xFF181818)
    val size = if (isPrimary) 64.dp else 52.dp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun TimerDurationDialog(
    initialMinutes: Float,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedMinutes by remember { mutableFloatStateOf(initialMinutes) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = StillColors.DisplayPanel
        ) {
            Column(
                modifier = Modifier
                    .background(StillColors.DisplayPanel)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set Countdown Duration",
                    style = MaterialTheme.typography.titleMedium,
                    color = StillColors.MainDigits
                )

                Spacer(modifier = Modifier.height(16.dp))

                val roundedMin = selectedMinutes.roundToInt().coerceIn(1, 180)
                val hrs = roundedMin / 60
                val mins = roundedMin % 60
                val displayStr = when {
                    hrs > 0 && mins > 0 -> "$hrs hour $mins minutes"
                    hrs > 0 -> if (hrs == 1) "1 hour" else "$hrs hours"
                    else -> "$mins minutes"
                }

                Text(
                    text = displayStr,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp),
                    color = StillColors.MainDigits
                )

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = selectedMinutes,
                    onValueChange = { selectedMinutes = it },
                    valueRange = 1f..180f,
                    steps = 179
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlPillButton(
                        text = "Cancel",
                        accessibilityDescription = "Cancel",
                        onClick = onDismiss
                    )
                    ControlPillButton(
                        text = "Apply",
                        accessibilityDescription = "Apply countdown duration",
                        onClick = { onConfirm(roundedMin) },
                        isEmphasized = true
                    )
                }
            }
        }
    }
}
