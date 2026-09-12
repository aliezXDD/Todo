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
    createdAt = createdAt
)
