package com.todo.data.repository

import com.todo.data.local.dao.DailyStatsDao
import com.todo.data.local.entity.DailyStatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val dailyStatsDao: DailyStatsDao
) {
    fun getRecentStats(limit: Int = 30): Flow<List<DailyStatsEntity>> =
        dailyStatsDao.getRecentStats(limit).distinctUntilChanged()

    fun getStatsBetween(startDate: String, endDate: String): Flow<List<DailyStatsEntity>> =
        dailyStatsDao.getStatsBetween(startDate, endDate).distinctUntilChanged()

    suspend fun insert(stats: DailyStatsEntity) = dailyStatsDao.insert(stats)

    suspend fun getByDate(date: String): DailyStatsEntity? = dailyStatsDao.getByDate(date)
}
