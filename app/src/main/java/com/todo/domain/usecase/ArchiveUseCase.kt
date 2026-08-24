package com.todo.domain.usecase

import com.todo.data.local.datastore.ArchivePreferences
import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.repository.StatsRepository
import com.todo.data.repository.TodoRepository
import com.todo.util.DateUtils
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

class ArchiveUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val statsRepository: StatsRepository,
    private val archivePreferences: ArchivePreferences
) {
    suspend operator fun invoke() {
        val today = DateUtils.today()
        val lastArchiveDate = archivePreferences.lastArchiveDateFlow.firstOrNull()

        if (lastArchiveDate == today) return

        val dates = todoRepository.getHistoryDates(today)
        dates.forEach { date ->
            if (statsRepository.getByDate(date) == null) {
                val todos = todoRepository.getTodosByDateSnapshot(date)
                statsRepository.insert(
                    DailyStatsEntity(
                        date = date,
                        totalCount = todos.size,
                        completedCount = todos.count { it.isCompleted }
                    )
                )
            }
        }

        archivePreferences.setLastArchiveDate(today)
    }
}
