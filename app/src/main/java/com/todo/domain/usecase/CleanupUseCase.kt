package com.todo.domain.usecase

import androidx.room.withTransaction
import com.todo.data.local.AppDatabase
import com.todo.data.local.entity.RecycleBinEntity
import com.todo.data.repository.RecycleBinRepository
import com.todo.data.repository.TodoRepository
import com.todo.util.DateUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 日常清理：把 7 天前的待办搬进回收站，并清理回收站里超过 30 天的记录。
 *
 * 两处并发保护，缺一不可：
 *
 * 1. **互斥锁**：本用例的触发点有两个——App 启动路径（`TodoApp.runStartupArchiveIfNeeded`）
 *    与每日定时任务（`DailyArchiveWorker`）。午夜时系统会拉起进程跑定时任务，两者可能同时在跑。
 *    锁要求实例是同一个，所以本类必须是 [Singleton]（否则 Hilt 每次注入都会给一把新锁，
 *    等于没锁）。因此这里也**刻意不放任何可变状态**。
 * 2. **读也放进事务**：原先"先把 7 天前的待办读出来（事务外）"再开事务写入。分开写时，
 *    并发的两次运行会读到同一批行、各自往回收站插一遍 → 回收站出现重复条目，
 *    用户还原后又会得到重复待办。放进同一个事务后，读到的行与删除的行必然是同一批。
 */
@Singleton
class CleanupUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val recycleBinRepository: RecycleBinRepository,
    private val database: AppDatabase
) {
    private val mutex = Mutex()

    suspend operator fun invoke() {
        mutex.withLock {
            val todoCutoffDate = DateUtils.daysAgo(7)

            database.withTransaction {
                val oldTodos = todoRepository.getTodosBeforeDate(todoCutoffDate)
                if (oldTodos.isNotEmpty()) {
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
}
