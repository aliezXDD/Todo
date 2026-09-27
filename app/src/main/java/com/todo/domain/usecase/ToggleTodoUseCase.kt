package com.todo.domain.usecase

import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import com.todo.util.DateUtils
import javax.inject.Inject

/**
 * 勾选 / 取消完成。
 *
 * 没有截止日期的待办走的还是原来那条路：翻一下完成状态、重算它那一天，日期一动不动。
 *
 * 设了截止日期的待办，完成状态一变，它**归属的那一天**也跟着变（见 `Todo.isDeferred`）：
 * - 完成 → 落到"完成的那一天"（就是今天），于是今天的进度立刻算上它，明天它出现在那天的往日记录里；
 * - 取消完成 → 回到"截止日那一天"，重新变成预留（今天同样不受它影响）。
 *
 * 归属日与完成状态在 DAO 里同一个事务改完，免得中间态把它算到错误的一天；
 * 统计则按"变化前后各自会被计入的那一天"重算（见 [RefreshDailyStatsUseCase.invoke] 的重载）。
 */
class ToggleTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val refreshDailyStats: RefreshDailyStatsUseCase
) {
    suspend operator fun invoke(todo: Todo, isCompleted: Boolean) {
        val targetDate = when {
            todo.dueDate == null -> todo.date
            isCompleted -> DateUtils.today()
            else -> todo.dueDate
        }
        todoRepository.setCompleted(
            id = todo.id,
            isCompleted = isCompleted,
            refileTo = targetDate.takeIf { it != todo.date }
        )
        refreshDailyStats(todo, todo.copy(isCompleted = isCompleted, date = targetDate))
    }
}
