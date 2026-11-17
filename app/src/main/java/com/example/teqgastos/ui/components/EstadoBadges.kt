package com.example.teqgastos.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.EstadoRendicion

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
