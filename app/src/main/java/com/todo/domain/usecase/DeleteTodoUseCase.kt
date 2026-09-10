package com.todo.domain.usecase

import com.todo.data.local.entity.RecycleBinEntity
import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.RecycleBinRepository
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import javax.inject.Inject

class DeleteTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val recycleBinRepository: RecycleBinRepository,
    private val refreshDailyStats: RefreshDailyStatsUseCase
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
        refreshDailyStats(todo.date)
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
