package com.todo.domain.usecase

import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import javax.inject.Inject

class ReorderTodosUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(reorderedList: List<Todo>) {
        reorderedList.forEachIndexed { index, todo ->
            todoRepository.updateSortOrder(todo.id, index)
        }
    }
}
