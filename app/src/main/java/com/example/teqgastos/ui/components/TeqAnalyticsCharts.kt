package com.example.teqgastos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.teqgastos.data.local.entity.GastoEntity
import com.example.teqgastos.ui.theme.TeqPrimary

data class SliceData(
    val label: String,
    val value: Double,
    val color: Color
)

// Paleta semántica para categorías de gasto TEQMED
val CATEGORY_COLORS = listOf(
    Color(0xFF0061A4), // Azul TEQMED (Combustible)
    Color(0xFF00A3E0), // Cian médico (Peajes)
    Color(0xFF10B981), // Verde esmeralda (Alimentación)
    Color(0xFFF59E0B), // Ámbar (Alojamiento)
    Color(0xFF8B5CF6), // Violeta (Estacionamiento)
    Color(0xFFEC4899), // Magenta (Insumos urgentes)
    Color(0xFF64748B)  // Gris slate (Otros)
)

/**
 * Gráfico Donut Chart en Compose Canvas con animación de apertura
 */
@Composable
fun TeqDonutChart(
    slices: List<SliceData>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 190.dp,
    strokeWidth: Dp = 26.dp,
    centerTitle: String = "Total Rendido",
    centerSubtitle: String = ""
) {
    val total = slices.sumOf { it.value }.coerceAtLeast(1.0)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750)
        )
    }

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(chartSize)) {
            val strokePx = strokeWidth.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            var startAngle = -90f

            if (slices.isEmpty()) {
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            } else {
                for (slice in slices) {
                    val sweep = ((slice.value / total) * 360f * animationProgress.value).toFloat()
                    if (sweep > 0.5f) {
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweep - 1.5f, // Pequeña separación visual
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                        )
                    }
                    startAngle += (slice.value / total * 360f).toFloat()
                }
            }
        }

        // Centro con texto informativo
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (centerSubtitle.isNotBlank()) {
                Text(
                    text = centerSubtitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary
                )
            }
        }
    }
}

/**
 * Gráfico de Barras horizontales para comparar montos por Centro de Costo o Categoría
 */
@Composable
fun TeqHorizontalBarChart(
    items: List<SliceData>,
    modifier: Modifier = Modifier
) {
    val maxValue = items.maxOfOrNull { it.value }?.coerceAtLeast(1.0) ?: 1.0

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (item in items) {
            val ratio = (item.value / maxValue).toFloat().coerceIn(0.05f, 1f)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = item.value.formatClp(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = item.color
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .height(8.dp)
                            .background(item.color, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

/**
 * Modal Completo de Análisis Financiero y Distribución de Gastos
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeqGastosAnalyticsDialog(
    gastos: List<GastoEntity>,
    onDismissRequest: () -> Unit
) {
    val totalGastos = gastos.sumOf { it.montoClp }

    // Agrupación por categoría
    val slices = remember(gastos) {
        val grouped = gastos.groupBy { it.categoriaNombre }
        grouped.entries.mapIndexed { index, entry ->
            val color = CATEGORY_COLORS[index % CATEGORY_COLORS.size]
            val subtotal = entry.value.sumOf { it.montoClp }
            SliceData(label = entry.key, value = subtotal, color = color)
        }.sortedByDescending { it.value }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = TeqPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Análisis Financiero",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Donut Chart
                TeqDonutChart(
                    slices = slices,
                    chartSize = 180.dp,
                    centerTitle = "Total",
                    centerSubtitle = totalGastos.formatClp()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Leyenda en FlowRow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (slice in slices) {
                        val pct = if (totalGastos > 0) ((slice.value / totalGastos) * 100).toInt() else 0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = slice.color.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, slice.color.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(slice.color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${slice.label} ($pct%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = slice.color
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Barras comparativas de mayor a menor
                Text(
                    text = "Montos por Categoría:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TeqHorizontalBarChart(items = slices, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
