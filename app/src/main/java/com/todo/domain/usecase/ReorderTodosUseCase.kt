package com.todo.domain.usecase

import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import javax.inject.Inject

class ReorderTodosUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(reorderedList: List<Todo>) {
        // 一次事务写完整组：逐行写会让列表先闪出中间态（见 TodoDao.updateSortOrders 的说明）
        todoRepository.updateSortOrders(reorderedList.map { it.id })
    }
}
