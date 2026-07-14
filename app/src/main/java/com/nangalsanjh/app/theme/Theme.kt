package com.nangalsanjh.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Color Tokens ─────────────────────────────────────────────
object NangalColors {
    // Orange Accent
    val Orange500 = Color(0xFFFF6B00)
    val Orange400 = Color(0xFFFF8534)
    val Orange100 = Color(0xFFFF6B00).copy(alpha = 0.12f)

    // Dark Surface
    val Gray950 = Color(0xFF0C0C0E)
    val Gray900 = Color(0xFF111113)
    val Gray850 = Color(0xFF161618)
    val Gray800 = Color(0xFF1C1C1F)
    val Gray700 = Color(0xFF242428)
    val Gray600 = Color(0xFF2C2C31)
    val Gray400 = Color(0xFF5A5A63)
    val Gray200 = Color(0xFFAAAAAF)
    val Gray100 = Color(0xFFE8E8ED)

    // Light Surface
    val White = Color(0xFFFFFFFF)
    val Cream50 = Color(0xFFFAFAF8)
    val Cream100 = Color(0xFFF4F4F0)
    val Cream200 = Color(0xFFE8E8E2)
    val LightGray400 = Color(0xFFAAAAAA)
    val LightGray600 = Color(0xFF666666)
    val LightGray900 = Color(0xFF1A1A1A)

    // Status
    val Green = Color(0xFF30D158)
    val Yellow = Color(0xFFFFD60A)
    val Red = Color(0xFFFF453A)

    // Blood
    val Blood = Color(0xFF8B0000)
}

val NangalDarkColorScheme = darkColorScheme(
    primary = NangalColors.Orange500,
    onPrimary = NangalColors.White,
    primaryContainer = NangalColors.Orange100,
    onPrimaryContainer = NangalColors.Orange400,
    secondary = NangalColors.Orange400,
    onSecondary = NangalColors.White,
    tertiary = NangalColors.Yellow,
    background = NangalColors.Gray950,
    onBackground = NangalColors.Gray100,
    surface = NangalColors.Gray800,
    onSurface = NangalColors.Gray100,
    surfaceVariant = NangalColors.Gray700,
    onSurfaceVariant = NangalColors.Gray200,
    outline = NangalColors.Gray600,
    outlineVariant = NangalColors.Gray700,
    error = NangalColors.Red,
    onError = NangalColors.White,
    inverseSurface = NangalColors.Gray100,
    inverseOnSurface = NangalColors.Gray900
)

val NangalLightColorScheme = lightColorScheme(
    primary = NangalColors.Orange500,
    onPrimary = NangalColors.White,
    primaryContainer = NangalColors.Orange100,
    onPrimaryContainer = NangalColors.Orange500,
    secondary = NangalColors.Orange400,
    onSecondary = NangalColors.White,
    tertiary = Color(0xFFBF5700),
    background = NangalColors.Cream50,
    onBackground = NangalColors.LightGray900,
    surface = NangalColors.White,
    onSurface = NangalColors.LightGray900,
    surfaceVariant = NangalColors.Cream100,
    onSurfaceVariant = NangalColors.LightGray600,
    outline = NangalColors.Cream200,
    outlineVariant = NangalColors.Cream200,
    error = NangalColors.Red,
    onError = NangalColors.White,
    inverseSurface = NangalColors.LightGray900,
    inverseOnSurface = NangalColors.White
)

val NangalTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun NangalSevaTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDark) NangalDarkColorScheme else NangalLightColorScheme,
        typography = NangalTypography,
        content = content
    )
}