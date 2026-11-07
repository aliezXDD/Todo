package com.todo.domain.usecase

import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.isDeferred
import com.todo.util.DateUtils
import javax.inject.Inject

/**
 * 新建一条待办。
 *
 * [dueDate] 为空 → 落在今天，与以前完全一样（从预设添加的三条路径都走这里）。
 *
 * [dueDate] 非空 → 归属到**截止日那一天**（与「编辑待办」设日期同一套规则：未完成的待办，
 * 归属日恒等于截止日，见 `com.todo.domain.model.isDeferred`）。于是它一创建就是**预留**：
 * 截止日之前不出现在今日清单的完成计数里，到那天才计入。
 *
 * 这种情况下**不写今天的统计**：预留中的待办不属于任何一天，给它写行只会得到一行假的 0/0。
 * 反过来，截止日恰好是今天时它本来就该算账，按 [isDeferred] 判定后照常重算当天。
 */
class AddTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val refreshDailyStats: RefreshDailyStatsUseCase
) {
    suspend operator fun invoke(content: String, dueDate: String? = null): Long {
        val today = DateUtils.today()
        val targetDate = dueDate ?: today
        // 取号与插入在 DAO 里同一个事务完成：分开写会在并发添加时产生重复 sortOrder。
        // 这里传 0 只是占位，真正的 sortOrder 由 insertAtEnd 在事务里算出来覆盖掉
        val id = todoRepository.insertAtEnd(
            TodoEntity(content = content, date = targetDate, sortOrder = 0, dueDate = dueDate)
        )
        if (!isDeferred(dueDate = dueDate, isCompleted = false, today = today)) {
            refreshDailyStats(targetDate)
        }
        return id
    }
}
