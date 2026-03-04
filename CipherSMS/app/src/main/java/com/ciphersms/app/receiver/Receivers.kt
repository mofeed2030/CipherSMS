package com.ciphersms.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION ||
            intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val sender = messages[0].originatingAddress ?: return
            val body = messages.joinToString("") { it.messageBody }

            // Notify via notification service
            NotificationHelper.showMessageNotification(context, sender, body)
        }
    }
}

@AndroidEntryPoint
class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Handle incoming MMS
        if (intent.action == Telephony.Mms.Intents.CONTENT_CHANGED_ACTION) {
            // Process MMS
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule any pending messages after reboot
        }
    }
}

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_REPLY -> {
                val reply = intent.getStringExtra(EXTRA_REPLY) ?: return
                val address = intent.getStringExtra(EXTRA_ADDRESS) ?: return
                // Send reply
            }
            ACTION_MARK_READ -> {
                val threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1L)
                if (threadId != -1L) {
                    // Mark as read
                }
            }
        }
    }

    companion object {
        const val ACTION_REPLY = "com.ciphersms.ACTION_REPLY"
        const val ACTION_MARK_READ = "com.ciphersms.ACTION_MARK_READ"
        const val EXTRA_REPLY = "extra_reply"
        const val EXTRA_ADDRESS = "extra_address"
        const val EXTRA_THREAD_ID = "extra_thread_id"
    }
}
