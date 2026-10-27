package com.example.teqgastos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.EstadoRendicion
import com.example.teqgastos.ui.theme.TeqError
import com.example.teqgastos.ui.theme.TeqSuccess
import com.example.teqgastos.ui.theme.TeqWarning
import java.text.NumberFormat
import java.util.Locale

// Formateador monetario chileno
fun Double.formatClp(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0
    return format.format(this)
}

// Validador de RUT Chileno (Módulo 11)
object RutValidator {
    fun formatRut(input: String): String {
        val clean = input.replace(".", "").replace("-", "").uppercase(Locale.getDefault())
        if (clean.length < 2) return clean
        val dv = clean.takeLast(1)
        val body = clean.dropLast(1)
        return try {
            val formattedBody = body.reversed().chunked(3).joinToString(".").reversed()
            "$formattedBody-$dv"
        } catch (e: Exception) {
            input
        }
    }

    fun isValid(rut: String): Boolean {
        val clean = rut.replace(".", "").replace("-", "").uppercase(Locale.getDefault())
        if (clean.length < 8) return false
        val dv = clean.last()
        val cuerpo = clean.dropLast(1)

        var suma = 0
        var multiplicador = 2
        for (i in cuerpo.indices.reversed()) {
            val digito = cuerpo[i].digitToIntOrNull() ?: return false
            suma += digito * multiplicador
            multiplicador = if (multiplicador < 7) multiplicador + 1 else 2
        }

        val resto = 11 - (suma % 11)
        val dvEsperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> ('0'.code + resto).toChar()
        }
        return dv == dvEsperado
    }
}

// Badge de Estado para Rendiciones
@Composable
fun EstadoRendicionBadge(estado: EstadoRendicion, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (estado) {
        EstadoRendicion.BORRADOR -> Triple(Color(0xFFE2E8F0), Color(0xFF475569), Icons.Default.HourglassEmpty)
        EstadoRendicion.ENVIADA -> Triple(Color(0xFFDBEAFE), Color(0xFF1E40AF), Icons.Default.Sync)
        EstadoRendicion.OBSERVADA -> Triple(Color(0xFFFFEDD5), Color(0xFFC2410C), Icons.Default.Warning)
        EstadoRendicion.APROBADA_JEFATURA -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.CheckCircle)
        EstadoRendicion.APROBADA_FINANZAS -> Triple(Color(0xFFCCFBF1), Color(0xFF0F766E), Icons.Default.CheckCircle)
        EstadoRendicion.REEMBOLSADA -> Triple(Color(0xFFEDE9FE), Color(0xFF6D28D9), Icons.Default.CheckCircle)
        EstadoRendicion.RECHAZADA -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), Icons.Default.Error)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = estado.label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Badge de Estado para Gastos
@Composable
fun EstadoGastoBadge(estado: EstadoGasto, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (estado) {
        EstadoGasto.REGISTRADO -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
        EstadoGasto.APROBADO -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        EstadoGasto.OBSERVADO -> Pair(Color(0xFFFFEDD5), Color(0xFFC2410C))
        EstadoGasto.RECHAZADO -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = estado.label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// Tarjeta de Métrica Resumen
@Composable
fun MetricCard(
    title: String,
    amount: Double,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount.formatClp(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

// Banner Transversal Offline
@Composable
fun OfflineNoticeBanner(isOffline: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = isOffline,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF334155))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Modo sin conexión. Guardando de forma local en el dispositivo.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Diálogo de Confirmación Reutilizable (Heurísticas #3 Libertad del Usuario y #5 Prevención de Errores)
@Composable
fun TeqConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirmar",
    cancelText: String = "Cancelar",
    isDestructive: Boolean = false,
    icon: ImageVector = Icons.Default.Warning,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) TeqError else TeqWarning,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) TeqError else com.example.teqgastos.ui.theme.TeqPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            androidx.compose.material3.OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(cancelText)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

