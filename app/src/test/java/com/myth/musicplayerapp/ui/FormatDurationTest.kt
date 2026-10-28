package com.myth.musicplayerapp.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatDurationTest {

    @Test
    fun `formats minutes and seconds`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("3:05", formatDuration(185_000))
        assertEquals("59:59", formatDuration(3_599_999))
    }

    @Test
    fun `adds hours for long tracks`() {
        assertEquals("1:02:03", formatDuration(3_723_000))
    }

    @Test
    fun `clamps negative values`() {
        assertEquals("0:00", formatDuration(-5_000))
    }

    @Test
    fun `formats collection running times`() {
        assertEquals("< 1 min", formatTotalDuration(25_000))
        assertEquals("1 min", formatTotalDuration(59_840))
        assertEquals("42 min", formatTotalDuration(42 * 60_000L + 10_000))
        assertEquals("43 min", formatTotalDuration(42 * 60_000L + 30_000))
        assertEquals("2 hr", formatTotalDuration(120 * 60_000L))
        assertEquals("1 hr 5 min", formatTotalDuration(65 * 60_000L))
    }

    @Test
    fun `pluralizes song counts`() {
        assertEquals("1 song", songCount(1))
        assertEquals("0 songs", songCount(0))
        assertEquals("12 songs", songCount(12))
    }
}
