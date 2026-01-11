package com.habitforge.app.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateUtils {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val displayFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    // Convert LocalDate to ISO string (yyyy-MM-dd)
    fun toIsoString(date: LocalDate): String = date.format(isoFormatter)

    // Parse ISO string to LocalDate
    fun fromIsoString(dateStr: String): LocalDate = LocalDate.parse(dateStr, isoFormatter)

    // Format date for display
    fun formatForDisplay(date: LocalDate): String = date.format(displayFormatter)

    // Get today's date as ISO string
    fun today(): String = toIsoString(LocalDate.now())

    // Get yesterday's date as ISO string
    fun yesterday(): String = toIsoString(LocalDate.now().minusDays(1))

    // Check if date string is today
    fun isToday(dateStr: String): Boolean = dateStr == today()

    // Get date range for last N days
    fun getLastNDays(n: Int): List<String> {
        val today = LocalDate.now()
        return (0 until n).map { toIsoString(today.minusDays(it.toLong())) }
    }
}

