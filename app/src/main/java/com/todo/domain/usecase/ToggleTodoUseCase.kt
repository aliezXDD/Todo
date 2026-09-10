package com.todo.domain.usecase

import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import javax.inject.Inject

class ToggleTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val refreshDailyStats: RefreshDailyStatsUseCase
) {
    suspend operator fun invoke(todo: Todo, isCompleted: Boolean) {
        todoRepository.updateCompleted(todo.id, isCompleted)
        refreshDailyStats(todo.date)
    }
}
