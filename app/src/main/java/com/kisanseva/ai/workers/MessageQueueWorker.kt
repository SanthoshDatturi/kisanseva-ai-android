package com.kisanseva.ai.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kisanseva.ai.domain.repository.ChatRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MessageQueueWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val chatRepository: ChatRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            chatRepository.sendQueuedMessages()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
