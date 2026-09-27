package com.todo.domain.usecase

import com.todo.data.local.entity.RecycleBinEntity
import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.RecycleBinRepository
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import com.todo.domain.model.isDeferred
import com.todo.util.DateUtils
import javax.inject.Inject

class DeleteTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val recycleBinRepository: RecycleBinRepository,
    private val refreshDailyStats: RefreshDailyStatsUseCase
) {
    suspend operator fun invoke(todo: Todo) {
        val today = DateUtils.today()
        todoRepository.delete(todo.toEntity())
        recycleBinRepository.insert(
            RecycleBinEntity(
                // 预留中的待办，归属日是**将来**的截止日；照抄进回收站会让"还原"把它放回一个未来的日期上
                // ——那种待办既不在今日清单里、也不会进往日记录，等于凭空消失。
                // 回收站里按今天记，还原后就是一条普通的今日待办。
                originalDate = if (todo.isDeferred(today)) today else todo.date,
                content = todo.content,
                wasCompleted = todo.isCompleted
            )
        )
        // 变化前后的统计：预留中的待办本来就不算在任何一天里，传 before/after 只会重算真正计入的那天
        //（这里 after 为 null = 这条已经没了），因此不会给未来的截止日写出一行空统计。
        refreshDailyStats(todo, null)
    }
}

private fun Todo.toEntity(): TodoEntity = TodoEntity(
    id = id,
    content = content,
    isCompleted = isCompleted,
    date = date,
    sortOrder = sortOrder,
    createdAt = createdAt,
    dueDate = dueDate
)
