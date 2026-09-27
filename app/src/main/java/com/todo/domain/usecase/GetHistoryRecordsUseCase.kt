package com.todo.domain.usecase

import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.DailyRecord
import com.todo.domain.model.Todo
import com.todo.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 往日记录：以 [today] 为基准往前取最近若干天。
 *
 * [today] 同样由调用方传入（见 [GetTodayTodosUseCase] 的说明）：原先在用例内部取当天日期，
 * 那个值在流构建时就固定了，跨零点后 7 天窗口会一直停在旧的一天上。
 *
 * **预留中的待办不会出现在这里**，而且不需要额外过滤：它们的归属日（`date`）就是截止日、在将来，
 * 而这里的窗口只取早于 [today] 的日期。它们只在完成（落到完成那天）或过期（落到截止日那天）
 * 之后，才会随那一天的窗口进来。
 */
class GetHistoryRecordsUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(today: String, limit: Int = 7): Flow<List<DailyRecord>> {
        val cutoff = DateUtils.daysBefore(today, 7)
        val dates = todoRepository.getHistoryDates(today)
            .filter { it >= cutoff }
            .take(limit)
        if (dates.isEmpty()) {
            return flowOf(emptyList())
        }
        return todoRepository.getTodosForDates(dates).map { entities ->
            entities
                .groupBy { it.date }
                .toSortedMap(compareByDescending { it })
                .map { (date, todos) ->
                    DailyRecord(
                        date = date,
                        todos = todos.sortedBy { it.sortOrder }.map(TodoEntity::toDomain)
                    )
                }
        }
    }
}

private fun TodoEntity.toDomain(): Todo = Todo(
    id = id,
    content = content,
    isCompleted = isCompleted,
    date = date,
    sortOrder = sortOrder,
    createdAt = createdAt,
    dueDate = dueDate
)
