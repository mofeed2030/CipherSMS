package com.ciphersms.app.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Cipher Color Palette ─────────────────────────────────────────────────────
object CipherColors {
    // Core palette - Military/Intel aesthetic
    val Black = Color(0xFF000000)
    val DeepBlack = Color(0xFF020204)
    val SurfaceBlack = Color(0xFF080C0A)
    val CardBlack = Color(0xFF0D120E)
    val BorderBlack = Color(0xFF141C15)

    // Neon Green spectrum
    val NeonGreen = Color(0xFF00FF41)
    val NeonGreenDim = Color(0xFF00CC33)
    val NeonGreenFaint = Color(0xFF004D14)
    val NeonGreenGlow = Color(0x4000FF41)
    val NeonGreenTrace = Color(0x1A00FF41)
    val MatrixGreen = Color(0xFF00D429)
    val CipherGreen = Color(0xFF39FF14)
    val TerminalGreen = Color(0xFF00FF7F)

    // Status colors
    val Sent = Color(0xFF00FF41)
    val Delivered = Color(0xFF00CC33)
    val Read = Color(0xFF00FFFF)
    val Failed = Color(0xFFFF3333)
    val Pending = Color(0xFFFFAA00)

    // Surface colors
    val SurfaceVariant = Color(0xFF0D1A0E)
    val OnSurface = Color(0xFFCCFFCC)
    val OnSurfaceDim = Color(0xFF668866)

    // Bubble colors
    val OutgoingBubble = Color(0xFF0A2A0E)
    val IncomingBubble = Color(0xFF0C1410)
    val OutgoingBubbleBorder = Color(0xFF00FF41)
    val IncomingBubbleBorder = Color(0xFF1A3A1A)

    // Vault accent
    val VaultPurple = Color(0xFF8B00FF)
    val VaultGlow = Color(0x408B00FF)

    // Danger
    val DangerRed = Color(0xFFFF0033)
    val WarningAmber = Color(0xFFFFAA00)
    val SpamOrange = Color(0xFFFF6600)
}

// ─── Color Scheme ─────────────────────────────────────────────────────────────
private val CipherDarkColorScheme = darkColorScheme(
    primary = CipherColors.NeonGreen,
    onPrimary = CipherColors.Black,
    primaryContainer = CipherColors.NeonGreenFaint,
    onPrimaryContainer = CipherColors.NeonGreen,

    secondary = CipherColors.MatrixGreen,
    onSecondary = CipherColors.Black,
    secondaryContainer = Color(0xFF003311),
    onSecondaryContainer = CipherColors.TerminalGreen,

    tertiary = Color(0xFF00FFFF),
    onTertiary = CipherColors.Black,
    tertiaryContainer = Color(0xFF003333),
    onTertiaryContainer = Color(0xFF00FFFF),

    error = CipherColors.DangerRed,
    errorContainer = Color(0xFF330011),
    onError = Color.White,
    onErrorContainer = CipherColors.DangerRed,

    background = CipherColors.DeepBlack,
    onBackground = CipherColors.NeonGreen,

    surface = CipherColors.SurfaceBlack,
    onSurface = CipherColors.OnSurface,
    surfaceVariant = CipherColors.SurfaceVariant,
    onSurfaceVariant = CipherColors.OnSurfaceDim,

    outline = CipherColors.NeonGreenFaint,
    outlineVariant = CipherColors.BorderBlack,

    scrim = Color(0xCC000000),
    inverseSurface = CipherColors.NeonGreen,
    inverseOnSurface = CipherColors.Black,
    inversePrimary = CipherColors.Black,
)

// ─── Typography ──────────────────────────────────────────────────────────────
val CipherTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)

// ─── Theme Composable ─────────────────────────────────────────────────────────
@Composable
fun CipherSMSTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CipherDarkColorScheme,
        typography = CipherTypography,
        content = content
    )
}
