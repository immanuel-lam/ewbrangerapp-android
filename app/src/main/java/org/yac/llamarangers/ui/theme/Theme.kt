package org.yac.llamarangers.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // Primary — forest green
    primary = RangerGreen,
    onPrimary = Color.White,
    primaryContainer = RangerGreenPale,
    onPrimaryContainer = RangerGreenOnPale,
    // Secondary — earthy brown
    secondary = RangerBrown,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = Color(0xFF2C1600),
    // Tertiary — informational blue
    tertiary = RangerBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCDE5FF),
    onTertiaryContainer = Color(0xFF001E30),
    // Error
    error = RangerRed,
    onError = Color.White,
    errorContainer = RangerErrorContainer,
    onErrorContainer = Color(0xFF410002),
    // Background & surface
    background = BackgroundLight,
    onBackground = Color(0xFF1A1C1A),
    surface = SurfaceLight,
    onSurface = Color(0xFF1A1C1A),
    surfaceVariant = SurfaceContainerHighLight,
    onSurfaceVariant = Color(0xFF3F4944),
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    // Outline
    outline = Color(0xFF6F7971),
    outlineVariant = Color(0xFFBFC9C2),
    // Inverse
    inverseSurface = Color(0xFF2E312F),
    inverseOnSurface = Color(0xFFEFF1EE),
    inversePrimary = RangerGreenLight,
    // Scrim
    scrim = Color(0xFF000000)
)

private val DarkColorScheme = darkColorScheme(
    // Primary
    primary = RangerGreenLight,
    onPrimary = Color(0xFF00391F),
    primaryContainer = RangerGreenDark,
    onPrimaryContainer = RangerGreenPale,
    // Secondary
    secondary = Color(0xFFD5BFA0),
    onSecondary = Color(0xFF3C2200),
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = SecondaryContainerLight,
    // Tertiary
    tertiary = Color(0xFF96CCFF),
    onTertiary = Color(0xFF003352),
    tertiaryContainer = Color(0xFF004B73),
    onTertiaryContainer = Color(0xFFCDE5FF),
    // Error
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    // Background & surface
    background = BackgroundDark,
    onBackground = Color(0xFFE1E3E0),
    surface = SurfaceDark,
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = SurfaceContainerHighDark,
    onSurfaceVariant = Color(0xFFBFC9C2),
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    // Outline
    outline = Color(0xFF89938D),
    outlineVariant = Color(0xFF3F4944),
    // Inverse
    inverseSurface = Color(0xFFE1E3E0),
    inverseOnSurface = Color(0xFF2E312F),
    inversePrimary = RangerGreen,
    // Scrim
    scrim = Color(0xFF000000)
)

@Composable
fun LlamaRangersTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
