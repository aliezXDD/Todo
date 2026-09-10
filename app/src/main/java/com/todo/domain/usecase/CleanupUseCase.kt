package com.todo.domain.usecase

import androidx.room.withTransaction
import com.todo.data.local.AppDatabase
import com.todo.data.local.entity.RecycleBinEntity
import com.todo.data.repository.RecycleBinRepository
import com.todo.data.repository.TodoRepository
import com.todo.util.DateUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CleanupUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val recycleBinRepository: RecycleBinRepository,
    private val database: AppDatabase
) {
    suspend operator fun invoke() {
        val todoCutoffDate = DateUtils.daysAgo(7)
        val oldTodos = todoRepository.getTodosBeforeDate(todoCutoffDate)

        if (oldTodos.isNotEmpty()) {
            database.withTransaction {
                recycleBinRepository.insertAll(
                    oldTodos.map { todo ->
                        RecycleBinEntity(
                            originalDate = todo.date,
                            content = todo.content,
                            wasCompleted = todo.isCompleted
                        )
                    }
                )
                todoRepository.deleteTodosBeforeDate(todoCutoffDate)
            }
        }

        val recycleCutoffTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        recycleBinRepository.deleteOlderThan(recycleCutoffTimestamp)
    }
}
