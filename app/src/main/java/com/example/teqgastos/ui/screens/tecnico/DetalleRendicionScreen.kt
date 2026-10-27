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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.teqgastos.data.local.entity.GastoEntity
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.EstadoRendicion
import com.example.teqgastos.model.FormaPago
import com.example.teqgastos.ui.components.EstadoGastoBadge
import com.example.teqgastos.ui.components.EstadoRendicionBadge
import com.example.teqgastos.ui.components.GlassmorphicButton
import com.example.teqgastos.ui.components.GlassmorphicExtendedFab
import com.example.teqgastos.ui.components.NielsenHelpDialog
import com.example.teqgastos.ui.components.NeumorphicCard
import com.example.teqgastos.ui.components.TeqConfirmationDialog
import com.example.teqgastos.ui.components.formatClp
import com.example.teqgastos.ui.theme.NeuBackground
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSecondary
import com.example.teqgastos.ui.theme.TeqWarning
import com.example.teqgastos.ui.viewmodel.TecnicoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRendicionScreen(
    rendicionId: Long,
    viewModel: TecnicoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNuevoGasto: (Long) -> Unit,
    onNavigateToEditarGasto: (Long, Long) -> Unit,
    onNavigateToVisorFoto: (Long) -> Unit,
    onNavigateToImportarTeqRuta: (Long) -> Unit
) {
    LaunchedEffect(rendicionId) {
        viewModel.loadRendicionDetalle(rendicionId)
    }

    val rendicionConGastos by viewModel.currentRendicionConGastos.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiMessage by viewModel.uiMessage.collectAsState()

    var gastoToDelete by remember { mutableStateOf<GastoEntity?>(null) }
    var showConfirmSendDialog by remember { mutableStateOf(false) }
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

    if (gastoToDelete != null) {
        val gasto = gastoToDelete!!
        TeqConfirmationDialog(
            title = "¿Eliminar Comprobante de Gasto?",
            message = "¿Deseas eliminar el gasto por ${gasto.montoClp.formatClp()} en '${gasto.nombreProveedor}' (${gasto.categoriaNombre})? Esta acción no se puede deshacer y actualizará los balances.",
            confirmText = "Eliminar Gasto",
            cancelText = "Cancelar",
            isDestructive = true,
            onConfirm = {
                viewModel.eliminarGasto(gasto)
                gastoToDelete = null
            },
            onDismiss = { gastoToDelete = null }
        )
    }

    if (showConfirmSendDialog && rendicionConGastos != null) {
        val rItem = rendicionConGastos!!
        TeqConfirmationDialog(
            title = "¿Enviar Rendición a Jefatura?",
            message = "Se enviarán formalmente ${rItem.gastos.size} gastos por un total de ${rItem.totalRendidoClp.formatClp()} a tu supervisor técnico. Mientras esté en revisión no podrás añadir ni editar gastos.",
            confirmText = "Confirmar y Enviar",
            cancelText = "Revisar más",
            icon = Icons.AutoMirrored.Filled.Send,
            isDestructive = false,
            onConfirm = {
                showConfirmSendDialog = false
                viewModel.enviarAJefatura(rendicionId) {}
            },
            onDismiss = { showConfirmSendDialog = false }
        )
    }

    if (rendicionConGastos == null) {
        Scaffold(containerColor = NeuBackground) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TeqPrimary)
            }
        }
        return
    }

    val item = rendicionConGastos!!
    val r = item.rendicion
    val isEditable = r.estado == EstadoRendicion.BORRADOR || r.estado == EstadoRendicion.OBSERVADA

    Scaffold(
        containerColor = NeuBackground, // Fondo Neumorphic
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = r.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${r.centroCostoNombre} · ${r.fechaInicio}",
                            style = MaterialTheme.typography.bodySmall,
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
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Ayuda y Políticas", tint = TeqPrimary)
                    }
                    EstadoRendicionBadge(estado = r.estado, modifier = Modifier.padding(end = 12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeuBackground)
            )
        },
        floatingActionButton = {
            if (isEditable) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassmorphicButton(
                        onClick = { onNavigateToImportarTeqRuta(rendicionId) },
                        cornerRadius = 14.dp,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TeqRuta", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                    GlassmorphicExtendedFab(
                        onClick = { onNavigateToNuevoGasto(rendicionId) },
                        text = "Registrar Gasto",
                        icon = Icons.Default.Add
                    )
                }
            }
        },
        bottomBar = {
            if (isEditable && item.gastos.isNotEmpty()) {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    cornerRadius = 20.dp,
                    elevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "A Reembolsar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = item.totalReembolsoTecnicoClp.formatClp(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TeqPrimary
                            )
                        }
                        GlassmorphicButton(
                            onClick = {
                                showConfirmSendDialog = true
                            },
                            cornerRadius = 14.dp
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enviar a Jefatura", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tarjeta de Resumen Financiero con Neumorphism (Relieve 3D)
            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    elevation = 6.dp
                ) {
                    Text(
                        text = "Resumen de Rendición",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TeqPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Total Rendido",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = item.totalRendidoClp.formatClp(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = TeqPrimary
                            )
                        }
                        Text(
                            text = "${item.gastos.size} gastos registrados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFD5DFEC))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reembolso", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(item.totalReembolsoTecnicoClp.formatClp(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tarjeta Corp.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(item.totalTarjetaCorporativaClp.formatClp(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fondo Rendir", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(item.totalFondoRendirClp.formatClp(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (r.fondoInicial > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Saldo Fondo: ${item.saldoFondoPorRendir.formatClp()} (Anticipo: ${r.fondoInicial.formatClp()})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.saldoFondoPorRendir >= 0) TeqPrimary else Color.Red,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (item.tieneObservaciones) {
                item {
                    NeumorphicCard(
                        backgroundColor = Color(0xFFFFEDD5),
                        cornerRadius = 16.dp,
                        elevation = 4.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = TeqWarning)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Hay ${item.cantidadGastosObservados} gasto(s) devuelto(s) por el supervisor. Revise los comentarios para subsanarlos.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9A3412)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Detalle de Gastos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (item.gastos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Aún no has registrado gastos en esta rendición.", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                items(item.gastos, key = { it.id }) { gasto ->
                    NeumorphicGastoItemCard(
                        gasto = gasto,
                        isEditable = isEditable,
                        onEdit = { onNavigateToEditarGasto(rendicionId, gasto.id) },
                        onDelete = { gastoToDelete = gasto },
                        onViewPhoto = { onNavigateToVisorFoto(gasto.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun NeumorphicGastoItemCard(
    gasto: GastoEntity,
    isEditable: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewPhoto: () -> Unit
) {
    val categoryIcon = when (gasto.categoriaNombre) {
        "Combustible" -> Icons.Default.LocalGasStation
        "Peajes" -> Icons.Default.Toll
        "Estacionamiento" -> Icons.Default.LocalParking
        "Alimentación" -> Icons.Default.Restaurant
        "Alojamiento" -> Icons.Default.Hotel
        else -> Icons.Default.MedicalServices
    }

    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = TeqPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(categoryIcon, contentDescription = null, tint = TeqPrimary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(gasto.categoriaNombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("${gasto.fechaGasto} · ${gasto.nroComprobante}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            EstadoGastoBadge(estado = gasto.estadoGasto)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(gasto.nombreProveedor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("RUT: ${gasto.rutProveedor}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(gasto.formaPago.shortLabel, style = MaterialTheme.typography.labelSmall, color = TeqSecondary, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = gasto.montoClp.formatClp(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary
                )
                if (gasto.moneda.name != "CLP") {
                    Text(
                        text = "${gasto.moneda.symbol} ${gasto.montoOriginal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        if (gasto.estadoGasto == EstadoGasto.OBSERVADO && !gasto.observacionSupervisor.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFFFFEDD5),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = TeqWarning, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Observación: ${gasto.observacionSupervisor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9A3412),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (gasto.fotoComprobanteUri != null) {
                Row(
                    modifier = Modifier
                        .clickable { onViewPhoto() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = TeqPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver Comprobante", style = MaterialTheme.typography.labelMedium, color = TeqPrimary, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Sin comprobante adjunto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }

            if (isEditable) {
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
