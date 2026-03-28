package org.yac.llamarangers.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary: forest green ────────────────────────────────────────────────────
val RangerGreen = Color(0xFF2D6A4F)               // primary
val RangerGreenLight = Color(0xFF52936E)           // inversePrimary / dark-theme primary
val RangerGreenDark = Color(0xFF1A4A35)            // dark-theme primaryContainer

// M3-spec primary tonal palette (light scheme)
val PrimaryContainer = Color(0xFFB7E4C7)           // primaryContainer
val OnPrimaryContainer = Color(0xFF0D3320)         // onPrimaryContainer

// ── Secondary: earthy brown ──────────────────────────────────────────────────
val RangerBrown = Color(0xFF6B5D4F)                // secondary
val SecondaryContainer = Color(0xFFEDE0D4)         // secondaryContainer
val OnSecondaryContainer = Color(0xFF241A12)       // onSecondaryContainer
val SecondaryContainerDark = Color(0xFF4A3520)

// ── Tertiary: khaki / gold ───────────────────────────────────────────────────
val RangerKhaki = Color(0xFFA68C5C)                // tertiary
val TertiaryContainer = Color(0xFFF3E6C8)          // tertiaryContainer
val OnTertiaryContainer = Color(0xFF2E1F00)        // onTertiaryContainer

// ── Surface & background ─────────────────────────────────────────────────────
val SurfaceLight = Color(0xFFFFFBFE)
val SurfaceDark = Color(0xFF101510)
val BackgroundLight = Color(0xFFFFFBFE)
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

// ── Semantic ─────────────────────────────────────────────────────────────────
val RangerRed = Color(0xFFBA1A1A)
val RangerErrorContainer = Color(0xFFFFDAD6)
val RangerOrange = Color(0xFFBF5B00)
val RangerOrangeContainer = Color(0xFFFFDCBE)
val RangerYellow = Color(0xFFA08500)

// ── Status zone colours ───────────────────────────────────────────────────────
val ZoneActive = Color(0xFFBA1A1A)
val ZoneUnderTreatment = Color(0xFFBF5B00)
val ZoneCleared = Color(0xFF2D6A4F)

// ── Variant colours (matching iOS LantanaVariant.color) ──────────────────────
val VariantPink = Color(0xFFFF69B4)
val VariantRed = Color(0xFFBA1A1A)
val VariantPinkEdgedRed = Color(0xFFD94F6F)
val VariantOrange = Color(0xFFBF5B00)
val VariantWhite = Color(0xFFF0EDED)
val VariantUnknown = Color(0xFF8D9188)

// ── PIN dots ──────────────────────────────────────────────────────────────────
val PinFilled = Color(0xFF2D6A4F)   // RangerGreen
val PinEmpty = Color(0xFFCAC4C0)    // surface outline variant

// ── Hero gradient — primary to primaryContainer (top → bottom) ───────────────
val HeroGradientTop = Color(0xFF2D6A4F)    // primary
val HeroGradientBottom = Color(0xFFB7E4C7) // primaryContainer

// ── Avatar ────────────────────────────────────────────────────────────────────
val AvatarSelected = Color(0xFF2D6A4F)
val AvatarSelectedBorder = Color(0xFF52936E)
val AvatarDefault = Color(0xFFE5E1DC)
