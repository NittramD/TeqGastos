package com.example.teqgastos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Banner transversal reactivo para modo offline y estado de sincronización (Fase 2).
 * Integra animaciones fluidas con slide/fade, indicador de progreso y botón táctil de sincronización.
 */
@Composable
fun OfflineNoticeBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    isSyncing: Boolean = false,
    syncProgress: Float? = null,
    syncItem: String? = null,
    pendingCount: Int = 0,
    onSyncClick: (() -> Unit)? = null
) {
    val isVisible = isOffline || isSyncing || pendingCount > 0

    val infiniteTransition = rememberInfiniteTransition(label = "bannerSyncSpin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(250)),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(tween(200)),
        modifier = modifier
    ) {
        val bannerBg = when {
            isSyncing -> Color(0xFF1E3A8A) // Azul profesional TEQMED en sincronización
            isOffline -> Color(0xFF334155) // Pizarra neutral
            else -> Color(0xFF0F766E)     // Teal para pendientes con conexión activa
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = bannerBg,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconModifier = if (isSyncing) Modifier.size(18.dp).rotate(spinAngle) else Modifier.size(18.dp)

                        Icon(
                            imageVector = when {
                                isSyncing -> Icons.Default.Sync
                                isOffline -> Icons.Default.CloudOff
                                else -> Icons.Default.CloudQueue
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = iconModifier
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            val mainText = when {
                                isSyncing -> syncItem ?: "Sincronizando con ERP central TEQMED..."
                                isOffline -> if (pendingCount > 0) {
                                    "Modo offline · $pendingCount cambio(s) en cola local"
                                } else {
                                    "Modo sin conexión. Guardando localmente en dispositivo."
                                }
                                else -> "$pendingCount registro(s) listos para sincronizar"
                            }

                            Text(
                                text = mainText,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (isOffline && pendingCount > 0) {
                                Text(
                                    text = "Se enviarán automáticamente al retomar señal hospitalaria",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    // Botón táctil para forzar sincronización manual
                    if (onSyncClick != null && !isSyncing) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.clickable {
                                onSyncClick()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sincronizar ahora",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sincronizar",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Indicador de progreso continuo o determinado en sincronización activa
                if (isSyncing) {
                    if (syncProgress != null && syncProgress in 0f..1f) {
                        LinearProgressIndicator(
                            progress = { syncProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = Color(0xFF60A5FA),
                            trackColor = Color(0xFF1E40AF)
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = Color(0xFF60A5FA),
                            trackColor = Color(0xFF1E40AF)
                        )
                    }
                }
            }
        }
    }
}
