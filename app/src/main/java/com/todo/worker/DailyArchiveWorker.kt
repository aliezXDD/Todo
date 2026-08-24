package com.todo.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.todo.domain.usecase.ArchiveUseCase
import com.todo.domain.usecase.CleanupUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyArchiveWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val archiveUseCase: ArchiveUseCase,
    private val cleanupUseCase: CleanupUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            archiveUseCase()
            cleanupUseCase()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
