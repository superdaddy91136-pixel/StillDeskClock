package com.still.deskclock.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class TimerStatus {
    READY,
    RUNNING,
    PAUSED,
    COMPLETED
}

data class CountdownTimerUiState(
    val configuredDurationMs: Long = DEFAULT_DURATION_MS,
    val remainingMs: Long = DEFAULT_DURATION_MS,
    val status: TimerStatus = TimerStatus.READY
) {
    companion object {
        const val DEFAULT_DURATION_MS: Long = 30 * 60 * 1000L // 30 minutes default
        const val FIVE_MINUTES_MS: Long = 5 * 60 * 1000L
        const val MIN_DURATION_MS: Long = 60 * 1000L // 1 minute minimum custom/adjusted duration
        const val MAX_DURATION_MS: Long = 99 * 3600 * 1000L // 99 hours maximum
        val PRESET_MINUTES: List<Int> = listOf(15, 30, 45, 60, 90)
    }
}

class CountdownTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(CountdownTimerUiState())
    val uiState: StateFlow<CountdownTimerUiState> = _uiState.asStateFlow()

    // Monotonic deadline in SystemClock.elapsedRealtime() milliseconds when RUNNING
    private var deadlineElapsedRealtimeMs: Long = 0L
    private var tickerJob: Job? = null

    init {
        restoreSessionIfAvailable()
    }

    /**
     * Recalculates remaining time when the activity returns to the foreground.
     */
    fun onForegroundSync() {
        val current = _uiState.value
        if (current.status == TimerStatus.RUNNING) {
            val nowElapsed = SystemClock.elapsedRealtime()
            val remaining = (deadlineElapsedRealtimeMs - nowElapsed).coerceAtLeast(0L)
            if (remaining <= 0L) {
                completeTimer(triggerAlert = false)
            } else {
                _uiState.update { it.copy(remainingMs = remaining) }
                startTicker()
            }
        }
    }

    fun selectPresetMinutes(minutes: Int) {
        val durationMs = (minutes.coerceAtLeast(1) * 60_000L)
            .coerceIn(CountdownTimerUiState.MIN_DURATION_MS, CountdownTimerUiState.MAX_DURATION_MS)
        applyNewConfiguredDuration(durationMs)
    }

    fun setCustomDuration(hours: Int, minutes: Int) {
        val safeHours = hours.coerceIn(0, 99)
        val safeMinutes = minutes.coerceIn(0, 599)
        val totalMinutes = (safeHours * 60 + safeMinutes).coerceAtLeast(1)
        val durationMs = (totalMinutes * 60_000L)
            .coerceIn(CountdownTimerUiState.MIN_DURATION_MS, CountdownTimerUiState.MAX_DURATION_MS)
        applyNewConfiguredDuration(durationMs)
    }

    fun setDurationMinutes(minutes: Int) {
        val durationMs = (minutes.coerceAtLeast(1) * 60_000L)
            .coerceIn(CountdownTimerUiState.MIN_DURATION_MS, CountdownTimerUiState.MAX_DURATION_MS)
        applyNewConfiguredDuration(durationMs)
    }

    private fun applyNewConfiguredDuration(durationMs: Long) {
        stopTicker()
        deadlineElapsedRealtimeMs = 0L
        _uiState.value = CountdownTimerUiState(
            configuredDurationMs = durationMs,
            remainingMs = durationMs,
            status = TimerStatus.READY
        )
        persistState()
    }

    fun start() {
        val current = _uiState.value
        val startDuration = if (current.remainingMs <= 0L) {
            current.configuredDurationMs.coerceAtLeast(CountdownTimerUiState.MIN_DURATION_MS)
        } else {
            current.remainingMs
        }
        deadlineElapsedRealtimeMs = SystemClock.elapsedRealtime() + startDuration
        _uiState.update {
            it.copy(
                remainingMs = startDuration,
                status = TimerStatus.RUNNING
            )
        }
        persistState()
        startTicker()
    }

    fun pause() {
        if (_uiState.value.status != TimerStatus.RUNNING) return
        stopTicker()
        val remaining = (deadlineElapsedRealtimeMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        if (remaining <= 0L) {
            completeTimer(triggerAlert = true)
            return
        }
        deadlineElapsedRealtimeMs = 0L
        _uiState.update {
            it.copy(
                remainingMs = remaining,
                status = TimerStatus.PAUSED
            )
        }
        persistState()
    }

    fun resume() {
        val current = _uiState.value
        if (current.status != TimerStatus.PAUSED) return
        if (current.remainingMs <= 0L) {
            completeTimer(triggerAlert = false)
            return
        }
        deadlineElapsedRealtimeMs = SystemClock.elapsedRealtime() + current.remainingMs
        _uiState.update { it.copy(status = TimerStatus.RUNNING) }
        persistState()
        startTicker()
    }

    fun reset() {
        stopTicker()
        deadlineElapsedRealtimeMs = 0L
        val configured = _uiState.value.configuredDurationMs
            .coerceIn(CountdownTimerUiState.MIN_DURATION_MS, CountdownTimerUiState.MAX_DURATION_MS)
        _uiState.value = CountdownTimerUiState(
            configuredDurationMs = configured,
            remainingMs = configured,
            status = TimerStatus.READY
        )
        persistState()
    }

    fun addFiveMinutes() {
        val delta = CountdownTimerUiState.FIVE_MINUTES_MS
        val current = _uiState.value
        when (current.status) {
            TimerStatus.RUNNING -> {
                val nowElapsed = SystemClock.elapsedRealtime()
                val currentRemaining = (deadlineElapsedRealtimeMs - nowElapsed).coerceAtLeast(0L)
                val newRemaining = (currentRemaining + delta)
                    .coerceAtMost(CountdownTimerUiState.MAX_DURATION_MS)
                deadlineElapsedRealtimeMs = nowElapsed + newRemaining
                _uiState.update {
                    it.copy(
                        remainingMs = newRemaining,
                        configuredDurationMs = maxOf(it.configuredDurationMs, newRemaining)
                    )
                }
            }
            TimerStatus.PAUSED -> {
                val newRemaining = (current.remainingMs + delta)
                    .coerceAtMost(CountdownTimerUiState.MAX_DURATION_MS)
                _uiState.update {
                    it.copy(
                        remainingMs = newRemaining,
                        configuredDurationMs = maxOf(it.configuredDurationMs, newRemaining)
                    )
                }
            }
            TimerStatus.READY, TimerStatus.COMPLETED -> {
                val base = if (current.status == TimerStatus.COMPLETED) current.configuredDurationMs else current.remainingMs
                val newDuration = (base + delta)
                    .coerceIn(CountdownTimerUiState.MIN_DURATION_MS, CountdownTimerUiState.MAX_DURATION_MS)
                _uiState.value = CountdownTimerUiState(
                    configuredDurationMs = newDuration,
                    remainingMs = newDuration,
                    status = TimerStatus.READY
                )
            }
        }
        persistState()
    }

    fun subtractFiveMinutes() {
        val delta = CountdownTimerUiState.FIVE_MINUTES_MS
        val current = _uiState.value
        when (current.status) {
            TimerStatus.RUNNING -> {
                val nowElapsed = SystemClock.elapsedRealtime()
                val currentRemaining = (deadlineElapsedRealtimeMs - nowElapsed).coerceAtLeast(0L)
                // Prevent invalid negative or zero remaining duration while running
                if (currentRemaining <= CountdownTimerUiState.MIN_DURATION_MS) return
                val newRemaining = (currentRemaining - delta)
                    .coerceAtLeast(CountdownTimerUiState.MIN_DURATION_MS)
                deadlineElapsedRealtimeMs = nowElapsed + newRemaining
                _uiState.update { it.copy(remainingMs = newRemaining) }
            }
            TimerStatus.PAUSED -> {
                val newRemaining = (current.remainingMs - delta)
                    .coerceAtLeast(CountdownTimerUiState.MIN_DURATION_MS)
                _uiState.update { it.copy(remainingMs = newRemaining) }
            }
            TimerStatus.READY, TimerStatus.COMPLETED -> {
                val base = if (current.status == TimerStatus.COMPLETED) current.configuredDurationMs else current.remainingMs
                val newDuration = (base - delta)
                    .coerceAtLeast(CountdownTimerUiState.MIN_DURATION_MS)
                _uiState.value = CountdownTimerUiState(
                    configuredDurationMs = newDuration,
                    remainingMs = newDuration,
                    status = TimerStatus.READY
                )
            }
        }
        persistState()
    }

    private fun startTicker() {
        stopTicker()
        tickerJob = viewModelScope.launch {
            while (isActive && _uiState.value.status == TimerStatus.RUNNING) {
                val remaining = (deadlineElapsedRealtimeMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                if (remaining <= 0L) {
                    completeTimer(triggerAlert = true)
                    break
                } else {
                    _uiState.update { it.copy(remainingMs = remaining) }
                    delay(200L)
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun completeTimer(triggerAlert: Boolean) {
        stopTicker()
        deadlineElapsedRealtimeMs = 0L
        _uiState.update {
            it.copy(
                remainingMs = 0L,
                status = TimerStatus.COMPLETED
            )
        }
        persistState()
        if (triggerAlert) {
            playGentleCompletionAlert()
        }
    }

    /**
     * Plays a short, gentle local alert sound and subtle haptic pulse compatible with Android 9 (API 28+).
     * Requires no network or notification permissions.
     */
    private fun playGentleCompletionAlert() {
        val appContext = getApplication<Application>()

        // 1. Subtle haptic pulse (Android 9 API 28+ VibrationEffect)
        try {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                val effect = VibrationEffect.createWaveform(longArrayOf(0L, 180L, 140L, 240L), -1)
                vibrator.vibrate(effect)
            }
        } catch (_: Exception) {
            // Safely ignore if device lacks vibrator hardware or restricts haptics
        }

        // 2. Short, gentle local tone via ToneGenerator
        viewModelScope.launch {
            var toneGenerator: ToneGenerator? = null
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 65)
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 450)
                delay(550L)
            } catch (_: Exception) {
                // Safely ignore audio output errors on muted/restricted devices
            } finally {
                try {
                    toneGenerator?.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun persistState() {
        val state = _uiState.value
        val wallClockDeadline = if (state.status == TimerStatus.RUNNING) {
            val remaining = (deadlineElapsedRealtimeMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
            System.currentTimeMillis() + remaining
        } else {
            0L
        }

        prefs.edit()
            .putLong(KEY_CONFIGURED_MS, state.configuredDurationMs)
            .putLong(KEY_REMAINING_MS, state.remainingMs)
            .putString(KEY_STATUS, state.status.name)
            .putLong(KEY_DEADLINE_ELAPSED_MS, deadlineElapsedRealtimeMs)
            .putLong(KEY_DEADLINE_WALL_MS, wallClockDeadline)
            .apply()
    }

    private fun restoreSessionIfAvailable() {
        try {
            val configuredMs = prefs.getLong(KEY_CONFIGURED_MS, CountdownTimerUiState.DEFAULT_DURATION_MS)
                .coerceIn(CountdownTimerUiState.MIN_DURATION_MS, CountdownTimerUiState.MAX_DURATION_MS)
            val savedRemainingMs = prefs.getLong(KEY_REMAINING_MS, configuredMs)
                .coerceIn(0L, CountdownTimerUiState.MAX_DURATION_MS)
            val statusName = prefs.getString(KEY_STATUS, TimerStatus.READY.name) ?: TimerStatus.READY.name
            val savedStatus = try {
                TimerStatus.valueOf(statusName)
            } catch (_: IllegalArgumentException) {
                TimerStatus.READY
            }

            when (savedStatus) {
                TimerStatus.RUNNING -> {
                    val savedWallDeadline = prefs.getLong(KEY_DEADLINE_WALL_MS, 0L)
                    val remainingFromWall = (savedWallDeadline - System.currentTimeMillis()).coerceAtLeast(0L)
                    if (remainingFromWall <= 0L) {
                        _uiState.value = CountdownTimerUiState(
                            configuredDurationMs = configuredMs,
                            remainingMs = 0L,
                            status = TimerStatus.COMPLETED
                        )
                        persistState()
                    } else {
                        val clampedRemaining = remainingFromWall.coerceAtMost(configuredMs)
                        deadlineElapsedRealtimeMs = SystemClock.elapsedRealtime() + clampedRemaining
                        _uiState.value = CountdownTimerUiState(
                            configuredDurationMs = configuredMs,
                            remainingMs = clampedRemaining,
                            status = TimerStatus.RUNNING
                        )
                        startTicker()
                    }
                }
                TimerStatus.PAUSED -> {
                    val validRemaining = if (savedRemainingMs <= 0L) configuredMs else savedRemainingMs
                    _uiState.value = CountdownTimerUiState(
                        configuredDurationMs = configuredMs,
                        remainingMs = validRemaining,
                        status = TimerStatus.PAUSED
                    )
                }
                TimerStatus.COMPLETED -> {
                    _uiState.value = CountdownTimerUiState(
                        configuredDurationMs = configuredMs,
                        remainingMs = 0L,
                        status = TimerStatus.COMPLETED
                    )
                }
                TimerStatus.READY -> {
                    _uiState.value = CountdownTimerUiState(
                        configuredDurationMs = configuredMs,
                        remainingMs = configuredMs,
                        status = TimerStatus.READY
                    )
                }
            }
        } catch (_: Exception) {
            _uiState.value = CountdownTimerUiState()
        }
    }

    override fun onCleared() {
        persistState()
        stopTicker()
        super.onCleared()
    }

    companion object {
        private const val PREFS_NAME = "still_countdown_timer_prefs"
        private const val KEY_CONFIGURED_MS = "configured_ms"
        private const val KEY_REMAINING_MS = "remaining_ms"
        private const val KEY_STATUS = "status"
        private const val KEY_DEADLINE_ELAPSED_MS = "deadline_elapsed_ms"
        private const val KEY_DEADLINE_WALL_MS = "deadline_wall_ms"
    }
}
