package com.example.teqgastos.ui.screens.tecnico

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.ui.components.formatClp
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.viewmodel.TecnicoViewModel
import com.example.teqgastos.ui.viewmodel.TrasladoTeqRuta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportarTeqRutaScreen(
    rendicionId: Long,
    viewModel: TecnicoViewModel,
    onNavigateBack: () -> Unit
) {
    // Traslados calculados simulados desde la API / caso TeqRuta
    val trasladosDisponibles = remember {
        listOf(
            TrasladoTeqRuta(
                id = "TR-01",
                tramo = "Concepción ➔ Hospital Los Ángeles",
                kilometros = 132.5,
                peajesClp = 4900.0,
                combustibleClp = 23800.0,
                fecha = "2026-10-18"
            ),
            TrasladoTeqRuta(
                id = "TR-02",
                tramo = "Los Ángeles ➔ Hospital Regional Temuco",
                kilometros = 168.0,
                peajesClp = 6200.0,
                combustibleClp = 31000.0,
                fecha = "2026-10-19"
            ),
            TrasladoTeqRuta(
                id = "TR-03",
                tramo = "Temuco ➔ Hospital Las Higueras (Retorno)",
                kilometros = 285.0,
                peajesClp = 9500.0,
                combustibleClp = 48500.0,
                fecha = "2026-10-20"
            )
        )
    }

    val seleccionados = remember { mutableStateListOf<TrasladoTeqRuta>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar desde TeqRuta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked == true) seleccionados.add(traslado) else seleccionados.remove(traslado)
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(traslado.tramo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Fecha: ${traslado.fecha} · Distancia: ${traslado.kilometros} km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(6.dp))
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
                        }
                    }
                }
            }
        }
    }
}
