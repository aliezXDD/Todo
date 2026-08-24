package com.todo.domain.usecase

import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.repository.StatsRepository
import com.todo.domain.model.DailyStats
import com.todo.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    operator fun invoke(days: Int = 30): Flow<List<DailyStats>> {
        val startDate = DateUtils.daysAgo(days - 1)
        val endDate = DateUtils.today()
        return statsRepository.getStatsBetween(startDate, endDate).map { entities ->
            entities.map(DailyStatsEntity::toDomain)
        }
    }
}

private fun DailyStatsEntity.toDomain(): DailyStats = DailyStats(
    date = date,
    totalCount = totalCount,
    completedCount = completedCount
)
