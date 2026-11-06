package com.still.deskclock.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Pure formatting functions for Clock, Countdown Timer, and Stopwatch modes.
 * Compatible with Android 9 (API 28) where java.time is natively available.
 */
object TimeFormatters {

    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.US)

    /**
     * Formats 24-hour clock time as "HH mm" (e.g., "02 43") matching the reference photograph.
     */
    fun formatClockTime(hour24: Int, minute: Int): String {
        val safeHour = hour24.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        return String.format(Locale.US, "%02d %02d", safeHour, safeMinute)
    }

    /**
     * Formats date as "DD/MM/YY" (e.g., "24/10/26").
     */
    fun formatDate(date: LocalDate): String {
        return date.format(DATE_FORMATTER)
    }

    /**
     * Formats full weekday name (e.g., "Saturday").
     */
    fun formatWeekday(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        val raw = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    /**
     * Formats remaining countdown duration in milliseconds.
     * Uses ceiling seconds so "30:00" only steps to "29:59" after a full second has elapsed,
     * and reaches "00:00" strictly when remainingMs <= 0.
     *
     * Under 1 hour: "MM:SS" (e.g., "30:00")
     * 1 hour or longer: "HH:MM:SS" (e.g., "01:30:00")
     */
    fun formatCountdown(remainingMs: Long, configuredDurationMs: Long = -1L): String {
        val clampedMs = remainingMs.coerceAtLeast(0L)
        val totalSeconds = if (clampedMs == 0L) 0L else (clampedMs + 999L) / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        val showHours = if (configuredDurationMs >= 0L) {
            configuredDurationMs >= 3_600_000L
        } else {
            hours > 0L || clampedMs >= 3_600_000L
        }

        return if (showHours) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Formats elapsed stopwatch duration in milliseconds.
     * Under 1 hour: "MM:SS.CC" (e.g., "00:12.48")
     * 1 hour or longer: "HH:MM:SS.CC" (or "HH:MM:SS" if includeHundredths is false)
     */
    fun formatStopwatch(elapsedMs: Long, includeHundredths: Boolean = true): String {
        val clampedMs = elapsedMs.coerceAtLeast(0L)
        val totalSeconds = clampedMs / 1000L
        val hundredths = (clampedMs % 1000L) / 10L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return when {
            hours > 0L && includeHundredths ->
                String.format(Locale.US, "%02d:%02d:%02d.%02d", hours, minutes, seconds, hundredths)
            hours > 0L ->
                String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
            includeHundredths ->
                String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, hundredths)
            else ->
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    data class ClockSnapshot(
        val timeText: String,
        val dateText: String,
        val weekdayText: String,
        val hour: Int,
        val minute: Int,
        val second: Int
    )

    fun currentClockSnapshot(now: LocalDateTime = LocalDateTime.now()): ClockSnapshot {
        val date = now.toLocalDate()
        return ClockSnapshot(
            timeText = formatClockTime(now.hour, now.minute),
            dateText = formatDate(date),
            weekdayText = formatWeekday(date),
            hour = now.hour,
            minute = now.minute,
            second = now.second
        )
    }

    fun getCountdownParts(remainingMs: Long, configuredDurationMs: Long = -1L): List<String> {
        val clampedMs = remainingMs.coerceAtLeast(0L)
        val totalSeconds = if (clampedMs == 0L) 0L else (clampedMs + 999L) / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        val showHours = if (configuredDurationMs >= 0L) {
            configuredDurationMs >= 3_600_000L
        } else {
            hours > 0L || clampedMs >= 3_600_000L
        }

        return if (showHours) {
            listOf(
                String.format(Locale.US, "%02d", hours),
                String.format(Locale.US, "%02d", minutes),
                String.format(Locale.US, "%02d", seconds)
            )
        } else {
            listOf(
                String.format(Locale.US, "%02d", minutes),
                String.format(Locale.US, "%02d", seconds)
            )
        }
    }

    fun getStopwatchParts(elapsedMs: Long): List<String> {
        val clampedMs = elapsedMs.coerceAtLeast(0L)
        val totalSeconds = clampedMs / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return if (hours > 0L) {
            listOf(
                String.format(Locale.US, "%02d", hours),
                String.format(Locale.US, "%02d", minutes),
                String.format(Locale.US, "%02d", seconds)
            )
        } else {
            listOf(
                String.format(Locale.US, "%02d", minutes),
                String.format(Locale.US, "%02d", seconds)
            )
        }
    }
}
