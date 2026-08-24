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

class GetHistoryRecordsUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(limit: Int = 7): Flow<List<DailyRecord>> {
        val today = DateUtils.today()
        val cutoff = DateUtils.daysAgo(7)
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
