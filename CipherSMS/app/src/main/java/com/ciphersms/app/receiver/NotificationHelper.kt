package com.ciphersms.app.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ciphersms.app.R
import com.ciphersms.app.presentation.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "cipher_messages"
    private const val CHANNEL_NAME = "Messages"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "CipherSMS message notifications"
            enableVibration(true)
            enableLights(true)
            lightColor = 0xFF00FF41.toInt()
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showMessageNotification(context: Context, sender: String, body: String) {
        createNotificationChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingOpen = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Reply action
        val replyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_REPLY
            putExtra(NotificationActionReceiver.EXTRA_ADDRESS, sender)
        }
        val replyPending = PendingIntent.getBroadcast(
            context, 1, replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        // Mark read action
        val readIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_READ
        }
        val readPending = PendingIntent.getBroadcast(
            context, 2, readIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("◈ $sender")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingOpen)
            .setAutoCancel(true)
            .setColor(0xFF00FF41.toInt())
            .addAction(0, "Reply", replyPending)
            .addAction(0, "Mark Read", readPending)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(sender.hashCode(), notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }
}
