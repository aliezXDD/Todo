package com.todo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.todo.data.local.dao.DailyStatsDao
import com.todo.data.local.dao.PresetDao
import com.todo.data.local.dao.RecycleBinDao
import com.todo.data.local.dao.TodoDao
import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.local.entity.PresetEntity
import com.todo.data.local.entity.RecycleBinEntity
import com.todo.data.local.entity.TodoEntity

@Database(
    entities = [
        TodoEntity::class,
        PresetEntity::class,
        RecycleBinEntity::class,
        DailyStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun presetDao(): PresetDao
    abstract fun recycleBinDao(): RecycleBinDao
    abstract fun dailyStatsDao(): DailyStatsDao
}
