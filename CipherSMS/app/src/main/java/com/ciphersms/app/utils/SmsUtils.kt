package com.ciphersms.app.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.Telephony

object SmsUtils {
    fun getOrCreateThreadId(context: Context, address: String): Long {
        return try {
            Telephony.Threads.getOrCreateThreadId(context, address)
        } catch (e: Exception) {
            address.hashCode().toLong()
        }
    }

    fun formatPhoneNumber(number: String): String {
        val digits = number.filter { it.isDigit() }
        return when (digits.length) {
            10 -> "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
            11 -> "+${digits[0]} (${digits.substring(1, 4)}) ${digits.substring(4, 7)}-${digits.substring(7)}"
            else -> number
        }
    }
}
