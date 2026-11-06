package com.still.deskclock.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import com.still.deskclock.ui.components.ClockBottomNavigation
import com.still.deskclock.ui.components.ClockNavigationRail
import com.still.deskclock.ui.components.StandByClockCard
import com.still.deskclock.util.TimeFormatters
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.Locale

/**
 * Mode One — Immersive Apple StandBy-inspired Split-Flap Clock Screen.
 *
 * - Pure black background `#000000`.
 * - Tapping anywhere on the screen smoothly toggles the visibility of the bottom navigation bar.
 * - When navigation hides, the clock cards automatically expand to fill the available screen space.
 * - Upper Card: Displays current 12-hour hour, top Date (`10/24/26`), bottom-left `AM/PM`.
 * - Lower Card: Displays current Minute, top Weekday (`Saturday`), bottom-right secondary hour (`12`).
 * - Distinctive horizontal split line `#000000` across exact middle (50%) of cards.
 */
@Composable
fun DeskClockScreen(
    currentPage: Int = 0,
    onSelectPage: (Int) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isNavVisible by remember { mutableStateOf(true) }
    var snapshot by remember { mutableStateOf(TimeFormatters.currentClockSnapshot()) }

    // Listen for system minute ticks, manual time changes, and time-zone updates
    DisposableEffect(context, lifecycleOwner) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val updated = TimeFormatters.currentClockSnapshot(LocalDateTime.now())
                if (updated != snapshot) {
                    snapshot = updated
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
        }
        context.registerReceiver(receiver, filter)

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val updated = TimeFormatters.currentClockSnapshot(LocalDateTime.now())
                if (updated != snapshot) {
                    snapshot = updated
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) {
            }
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    // Fallback second-boundary wake-up while resumed
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                val updated = TimeFormatters.currentClockSnapshot(LocalDateTime.now())
                if (updated != snapshot) {
                    snapshot = updated
                }
                val nowMs = System.currentTimeMillis()
                val delayUntilNextSecondMs = (1_000L - (nowMs % 1_000L)).coerceIn(100L, 1_000L)
                delay(delayUntilNextSecondMs)
            }
        }
    }

    val hour12Int = when (snapshot.hour % 12) {
        0 -> 12
        else -> snapshot.hour % 12
    }
    val hourStr = String.format(Locale.US, "%02d", hour12Int)
    val minuteStr = String.format(Locale.US, "%02d", snapshot.minute)
    val secondStr = String.format(Locale.US, "%02d", snapshot.second)
    val amPmStr = if (snapshot.hour >= 12) "PM" else "AM"

    val accessibilityDesc = "Current time $hour12Int $minuteStr $amPmStr, ${snapshot.weekdayText} ${snapshot.dateText}"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { isNavVisible = !isNavVisible }
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -30f) {
                        onOpenSettings()
                    }
                }
            }
            .semantics { contentDescription = accessibilityDesc },
        contentAlignment = Alignment.Center
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clock Cards area taking remaining width
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(if (isNavVisible) 0.90f else 0.98f)
                            .fillMaxHeight(0.92f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StandByClockCard(
                            digitsText = hourStr,
                            topLabel = snapshot.dateText,
                            bottomLeftLabel = amPmStr,
                            bottomRightLabel = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        StandByClockCard(
                            digitsText = minuteStr,
                            topLabel = snapshot.weekdayText,
                            bottomLeftLabel = null,
                            bottomRightLabel = secondStr,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }

                // Vertical Navigation Rail on the right side
                AnimatedVisibility(
                    visible = isNavVisible,
                    enter = fadeIn(tween(300, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it },
                    exit = fadeOut(tween(300, easing = FastOutSlowInEasing)) +
                            slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { it }
                ) {
                    ClockNavigationRail(
                        currentPage = currentPage,
                        onSelectPage = onSelectPage,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (isNavVisible) 8.dp else 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .fillMaxHeight(if (isNavVisible) 0.90f else 0.97f),
                        verticalArrangement = Arrangement.spacedBy(if (isNavVisible) 14.dp else 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        StandByClockCard(
                            digitsText = hourStr,
                            topLabel = snapshot.dateText,
                            bottomLeftLabel = amPmStr,
                            bottomRightLabel = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        StandByClockCard(
                            digitsText = minuteStr,
                            topLabel = snapshot.weekdayText,
                            bottomLeftLabel = null,
                            bottomRightLabel = secondStr,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isNavVisible,
                    enter = fadeIn(tween(300, easing = FastOutSlowInEasing)) +
                            slideInVertically(tween(300, easing = FastOutSlowInEasing)) { it },
                    exit = fadeOut(tween(300, easing = FastOutSlowInEasing)) +
                            slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it }
                ) {
                    ClockBottomNavigation(
                        currentPage = currentPage,
                        onSelectPage = onSelectPage,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}
