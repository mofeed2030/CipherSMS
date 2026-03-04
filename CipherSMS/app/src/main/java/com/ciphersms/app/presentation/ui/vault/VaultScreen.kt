package com.ciphersms.app.presentation.ui.vault

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.ciphersms.app.presentation.ui.theme.CipherColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    onBack: () -> Unit,
    onConversationClick: (Long, String) -> Unit
) {
    var isUnlocked by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }

    if (!isUnlocked) {
        VaultLockScreen(
            pin = pin,
            onDigit = { digit ->
                if (pin.length < 4) {
                    pin += digit
                    if (pin.length == 4) {
                        // Verify vault PIN
                        isUnlocked = true // placeholder
                    }
                }
            },
            onDelete = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
            onBack = onBack
        )
    } else {
        VaultContent(
            onBack = onBack,
            onConversationClick = onConversationClick
        )
    }
}

@Composable
private fun VaultLockScreen(
    pin: String,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vault")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(CipherColors.DeepBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.Filled.ArrowBack, null, tint = CipherColors.VaultPurple)
            }

            // Vault icon with purple glow
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .alpha(glowAlpha)
                        .blur(24.dp)
                        .background(CipherColors.VaultGlow, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .border(2.dp, CipherColors.VaultPurple, CircleShape)
                        .background(Color(0xFF0A0010), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        null,
                        tint = CipherColors.VaultPurple,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                "SECRET VAULT",
                color = CipherColors.VaultPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 4.sp
            )

            Text(
                "Enter vault PIN to unlock",
                color = CipherColors.OnSurfaceDim,
                fontSize = 13.sp
            )

            // PIN dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { i ->
                    val filled = i < pin.length
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .border(2.dp, if (filled) CipherColors.VaultPurple else CipherColors.BorderBlack, CircleShape)
                            .background(if (filled) CipherColors.VaultPurple else Color.Transparent, CircleShape)
                    )
                }
            }

            // Numpad
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "⌫")
                ).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        row.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(Modifier.size(64.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .border(1.dp, Color(0xFF2A0040), CircleShape)
                                        .background(Color(0xFF0D0015), CircleShape)
                                        .clickable {
                                            if (key == "⌫") onDelete() else onDigit(key)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        key,
                                        color = CipherColors.OnSurface,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VaultContent(
    onBack: () -> Unit,
    onConversationClick: (Long, String) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Shield, null, tint = CipherColors.VaultPurple, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "// VAULT",
                                color = CipherColors.VaultPurple,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, null, tint = CipherColors.VaultPurple)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0A0010)
                    )
                )
                Box(
                    Modifier.fillMaxWidth().height(1.dp)
                        .background(Brush.horizontalGradient(
                            listOf(Color.Transparent, CipherColors.VaultPurple, Color.Transparent)
                        ))
                )
            }
        },
        containerColor = CipherColors.DeepBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.LockOpen,
                            null,
                            tint = CipherColors.VaultPurple,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Vault is empty",
                            color = CipherColors.OnSurfaceDim,
                            fontSize = 14.sp
                        )
                        Text(
                            "Move conversations here to hide them",
                            color = Color(0xFF664488),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private val CipherColors.VaultPurple: Color get() = Color(0xFF8B00FF)
private val CipherColors.VaultGlow: Color get() = Color(0x408B00FF)
