package com.example.teqgastos.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TeqDarkPrimary,
    onPrimary = TeqDarkOnPrimary,
    primaryContainer = TeqDarkPrimaryContainer,
    background = TeqDarkBackground,
    surface = TeqDarkSurface,
    onSurface = TeqDarkOnSurface
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
    background = TeqBackground,
    onBackground = TeqOnBackground,
    surface = TeqSurface,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
