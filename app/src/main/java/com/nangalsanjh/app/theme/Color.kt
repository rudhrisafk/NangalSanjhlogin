// Color.kt — clean civic palette: white base, navy ink, saffron accent

package com.nangalsanjh.app.theme

import androidx.compose.ui.graphics.Color

// Core brand gradient — warm sunset orange to amber
val BrandOrange = Color(0xFFE8590C)
val BrandAmber = Color(0xFFF59F00)
val BrandOrangeDeep = Color(0xFFC2410C)

// Rich near-black surfaces with a hint of warmth (not pure #000, feels premium)
val InkBlack = Color(0xFF0A1220)
val CharcoalBase = Color(0xFF0F1828)
val CharcoalSurface = Color(0xFF142034)
val CharcoalElevated = Color(0xFF1B2A42)
val CharcoalBorder = Color(0xFF283950)
val CharcoalDivider = Color(0xFF314560)

// Light theme — warm cream, not sterile white
val CreamBase = Color(0xFFFFFFFF)
val CreamSurface = Color(0xFFFFFFFF)
val CreamElevated = Color(0xFFF4F6FA)
val CreamBorder = Color(0xFFE2E7EF)
val InkOnLight = Color(0xFF0B1F3A)
val MutedOnLight = Color(0xFF55627A)

// Text hierarchy on dark
val TextPrimaryDark = Color(0xFFF1F5FB)
val TextSecondaryDark = Color(0xFFB4C0D3)
val TextMutedDark = Color(0xFF7F8DA3)

// Status — deeper, richer, not neon-flat
val EmeraldSuccess = Color(0xFF138A4B)
val AmberWarning = Color(0xFFB7791F)
val CrimsonError = Color(0xFFD92D3A)
val SapphireInfo = Color(0xFF1F5FBF)

// Blood / emergency accent
val BloodRed = Color(0xFFB3101B)

// Legacy aliases kept for backward-compat with old imports
val Orange = BrandOrange
val BackgroundBlack = InkBlack
val SurfaceDarkGray = CharcoalSurface
val TextWhite = TextPrimaryDark