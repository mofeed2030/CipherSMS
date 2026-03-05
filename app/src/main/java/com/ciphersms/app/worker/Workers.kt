package com.ciphersms.app.worker

import android.content.Context
import android.telephony.SmsManager
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ScheduledMessageWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val address = inputData.getString(KEY_ADDRESS) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return Result.failure()

        return try {
            val smsManager = applicationContext.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(body)
            smsManager.sendMultipartTextMessage(address, null, parts, null, null)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_ADDRESS = "address"
        const val KEY_BODY = "body"
        const val KEY_MESSAGE_ID = "message_id"

        fun schedule(
            context: Context,
            messageId: Long,
            address: String,
            body: String,
            delayMillis: Long
        ): String {
            val data = workDataOf(
                KEY_ADDRESS to address,
                KEY_BODY to body,
                KEY_MESSAGE_ID to messageId
            )

            val workRequest = OneTimeWorkRequestBuilder<ScheduledMessageWorker>()
                .setInputData(data)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .addTag("scheduled_message_$messageId")
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            return workRequest.id.toString()
        }

        fun cancel(context: Context, messageId: Long) {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag("scheduled_message_$messageId")
        }
    }
}

@HiltWorker
class ExpiredMessagesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Clean up expired disappearing messages
        return Result.success()
    }

    companion object {
        fun schedulePeriodicCleanup(context: Context) {
            val request = PeriodicWorkRequestBuilder<ExpiredMessagesWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "expired_messages_cleanup",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
