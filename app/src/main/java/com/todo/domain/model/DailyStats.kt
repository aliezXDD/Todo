package com.todo.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class DailyStats(
    val date: String,
    val totalCount: Int,
    val completedCount: Int
) {
    val completionRate: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()

    /** 当天有待办且全部完成。空白天不算"全部完成"，否则连续天数会被无待办的日子灌水。 */
    val isFullyCompleted: Boolean
        get() = totalCount > 0 && completedCount == totalCount
}
