package com.kisanseva.ai.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kisanseva.ai.data.remote.websocket.WebSocketController
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MessageQueueWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val webSocketController: WebSocketController
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val success = webSocketController.flushQueue()
            if (success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
