package com.example.teqgastos.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.ui.theme.LocalIsDarkTheme
import com.example.teqgastos.ui.theme.TeqPrimary

/**
 * Modificador Shimmer que proyecta un barrido de luz degradado continuo para estados de carga (Skeleton loading).
 */
@Composable
fun Modifier.shimmerLoading(
    durationMillis: Int = 1200
): Modifier {
    val isDark = LocalIsDarkTheme.current
    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF1E293B),
            Color(0xFF334155),
            Color(0xFF1E293B)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF8FAFC),
            Color(0xFFE2E8F0)
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation, y = translateAnimation)
    )

    return this.background(brush)
}

/**
 * Caja esqueleto con animación Shimmer integrada.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmerLoading()
    )
}

/**
 * Tarjeta Esqueleto Shimmer para la lista de Rendiciones de Terreno.
 */
@Composable
fun ShimmerRendicionCard(
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(20.dp),
                    shape = RoundedCornerShape(6.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
            ShimmerBox(
                modifier = Modifier
                    .size(width = 70.dp, height = 24.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerBox(
                    modifier = Modifier
                        .width(110.dp)
                        .height(22.dp),
                    shape = RoundedCornerShape(6.dp)
                )
            }
            ShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp),
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}

/**
 * Tarjeta Esqueleto Shimmer para los ítems de Comprobantes de Gastos.
 */
@Composable
fun ShimmerGastoCard(
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(18.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                ShimmerBox(
                    modifier = Modifier
                        .width(75.dp)
                        .height(20.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(
                    modifier = Modifier
                        .width(50.dp)
                        .height(12.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
    }
}

/**
 * Indicador de carga pulsante circular con ondas para procesos de red, OCR o sincronización.
 */
@Composable
fun TeqPulsingLoader(
    message: String = "Cargando datos...",
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulsingTransition")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Onda pulsante exterior
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(TeqPrimary.copy(alpha = 0.15f * alpha))
            )
            // Círculo concéntrico medio
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(TeqPrimary.copy(alpha = 0.25f))
            )
            // Icono central
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = TeqPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        CircularProgressIndicator(
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(20.dp),
            color = TeqPrimary
        )
    }
}
