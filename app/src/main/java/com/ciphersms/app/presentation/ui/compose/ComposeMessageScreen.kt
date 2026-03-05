package com.ciphersms.app.presentation.ui.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.ciphersms.app.presentation.ui.theme.CipherColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeMessageScreen(
    initialAddress: String = "",
    onBack: () -> Unit,
    onStartChat: (Long, String) -> Unit
) {
    var recipient by remember { mutableStateOf(initialAddress) }
    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "// NEW_MESSAGE",
                            color = CipherColors.NeonGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, null, tint = CipherColors.NeonGreen)
                        }
                    },
                    actions = {
                        if (recipient.isNotEmpty() && message.isNotEmpty()) {
                            IconButton(onClick = {
                                onStartChat(0L, recipient)
                            }) {
                                Icon(Icons.Filled.Send, null, tint = CipherColors.NeonGreen)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CipherColors.SurfaceBlack
                    )
                )
                Box(
                    Modifier.fillMaxWidth().height(1.dp)
                        .background(Brush.horizontalGradient(
                            listOf(Color.Transparent, CipherColors.NeonGreenFaint, Color.Transparent)
                        ))
                )
            }
        },
        containerColor = CipherColors.DeepBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // To field
            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("To:", color = CipherColors.OnSurfaceDim) },
                placeholder = { Text("> phone_number_or_name...", color = CipherColors.OnSurfaceDim) },
                leadingIcon = {
                    Icon(Icons.Filled.Person, null, tint = CipherColors.NeonGreen)
                },
                trailingIcon = {
                    IconButton(onClick = { /* open contacts picker */ }) {
                        Icon(Icons.Filled.Contacts, null, tint = CipherColors.NeonGreenDim)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CipherColors.NeonGreen,
                    unfocusedBorderColor = CipherColors.BorderBlack,
                    focusedTextColor = CipherColors.OnSurface,
                    unfocusedTextColor = CipherColors.OnSurface,
                    cursorColor = CipherColors.NeonGreen,
                    focusedContainerColor = CipherColors.CardBlack,
                    unfocusedContainerColor = CipherColors.CardBlack
                ),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // Message field
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                label = { Text("Message:", color = CipherColors.OnSurfaceDim) },
                placeholder = { Text("> type_your_message...", color = CipherColors.OnSurfaceDim) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CipherColors.NeonGreen,
                    unfocusedBorderColor = CipherColors.BorderBlack,
                    focusedTextColor = CipherColors.OnSurface,
                    unfocusedTextColor = CipherColors.OnSurface,
                    cursorColor = CipherColors.NeonGreen,
                    focusedContainerColor = CipherColors.CardBlack,
                    unfocusedContainerColor = CipherColors.CardBlack
                ),
                maxLines = 10
            )

            Spacer(Modifier.height(16.dp))

            // Quick action chips
            Text("// QUICK_OPTIONS", color = CipherColors.NeonGreenDim, fontSize = 10.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionChip("Schedule", Icons.Filled.Schedule)
                QuickActionChip("Encrypt", Icons.Filled.Lock)
                QuickActionChip("Location", Icons.Filled.LocationOn)
            }
        }
    }
}

@Composable
private fun QuickActionChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    FilterChip(
        selected = false,
        onClick = {},
        label = { Text(label, color = CipherColors.NeonGreenDim, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, null, tint = CipherColors.NeonGreenDim, modifier = Modifier.size(14.dp)) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = CipherColors.CardBlack,
            selectedContainerColor = CipherColors.NeonGreenTrace
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = CipherColors.BorderBlack
        )
    )
}
