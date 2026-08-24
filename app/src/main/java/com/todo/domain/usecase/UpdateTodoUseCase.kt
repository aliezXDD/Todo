package com.todo.domain.usecase

import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import javax.inject.Inject

class UpdateTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(todo: Todo) {
        todoRepository.updateContent(todo.id, todo.content)
    }
}
