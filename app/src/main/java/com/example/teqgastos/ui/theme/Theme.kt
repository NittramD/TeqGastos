package com.example.teqgastos.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun neuBackgroundColor(): Color = if (LocalIsDarkTheme.current) NeuDarkBackground else NeuBackground

@Composable
fun neuSurfaceColor(): Color = if (LocalIsDarkTheme.current) NeuDarkSurface else NeuSurface

private val DarkColorScheme = darkColorScheme(
    primary = TeqDarkPrimary,
    onPrimary = TeqDarkOnPrimary,
    primaryContainer = TeqDarkPrimaryContainer,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF4FD8EB),
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    background = NeuDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = NeuDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A)
)

private val LightColorScheme = lightColorScheme(
    primary = TeqPrimary,
    onPrimary = TeqOnPrimary,
    primaryContainer = TeqPrimaryContainer,
    onPrimaryContainer = TeqOnPrimaryContainer,
    secondary = TeqSecondary,
    onSecondary = TeqOnSecondary,
    secondaryContainer = TeqSecondaryContainer,
    onSecondaryContainer = TeqOnSecondaryContainer,
    tertiary = TeqTertiary,
    tertiaryContainer = TeqTertiaryContainer,
    background = NeuBackground,
    onBackground = TeqOnBackground,
    surface = NeuSurface,
    onSurface = TeqOnSurface,
    surfaceVariant = TeqSurfaceVariant,
    onSurfaceVariant = TeqOnSurfaceVariant,
    error = TeqError,
    errorContainer = TeqErrorContainer
)

@Composable
fun TeqGastosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Mantener branding TEQMED
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

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
