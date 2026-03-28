package org.yac.llamarangers.ui.theme

import androidx.compose.ui.graphics.Color

// Primary: earthy forest greens — M3 tonal palette anchored at #2D6A4F
val RangerGreenDark = Color(0xFF1A4A35)     // darker tone for dark theme primary container
val RangerGreen = Color(0xFF2D6A4F)          // forest green — primary
val RangerGreenLight = Color(0xFF52936E)     // lighter tone for light theme primary container
val RangerGreenPale = Color(0xFFCCE8D6)     // pale tint for primaryContainer in light theme
val RangerGreenOnPale = Color(0xFF0A3320)   // onPrimaryContainer in light theme

// Secondary: earthy browns / khaki
val RangerBrown = Color(0xFF6D4C2B)
val RangerBrownLight = Color(0xFF8B6914)
val SecondaryContainerLight = Color(0xFFE8DED0)
val SecondaryContainerDark = Color(0xFF4A3520)

// Surface & background — warm off-white tonal surfaces
val SurfaceLight = Color(0xFFFFFBFE)
val SurfaceDark = Color(0xFF101510)
val BackgroundLight = Color(0xFFF6F3EE)      // warm off-white
val BackgroundDark = Color(0xFF101510)

// M3 surface container tones (light)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF1EDE8)
val SurfaceContainerLight = Color(0xFFEBE7E2)
val SurfaceContainerHighLight = Color(0xFFE5E1DC)
val SurfaceContainerHighestLight = Color(0xFFE0DCD7)

// M3 surface container tones (dark)
val SurfaceContainerLowestDark = Color(0xFF0B100E)
val SurfaceContainerLowDark = Color(0xFF181D1B)
val SurfaceContainerDark = Color(0xFF1C2120)
val SurfaceContainerHighDark = Color(0xFF262C2A)
val SurfaceContainerHighestDark = Color(0xFF313734)

// Semantic
val RangerRed = Color(0xFFBA1A1A)            // M3-compliant error red
val RangerErrorContainer = Color(0xFFFFDAD6) // errorContainer light
val RangerOrange = Color(0xFFBF5B00)         // on-surface orange for warnings
val RangerOrangeContainer = Color(0xFFFFDCBE)
val RangerYellow = Color(0xFFA08500)
val RangerBlue = Color(0xFF00639A)           // tertiary blue

// Status zone colours
val ZoneActive = Color(0xFFBA1A1A)            // M3-aligned red
val ZoneUnderTreatment = Color(0xFFBF5B00)    // M3-aligned orange
val ZoneCleared = Color(0xFF2D6A4F)           // RangerGreen

// Variant colours (matching iOS LantanaVariant.color)
val VariantPink = Color(0xFFFF69B4)
val VariantRed = Color(0xFFBA1A1A)
val VariantPinkEdgedRed = Color(0xFFD94F6F)
val VariantOrange = Color(0xFFBF5B00)
val VariantWhite = Color(0xFFF0EDED)
val VariantUnknown = Color(0xFF8D9188)

// PIN dot
val PinFilled = Color(0xFF2D6A4F)            // RangerGreen
val PinEmpty = Color(0xFFCAC4C0)             // surface outline variant

// Hero gradient
val HeroGradientTop = Color(0xFF0F2D1E)      // deep forest dark
val HeroGradientBottom = Color(0xFF1F4D36)   // mid forest

// Avatar
val AvatarSelected = Color(0xFF2D6A4F)
val AvatarSelectedBorder = Color(0xFF52936E)
val AvatarDefault = Color(0xFFE5E1DC)
