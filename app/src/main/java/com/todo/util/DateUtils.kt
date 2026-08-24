package com.todo.util

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_PATTERN)

    fun today(): String {
        return LocalDate.now().format(dateFormatter)
    }

    fun daysAgo(days: Int): String {
        return LocalDate.now().minusDays(days.toLong()).format(dateFormatter)
    }

    fun formatDisplayDate(dateStr: String): String {
        val date = LocalDate.parse(dateStr, dateFormatter)
        val weekDay = getChineseDayOfWeek(dateStr)
        return "${date.year}年${date.monthValue}月${date.dayOfMonth}日 $weekDay"
    }

    fun formatShortDate(dateStr: String): String {
        val date = LocalDate.parse(dateStr, dateFormatter)
        return "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
    }

    fun getRecentDates(days: Int): List<String> {
        return (1..days).map { offset ->
            LocalDate.now().minusDays(offset.toLong()).format(dateFormatter)
        }
    }

    fun calculateDelayUntilMidnight(): Long {
        val now = LocalDateTime.now()
        val nextMidnight = LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.MIDNIGHT)
        return Duration.between(now, nextMidnight).toMillis()
    }

    fun getChineseDayOfWeek(dateStr: String): String {
        val date = LocalDate.parse(dateStr, dateFormatter)
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
        }
    }
}
