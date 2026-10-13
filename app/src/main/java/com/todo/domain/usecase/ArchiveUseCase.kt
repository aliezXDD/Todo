package com.todo.domain.usecase

import com.todo.data.local.datastore.ArchivePreferences
import com.todo.data.local.entity.DailyStatsEntity
import com.todo.data.repository.StatsRepository
import com.todo.data.repository.TodoRepository
import com.todo.util.DateUtils
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

/**
 * 跨天归档（启动时、每日定时任务、以及进程内逻辑日翻页时各跑一次，由
 * [ArchivePreferences.lastArchiveDateFlow] 门控，保证每个逻辑日只跑一次）：
 *
 * 1. **补齐缺失的历史统计**：只补当天没有记录的日子，绝不覆盖已有记录。
 *    这条规则不能改成"全部重算"：超过 7 天的待办会被清理掉，那时重算只会把历史数字算成 0。
 * 2. **结清到期的预留待办**：截止日已到（含今天）、仍未完成的，要按**未完成**计入截止日那一天。
 *    那一天的行在当天就写过了（当时它还是预留、被排除在外），所以这一步必须**覆盖重算**，
 *    不能走第 1 步那条"只补缺"的路。
 *    其中"截止日恰好是今天"的那批是这条规则的关键：从今天起它不再是预留，要立刻并进当天的统计，
 *    而这一趟归档正是"跨天"唯一可靠的触发点 —— 不在这里补，今日进度卡已经算了它、统计页却还是 0。
 *
 * 顺序也有讲究：两步都在 [com.todo.domain.usecase.CleanupUseCase] 之前完成（调用方按 archive → cleanup
 * 的顺序执行），否则刚过期的那条待办可能先被搬进回收站，那天的统计就补不上了。
 *
 * 三个触发点都靠上面那道门控去重，所以谁先跑都行、重复跑是空转 —— 这一点对第 2 步尤其重要：
 * 进程一直开着跨过凌晨 4 点时，界面已经换到新的一天，用户会立刻想看"昨天到底算成什么样了"，
 * 不能等定时任务（它可能被 WorkManager 推迟）。
 */
class ArchiveUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val statsRepository: StatsRepository,
    private val refreshDailyStats: RefreshDailyStatsUseCase,
    private val archivePreferences: ArchivePreferences
) {
    suspend operator fun invoke() {
        val today = DateUtils.today()
        val lastArchiveDate = archivePreferences.lastArchiveDateFlow.firstOrNull()

        if (lastArchiveDate == today) return

        // 1) 补齐缺失的历史统计。这些日期都早于今天，而预留待办的归属日（date）就是将来的截止日，
        //    所以这一批里不可能混进预留待办，不需要过滤（口径见 RefreshDailyStatsUseCase）。
        val dates = todoRepository.getHistoryDates(today)
        dates.forEach { date ->
            if (statsRepository.getByDate(date) == null) {
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

        // 2) 结清已到期的预留待办：覆盖重算它们所属（= 截止日）的那一天。
        //    含"截止日就是今天"的那批 —— 它从今天起不再是预留，当天那一行要立刻跟上。
        todoRepository.getDeadlineDatesToSettle(today).forEach { date ->
            refreshDailyStats(date)
        }

        archivePreferences.setLastArchiveDate(today)
    }
}
