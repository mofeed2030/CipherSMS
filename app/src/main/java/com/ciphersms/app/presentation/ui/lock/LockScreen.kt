package com.ciphersms.app.presentation.ui.lock

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.ciphersms.app.presentation.ui.theme.CipherColors
import com.ciphersms.app.presentation.viewmodel.LockViewModel

@Composable
fun LockScreen(
    viewModel: LockViewModel = hiltViewModel(),
    onUnlocked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isUnlocked) {
        if (uiState.isUnlocked) onUnlocked()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CipherColors.DeepBlack),
        contentAlignment = Alignment.Center
    ) {
        // Background grid
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.04f)) {
            val step = 30f
            for (x in 0..size.width.toInt() step step.toInt()) {
                drawLine(
                    CipherColors.NeonGreen,
                    Offset(x.toFloat(), 0f),
                    Offset(x.toFloat(), size.height),
                    0.5f
                )
            }
            for (y in 0..size.height.toInt() step step.toInt()) {
                drawLine(
                    CipherColors.NeonGreen,
                    Offset(0f, y.toFloat()),
                    Offset(size.width, y.toFloat()),
                    0.5f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .alpha(glowAlpha)
                        .blur(20.dp)
                        .background(CipherColors.NeonGreenGlow, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(2.dp, CipherColors.NeonGreen, CircleShape)
                        .background(CipherColors.CardBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◈",
                        color = CipherColors.NeonGreen,
                        fontSize = 36.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "CIPHER_SMS",
                color = CipherColors.NeonGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = 4.sp
            )

            Text(
                "SECURE · ENCRYPTED · PRIVATE",
                color = CipherColors.OnSurfaceDim,
                fontSize = 10.sp,
                letterSpacing = 3.sp
            )

            Spacer(Modifier.height(48.dp))

            // PIN display
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(6) { i ->
                    val filled = i < uiState.pin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(
                                2.dp,
                                if (filled) CipherColors.NeonGreen else CipherColors.BorderBlack,
                                CircleShape
                            )
                            .background(
                                if (filled) CipherColors.NeonGreen else Color.Transparent,
                                CircleShape
                            )
                    )
                }
            }

            // Error message
            AnimatedVisibility(visible = uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = Color(0xFFFF0033),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Numpad
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "⌫")
                ).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        row.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(Modifier.size(72.dp))
                            } else {
                                NumpadButton(
                                    label = key,
                                    onClick = {
                                        if (key == "⌫") viewModel.deleteDigit()
                                        else viewModel.addDigit(key)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Biometric option
            TextButton(onClick = viewModel::tryBiometric) {
                Icon(Icons.Filled.Fingerprint, null, tint = CipherColors.NeonGreenDim)
                Spacer(Modifier.width(8.dp))
                Text("Use Biometric", color = CipherColors.NeonGreenDim, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun NumpadButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .border(1.dp, CipherColors.BorderBlack, CircleShape)
            .background(CipherColors.CardBlack, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = CipherColors.OnSurface,
            fontSize = if (label.length == 1 && label != "⌫") 22.sp else 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
