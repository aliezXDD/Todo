package com.todo.domain.usecase

import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.repository.StatsRepository
import com.todo.data.repository.TodoRepository
import javax.inject.Inject

/**
 * 重算某一天（或若干天）的完成情况快照。
 *
 * 新增、删除、勾选待办以及回收站恢复都会改变当天数据，原来这四处各写了一份同样的逻辑，
 * 现统一收敛到这里，保证口径一致（总数/完成数取自同一天的待办快照）。
 */
class RefreshDailyStatsUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(date: String) {
        val todos = todoRepository.getTodosByDateSnapshot(date)
        statsRepository.insert(
            DailyStatsEntity(
                date = date,
                totalCount = todos.size,
                completedCount = todos.count { it.isCompleted }
            )
        )
    }

    suspend operator fun invoke(dates: List<String>) {
        dates.forEach { invoke(it) }
    }
}
