package com.example.teqgastos.ui.screens.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teqgastos.ui.components.TeqGastosBrandLogo
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSecondary
import kotlinx.coroutines.delay

/**
 * Pantalla de Bienvenida / Splash Screen Oficial de TeqGastos (TEQMED SpA)
 * Muestra brevemente el logotipo animado con halo luminoso y tipografía corporativa
 * antes de dar paso a la pantalla de autenticación.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
    splashDurationMillis: Long = 1800L
) {
    val logoScale = remember { Animatable(0.82f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animación suave de entrada del logo y textos
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        // Permanencia breve para reconocimiento de marca
        delay(splashDurationMillis)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070F1E),
                        Color(0xFF0B192E),
                        Color(0xFF002F6C),
                        Color(0xFF0056B3)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Halo de resplandor ambiental detrás del logotipo
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(logoScale.value)
                .alpha(contentAlpha.value * 0.4f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            TeqSecondary.copy(alpha = 0.5f),
                            TeqPrimary.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .scale(logoScale.value)
                .alpha(contentAlpha.value)
        ) {
            // Logotipo oficial corporativo con relieve y animación
            TeqGastosBrandLogo(
                size = 120.dp,
                animated = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TEQMED SpA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    letterSpacing = 2.5.sp
                ),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "TeqGastos – Rendición en Terreno",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF00E5FF)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Mantenimiento y Equipamiento Médico · Macrozona Sur",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Indicador sutil de carga/iniciación
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF00E5FF),
                strokeWidth = 2.2.dp
            )
        }

        // Pie de página informativo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "v3.2.0 · Evaluación Parcial 3 · DSY1105",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}
