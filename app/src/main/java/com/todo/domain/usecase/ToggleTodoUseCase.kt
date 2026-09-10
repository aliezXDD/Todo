package com.todo.domain.usecase

import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.repository.StatsRepository
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import javax.inject.Inject

class ToggleTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(todo: Todo, isCompleted: Boolean) {
        todoRepository.updateCompleted(todo.id, isCompleted)
        refreshStats(todo.date)
    }

    private suspend fun refreshStats(date: String) {
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
