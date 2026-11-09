// Color.kt — FULL FILE (premium dark palette, warm amber-orange gradient accent, richer status colors)

package com.nangalsanjh.app.theme

import androidx.compose.ui.graphics.Color

// Core brand gradient — warm sunset orange to amber
val BrandOrange = Color(0xFFFF7A18)
val BrandAmber = Color(0xFFFFB238)
val BrandOrangeDeep = Color(0xFFE85D04)

// Rich near-black surfaces with a hint of warmth (not pure #000, feels premium)
val InkBlack = Color(0xFF0A0908)
val CharcoalBase = Color(0xFF121110)
val CharcoalSurface = Color(0xFF1A1816)
val CharcoalElevated = Color(0xFF221F1C)
val CharcoalBorder = Color(0xFF2D2A26)
val CharcoalDivider = Color(0xFF35312C)

// Light theme — warm cream, not sterile white
val CreamBase = Color(0xFFFBF8F4)
val CreamSurface = Color(0xFFFFFFFF)
val CreamElevated = Color(0xFFF3EEE7)
val CreamBorder = Color(0xFFE7DFD3)
val InkOnLight = Color(0xFF1C1712)
val MutedOnLight = Color(0xFF7A7168)

// Text hierarchy on dark
val TextPrimaryDark = Color(0xFFF5F0E8)
val TextSecondaryDark = Color(0xFFB8B0A4)
val TextMutedDark = Color(0xFF7A7268)

// Status — deeper, richer, not neon-flat
val EmeraldSuccess = Color(0xFF2BD576)
val AmberWarning = Color(0xFFFFC94D)
val CrimsonError = Color(0xFFFF4F58)
val SapphireInfo = Color(0xFF4DA3FF)

// Blood / emergency accent
val BloodRed = Color(0xFFB3101B)

// Legacy aliases kept for backward-compat with old imports
val Orange = BrandOrange
val BackgroundBlack = InkBlack
val SurfaceDarkGray = CharcoalSurface
val TextWhite = TextPrimaryDark