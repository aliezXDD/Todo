package com.todo.domain.usecase

import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.RecycleBinRepository
import com.todo.data.repository.StatsRepository
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.RecycleBinItem
import javax.inject.Inject

class RestoreRecycleBinUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val recycleBinRepository: RecycleBinRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(items: List<RecycleBinItem>) {
        if (items.isEmpty()) return

        val orderedItems = items
            .sortedWith(compareBy<RecycleBinItem> { it.originalDate }.thenBy { it.deletedAt }.thenBy { it.id })

        orderedItems.forEach { item ->
            val sortOrder = todoRepository.getNextSortOrder(item.originalDate)
            todoRepository.insert(
                TodoEntity(
                    content = item.content,
                    isCompleted = item.wasCompleted,
                    date = item.originalDate,
                    sortOrder = sortOrder
                )
            )
        }

        recycleBinRepository.deleteByIds(orderedItems.map { it.id })
        refreshStats(orderedItems.map { it.originalDate }.distinct())
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
