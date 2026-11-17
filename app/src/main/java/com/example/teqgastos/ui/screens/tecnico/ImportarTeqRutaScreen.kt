package com.example.teqgastos.ui.screens.tecnico

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.model.TrasladoTeqRuta
import com.example.teqgastos.ui.components.TeqRutaMapViewer
import com.example.teqgastos.ui.components.formatClp
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.viewmodel.TecnicoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportarTeqRutaScreen(
    rendicionId: Long,
    viewModel: TecnicoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMapa: (rendicionId: Long, tramoId: String?) -> Unit = { _, _ -> }
) {
    // Catálogo oficial de tramos en Macrozona Sur
    val trasladosDisponibles = remember { TRAMOS_CATALOGO_TEQRUTA }
    val seleccionados = remember { mutableStateListOf<TrasladoTeqRuta>() }

    // Control de vista: 0 = Lista de selección, 1 = Visor de mapa GPS integrado
    var vistaModo by remember { mutableIntStateOf(0) }
    var tramoSeleccionadoEnMapaId by remember { mutableStateOf(trasladosDisponibles.firstOrNull()?.id ?: "TR-01") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar desde TeqRuta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToMapa(rendicionId, tramoSeleccionadoEnMapaId) }) {
                        Icon(Icons.Default.Map, contentDescription = "Pantalla Completa Mapa", tint = TeqPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (seleccionados.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val totalCombustible = seleccionados.sumOf { it.combustibleClp }
                        val totalPeajes = seleccionados.sumOf { it.peajesClp }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total a Importar (${seleccionados.size} tramos):", fontWeight = FontWeight.SemiBold)
                            Text((totalCombustible + totalPeajes).formatClp(), fontWeight = FontWeight.Bold, color = TeqPrimary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.importarTrasladosTeqRuta(rendicionId, seleccionados.toList()) {
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TeqPrimary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Importar Gastos a Rendición", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Pestañas de Navegación entre Lista y Mapa
            TabRow(
                selectedTabIndex = vistaModo,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TeqPrimary
            ) {
                Tab(
                    selected = vistaModo == 0,
                    onClick = { vistaModo = 0 },
                    text = { Text("Lista de Tramos (${trasladosDisponibles.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = vistaModo == 1,
                    onClick = { vistaModo = 1 },
                    text = { Text("Visor en Mapa GPS", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (vistaModo == 0) {
                // Modo 0: Lista Detallada de Tramos con Selección y botón a Mapa
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AltRoute, contentDescription = null, tint = TeqPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Traslados Registrados en TeqRuta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TeqPrimary
                            )
                        }
                        Text(
                            text = "Seleccione los viajes realizados para convertirlos automáticamente en gastos de combustible y peajes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(trasladosDisponibles) { traslado ->
                        val isChecked = seleccionados.contains(traslado)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) seleccionados.remove(traslado) else seleccionados.add(traslado)
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked == true) seleccionados.add(traslado) else seleccionados.remove(traslado)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(traslado.tramo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text("Fecha: ${traslado.fecha} · Distancia: ${traslado.kilometros} km · Tiempo: ${traslado.tiempoEstimado}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Column {
                                            Text("Peajes TAG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                            Text(traslado.peajesClp.formatClp(), fontWeight = FontWeight.SemiBold, color = TeqPrimary)
                                        }
                                        Column {
                                            Text("Combustible", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                            Text(traslado.combustibleClp.formatClp(), fontWeight = FontWeight.SemiBold, color = TeqPrimary)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { onNavigateToMapa(rendicionId, traslado.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp), tint = TeqPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ver en Mapa", style = MaterialTheme.typography.labelSmall, color = TeqPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Modo 1: Visor Interactivo Integrado de Mapa
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    // Selector horizontal de tramo
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        items(trasladosDisponibles) { t ->
                            val isSelected = t.id == tramoSeleccionadoEnMapaId
                            FilterChip(
                                selected = isSelected,
                                onClick = { tramoSeleccionadoEnMapaId = t.id },
                                label = { Text(t.id + ": " + t.tramo.substringBefore("➔").trim() + " ➔ " + t.destinoNombre.substringBefore("(").trim()) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.AltRoute,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TeqPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Contenedor del Mapa Interactivo
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        TeqRutaMapViewer(
                            traslados = trasladosDisponibles,
                            selectedTramoId = tramoSeleccionadoEnMapaId,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Acciones Rápidas sobre el tramo actual en el mapa
                    val tramoActual = trasladosDisponibles.firstOrNull { it.id == tramoSeleccionadoEnMapaId }
                    if (tramoActual != null) {
                        val isTramoChecked = seleccionados.contains(tramoActual)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (isTramoChecked) seleccionados.remove(tramoActual) else seleccionados.add(tramoActual)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTramoChecked) Color(0xFF2E7D32) else TeqPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(if (isTramoChecked) Icons.Default.Check else Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTramoChecked) "Tramo Seleccionado ✓" else "Seleccionar para Importar", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { onNavigateToMapa(rendicionId, tramoActual.id) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = "Pantalla Completa", tint = TeqPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
