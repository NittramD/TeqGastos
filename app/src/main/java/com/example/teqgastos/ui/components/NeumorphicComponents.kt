package com.example.teqgastos.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.teqgastos.ui.theme.NeuBackground
import com.example.teqgastos.ui.theme.NeuBorder
import com.example.teqgastos.ui.theme.NeuDarkShadow
import com.example.teqgastos.ui.theme.NeuLightShadow
import com.example.teqgastos.ui.theme.NeuPressed
import com.example.teqgastos.ui.theme.NeuSurface
import com.example.teqgastos.ui.theme.TeqPrimary

// Modificador Central Neumorphic: Doble sombra (Luz superior izquierda + Sombra inferior derecha)
fun Modifier.neumorphic(
    cornerRadius: Dp = 16.dp,
    lightShadowColor: Color = NeuLightShadow.copy(alpha = 0.85f),
    darkShadowColor: Color = NeuDarkShadow.copy(alpha = 0.55f),
    offset: Dp = 4.dp,
    blur: Dp = 8.dp,
    isPressed: Boolean = false
): Modifier = this.drawBehind {
    val cornerRadiusPx = cornerRadius.toPx()
    val offsetPx = offset.toPx()
    val blurPx = blur.toPx()

    drawIntoCanvas { canvas ->
        if (!isPressed) {
            // Sombra ambiental inferior derecha (oscura)
            val darkPaint = android.graphics.Paint().apply {
                color = darkShadowColor.toArgb()
                isAntiAlias = true
                maskFilter = android.graphics.BlurMaskFilter(
                    blurPx.coerceAtLeast(1f),
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
            canvas.nativeCanvas.drawRoundRect(
                offsetPx,
                offsetPx,
                size.width + offsetPx,
                size.height + offsetPx,
                cornerRadiusPx,
                cornerRadiusPx,
                darkPaint
            )

            // Reflejo de luz cenital superior izquierdo (blanco)
            val lightPaint = android.graphics.Paint().apply {
                color = lightShadowColor.toArgb()
                isAntiAlias = true
                maskFilter = android.graphics.BlurMaskFilter(
                    blurPx.coerceAtLeast(1f),
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
            canvas.nativeCanvas.drawRoundRect(
                -offsetPx,
                -offsetPx,
                size.width - offsetPx,
                size.height - offsetPx,
                cornerRadiusPx,
                cornerRadiusPx,
                lightPaint
            )
        }
    }
}

// Tarjeta Neumorphic Extruida
@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 4.dp,
    backgroundColor: Color = NeuSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentOffset by animateDpAsState(
        targetValue = if (isPressed) 1.dp else elevation,
        animationSpec = tween(durationMillis = 150),
        label = "offset"
    )

    Box(
        modifier = modifier
            .padding(elevation) // Espacio para que respiren las sombras
            .neumorphic(
                cornerRadius = cornerRadius,
                offset = currentOffset,
                blur = currentOffset * 2,
                isPressed = isPressed
            )
            .clip(shape)
            .background(if (isPressed) NeuPressed else backgroundColor)
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

// Botón Táctil Neumorphic con microinteracción de presión
@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    elevation: Dp = 4.dp,
    backgroundColor: Color = NeuSurface,
    contentColor: Color = TeqPrimary,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentOffset by animateDpAsState(
        targetValue = if (isPressed) 1.dp else elevation,
        animationSpec = tween(durationMillis = 120),
        label = "btnOffset"
    )

    Box(
        modifier = modifier
            .padding(elevation)
            .neumorphic(
                cornerRadius = cornerRadius,
                offset = currentOffset,
                blur = currentOffset * 2,
                isPressed = isPressed
            )
            .clip(shape)
            .background(if (isPressed) NeuPressed else backgroundColor)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
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

// Métrica Neumorphic
@Composable
fun NeumorphicMetricCard(
    title: String,
    amount: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier,
        cornerRadius = 16.dp,
        elevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TeqPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TeqPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
