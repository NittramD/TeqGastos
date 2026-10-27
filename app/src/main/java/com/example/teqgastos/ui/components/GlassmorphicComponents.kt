package com.example.teqgastos.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.teqgastos.ui.theme.GlassActiveCyan
import com.example.teqgastos.ui.theme.GlassBorderBrush
import com.example.teqgastos.ui.theme.GlassPrimaryBrush
import com.example.teqgastos.ui.theme.GlassShadowTint
import com.example.teqgastos.ui.theme.GlassSurfaceLight
import com.example.teqgastos.ui.theme.GlassSurfaceSubtle
import com.example.teqgastos.ui.theme.TeqPrimary

// Lienzo de Fondo Glassmorphic con orbes ambientales para refracción visual
@Composable
fun GlassmorphicBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE4EDF7),
                        Color(0xFFD6E4F0),
                        Color(0xFFCADBE9)
                    )
                )
            )
    ) {
        // Orbe de luz superior derecho
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x380061A4),
                            Color(0x000061A4)
                        )
                    ),
                    shape = CircleShape
                )
        )
        // Orbe de luz inferior izquierdo (cian)
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x3300838F),
                            Color(0x0000838F)
                        )
                    ),
                    shape = CircleShape
                )
        )

        content()
    }
}

// 1. Tarjeta Glassmorphic (Vidrio Esmerilado)
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 6.dp,
    backgroundColor: Color = GlassSurfaceLight,
    borderBrush: Brush = GlassBorderBrush,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = GlassShadowTint,
                spotColor = GlassShadowTint
            )
            .clip(shape)
            .background(backgroundColor)
            .border(width = 1.2.dp, brush = borderBrush, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = interactionSource, indication = null) { onClick() }
                } else Modifier
            )
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

// 2. Botón Principal Glassmorphic (Soluciona imagen del usuario 3)
@Composable
fun GlassmorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 6.dp,
    brush: Brush = GlassPrimaryBrush,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentElevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else elevation,
        animationSpec = tween(durationMillis = 100),
        label = "glassBtnElevation"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = currentElevation,
                shape = shape,
                ambientColor = Color(0x550061A4),
                spotColor = Color(0x550061A4)
            )
            .clip(shape)
            .background(brush)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.75f),
                        Color.White.copy(alpha = 0.20f)
                    )
                ),
                shape = shape
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

// 3. Extended Floating Action Button Glassmorphic (Soluciona imagen del usuario 2)
@Composable
fun GlassmorphicExtendedFab(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentElevation by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 8.dp,
        animationSpec = tween(durationMillis = 100),
        label = "fabElevation"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = currentElevation,
                shape = shape,
                ambientColor = Color(0x660061A4),
                spotColor = Color(0x660061A4)
            )
            .clip(shape)
            .background(GlassPrimaryBrush)
            .border(
                width = 1.4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.90f),
                        Color.White.copy(alpha = 0.30f)
                    )
                ),
                shape = shape
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 4. Chip de Filtro Glassmorphic (Soluciona imagen del usuario 1)
@Composable
fun GlassmorphicFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val interactionSource = remember { MutableInteractionSource() }

    val bgBrush = if (selected) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xEE97F0FF), // Cian translúcido de la foto del usuario
                Color(0xD9B2EBF2)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.70f),
                Color.White.copy(alpha = 0.40f)
            )
        )
    }

    val borderBrush = if (selected) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF006874).copy(alpha = 0.5f),
                Color(0xFF006874).copy(alpha = 0.15f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.25f)
            )
        )
    }

    val textColor = if (selected) Color(0xFF00363D) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .shadow(
                elevation = if (selected) 4.dp else 1.dp,
                shape = shape,
                ambientColor = GlassShadowTint,
                spotColor = GlassShadowTint
            )
            .clip(shape)
            .background(bgBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
