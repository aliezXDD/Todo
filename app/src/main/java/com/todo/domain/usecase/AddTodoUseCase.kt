package com.todo.domain.usecase

import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.StatsRepository
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import com.todo.util.DateUtils
import javax.inject.Inject

class AddTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(content: String): Long {
        val today = DateUtils.today()
        val sortOrder = todoRepository.getNextSortOrder(today)
        val entity = TodoEntity(
            content = content,
            date = today,
            sortOrder = sortOrder
        )
        val id = todoRepository.insert(entity)
        refreshTodayStats(today)
        return id
    }

    private suspend fun refreshTodayStats(date: String) {
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
