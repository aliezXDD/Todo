package com.todo.domain.usecase

import androidx.room.withTransaction
import com.todo.data.local.AppDatabase
import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.repository.StatsRepository
import com.todo.data.repository.TodoRepository
import javax.inject.Inject

/**
 * 重算某一天（或若干天）的完成情况快照。
 *
 * 新增、删除、勾选待办以及回收站恢复都会改变当天数据，原来这四处各写了一份同样的逻辑，
 * 现统一收敛到这里，保证口径一致（总数/完成数取自同一天的待办快照）。
 *
 * **读与写在同一个事务里**：先数一遍当天的待办、再写一行统计，如果中间插进另一次写入
 * （例如手速很快地连点两个勾选），就可能数到"只完成了一部分"的中间状态，把偏小的数字写进统计。
 * 而这个偏小的数字**不会被修正**（归档逻辑刻意只补缺失的日子，不覆盖已有记录——超过 7 天的待办
 * 会被清理掉，那时重算反而会算成 0），而"累计完成数/累计全部完成/平均完成率"都是全历史口径，
 * 于是这个错值会被永久累计进去。放进事务后，数出来的与写进去的必然是同一批数据。
 */
class RefreshDailyStatsUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val statsRepository: StatsRepository,
    private val database: AppDatabase
) {
    suspend operator fun invoke(date: String) {
        database.withTransaction {
            val todos = todoRepository.getTodosByDateSnapshot(date)
            statsRepository.insert(
                DailyStatsEntity(
                    date = date,
                    totalCount = todos.size,
                    completedCount = todos.count { it.isCompleted }
                )
            )
        }
    }

    suspend operator fun invoke(dates: List<String>) {
        dates.forEach { invoke(it) }
    }
}
