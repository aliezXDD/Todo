package com.todo.domain.usecase

import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.RecycleBinRepository
import com.todo.data.repository.StatsRepository
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.RecycleBinItem
import com.todo.util.DateUtils
import javax.inject.Inject

class RestoreRecycleBinUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val recycleBinRepository: RecycleBinRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(items: List<RecycleBinItem>) {
        if (items.isEmpty()) return

        val today = DateUtils.today()
        val cutoff = DateUtils.daysAgo(7)

        val orderedItems = items
            .sortedWith(compareBy<RecycleBinItem> { it.originalDate }.thenBy { it.deletedAt }.thenBy { it.id })

        val restoreDates = orderedItems.map { item ->
            if (item.originalDate < cutoff) today else item.originalDate
        }.distinct()

        orderedItems.forEach { item ->
            val restoreDate = if (item.originalDate < cutoff) today else item.originalDate
            val sortOrder = todoRepository.getNextSortOrder(restoreDate)
            todoRepository.insert(
                TodoEntity(
                    content = item.content,
                    isCompleted = item.wasCompleted,
                    date = restoreDate,
                    sortOrder = sortOrder
                )
            )
        }

        recycleBinRepository.deleteByIds(orderedItems.map { it.id })
        refreshStats(restoreDates)
    }

    private suspend fun refreshStats(dates: List<String>) {
        dates.forEach { date ->
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
}
