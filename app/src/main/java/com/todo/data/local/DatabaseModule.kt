package com.todo.data.local

import android.content.Context
import androidx.room.Room
import com.todo.data.local.dao.DailyStatsDao
import com.todo.data.local.dao.PresetDao
import com.todo.data.local.dao.RecycleBinDao
import com.todo.data.local.dao.TodoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "todo_database"
        ).build()
    }

    @Provides
    fun provideTodoDao(db: AppDatabase): TodoDao = db.todoDao()

    @Provides
    fun providePresetDao(db: AppDatabase): PresetDao = db.presetDao()

    @Provides
    fun provideRecycleBinDao(db: AppDatabase): RecycleBinDao = db.recycleBinDao()

    @Provides
    fun provideDailyStatsDao(db: AppDatabase): DailyStatsDao = db.dailyStatsDao()
}
