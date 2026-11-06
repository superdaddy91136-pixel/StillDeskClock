package com.still.deskclock

import com.still.deskclock.util.BurnInProtection
import com.still.deskclock.util.TimeFormatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class TimeFormattersTest {

    @Test
    fun formatClockTime_matches24HourReferenceFormat() {
        assertEquals("02 43", TimeFormatters.formatClockTime(2, 43))
        assertEquals("00 00", TimeFormatters.formatClockTime(0, 0))
        assertEquals("23 59", TimeFormatters.formatClockTime(23, 59))
    }

    @Test
    fun formatDateAndWeekday_matchesReferenceFormat() {
        val sampleDate = LocalDate.of(2026, 10, 24)
        assertEquals("24/10/26", TimeFormatters.formatDate(sampleDate))
        assertEquals("Saturday", TimeFormatters.formatWeekday(sampleDate, Locale.ENGLISH))
    }

    @Test
    fun formatCountdown_handlesMinutesAndHoursCorrectly() {
        assertEquals("30:00", TimeFormatters.formatCountdown(30 * 60 * 1000L))
        assertEquals("01:00:00", TimeFormatters.formatCountdown(60 * 60 * 1000L))
        assertEquals("01:30:00", TimeFormatters.formatCountdown(90 * 60 * 1000L))
        assertEquals("00:01", TimeFormatters.formatCountdown(150L))
        assertEquals("00:00", TimeFormatters.formatCountdown(0L))
        assertEquals("00:00", TimeFormatters.formatCountdown(-500L))
    }

    @Test
    fun getCountdownParts_under60Minutes_returnsTwoBoxes() {
        val parts45 = TimeFormatters.getCountdownParts(
            remainingMs = 45 * 60 * 1000L,
            configuredDurationMs = 45 * 60 * 1000L
        )
        assertEquals(listOf("45", "00"), parts45)
    }

    @Test
    fun getCountdownParts_sixtyMinutesOrLonger_returnsThreeBoxes() {
        val parts60 = TimeFormatters.getCountdownParts(
            remainingMs = 60 * 60 * 1000L,
            configuredDurationMs = 60 * 60 * 1000L
        )
        assertEquals(listOf("01", "00", "00"), parts60)

        val parts61 = TimeFormatters.getCountdownParts(
            remainingMs = 61 * 60 * 1000L,
            configuredDurationMs = 61 * 60 * 1000L
        )
        assertEquals(listOf("01", "01", "00"), parts61)

        val parts90 = TimeFormatters.getCountdownParts(
            remainingMs = 90 * 60 * 1000L,
            configuredDurationMs = 90 * 60 * 1000L
        )
        assertEquals(listOf("01", "30", "00"), parts90)

        val parts120 = TimeFormatters.getCountdownParts(
            remainingMs = 120 * 60 * 1000L,
            configuredDurationMs = 120 * 60 * 1000L
        )
        assertEquals(listOf("02", "00", "00"), parts120)
    }

    @Test
    fun getCountdownParts_preservesThreeBoxesThroughoutCountdown() {
        val cfg60 = 60 * 60 * 1000L
        val partsMidway = TimeFormatters.getCountdownParts(
            remainingMs = 30 * 60 * 1000L,
            configuredDurationMs = cfg60
        )
        assertEquals(listOf("00", "30", "00"), partsMidway)

        val partsEnd = TimeFormatters.getCountdownParts(
            remainingMs = 0L,
            configuredDurationMs = cfg60
        )
        assertEquals(listOf("00", "00", "00"), partsEnd)
    }

    @Test
    fun formatStopwatch_handlesHundredthsAndHours() {
        assertEquals("00:12.48", TimeFormatters.formatStopwatch(12_480L, includeHundredths = true))
        assertEquals("01:00:00.00", TimeFormatters.formatStopwatch(3_600_000L, includeHundredths = true))
    }

    @Test
    fun getStopwatchParts_handlesHoursAndMinutes() {
        assertEquals(listOf("15", "00"), TimeFormatters.getStopwatchParts(15 * 60 * 1000L))
        assertEquals(listOf("01", "00", "00"), TimeFormatters.getStopwatchParts(60 * 60 * 1000L))
        assertEquals(listOf("01", "30", "00"), TimeFormatters.getStopwatchParts(90 * 60 * 1000L))
    }

    @Test
    fun burnInProtection_staysWithinBoundedOffsets() {
        for (step in 0..20) {
            val offset = BurnInProtection.offsetForStep(step)
            assertTrue(offset.x.value in -5f..5f)
            assertTrue(offset.y.value in -5f..5f)
        }
    }
}
