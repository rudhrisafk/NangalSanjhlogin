// Type.kt — FULL FILE (kept minimal legacy typography object for compatibility; real typography lives in Theme.kt's NangalTypography)

package com.nangalsanjh.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        letterSpacing = (-0.3).sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp
    )
)