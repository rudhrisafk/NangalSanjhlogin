package com.nangalsanjh.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NangalDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B00),          // Orange
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3D1F00),
    onPrimaryContainer = Color(0xFFFFD9B3),
    secondary = Color(0xFFFF8C00),         // Amber Orange
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFFFB347),          // Light Orange
    background = Color(0xFF0D0D0D),        // Almost Black
    onBackground = Color(0xFFEEEEEE),
    surface = Color(0xFF1A1A1A),           // Dark Gray
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2A2A2A),
    outline = Color(0xFF444444),
    error = Color(0xFFFF4444),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun NangalSevaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NangalDarkColorScheme,
        typography = Typography,
        content = content
    )
}