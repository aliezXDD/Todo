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

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDailyArchive()
        runStartupArchiveIfNeeded()
    }

    private fun scheduleDailyArchive() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val dailyRequest = PeriodicWorkRequestBuilder<DailyArchiveWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(DateUtils.calculateDelayUntilMidnight(), TimeUnit.MILLISECONDS)
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
}
