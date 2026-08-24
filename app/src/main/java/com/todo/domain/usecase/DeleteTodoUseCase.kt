package com.todo.domain.usecase

import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.local.entity.RecycleBinEntity
import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.RecycleBinRepository
import com.todo.data.repository.StatsRepository
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import javax.inject.Inject

class DeleteTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val recycleBinRepository: RecycleBinRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(todo: Todo) {
        todoRepository.delete(todo.toEntity())
        recycleBinRepository.insert(
            RecycleBinEntity(
                originalDate = todo.date,
                content = todo.content,
                wasCompleted = todo.isCompleted
            )
        )
        refreshTodayStatsIfNeeded(todo.date)
    }

    private suspend fun refreshTodayStatsIfNeeded(date: String) {
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

private fun Todo.toEntity(): TodoEntity = TodoEntity(
    id = id,
    content = content,
    isCompleted = isCompleted,
    date = date,
    sortOrder = sortOrder,
    createdAt = createdAt
)
