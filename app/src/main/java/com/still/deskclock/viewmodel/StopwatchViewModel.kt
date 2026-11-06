package com.still.deskclock.viewmodel

import android.app.Application
import android.content.Context
import android.os.SystemClock
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

data class StopwatchLap(
    val lapNumber: Int,
    val splitDurationMs: Long,
    val totalElapsedMs: Long
)

data class StopwatchUiState(
    val elapsedMs: Long = 0L,
    val isRunning: Boolean = false,
    val laps: List<StopwatchLap> = emptyList()
)

class StopwatchViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(StopwatchUiState())
    val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()

    // Monotonic start timestamp in SystemClock.elapsedRealtime() for the active run segment
    private var startElapsedRealtimeMs: Long = 0L
    // Total accumulated duration across previously paused segments
    private var accumulatedPausedMs: Long = 0L
    private var tickerJob: Job? = null

    init {
        restoreSessionIfAvailable()
    }

    /**
     * Recalculates elapsed duration accurately when returning to the foreground.
     */
    fun onForegroundSync() {
        if (_uiState.value.isRunning) {
            val totalElapsed = currentTotalElapsedMs()
            _uiState.update { it.copy(elapsedMs = totalElapsed) }
            startTicker()
        }
    }

    fun start() {
        if (_uiState.value.isRunning) return
        startElapsedRealtimeMs = SystemClock.elapsedRealtime()
        _uiState.update { it.copy(isRunning = true) }
        persistState()
        startTicker()
    }

    fun pause() {
        if (!_uiState.value.isRunning) return
        stopTicker()
        accumulatedPausedMs = currentTotalElapsedMs()
        startElapsedRealtimeMs = 0L
        _uiState.update {
            it.copy(
                elapsedMs = accumulatedPausedMs,
                isRunning = false
            )
        }
        persistState()
    }

    fun resume() {
        if (_uiState.value.isRunning) return
        startElapsedRealtimeMs = SystemClock.elapsedRealtime()
        _uiState.update { it.copy(isRunning = true) }
        persistState()
        startTicker()
    }

    fun reset() {
        stopTicker()
        startElapsedRealtimeMs = 0L
        accumulatedPausedMs = 0L
        _uiState.value = StopwatchUiState(
            elapsedMs = 0L,
            isRunning = false,
            laps = emptyList()
        )
        persistState()
    }

    fun recordLap() {
        val current = _uiState.value
        if (!current.isRunning && current.elapsedMs <= 0L) return
        val totalNow = if (current.isRunning) currentTotalElapsedMs() else current.elapsedMs
        val previousLapTotal = current.laps.firstOrNull()?.totalElapsedMs ?: 0L
        val splitMs = (totalNow - previousLapTotal).coerceAtLeast(0L)
        val nextNumber = current.laps.size + 1
        val newLap = StopwatchLap(
            lapNumber = nextNumber,
            splitDurationMs = splitMs,
            totalElapsedMs = totalNow
        )
        // Store newest lap first, capped at 50 laps to stay lightweight
        val updatedLaps = (listOf(newLap) + current.laps).take(MAX_STORED_LAPS)
        _uiState.update {
            it.copy(
                elapsedMs = totalNow,
                laps = updatedLaps
            )
        }
        persistState()
    }

    private fun currentTotalElapsedMs(): Long {
        if (!_uiState.value.isRunning) return accumulatedPausedMs
        val activeSegment = (SystemClock.elapsedRealtime() - startElapsedRealtimeMs).coerceAtLeast(0L)
        return (accumulatedPausedMs + activeSegment).coerceAtLeast(0L)
    }

    private fun startTicker() {
        stopTicker()
        tickerJob = viewModelScope.launch {
            while (isActive && _uiState.value.isRunning) {
                val total = currentTotalElapsedMs()
                _uiState.update { it.copy(elapsedMs = total) }
                // 33ms (~30 fps) updates hundredths smoothly while running without high CPU load
                delay(33L)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun persistState() {
        val state = _uiState.value
        val nowWall = System.currentTimeMillis()
        val currentElapsed = if (state.isRunning) currentTotalElapsedMs() else accumulatedPausedMs
        val serializedLaps = state.laps.joinToString(separator = ";") { lap ->
            "${lap.lapNumber},${lap.splitDurationMs},${lap.totalElapsedMs}"
        }

        prefs.edit()
            .putBoolean(KEY_IS_RUNNING, state.isRunning)
            .putLong(KEY_ACCUMULATED_MS, currentElapsed)
            .putLong(KEY_SAVED_WALL_MS, nowWall)
            .putString(KEY_LAPS, serializedLaps)
            .apply()
    }

    private fun restoreSessionIfAvailable() {
        try {
            val wasRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
            val savedAccumulated = prefs.getLong(KEY_ACCUMULATED_MS, 0L).coerceAtLeast(0L)
            val savedWall = prefs.getLong(KEY_SAVED_WALL_MS, 0L)
            val rawLaps = prefs.getString(KEY_LAPS, "").orEmpty()

            val restoredLaps = if (rawLaps.isBlank()) {
                emptyList()
            } else {
                rawLaps.split(";").mapNotNull { entry ->
                    val parts = entry.split(",")
                    if (parts.size == 3) {
                        val num = parts[0].toIntOrNull()
                        val split = parts[1].toLongOrNull()
                        val total = parts[2].toLongOrNull()
                        if (num != null && split != null && total != null) {
                            StopwatchLap(num, split.coerceAtLeast(0L), total.coerceAtLeast(0L))
                        } else null
                    } else null
                }
            }

            if (wasRunning && savedWall > 0L) {
                val wallDelta = (System.currentTimeMillis() - savedWall).coerceAtLeast(0L)
                accumulatedPausedMs = savedAccumulated + wallDelta
                startElapsedRealtimeMs = SystemClock.elapsedRealtime()
                _uiState.value = StopwatchUiState(
                    elapsedMs = accumulatedPausedMs,
                    isRunning = true,
                    laps = restoredLaps
                )
                startTicker()
            } else {
                accumulatedPausedMs = savedAccumulated
                startElapsedRealtimeMs = 0L
                _uiState.value = StopwatchUiState(
                    elapsedMs = accumulatedPausedMs,
                    isRunning = false,
                    laps = restoredLaps
                )
            }
        } catch (_: Exception) {
            _uiState.value = StopwatchUiState()
        }
    }

    override fun onCleared() {
        persistState()
        stopTicker()
        super.onCleared()
    }

    companion object {
        private const val PREFS_NAME = "still_stopwatch_prefs"
        private const val KEY_IS_RUNNING = "is_running"
        private const val KEY_ACCUMULATED_MS = "accumulated_ms"
        private const val KEY_SAVED_WALL_MS = "saved_wall_ms"
        private const val KEY_LAPS = "laps"
        private const val MAX_STORED_LAPS = 50
    }
}
