package com.still.deskclock

import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.still.deskclock.settings.AppearancePreferences
import com.still.deskclock.settings.AppearanceScreen
import com.still.deskclock.settings.TimerCardStyle
import com.still.deskclock.settings.TimerTypography
import com.still.deskclock.settings.LocalAppearancePreferences
import com.still.deskclock.settings.LocalTimerCardStyle
import com.still.deskclock.settings.LocalTimerTypography
import com.still.deskclock.ui.components.ClockBottomNavigation
import com.still.deskclock.ui.screens.CountdownTimerScreen
import com.still.deskclock.ui.screens.DeskClockScreen
import com.still.deskclock.ui.screens.StopwatchScreen
import com.still.deskclock.ui.theme.StillColors
import com.still.deskclock.ui.theme.StillDeskClockTheme
import com.still.deskclock.util.rememberBurnInOffset
import com.still.deskclock.viewmodel.CountdownTimerViewModel
import com.still.deskclock.viewmodel.StopwatchViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val countdownTimerViewModel: CountdownTimerViewModel by viewModels()
    private val stopwatchViewModel: StopwatchViewModel by viewModels()
    private var orientationEventListener: OrientationEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureImmersiveWindow()
        setupSensorOrientation()

        val appearancePrefs = AppearancePreferences(this)

        setContent {
            val typography by appearancePrefs.typographyFlow.collectAsState()
            val cardStyle by appearancePrefs.cardStyleFlow.collectAsState()

            CompositionLocalProvider(
                LocalAppearancePreferences provides appearancePrefs,
                LocalTimerTypography provides typography,
                LocalTimerCardStyle provides cardStyle
            ) {
                StillDeskClockTheme {
                    StillDeskClockApp(
                        countdownTimerViewModel = countdownTimerViewModel,
                        stopwatchViewModel = stopwatchViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Keep the screen awake while the desk clock activity is visible
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()
        // Synchronize monotonic timekeeping states upon returning to foreground
        countdownTimerViewModel.onForegroundSync()
        stopwatchViewModel.onForegroundSync()
    }

    override fun onPause() {
        // Release screen-on flag when the activity is paused or backgrounded
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun configureImmersiveWindow() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        hideSystemBars()
    }

    private fun hideSystemBars() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun setupSensorOrientation() {
        orientationEventListener = object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                when (orientation) {
                    in 45..135, in 225..315 -> {
                        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        }
                    }
                    in 0..45, in 315..360, in 135..225 -> {
                        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                        }
                    }
                }
            }
        }
        if (orientationEventListener?.canDetectOrientation() == true) {
            orientationEventListener?.enable()
        }
    }

    override fun onDestroy() {
        orientationEventListener?.disable()
        orientationEventListener = null
        super.onDestroy()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StillDeskClockApp(
    countdownTimerViewModel: CountdownTimerViewModel,
    stopwatchViewModel: StopwatchViewModel
) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val burnInOffset by rememberBurnInOffset()
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Sensible back navigation: return to Clock mode if viewing Timer or Stopwatch
    BackHandler(enabled = pagerState.currentPage != 1) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StillColors.MainBackground)
            .windowInsetsPadding(WindowInsets.displayCutout)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { burnInOffset }
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                beyondBoundsPageCount = 2
            ) { page ->
                when (page) {
                    0 -> StopwatchScreen(viewModel = stopwatchViewModel)
                    1 -> DeskClockScreen(
                        currentPage = pagerState.currentPage,
                        onSelectPage = { targetPage ->
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetPage)
                            }
                        },
                        onOpenSettings = { showSettingsDialog = true }
                    )
                    2 -> CountdownTimerScreen(viewModel = countdownTimerViewModel)
                }
            }
        }

        if (showSettingsDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showSettingsDialog = false }) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StillColors.MainBackground
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppearanceScreen()
                        com.still.deskclock.ui.components.ControlPillButton(
                            text = "Close",
                            accessibilityDescription = "Close settings",
                            onClick = { showSettingsDialog = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            isEmphasized = true
                        )
                    }
                }
            }
        }
    }
}
