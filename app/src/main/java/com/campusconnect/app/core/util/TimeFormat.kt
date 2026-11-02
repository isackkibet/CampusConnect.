package com.campusconnect.app.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeFormat {

    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.getDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val zone: ZoneId = ZoneId.systemDefault()

    fun relative(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
        val diff = (now - epochMillis).coerceAtLeast(0)
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> date(epochMillis)
        }
    }

    fun date(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(dateFormatter)

    fun dateTime(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(dateTimeFormatter)

    fun time(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(timeFormatter)

    fun countdown(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
        val days = (epochMillis - now) / 86_400_000
        return when {
            days < 0 -> "Closed"
            days == 0L -> "Closes today"
            days == 1L -> "1 day left"
            else -> "$days days left"
        }
    }

    fun greeting(now: Long = System.currentTimeMillis()): String {
        val hour = Instant.ofEpochMilli(now).atZone(zone).hour
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
