package com.todo.domain.usecase

import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import com.todo.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTodayTodosUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    operator fun invoke(): Flow<List<Todo>> {
        val today = DateUtils.today()
        return todoRepository.getTodosByDate(today).map { entities ->
            entities.map(TodoEntity::toDomain)
        }
    }
}

private fun TodoEntity.toDomain(): Todo = Todo(
    id = id,
    content = content,
    isCompleted = isCompleted,
    date = date,
    sortOrder = sortOrder,
    createdAt = createdAt
)
