package com.todo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val date: String,
    val totalCount: Int,
    val completedCount: Int
)
