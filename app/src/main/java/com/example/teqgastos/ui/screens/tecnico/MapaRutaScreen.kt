package com.example.teqgastos.ui.screens.tecnico

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.model.CoordenadaGps
import com.example.teqgastos.model.TrasladoTeqRuta
import com.example.teqgastos.ui.components.TeqOdometerWidget
import com.example.teqgastos.ui.components.TeqRutaMapViewer
import com.example.teqgastos.ui.components.formatClp
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.neuBackgroundColor

val TRAMOS_CATALOGO_TEQRUTA = listOf(
    TrasladoTeqRuta(
        id = "TR-01",
        tramo = "Concepción ➔ Hospital Los Ángeles",
        kilometros = 132.5,
        peajesClp = 4900.0,
        combustibleClp = 23800.0,
        fecha = "2026-10-18",
        origenNombre = "Base TEQMED Concepción",
        origenLat = -36.8270,
        origenLng = -73.0503,
        destinoNombre = "Hospital Dr. Víctor Ríos Ruiz (Los Ángeles)",
        destinoLat = -37.4697,
        destinoLng = -72.3537,
        puntosRuta = listOf(
            CoordenadaGps(-36.8850, -72.8250, "Peaje Chaimávida (Ruta 146)"),
            CoordenadaGps(-37.0340, -72.4040, "Enlace Cabrero / Ruta 5 Sur"),
            CoordenadaGps(-37.2150, -72.3830, "Salto del Laja")
        ),
        peajesDetalle = listOf("Peaje Chaimávida", "Peaje Cabrero"),
        tiempoEstimado = "1h 45m"
    ),
    TrasladoTeqRuta(
        id = "TR-02",
        tramo = "Los Ángeles ➔ Hospital Regional Temuco",
        kilometros = 168.0,
        peajesClp = 6200.0,
        combustibleClp = 31000.0,
        fecha = "2026-10-19",
        origenNombre = "Hospital Los Ángeles",
        origenLat = -37.4697,
        origenLng = -72.3537,
        destinoNombre = "Hospital Dr. Hernán Henríquez Aravena (Temuco)",
        destinoLat = -38.7397,
        destinoLng = -72.5901,
        puntosRuta = listOf(
            CoordenadaGps(-37.7170, -72.2410, "Mulchén / Peaje Las Maicas"),
            CoordenadaGps(-37.9550, -72.4340, "Collipulli / Viaducto Malleco"),
            CoordenadaGps(-38.2320, -72.3330, "Victoria / Peaje Púa"),
            CoordenadaGps(-38.5310, -72.4410, "Lautaro")
        ),
        peajesDetalle = listOf("Peaje Las Maicas", "Peaje Púa"),
        tiempoEstimado = "2h 10m"
    ),
    TrasladoTeqRuta(
        id = "TR-03",
        tramo = "Temuco ➔ Hospital Las Higueras Talcahuano",
        kilometros = 285.0,
        peajesClp = 9500.0,
        combustibleClp = 48500.0,
        fecha = "2026-10-20",
        origenNombre = "Hospital Temuco",
        origenLat = -38.7397,
        origenLng = -72.5901,
        destinoNombre = "Hospital Las Higueras (Talcahuano)",
        destinoLat = -36.7262,
        destinoLng = -73.1166,
        puntosRuta = listOf(
            CoordenadaGps(-38.2320, -72.3330, "Victoria"),
            CoordenadaGps(-37.4697, -72.3537, "Los Ángeles"),
            CoordenadaGps(-37.0340, -72.4040, "Cabrero / Ruta 146"),
            CoordenadaGps(-36.8270, -73.0503, "Concepción")
        ),
        peajesDetalle = listOf("Peaje Púa", "Peaje Las Maicas", "Peaje Chaimávida"),
        tiempoEstimado = "3h 30m"
    ),
    TrasladoTeqRuta(
        id = "TR-04",
        tramo = "Concepción ➔ Hospital Herminda Martín Chillán",
        kilometros = 104.0,
        peajesClp = 4700.0,
        combustibleClp = 19200.0,
        fecha = "2026-10-21",
        origenNombre = "Base TEQMED Concepción",
        origenLat = -36.8270,
        origenLng = -73.0503,
        destinoNombre = "Hospital Clínico Herminda Martín (Chillán)",
        destinoLat = -36.6066,
        destinoLng = -72.1034,
        puntosRuta = listOf(
            CoordenadaGps(-36.7450, -72.7630, "Autopista del Itata / Peaje Agua Amarilla")
        ),
        peajesDetalle = listOf("Peaje Agua Amarilla"),
        tiempoEstimado = "1h 15m"
    )
)

/**
 * Pantalla Completa para Visualización de Tramos Recorridos en Mapa
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaRutaScreen(
    rendicionId: Long = 0L,
    initialTramoId: String? = null,
    onNavigateBack: () -> Unit
) {
    var selectedTramoId by remember { mutableStateOf(initialTramoId ?: "TR-01") }
    var showOdometer by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Mapa de Tramos Recorridos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TeqPrimary
                        )
                        Text(
                            text = "TeqRuta · Macrozona Sur TEQMED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showOdometer = !showOdometer }) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Odómetro Flota TEQMED",
                            tint = if (showOdometer) Color(0xFFDC2626) else TeqPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = neuBackgroundColor())
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showOdometer) {
                TeqOdometerWidget(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            // Selector horizontal de tramos
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TRAMOS_CATALOGO_TEQRUTA) { t ->
                    val isSelected = t.id == selectedTramoId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTramoId = t.id },
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

            // Visor de Mapa
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                TeqRutaMapViewer(
                    traslados = TRAMOS_CATALOGO_TEQRUTA,
                    selectedTramoId = selectedTramoId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
