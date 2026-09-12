package com.todo.util

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateUtils {
    /**
     * 一天的起点：**凌晨 4 点**。
     *
     * 0:00–3:59 之间的一切都算作**前一天**（熬夜接着做事，不该在午夜被硬切成两天）。
     * 这里只改"哪一天"的判定，不改任何存储格式：数据库里仍然是 `yyyy-MM-dd`，
     * 于是所有按日期字符串比较的地方（统计窗口、清理阈值、历史列表）自动跟着一起变。
     */
    const val DAY_START_HOUR = 4

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_PATTERN)

    fun today(): String = logicalDate(LocalDateTime.now()).format(dateFormatter)

    fun daysAgo(days: Int): String {
        return logicalDate(LocalDateTime.now()).minusDays(days.toLong()).format(dateFormatter)
    }

    /** 某个日期往前 [days] 天。用于"以某一天为基准"的窗口计算，避免依赖"此刻"。 */
    fun daysBefore(dateStr: String, days: Int): String =
        LocalDate.parse(dateStr, dateFormatter).minusDays(days.toLong()).format(dateFormatter)

    /** 距离下一个"逻辑日"起点（下一个凌晨 4 点）还有多少毫秒。 */
    fun millisUntilNextDayStart(): Long {
        val now = LocalDateTime.now()
        val todayStart = now.toLocalDate().atTime(DAY_START_HOUR, 0)
        val next = if (todayStart.isAfter(now)) todayStart else todayStart.plusDays(1)
        return Duration.between(now, next).toMillis()
    }

    private fun logicalDate(now: LocalDateTime): LocalDate =
        if (now.hour < DAY_START_HOUR) now.toLocalDate().minusDays(1) else now.toLocalDate()

    fun formatDisplayDate(dateStr: String): String {
        val date = LocalDate.parse(dateStr, dateFormatter)
        val weekDay = getChineseDayOfWeek(dateStr)
        return "${date.year}年${date.monthValue}月${date.dayOfMonth}日 $weekDay"
    }

    fun formatShortDate(dateStr: String): String {
        val date = LocalDate.parse(dateStr, dateFormatter)
        return "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
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
