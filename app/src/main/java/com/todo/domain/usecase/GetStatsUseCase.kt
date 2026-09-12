package com.todo.domain.usecase

import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.repository.StatsRepository
import com.todo.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 全部历史的每日完成快照（按日期升序）。
 *
 * 这里**不做时间窗裁剪**：调用方各取所需——图表和平均完成率只看最近 30 天，
 * 而「累计完成 / 累计全部完成」要的是真正的历史总量。
 */
class GetStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    operator fun invoke(): Flow<List<DailyStats>> =
        statsRepository.getAllStats().map { entities ->
            entities.map(DailyStatsEntity::toDomain)
        }
}

private fun DailyStatsEntity.toDomain(): DailyStats = DailyStats(
    date = date,
    totalCount = totalCount,
    completedCount = completedCount
)
