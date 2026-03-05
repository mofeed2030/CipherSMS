package com.ciphersms.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val permissions = arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_CONTACTS
    )

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> loadMessages() }

    private var messages = mutableStateListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) requestPermissions.launch(missing.toTypedArray())
        else loadMessages()

        if (Telephony.Sms.getDefaultSmsPackage(this) != packageName) {
            startActivity(Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            })
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF00FF41),
                background = Color.Black,
                surface = Color(0xFF0D0D0D)
            )) {
                MainScreen(messages) { to, body -> sendSms(to, body) }
            }
        }
    }

    private fun loadMessages() {
        messages.clear()
        val cursor = contentResolver.query(
            android.provider.Telephony.Sms.CONTENT_URI, null, null, null,
            android.provider.Telephony.Sms.DATE + " DESC"
        )
        cursor?.use {
            val addressIdx = it.getColumnIndex(android.provider.Telephony.Sms.ADDRESS)
            val bodyIdx = it.getColumnIndex(android.provider.Telephony.Sms.BODY)
            while (it.moveToNext() && messages.size < 50) {
                val address = it.getString(addressIdx) ?: "Unknown"
                val body = it.getString(bodyIdx) ?: ""
                messages.add(Pair(address, body))
            }
        }
    }

    private fun sendSms(to: String, body: String) {
        android.telephony.SmsManager.getDefault().sendTextMessage(to, null, body, null, null)
        loadMessages()
    }
}

@Composable
fun MainScreen(messages: List<Pair<String, String>>, onSend: (String, String) -> Unit) {
    var showCompose by remember { mutableStateOf(false) }
    var toNumber by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        if (messages.isEmpty()) {
            Text("لا توجد رسائل", color = Color(0xFF00FF41), modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn {
                items(messages) { (from, body) ->
                    Card(modifier = Modifier.fillMaxWidth().padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(from, color = Color(0xFF00FF41))
                            Text(body, color = Color.White, maxLines = 2)
                        }
                    }
                }
            }
        }

        FloatingActionButton(onClick = { showCompose = true },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            containerColor = Color(0xFF00FF41)) {
            Text("+", color = Color.Black)
        }

        if (showCompose) {
            AlertDialog(onDismissRequest = { showCompose = false },
                title = { Text("رسالة جديدة", color = Color(0xFF00FF41)) },
                text = {
                    Column {
                        OutlinedTextField(value = toNumber, onValueChange = { toNumber = it },
                            label = { Text("رقم الهاتف") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = messageBody, onValueChange = { messageBody = it },
                            label = { Text("الرسالة") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(onClick = { onSend(toNumber, messageBody); showCompose = false; toNumber = ""; messageBody = "" }) {
                        Text("إرسال")
                    }
                })
        }
    }
}
