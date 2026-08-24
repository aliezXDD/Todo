package com.todo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.todo.data.local.entity.DailyStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :limit")
    fun getRecentStats(limit: Int = 30): Flow<List<DailyStatsEntity>>

    @Query("SELECT * FROM daily_stats WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getStatsBetween(startDate: String, endDate: String): Flow<List<DailyStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: DailyStatsEntity)

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getByDate(date: String): DailyStatsEntity?
}
