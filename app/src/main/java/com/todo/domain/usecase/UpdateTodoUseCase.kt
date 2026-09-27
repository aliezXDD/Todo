package com.todo.domain.usecase

import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import com.todo.domain.model.isDeferred
import com.todo.util.DateUtils
import javax.inject.Inject

/**
 * 保存编辑：改内容，同时改（或清除）截止日期。
 *
 * 截止日期的语义见 `Todo.isDeferred`，落到"归属哪一天"上是三条规则：
 * - 设/改截止日期（尚未完成）→ 这条待办**预留**到截止日那一天（归属日跟着过去），从此不再计入统计，
 *   也不进往日记录，直到完成或过期；
 * - 清除截止日期 → 原本预留的回到今天，重新作为普通待办计入今天的统计；已有归属的（例如算在
 *   完成那天的）保持不动；
 * - 已完成的待办只把截止日期记下来，归属日不动（它已经算在完成那天了）。
 *
 * [before] 必须是编辑前的那条（ViewModel 手上的 `editingTodo`）：归属日可能变了，
 * 统计要按变化**前后两天**各重算一次，只有拿到旧值才知道该重算哪一天。
 */
class UpdateTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val refreshDailyStats: RefreshDailyStatsUseCase
) {
    suspend operator fun invoke(before: Todo, content: String, dueDate: String?) {
        val today = DateUtils.today()
        val targetDate = when {
            dueDate == null -> if (before.isDeferred(today)) today else before.date
            before.isCompleted -> before.date
            else -> dueDate
        }
        todoRepository.applyEdit(
            id = before.id,
            content = content,
            dueDate = dueDate,
            refileTo = targetDate.takeIf { it != before.date }
        )
        refreshDailyStats(
            before,
            before.copy(content = content, dueDate = dueDate, date = targetDate)
        )
    }
}
