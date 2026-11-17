package com.example.teqgastos.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.ui.theme.TeqPrimary
import kotlinx.coroutines.delay

data class VehiculoTeqmed(
    val nombre: String,
    val patente: String,
    val rendimientoKmPorLitro: Double
)

val FLOTA_TEQMED = listOf(
    VehiculoTeqmed("Camioneta Toyota Hilux 4x4", "HG-KP-42", 11.5),
    VehiculoTeqmed("Furgón Técnico Peugeot Partner", "LL-RT-89", 15.0),
    VehiculoTeqmed("SUV Servicio Médico Mitsubishi L200", "PD-TR-14", 12.0)
)

const val PRECIO_LITRO_COMBUSTIBLE_CLP = 1320.0 // Diésel promedio Chile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeqOdometerWidget(
    onTramoFinalizado: (kilometros: Double, costoCombustible: Double) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var isRecording by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var currentKm by remember { mutableDoubleStateOf(0.0) }
    var vehiculoSeleccionado by remember { mutableStateOf(FLOTA_TEQMED.first()) }
    var vehiculoExpanded by remember { mutableStateOf(false) }

    // Simulación de avance en tiempo real cuando está grabando
    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000L)
            elapsedSeconds += 1
            // Simula avance proporcional de 80 km/h en carretera (~0.022 km por segundo)
            currentKm += 0.022
        }
    }

    val minutos = elapsedSeconds / 60
    val segundos = elapsedSeconds % 60
    val tiempoStr = String.format("%02d:%02d", minutos, segundos)

    val litrosConsumidos = (currentKm / vehiculoSeleccionado.rendimientoKmPorLitro)
    val costoCombustible = litrosConsumidos * PRECIO_LITRO_COMBUSTIBLE_CLP

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecording) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isRecording) Color(0xFFEF4444) else TeqPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Speed else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Odómetro GPS Flota TEQMED",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isRecording) "Grabando recorrido en ruta..." else "Listo para iniciar viaje",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isRecording) Color(0xFFB91C1C) else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (isRecording) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tiempoStr, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de Vehículo TEQMED
            if (!isRecording) {
                ExposedDropdownMenuBox(
                    expanded = vehiculoExpanded,
                    onExpandedChange = { vehiculoExpanded = !vehiculoExpanded }
                ) {
                    OutlinedTextField(
                        value = "${vehiculoSeleccionado.nombre} (${vehiculoSeleccionado.patente})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vehículo Asignado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehiculoExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = vehiculoExpanded,
                        onDismissRequest = { vehiculoExpanded = false }
                    ) {
                        FLOTA_TEQMED.forEach { vehiculo ->
                            DropdownMenuItem(
                                text = { Text("${vehiculo.nombre} (${vehiculo.patente}) - ${vehiculo.rendimientoKmPorLitro} km/L") },
                                onClick = {
                                    vehiculoSeleccionado = vehiculo
                                    vehiculoExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Métricas en Vivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Distancia", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = String.format("%.2f km", currentKm),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TeqPrimary
                    )
                }
                Column {
                    Text("Combustible Est.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = String.format("%.1f L", litrosConsumidos),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("Costo Est. CLP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = costoCombustible.formatClp(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón de Inicio / Parada
            Button(
                onClick = {
                    if (isRecording) {
                        isRecording = false
                        onTramoFinalizado(currentKm, costoCombustible)
                    } else {
                        currentKm = 0.0
                        elapsedSeconds = 0
                        isRecording = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color(0xFFDC2626) else TeqPrimary
                )
            ) {
                Icon(if (isRecording) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRecording) "Finalizar Tramo y Registrar" else "Iniciar Grabación de Tramo en Ruta",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
