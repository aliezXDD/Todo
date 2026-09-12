package com.todo.domain.usecase

import androidx.room.withTransaction
import com.todo.data.local.AppDatabase
import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.RecycleBinRepository
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.RecycleBinItem
import com.todo.util.DateUtils
import javax.inject.Inject

class RestoreRecycleBinUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val recycleBinRepository: RecycleBinRepository,
    private val refreshDailyStats: RefreshDailyStatsUseCase,
    private val database: AppDatabase
) {
    suspend operator fun invoke(items: List<RecycleBinItem>) {
        if (items.isEmpty()) return

        val today = DateUtils.today()
        val cutoff = DateUtils.daysAgo(7)

        val orderedItems = items
            .sortedWith(compareBy<RecycleBinItem> { it.originalDate }.thenBy { it.deletedAt }.thenBy { it.id })

        val restoreDates = orderedItems.map { item ->
            if (item.originalDate < cutoff) today else item.originalDate
        }.distinct()

        database.withTransaction {
            orderedItems.forEach { item ->
                val restoreDate = if (item.originalDate < cutoff) today else item.originalDate
                // 取号与插入同一个事务（嵌套在外层事务内）：按还原顺序追加到该日末尾。
                // sortOrder 传 0 只是占位，真值由 DAO 在事务里算出来覆盖
                todoRepository.insertAtEnd(
                    TodoEntity(
                        content = item.content,
                        isCompleted = item.wasCompleted,
                        date = restoreDate,
                        sortOrder = 0
                    )
                )
            }

            recycleBinRepository.deleteByIds(orderedItems.map { it.id })
        }
        refreshDailyStats(restoreDates)
    }
}
