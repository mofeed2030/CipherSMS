package com.ciphersms.app.presentation.ui.settings

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.ciphersms.app.presentation.ui.theme.CipherColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "// SETTINGS",
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CipherColors.SurfaceBlack
                    )
                )
                Box(
                    modifier = Modifier.fillMaxWidth().height(1.dp)
                        .background(Brush.horizontalGradient(
                            listOf(Color.Transparent, CipherColors.NeonGreenFaint, Color.Transparent)
                        ))
                )
            }
        },
        containerColor = CipherColors.DeepBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item { SettingsSection("SECURITY") }

            item {
                SettingsTile(
                    icon = Icons.Filled.Fingerprint,
                    title = "App Lock",
                    subtitle = "PIN · Biometric · Pattern",
                    trailingContent = {
                        Switch(
                            checked = false,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CipherColors.Black,
                                checkedTrackColor = CipherColors.NeonGreen
                            )
                        )
                    }
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.Security,
                    title = "End-to-End Encryption",
                    subtitle = "Encrypt RCS messages",
                    trailingContent = {
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CipherColors.Black,
                                checkedTrackColor = CipherColors.NeonGreen
                            )
                        )
                    }
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.VisibilityOff,
                    title = "Screenshot Protection",
                    subtitle = "Block screenshots in conversation",
                    trailingContent = {
                        Switch(
                            checked = false,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CipherColors.Black,
                                checkedTrackColor = CipherColors.NeonGreen
                            )
                        )
                    }
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.Lock,
                    title = "Vault",
                    subtitle = "Configure secret vault PIN"
                )
            }

            item { SettingsSection("APPEARANCE") }

            item {
                SettingsTile(
                    icon = Icons.Filled.Palette,
                    title = "Theme",
                    subtitle = "Cipher Dark (Military)",
                    trailingContent = {
                        Text("◈", color = CipherColors.NeonGreen, fontSize = 18.sp)
                    }
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.FormatSize,
                    title = "Font Size",
                    subtitle = "Medium"
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.Bubble,
                    title = "Bubble Style",
                    subtitle = "Rounded with glow"
                )
            }

            item { SettingsSection("MESSAGING") }

            item {
                SettingsTile(
                    icon = Icons.Filled.Sim,
                    title = "Default SIM",
                    subtitle = "SIM 1 - Primary"
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.Schedule,
                    title = "Auto-Delete Messages",
                    subtitle = "Off",
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.DoneAll,
                    title = "Read Receipts",
                    subtitle = "Send read receipts",
                    trailingContent = {
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CipherColors.Black,
                                checkedTrackColor = CipherColors.NeonGreen
                            )
                        )
                    }
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.TextFields,
                    title = "Typing Indicators",
                    subtitle = "Show when you're typing",
                    trailingContent = {
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CipherColors.Black,
                                checkedTrackColor = CipherColors.NeonGreen
                            )
                        )
                    }
                )
            }

            item { SettingsSection("AI FEATURES") }

            item {
                SettingsTile(
                    icon = Icons.Filled.AutoAwesome,
                    title = "Smart Replies",
                    subtitle = "AI-powered quick responses",
                    trailingContent = {
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CipherColors.Black,
                                checkedTrackColor = CipherColors.NeonGreen
                            )
                        )
                    }
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.Warning,
                    title = "Spam Filter",
                    subtitle = "AI-powered spam detection",
                    trailingContent = {
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CipherColors.Black,
                                checkedTrackColor = CipherColors.NeonGreen
                            )
                        )
                    }
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.Translate,
                    title = "Auto Translate",
                    subtitle = "Translate incoming messages"
                )
            }

            item { SettingsSection("NOTIFICATIONS") }

            item {
                SettingsTile(
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    subtitle = "Manage notification settings"
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.VolumeUp,
                    title = "Notification Sound",
                    subtitle = "Cipher Alert"
                )
            }

            item { SettingsSection("BACKUP") }

            item {
                SettingsTile(
                    icon = Icons.Filled.CloudUpload,
                    title = "Encrypted Cloud Backup",
                    subtitle = "Backup messages securely",
                    trailingContent = {
                        Switch(
                            checked = false,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CipherColors.Black,
                                checkedTrackColor = CipherColors.NeonGreen
                            )
                        )
                    }
                )
            }

            item {
                SettingsTile(
                    icon = Icons.Filled.Devices,
                    title = "Linked Devices",
                    subtitle = "Manage multi-device sync"
                )
            }

            item { SettingsSection("ABOUT") }

            item {
                SettingsTile(
                    icon = Icons.Filled.Info,
                    title = "Version",
                    subtitle = "CipherSMS v1.0.0"
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "// $title",
            color = CipherColors.NeonGreenDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(1.dp).background(CipherColors.BorderBlack))
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun SettingsTile(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(CipherColors.CardBlack, RoundedCornerShape(8.dp))
            .border(1.dp, CipherColors.BorderBlack, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(CipherColors.NeonGreenTrace, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = CipherColors.NeonGreen, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(title, color = CipherColors.OnSurface, fontSize = 15.sp)
            if (subtitle != null) {
                Text(subtitle, color = CipherColors.OnSurfaceDim, fontSize = 12.sp)
            }
        }

        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(Icons.Filled.ChevronRight, null, tint = CipherColors.BorderBlack, modifier = Modifier.size(18.dp))
        }
    }
}

// Extension icons not in default set
private val Icons.Filled.Bubble: ImageVector get() = Icons.Filled.ChatBubble
private val Icons.Filled.Sim: ImageVector get() = Icons.Filled.SimCard
private val Icons.Filled.Devices: ImageVector get() = Icons.Filled.DevicesOther
