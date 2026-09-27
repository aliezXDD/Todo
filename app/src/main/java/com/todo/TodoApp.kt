package com.todo

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.todo.data.local.datastore.ArchivePreferences
import com.todo.domain.usecase.ArchiveUseCase
import com.todo.domain.usecase.CleanupUseCase
import com.todo.util.Constants
import com.todo.util.CurrentDay
import com.todo.util.DateUtils
import com.todo.worker.DailyArchiveWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltAndroidApp
class TodoApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var archiveUseCase: ArchiveUseCase

    @Inject
    lateinit var cleanupUseCase: CleanupUseCase

    @Inject
    lateinit var archivePreferences: ArchivePreferences

    @Inject
    lateinit var currentDay: CurrentDay

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDailyArchive()
        runStartupArchiveIfNeeded()
        settleOnDayChange()
    }

    private fun scheduleDailyArchive() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val dailyRequest = PeriodicWorkRequestBuilder<DailyArchiveWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(DateUtils.millisUntilNextDayStart(), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            Constants.DAILY_ARCHIVE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyRequest
        )
    }

    private fun runStartupArchiveIfNeeded() {
        appScope.launch {
            val today = DateUtils.today()
            val lastArchiveDate = archivePreferences.lastArchiveDateFlow.firstOrNull()
            if (lastArchiveDate != today) {
                runCatching {
                    archiveUseCase()
                    cleanupUseCase()
                    archivePreferences.setLastArchiveDate(today)
                }
            }
        }
    }

    /**
     * 逻辑日翻页时**立刻**把每日维护跑一遍（进程还活着的情况）。
     *
     * 为什么必须有这一条：跨天该做的事（尤其是"到期的预留待办按未完成计入截止日那一天"）原先只挂在
     * 启动那一趟与每日定时任务上。进程一直开着跨过凌晨 4 点时，界面已经换到新的一天，定时任务却可能
     * 被 WorkManager 推迟（锁屏、省电、后台限制），于是统计页里截止日那天的数字会一直停在旧值
     *（今日清单与往日记录是实时算的，不受影响，所以只表现为"图表里那天的柱子没跟上"）。
     *
     * 与另外两个触发点同一套动作（归档 → 清理），安全性由它们自己保证：[ArchiveUseCase] 有
     * "每个逻辑日只跑一次"的门控（重复调用是空转），[CleanupUseCase] 是单例 + 互斥锁 + 读写在同一个事务里，
     * 所以和启动那一趟、定时任务撞上也不会重复干活。
     */
    private fun settleOnDayChange() {
        appScope.launch {
            var lastDay = currentDay.day.value
            currentDay.day.collect { day ->
                if (day != lastDay) {
                    lastDay = day
                    runCatching {
                        archiveUseCase()
                        cleanupUseCase()
                    }
                }
            }
        }
    }
}
