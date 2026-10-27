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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.teqgastos.data.local.entity.RendicionEntity
import com.example.teqgastos.model.EstadoRendicion
import com.example.teqgastos.ui.components.EstadoRendicionBadge
import com.example.teqgastos.ui.components.GlassmorphicExtendedFab
import com.example.teqgastos.ui.components.GlassmorphicFilterChip
import com.example.teqgastos.ui.components.NielsenHelpDialog
import com.example.teqgastos.ui.components.NeumorphicCard
import com.example.teqgastos.ui.components.NeumorphicMetricCard
import com.example.teqgastos.ui.components.OfflineNoticeBanner
import com.example.teqgastos.ui.components.formatClp
import com.example.teqgastos.ui.theme.NeuBackground
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.viewmodel.TecnicoViewModel
import com.example.teqgastos.util.NetworkMonitor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TecnicoHomeScreen(
    viewModel: TecnicoViewModel,
    networkMonitor: NetworkMonitor,
    onNavigateToNuevaRendicion: () -> Unit,
    onNavigateToDetalle: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val rendiciones by viewModel.rendiciones.collectAsState()
    val isOnlineReal by networkMonitor.isOnlineFlow.collectAsState(initial = true)
    var manualOfflineToggle by remember { mutableStateOf(false) }

    val isEffectiveOffline = !isOnlineReal || manualOfflineToggle
    var selectedFilter by remember { mutableStateOf<EstadoRendicion?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val uiMessage by viewModel.uiMessage.collectAsState()

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiMessage()
        }
    }

    val filteredList = remember(rendiciones, selectedFilter, searchQuery) {
        rendiciones.filter { r ->
            val matchFilter = selectedFilter == null || r.estado == selectedFilter
            val matchSearch = searchQuery.isBlank() ||
                    r.titulo.contains(searchQuery, ignoreCase = true) ||
                    r.centroCostoNombre.contains(searchQuery, ignoreCase = true)
            matchFilter && matchSearch
        }
    }

    if (showHelpDialog) {
        NielsenHelpDialog(onDismissRequest = { showHelpDialog = false })
    }

    Scaffold(
        containerColor = NeuBackground, // Fondo Neumórfico suave
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TeqGastos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TeqPrimary
                            )
                            Text(
                                text = "TEQMED SpA · Terreno",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { manualOfflineToggle = !manualOfflineToggle }) {
                            Icon(
                                imageVector = if (isEffectiveOffline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                contentDescription = "Modo Conexión",
                                tint = if (isEffectiveOffline) Color(0xFFE65100) else Color(0xFF2E7D32)
                            )
                        }
                    },
                    actions = {
                        // Heurística #10: Ayuda y Documentación (Leyes de Nielsen y Políticas)
                        IconButton(onClick = { showHelpDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Centro de Ayuda y Heurísticas de Nielsen",
                                tint = TeqPrimary
                            )
                        }
                        // Heurística #1: Visibilidad del Estado del Sistema (Sincronización)
                        IconButton(onClick = { viewModel.sincronizarManual() }) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sincronizar",
                                tint = TeqPrimary
                            )
                        }
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cambiar Rol / Salir",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NeuBackground)
                )
                OfflineNoticeBanner(isOffline = isEffectiveOffline)
            }
        },
        // Botón FAB Flotante Exclusivo con Efecto Glassmorphism (Vidrio Esmerilado)
        floatingActionButton = {
            GlassmorphicExtendedFab(
                onClick = onNavigateToNuevaRendicion,
                text = "Nueva Rendición",
                icon = Icons.Default.Add
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Métricas con Neumorphism (Relieve táctil 3D)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeumorphicMetricCard(
                        title = "Rendiciones",
                        amount = "${rendiciones.size}",
                        subtitle = "Total comisiones",
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        modifier = Modifier.weight(1f)
                    )
                    NeumorphicMetricCard(
                        title = "Anticipos",
                        amount = rendiciones.sumOf { it.fondoInicial }.formatClp(),
                        subtitle = "Fondos entregados",
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Alerta si hay rendiciones observadas (Tarjeta Neumorphic)
            val observadasCount = rendiciones.count { it.estado == EstadoRendicion.OBSERVADA }
            if (observadasCount > 0) {
                item {
                    NeumorphicCard(
                        backgroundColor = Color(0xFFFFEDD5),
                        cornerRadius = 14.dp,
                        elevation = 3.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC2410C))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tienes $observadasCount rendición(es) con observaciones de la Jefatura.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9A3412)
                            )
                        }
                    }
                }
            }

            // Heurística #7 (Flexibilidad y Eficiencia): Buscador instantáneo
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por comisión o centro de costo...", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TeqPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                        focusedBorderColor = TeqPrimary,
                        unfocusedBorderColor = Color(0xFFD5DFEC)
                    ),
                    singleLine = true
                )
            }

            // Chips Exclusivos con Efecto Glassmorphism (Cápsulas de Cristal Translúcido)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Comisiones de Servicio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (searchQuery.isNotBlank() || selectedFilter != null) {
                        Text(
                            text = "${filteredList.size} resultado(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TeqPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        GlassmorphicFilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = "Todas (${rendiciones.size})"
                        )
                    }
                    items(EstadoRendicion.values()) { estado ->
                        val count = rendiciones.count { it.estado == estado }
                        if (count > 0 || estado == EstadoRendicion.BORRADOR) {
                            GlassmorphicFilterChip(
                                selected = selectedFilter == estado,
                                onClick = { selectedFilter = if (selectedFilter == estado) null else estado },
                                label = "${estado.label} ($count)"
                            )
                        }
                    }
                }
            }

            // Lista de Rendiciones con Tarjetas Neumórficas (Relieve suave)
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No se encontraron rendiciones que coincidan con \"$searchQuery\"." else "No hay rendiciones en este estado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Restablecer filtros",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TeqPrimary,
                                    modifier = Modifier.clickable {
                                        searchQuery = ""
                                        selectedFilter = null
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { rendicion ->
                    NeumorphicRendicionCardItem(
                        rendicion = rendicion,
                        onClick = { onNavigateToDetalle(rendicion.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

// Tarjeta Neumorphic de Rendición
@Composable
fun NeumorphicRendicionCardItem(
    rendicion: RendicionEntity,
    onClick: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        elevation = 5.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rendicion.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TeqPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = rendicion.centroCostoNombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            EstadoRendicionBadge(estado = rendicion.estado)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Período",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${rendicion.fechaInicio} al ${rendicion.fechaFin}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            if (rendicion.fondoInicial > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Fondo Inicial",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = rendicion.fondoInicial.formatClp(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TeqPrimary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
