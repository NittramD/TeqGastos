package com.example.teqgastos.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta Médica Profesional TEQMED SpA
val TeqPrimary = Color(0xFF0061A4)
val TeqOnPrimary = Color(0xFFFFFFFF)
val TeqPrimaryContainer = Color(0xFFD1E4FF)
val TeqOnPrimaryContainer = Color(0xFF001D36)

val TeqSecondary = Color(0xFF006874)
val TeqOnSecondary = Color(0xFFFFFFFF)
val TeqSecondaryContainer = Color(0xFF97F0FF)
val TeqOnSecondaryContainer = Color(0xFF001F24)

val TeqTertiary = Color(0xFF525F70)
val TeqTertiaryContainer = Color(0xFFD5E3F7)

// Tokens Neumorphic
val NeuBackground = Color(0xFFE8EEF5)
val NeuSurface = Color(0xFFE8EEF5)
val NeuLightShadow = Color(0xFFFFFFFF)
val NeuDarkShadow = Color(0xFFA6B7CD)
val NeuPressed = Color(0xFFDEE5EF)
val NeuBorder = Color(0xFFD5DFEC)

// Tokens Glassmorphic (Frosted Glass / Vidrio Esmerilado)
val GlassSurfaceLight = Color(0xD9FFFFFF)    // 85% blanco vítreo
val GlassSurfaceSubtle = Color(0x99FFFFFF)   // 60% blanco translúcido
val GlassBorderHighlight = Color(0xCCFFFFFF) // 80% blanco especular
val GlassBorderShadow = Color(0x33FFFFFF)    // 20% blanco tenue
val GlassActiveCyan = Color(0xB397F0FF)      // 70% cian translúcido activo
val GlassShadowTint = Color(0x2E002544)      // Sombra etérea de cristal

val GlassPrimaryBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xEE005FA3),
        Color(0xEE008594)
    )
)

val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        GlassBorderHighlight,
        GlassBorderShadow
    )
)

val TeqBackground = NeuBackground
val TeqOnBackground = Color(0xFF191C1E)
val TeqSurface = NeuSurface
val TeqOnSurface = Color(0xFF191C1E)
val TeqSurfaceVariant = Color(0xFFDFE5ED)
val TeqOnSurfaceVariant = Color(0xFF43474E)

val TeqSuccess = Color(0xFF1E8E3E)
val TeqWarning = Color(0xFFE65100)
val TeqError = Color(0xFFBA1A1A)
val TeqErrorContainer = Color(0xFFFFDAD6)

// Dark Theme Colors
val TeqDarkPrimary = Color(0xFF9ECAFF)
val TeqDarkOnPrimary = Color(0xFF003258)
val TeqDarkPrimaryContainer = Color(0xFF00497D)
val TeqDarkBackground = Color(0xFF0F172A)
val TeqDarkSurface = Color(0xFF1E293B)
val TeqDarkOnSurface = Color(0xFFE2E8F0)
