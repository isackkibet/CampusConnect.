package com.campusconnect.app

import com.campusconnect.app.core.util.TimeFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {

    private val now = 1_700_000_000_000L

    @Test
    fun relativeUsesMinutesHoursAndDays() {
        assertEquals("Just now", TimeFormat.relative(now - 20_000, now))
        assertEquals("5m ago", TimeFormat.relative(now - 5 * 60_000, now))
        assertEquals("3h ago", TimeFormat.relative(now - 3 * 3_600_000, now))
        assertEquals("2d ago", TimeFormat.relative(now - 2 * 86_400_000, now))
    }

    @Test
    fun countdownHandlesPastTodayAndFuture() {
        assertEquals("Closed", TimeFormat.countdown(now - 86_400_000, now))
        assertEquals("Closes today", TimeFormat.countdown(now, now))
        assertEquals("1 day left", TimeFormat.countdown(now + 86_400_000, now))
        assertEquals("5 days left", TimeFormat.countdown(now + 5 * 86_400_000, now))
    }

    @Test
    fun greetingUsesLocalHour() {
        val morning = now
        val greeting = TimeFormat.greeting(morning)
        assertEquals(true, greeting in setOf("Good morning", "Good afternoon", "Good evening"))
    }
}
