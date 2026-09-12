package com.todo.domain.usecase

import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import com.todo.util.DateUtils
import javax.inject.Inject

class AddTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val refreshDailyStats: RefreshDailyStatsUseCase
) {
    suspend operator fun invoke(content: String): Long {
        val today = DateUtils.today()
        // 取号与插入在 DAO 里同一个事务完成：分开写会在并发添加时产生重复 sortOrder。
        // 这里传 0 只是占位，真正的 sortOrder 由 insertAtEnd 在事务里算出来覆盖掉
        val id = todoRepository.insertAtEnd(
            TodoEntity(content = content, date = today, sortOrder = 0)
        )
        refreshDailyStats(today)
        return id
    }
}
