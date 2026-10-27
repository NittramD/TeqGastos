package com.example.teqgastos.ui.screens.supervisor

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.foundation.lazy.LazyRow
import com.example.teqgastos.data.local.entity.AuditoriaEntity
import com.example.teqgastos.data.local.entity.GastoEntity
import com.example.teqgastos.ui.components.EstadoGastoBadge
import com.example.teqgastos.ui.components.EstadoRendicionBadge
import com.example.teqgastos.ui.components.GlassmorphicButton
import com.example.teqgastos.ui.components.NielsenHelpDialog
import com.example.teqgastos.ui.components.TeqConfirmationDialog
import com.example.teqgastos.ui.components.formatClp
import com.example.teqgastos.ui.theme.NeuBackground
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSuccess
import com.example.teqgastos.ui.theme.TeqWarning
import com.example.teqgastos.ui.viewmodel.SupervisorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorBandejaScreen(
    viewModel: SupervisorViewModel,
    onNavigateToRevision: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val rendiciones by viewModel.rendicionesPendientes.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }

    if (showHelpDialog) {
        NielsenHelpDialog(onDismissRequest = { showHelpDialog = false })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bandeja de Aprobación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TeqPrimary)
                        Text("Jefatura de Servicio Técnico · TEQMED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Ayuda y Políticas", tint = TeqPrimary)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cambiar Rol")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            item {
                Text(
                    text = "Rendiciones Pendientes de tu Equipo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Revisa gastos en terreno y aprueba o devuelve con observaciones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (rendiciones.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = TeqSuccess, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("¡Estás al día!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("No tienes rendiciones pendientes de revisión.", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                items(rendiciones, key = { it.rendicion.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToRevision(item.rendicion.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = TeqPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(item.rendicion.tecnicoNombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                }
                                EstadoRendicionBadge(estado = item.rendicion.estado)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(item.rendicion.titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(item.rendicion.centroCostoNombre, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text("Total a Revisar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(item.totalRendidoClp.formatClp(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TeqPrimary)
                                }
                                Text(
                                    text = "${item.gastos.size} gastos registrados",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionRendicionScreen(
    rendicionId: Long,
    viewModel: SupervisorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToVisorFoto: (Long) -> Unit
) {
    LaunchedEffect(rendicionId) {
        viewModel.loadRendicion(rendicionId)
    }

    val rendicionConGastos by viewModel.currentRendicion.collectAsState()
    val auditoriaList by viewModel.getAuditoriaFlow(rendicionId).collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val uiMessage by viewModel.uiMessage.collectAsState()

    var gastoParaObservar by remember { mutableStateOf<GastoEntity?>(null) }
    var textoObservacion by remember { mutableStateOf("") }
    var observacionError by remember { mutableStateOf<String?>(null) }
    var mostrarModalAuditoria by remember { mutableStateOf(false) }
    var showConfirmAprobarDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiMessage()
        }
    }

    if (showHelpDialog) {
        NielsenHelpDialog(onDismissRequest = { showHelpDialog = false })
    }

    if (showConfirmAprobarDialog && rendicionConGastos != null) {
        val rItem = rendicionConGastos!!
        TeqConfirmationDialog(
            title = "¿Aprobar Rendición de Servicio?",
            message = "¿Confirmas la aprobación de '${rItem.rendicion.titulo}' por ${rItem.totalRendidoClp.formatClp()}? Pasará formalmente a Finanzas para el proceso de liquidación contable y reembolso.",
            confirmText = "Aprobar y Derivar",
            cancelText = "Continuar Revisando",
            isDestructive = false,
            icon = Icons.Default.CheckCircle,
            onConfirm = {
                showConfirmAprobarDialog = false
                viewModel.aprobarTodaLaRendicion(rItem.rendicion.id) {
                    onNavigateBack()
                }
            },
            onDismiss = { showConfirmAprobarDialog = false }
        )
    }

    if (rendicionConGastos == null) {
        Scaffold { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TeqPrimary)
            }
        }
        return
    }

    val item = rendicionConGastos!!
    val r = item.rendicion

    // Diálogo de Auditoría / Bitácora Inalterable
    if (mostrarModalAuditoria) {
        AlertDialog(
            onDismissRequest = { mostrarModalAuditoria = false },
            title = { Text("Bitácora de Auditoría", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    if (auditoriaList.isEmpty()) {
                        item { Text("Sin registros previos en la bitácora.", color = MaterialTheme.colorScheme.outline) }
                    } else {
                        items(auditoriaList) { registro ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = "${registro.fechaHora} · ${registro.accion}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TeqPrimary
                                )
                                Text(
                                    text = "${registro.usuarioNombre} (${registro.usuarioRut})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = registro.detalle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { mostrarModalAuditoria = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Diálogo de Observación de Gasto
    if (gastoParaObservar != null) {
        AlertDialog(
            onDismissRequest = { gastoParaObservar = null },
            title = { Text("Observar Gasto", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Indique la corrección requerida para el técnico (${gastoParaObservar?.categoriaNombre} - ${gastoParaObservar?.montoClp?.formatClp()}):",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Heurística #7 (Eficiencia) y #9 (Diagnóstico de Errores): Sugerencias rápidas
                    Text("Sugerencias rápidas:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    val sugerencias = listOf(
                        "Foto poco legible / borrosa",
                        "RUT no coincide con boleta",
                        "Falta justificar exceso de tope",
                        "Falta desglose o comprobante válido"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        items(sugerencias.size) { index ->
                            val sug = sugerencias[index]
                            FilterChip(
                                selected = textoObservacion.contains(sug),
                                onClick = {
                                    textoObservacion = if (textoObservacion.isBlank()) sug else "$textoObservacion. $sug"
                                    observacionError = null
                                },
                                label = { Text(sug, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = textoObservacion,
                        onValueChange = {
                            textoObservacion = it
                            observacionError = if (it.trim().length < 10) "Debe ingresar al menos 10 caracteres." else null
                        },
                        placeholder = { Text("Ej: Boleta arrugada o ilegible, adjuntar comprobante claro...") },
                        isError = observacionError != null,
                        supportingText = { observacionError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textoObservacion.trim().length >= 10) {
                            viewModel.observarGasto(
                                gastoId = gastoParaObservar!!.id,
                                rendicionId = r.id,
                                comentario = textoObservacion.trim(),
                                onComplete = {
                                    gastoParaObservar = null
                                    textoObservacion = ""
                                }
                            )
                        } else {
                            observacionError = "Ingrese un comentario de al menos 10 caracteres."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TeqWarning)
                ) {
                    Text("Confirmar Observación")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { gastoParaObservar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(r.tecnicoNombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(r.titulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Ayuda y Políticas", tint = TeqPrimary)
                    }
                    IconButton(onClick = { mostrarModalAuditoria = true }) {
                        Icon(Icons.Default.History, contentDescription = "Bitácora de Auditoría", tint = TeqPrimary)
                    }
                    EstadoRendicionBadge(estado = r.estado, modifier = Modifier.padding(end = 12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 6.dp, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Monto Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(item.totalRendidoClp.formatClp(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = TeqPrimary)
                    }
                    GlassmorphicButton(
                        onClick = { showConfirmAprobarDialog = true },
                        cornerRadius = 14.dp
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aprobar Rendición", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(item.gastos, key = { it.id }) { gasto ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(gasto.categoriaNombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            EstadoGastoBadge(estado = gasto.estadoGasto)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${gasto.nombreProveedor} · RUT: ${gasto.rutProveedor}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Folio: ${gasto.nroComprobante} · Fecha: ${gasto.fechaGasto} · ${gasto.formaPago.shortLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = gasto.montoClp.formatClp(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = TeqPrimary
                        )

                        if (!gasto.justificacionTope.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0xFFFFEDD5),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Justificación de exceso de tope:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                                    Text(gasto.justificacionTope, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9A3412))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (gasto.fotoComprobanteUri != null) {
                                Row(
                                    modifier = Modifier.clickable { onNavigateToVisorFoto(gasto.id) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = TeqPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Auditar Comprobante", style = MaterialTheme.typography.labelMedium, color = TeqPrimary, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Sin imagen", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { viewModel.aprobarGasto(gasto.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Aprobar Gasto", tint = TeqSuccess)
                                }
                                IconButton(
                                    onClick = {
                                        gastoParaObservar = gasto
                                        textoObservacion = gasto.observacionSupervisor ?: ""
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Observar Gasto", tint = TeqWarning)
                                }
                                IconButton(
                                    onClick = { viewModel.rechazarGasto(gasto.id, "Rechazado por Jefatura") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Rechazar Gasto", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
