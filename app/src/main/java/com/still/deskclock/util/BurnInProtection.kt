package com.still.deskclock.util

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Deterministic, bounded OLED burn-in protection.
 *
 * Shifts the main display panel and its typography together by 2–5 dp at periodic
 * 3-minute intervals while the screen is visible. Avoids continuous animation loops
 * or abrupt large jumps.
 */
object BurnInProtection {
    const val SHIFT_INTERVAL_MS: Long = 3 * 60 * 1000L // 3 minutes

    // Deterministic closed loop of subtle (x, y) dp offsets within [-4.dp, +4.dp] x [-3.dp, +3.dp].
    // Adjacent steps shift by only 2-3 dp so movement is gentle and coherent.
    private val OFFSET_PATH_DP: List<DpOffset> = listOf(
        DpOffset(0.dp, 0.dp),
        DpOffset(2.dp, 2.dp),
        DpOffset(4.dp, 0.dp),
        DpOffset(3.dp, (-2).dp),
        DpOffset(0.dp, (-3).dp),
        DpOffset((-3).dp, (-2).dp),
        DpOffset((-4).dp, 0.dp),
        DpOffset((-2).dp, 2.dp),
        DpOffset(0.dp, 3.dp)
    )

    fun offsetForStep(stepIndex: Int): DpOffset {
        val safeIndex = ((stepIndex % OFFSET_PATH_DP.size) + OFFSET_PATH_DP.size) % OFFSET_PATH_DP.size
        return OFFSET_PATH_DP[safeIndex]
    }
}

@Composable
fun rememberBurnInOffset(): State<IntOffset> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    var stepIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(BurnInProtection.SHIFT_INTERVAL_MS)
                stepIndex = (stepIndex + 1) % 9
            }
        }
    }

    val targetDpOffset = BurnInProtection.offsetForStep(stepIndex)
    val targetPxOffset = with(density) {
        IntOffset(
            x = targetDpOffset.x.toPx().roundToInt(),
            y = targetDpOffset.y.toPx().roundToInt()
        )
    }

    return animateIntOffsetAsState(
        targetValue = targetPxOffset,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "BurnInShiftOffset"
    )
}
