package com.example.teqgastos.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.teqgastos.R
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSecondary

/**
 * Logotipo Oficial Corporativo de TeqGastos (TEQMED SpA)
 * Diseño Neumorphic 3D con halo de respiración y borde especular de luz lineal
 */
@Composable
fun TeqGastosBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    showLabel: Boolean = false,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val pulseScale by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.035f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(pulseScale)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(size * 0.28f),
                    ambientColor = TeqPrimary.copy(alpha = 0.35f),
                    spotColor = TeqSecondary.copy(alpha = 0.45f)
                )
                .clip(RoundedCornerShape(size * 0.28f))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0B192E),
                            Color(0xFF003B7A),
                            Color(0xFF0056B3)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x8000E5FF),
                            Color(0x20FFFFFF),
                            Color(0x100056B3)
                        )
                    ),
                    shape = RoundedCornerShape(size * 0.28f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_teqgastos_logo),
                contentDescription = "Logotipo TeqGastos · TEQMED SpA",
                modifier = Modifier
                    .size(size * 0.92f)
                    .padding(size * 0.05f)
            )
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "TEQMED SpA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TeqPrimary
            )
            Text(
                text = "TeqGastos – Rendición en Terreno",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
