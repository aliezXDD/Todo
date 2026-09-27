package com.todo.domain.usecase

import androidx.room.withTransaction
import com.todo.data.local.AppDatabase
import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.repository.StatsRepository
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import com.todo.domain.model.isDeferred
import com.todo.util.DateUtils
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
 *
 * **预留中的待办不算进任何一天**（判定见 [isDeferred]）：它们还没到要算账的时候，
 * 所以这里数出来的总数/完成数会先把它们排除掉 —— 今日进度、平均完成率、连续全部完成、累计全部完成
 * 全都靠这一条统一口径，这也是"设了截止日期的待办在期限前不影响完成统计"的唯一实现点。
 */
class RefreshDailyStatsUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val statsRepository: StatsRepository,
    private val database: AppDatabase
) {
    suspend operator fun invoke(date: String) {
        val today = DateUtils.today()
        database.withTransaction {
            val todos = todoRepository.getTodosByDateSnapshot(date)
            val counted = todos.filterNot { isDeferred(it.dueDate, it.isCompleted, today) }
            statsRepository.insert(
                DailyStatsEntity(
                    date = date,
                    totalCount = counted.size,
                    completedCount = counted.count { it.isCompleted }
                )
            )
        }
    }

    suspend operator fun invoke(dates: List<String>) {
        dates.forEach { invoke(it) }
    }

    /**
     * 一条待办的"算在哪一天 / 算不算数"变了（勾选、改截止日期、删除）时调用：
     * 把变化前后**各自真正会被计入**的那一天各重算一次。
     *
     * 只重算"会被计入"的那些天：预留中的待办不属于任何一天，若跟着它去重算，就会给一个未来的日期
     * 写出一行空统计。两边都会计入且还是同一天时（例如只改了内容）只重算一次。
     * [after] 为 null 表示这条待办已经没了（删除）。
     */
    suspend operator fun invoke(before: Todo?, after: Todo?) {
        val today = DateUtils.today()
        val dates = buildList {
            before?.let { if (!it.isDeferred(today)) add(it.date) }
            after?.let { if (!it.isDeferred(today)) add(it.date) }
        }
        dates.distinct().forEach { invoke(it) }
    }
}
