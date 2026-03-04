package com.ciphersms.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager

class CipherHeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val address = intent.getStringExtra("address") ?: return START_NOT_STICKY
            val body = intent.getStringExtra("sms_body") ?: return START_NOT_STICKY

            try {
                val smsManager = getSystemService(SmsManager::class.java)
                val parts = smsManager.divideMessage(body)
                smsManager.sendMultipartTextMessage(address, null, parts, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
