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
}
