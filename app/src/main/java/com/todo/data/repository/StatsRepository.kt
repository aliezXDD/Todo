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
    /** 全部历史快照。需要时间窗时由调用方裁剪（见 GetStatsUseCase 的说明）。 */
    fun getAllStats(): Flow<List<DailyStatsEntity>> =
        dailyStatsDao.getAllStats().distinctUntilChanged()

    suspend fun insert(stats: DailyStatsEntity) = dailyStatsDao.insert(stats)

    suspend fun getByDate(date: String): DailyStatsEntity? = dailyStatsDao.getByDate(date)
}
