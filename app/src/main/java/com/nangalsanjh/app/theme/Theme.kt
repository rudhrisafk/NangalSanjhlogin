// Theme.kt — FULL FILE (premium color schemes + gradient brushes + refined typography)

package com.nangalsanjh.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object NangalColors {
    val Orange500 = BrandOrange
    val Orange400 = BrandAmber
    val Orange100 = BrandOrange.copy(alpha = 0.14f)

    val Gray950 = InkBlack
    val Gray900 = CharcoalBase
    val Gray850 = CharcoalSurface
    val Gray800 = CharcoalElevated
    val Gray700 = CharcoalBorder
    val Gray600 = CharcoalDivider
    val Gray400 = TextMutedDark
    val Gray200 = TextSecondaryDark
    val Gray100 = TextPrimaryDark

    val White = CreamSurface
    val Cream50 = CreamBase
    val Cream100 = CreamElevated
    val Cream200 = CreamBorder
    val LightGray400 = Color(0xFFB4AA9C)
    val LightGray600 = MutedOnLight
    val LightGray900 = InkOnLight

    val Green = EmeraldSuccess
    val Yellow = AmberWarning
    val Red = CrimsonError

    val Blood = BloodRed
}

// ── Signature gradients — used across buttons, headers, banners ──
object NangalGradients {
    val BrandHorizontal = Brush.horizontalGradient(listOf(BrandOrange, BrandAmber))
    val BrandDiagonal = Brush.linearGradient(listOf(BrandOrangeDeep, BrandOrange, BrandAmber))
    val BrandVertical = Brush.verticalGradient(listOf(BrandOrange, BrandOrangeDeep))

    val DarkGlow = Brush.radialGradient(
        colors = listOf(BrandOrange.copy(alpha = 0.18f), Color.Transparent)
    )

    val CardSheenDark = Brush.linearGradient(
        listOf(CharcoalElevated, CharcoalSurface)
    )
    val CardSheenLight = Brush.linearGradient(
        listOf(CreamSurface, CreamElevated)
    )

    val EmergencyGradient = Brush.horizontalGradient(listOf(Color(0xFF8E0E12), BloodRed))
}

val NangalDarkColorScheme = darkColorScheme(
    primary = BrandOrange,
    onPrimary = Color(0xFF1A0D00),
    primaryContainer = BrandOrange.copy(alpha = 0.16f),
    onPrimaryContainer = BrandAmber,
    secondary = BrandAmber,
    onSecondary = Color(0xFF1A0D00),
    tertiary = SapphireInfo,
    background = InkBlack,
    onBackground = TextPrimaryDark,
    surface = CharcoalSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CharcoalElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = CharcoalBorder,
    outlineVariant = CharcoalDivider,
    error = CrimsonError,
    onError = Color.White,
    inverseSurface = TextPrimaryDark,
    inverseOnSurface = CharcoalBase
)

val NangalLightColorScheme = lightColorScheme(
    primary = BrandOrangeDeep,
    onPrimary = Color.White,
    primaryContainer = BrandOrange.copy(alpha = 0.14f),
    onPrimaryContainer = BrandOrangeDeep,
    secondary = BrandAmber,
    onSecondary = Color(0xFF3A2200),
    tertiary = SapphireInfo,
    background = CreamBase,
    onBackground = InkOnLight,
    surface = CreamSurface,
    onSurface = InkOnLight,
    surfaceVariant = CreamElevated,
    onSurfaceVariant = MutedOnLight,
    outline = CreamBorder,
    outlineVariant = CreamBorder,
    error = CrimsonError,
    onError = Color.White,
    inverseSurface = InkOnLight,
    inverseOnSurface = CreamSurface
)

val NangalTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.8).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 29.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 23.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.4).sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.1).sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
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