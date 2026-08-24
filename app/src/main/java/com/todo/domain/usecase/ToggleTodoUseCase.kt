package com.todo.domain.usecase

import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.repository.StatsRepository
import com.todo.data.repository.TodoRepository
import com.todo.util.DateUtils
import javax.inject.Inject

class ToggleTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(id: Long, isCompleted: Boolean) {
        todoRepository.updateCompleted(id, isCompleted)
        refreshTodayStats()
    }

    private suspend fun refreshTodayStats() {
        val today = DateUtils.today()
        val todos = todoRepository.getTodosByDateSnapshot(today)
        statsRepository.insert(
            DailyStatsEntity(
                date = today,
                totalCount = todos.size,
                completedCount = todos.count { it.isCompleted }
            )
        )
    }
}
