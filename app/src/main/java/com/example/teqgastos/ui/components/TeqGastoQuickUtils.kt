package com.example.teqgastos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.ui.theme.TeqPrimary

/**
 * Semáforo y barra visual de consumo del Fondo por Rendir (Anticipo TEQMED)
 */
@Composable
fun TeqFondoSemaforoBar(
    fondoInicial: Double,
    totalRendidoFondo: Double,
    modifier: Modifier = Modifier
) {
    if (fondoInicial <= 0) return

    val porcentaje = (totalRendidoFondo / fondoInicial).coerceIn(0.0, 1.5)
    val saldoRemanente = fondoInicial - totalRendidoFondo

    val (colorBarra, textoEstado, badgeColor) = when {
        porcentaje < 0.70 -> Triple(Color(0xFF10B981), "Anticipo Disponible (Consumo Seguro)", Color(0xFF10B981))
        porcentaje <= 0.95 -> Triple(Color(0xFFF59E0B), "Anticipo Próximo a Agotarse (>70%)", Color(0xFFF59E0B))
        else -> Triple(Color(0xFFEF4444), "Anticipo Superado (Requiere Reembolso)", Color(0xFFEF4444))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(badgeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Semáforo Fondo por Rendir",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${(porcentaje * 100).toInt()}% consumido",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso tricolor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(porcentaje.toFloat().coerceAtMost(1f))
                        .height(10.dp)
                        .background(colorBarra, RoundedCornerShape(5.dp))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Anticipo TEQMED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(fondoInicial.formatClp(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Gastado con Fondo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(totalRendidoFondo.formatClp(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (saldoRemanente >= 0) "Saldo Disponible" else "A Reembolsar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = saldoRemanente.formatClp(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (saldoRemanente >= 0) TeqPrimary else Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = textoEstado,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Calculador de IVA 19% chileno (Boleta Exenta vs. Factura Afecta con Crédito Fiscal para TEQMED)
 */
@Composable
fun TeqIvaCalculatorRow(
    montoTotal: Double,
    esFacturaAfecta: Boolean,
    onTipoCambiado: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val neto = if (esFacturaAfecta) montoTotal / 1.19 else montoTotal
    val iva = if (esFacturaAfecta) montoTotal - neto else 0.0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !esFacturaAfecta,
                onClick = { onTipoCambiado(false) },
                label = { Text("Boleta / Exento") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TeqPrimary,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
            FilterChip(
                selected = esFacturaAfecta,
                onClick = { onTipoCambiado(true) },
                label = { Text("Factura Afecta (IVA 19%)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TeqPrimary,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        AnimatedVisibility(visible = esFacturaAfecta && montoTotal > 0) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF10B981).copy(alpha = 0.10f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Monto Neto (Gasto Real):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(neto.formatClp(), fontWeight = FontWeight.Bold, color = TeqPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("IVA Crédito Fiscal (19% Recuperable):", style = MaterialTheme.typography.labelSmall, color = Color(0xFF047857))
                        Text(iva.formatClp(), fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                    }
                }
            }
        }
    }
}

data class GastoTemplate(
    val titulo: String,
    val categoria: String,
    val proveedor: String,
    val rut: String,
    val montoSugerido: Double,
    val icon: ImageVector
)

val PLANTILLAS_GASTO_RAPIDO = listOf(
    GastoTemplate("Copec Ruta 5", "Combustible", "Compañía de Petróleos de Chile Copec S.A.", "99.520.000-7", 25000.0, Icons.Default.LocalGasStation),
    GastoTemplate("Peaje Autopista", "Peajes", "Sociedad Concesionaria Autopistas de Chile", "96.852.140-5", 4900.0, Icons.Default.Toll),
    GastoTemplate("Parking Hospital", "Estacionamiento", "Estacionamientos Médicos SpA", "76.432.190-2", 3500.0, Icons.Default.LocalParking),
    GastoTemplate("Almuerzo Terreno", "Alimentación", "Restaurant y Servicios Gastronómicos Ltda.", "77.123.456-0", 12500.0, Icons.Default.Restaurant)
)

/**
 * Plantillas de 1-Tap para rellenar gastos comunes en terreno
 */
@Composable
fun TeqQuickExpenseTemplates(
    onTemplateSelected: (GastoTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = TeqPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Plantillas Rápidas (1-Tap):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(PLANTILLAS_GASTO_RAPIDO) { template ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onTemplateSelected(template) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(template.icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = TeqPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = template.titulo,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
