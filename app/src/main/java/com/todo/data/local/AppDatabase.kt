package com.todo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    // 导出 schema 到 app/schemas 并纳入版本控制：这是后续所有结构变更写 Migration 的基线，
    // 不可改为 false，也不要下沉为破坏性迁移（fallbackToDestructiveMigration 会清空用户数据）。
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun presetDao(): PresetDao
    abstract fun recycleBinDao(): RecycleBinDao
    abstract fun dailyStatsDao(): DailyStatsDao

    companion object {
        /**
         * v1 → v2：给待办加"截止日期"。
         *
         * 新增的是**可空列**，老数据与所有既有插入路径都自动是 `NULL` = 普通待办，
         * 升级前后行为完全一致（没有回填、没有破坏性重建）。
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE todos ADD COLUMN dueDate TEXT")
            }
        }
    }
}
