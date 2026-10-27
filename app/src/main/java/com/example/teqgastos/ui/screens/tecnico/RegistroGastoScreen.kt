package com.example.teqgastos.ui.screens.tecnico

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.teqgastos.model.FormaPago
import com.example.teqgastos.model.TipoMoneda
import com.example.teqgastos.ui.components.GlassmorphicButton
import com.example.teqgastos.ui.components.NielsenHelpDialog
import com.example.teqgastos.ui.components.TeqConfirmationDialog
import com.example.teqgastos.ui.components.formatClp
import com.example.teqgastos.ui.theme.NeuBackground
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqWarning
import com.example.teqgastos.ui.viewmodel.GastoFormViewModel
import com.example.teqgastos.util.ImageCompressor
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroGastoScreen(
    viewModel: GastoFormViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.formState.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val indicadores by viewModel.indicadores.collectAsState()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var expandedCategorias by remember { mutableStateOf(false) }
    var photoUriTemp by remember { mutableStateOf<Uri?>(null) }
    var isCompressingImage by remember { mutableStateOf(false) }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val isDirty = state.montoInput.isNotBlank() ||
            state.nombreProveedor.isNotBlank() ||
            state.nroComprobante.isNotBlank() ||
            state.fotoComprobanteUri != null

    val handleBack = {
        if (isDirty) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = isDirty, onBack = handleBack)

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    if (showHelpDialog) {
        NielsenHelpDialog(onDismissRequest = { showHelpDialog = false })
    }

    if (showDiscardDialog) {
        TeqConfirmationDialog(
            title = "¿Descartar Gasto?",
            message = "Tienes datos o fotografía sin guardar. Si abandonas la pantalla ahora, los cambios se perderán.",
            confirmText = "Descartar",
            cancelText = "Continuar Editando",
            isDestructive = true,
            onConfirm = {
                showDiscardDialog = false
                onNavigateBack()
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    // Lanzador nativo de Cámara con Compresión
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUriTemp != null) {
            coroutineScope.launch {
                isCompressingImage = true
                val compressedUri = ImageCompressor.compressAndSaveImage(context, photoUriTemp!!)
                isCompressingImage = false
                if (compressedUri != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onFotoCaptured(compressedUri)
                }
            }
        }
    }

    if (state.categoria == null && categorias.isNotEmpty()) {
        viewModel.onCategoriaChanged(categorias.first())
    }

    Scaffold(
        containerColor = NeuBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.gastoId == 0L) "Registrar Gasto" else "Editar Gasto",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Ayuda y Políticas", tint = TeqPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeuBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SECCIÓN 1: Captura de Foto con Compresión
            item {
                Text(
                    text = "Comprobante de Respaldo *",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (isCompressingImage) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = TeqPrimary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Optimizando y comprimiendo imagen...", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else if (state.fotoComprobanteUri == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable {
                                val file = File(context.cacheDir, "camera_temp_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                photoUriTemp = uri
                                cameraLauncher.launch(uri)
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = TeqPrimary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tomar Fotografía con la Cámara", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TeqPrimary)
                            Text("Se comprimirá automáticamente a tamaño ligero", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = state.fotoComprobanteUri,
                                contentDescription = "Comprobante",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val file = File(context.cacheDir, "camera_temp_${System.currentTimeMillis()}.jpg")
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        photoUriTemp = uri
                                        cameraLauncher.launch(uri)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Repetir Foto", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // SECCIÓN 2: Categoría y Topes
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedCategorias,
                    onExpandedChange = { expandedCategorias = !expandedCategorias }
                ) {
                    OutlinedTextField(
                        value = state.categoria?.nombre ?: "Seleccionar Categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría de Gasto *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategorias) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategorias,
                        onDismissRequest = { expandedCategorias = false }
                    ) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(cat.nombre, fontWeight = FontWeight.Bold)
                                        Text("Tope TEQMED: ${cat.topeMaximoClp.formatClp()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                },
                                onClick = {
                                    viewModel.onCategoriaChanged(cat)
                                    expandedCategorias = false
                                }
                            )
                        }
                    }
                }
                state.categoria?.let { cat ->
                    Text(
                        text = "Tope máximo establecido por TEQMED: ${cat.topeMaximoClp.formatClp()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TeqPrimary,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }

            // SECCIÓN 3: Monto, Moneda e Indicadores Públicos
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.montoInput,
                        onValueChange = { viewModel.onMontoChanged(it) },
                        label = { Text("Monto *") },
                        placeholder = { Text("Ej: 18500") },
                        prefix = { Text("${state.tipoMoneda.symbol} ") },
                        isError = state.montoError != null,
                        supportingText = { state.montoError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(modifier = Modifier.weight(1.1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TipoMoneda.values().forEach { moneda ->
                            FilterChip(
                                selected = state.tipoMoneda == moneda,
                                onClick = { viewModel.onMonedaChanged(moneda) },
                                label = { Text(moneda.name, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                if (state.tipoMoneda != TipoMoneda.CLP && state.montoNumero > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Conversión a CLP: ${state.montoClp.formatClp()}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Fuente: API mindicador.cl (Tasa: ${state.tasaConversion.formatClp()} · ${if (indicadores.isOnline) "En línea" else "Caché offline"})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // SECCIÓN 4: Alerta de Tope Excedido
            item {
                AnimatedVisibility(
                    visible = state.isTopeExcedido,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDD5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = TeqWarning)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "El monto excede el tope de la categoría (${state.categoria?.topeMaximoClp?.formatClp()})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9A3412)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.justificacionTope,
                                onValueChange = { viewModel.onJustificacionChanged(it) },
                                label = { Text("Justificación obligatoria de sobregasto *") },
                                placeholder = { Text("Indique motivo operacional por el cual se excedió el tope...") },
                                isError = state.justificacionError != null,
                                supportingText = { state.justificacionError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // SECCIÓN 5: Fecha del Gasto con Control de Plazos
            item {
                OutlinedTextField(
                    value = state.fechaGasto,
                    onValueChange = { viewModel.onFechaChanged(it) },
                    label = { Text("Fecha del Gasto (AAAA-MM-DD) *") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    isError = state.fechaError != null,
                    supportingText = {
                        state.fechaError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        } ?: Text("Los gastos deben rendirse dentro de los últimos 30 días.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // SECCIÓN 6: Proveedor, RUT y Folio con Control de Duplicidad
            item {
                OutlinedTextField(
                    value = state.rutProveedor,
                    onValueChange = { viewModel.onRutChanged(it) },
                    label = { Text("RUT Proveedor *") },
                    placeholder = { Text("76.123.456-7") },
                    isError = state.rutError != null,
                    supportingText = { state.rutError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = state.nombreProveedor,
                    onValueChange = { viewModel.onNombreProveedorChanged(it) },
                    label = { Text("Razón Social / Proveedor *") },
                    placeholder = { Text("Ej: Copec S.A. / Hotel Diego de Almagro") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = state.nroComprobante,
                    onValueChange = { viewModel.onNroComprobanteChanged(it) },
                    label = { Text("N° Boleta / Factura / Folio *") },
                    placeholder = { Text("Ej: 49210") },
                    isError = state.duplicadoError != null,
                    supportingText = { state.duplicadoError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // SECCIÓN 7: Forma de Pago
            item {
                Text("Mecanismo de Pago *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FormaPago.values().forEach { forma ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onFormaPagoChanged(forma) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (state.formaPago == forma) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = if (state.formaPago == forma) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TeqPrimary)) else null
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = if (state.formaPago == forma) TeqPrimary else MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = forma.shortLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.formaPago == forma) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = forma.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECCIÓN 8: Botón Guardar con Guía Constructiva (Heurística #9)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                GlassmorphicButton(
                    onClick = {
                        if (!state.isFormValid) {
                            val missing = when {
                                state.categoria?.requiereComprobante == true && state.fotoComprobanteUri == null ->
                                    "Por favor toma una fotografía del comprobante físico."
                                state.montoNumero <= 0 ->
                                    "Ingresa un monto válido mayor a $0."
                                state.nombreProveedor.isBlank() ->
                                    "Indica el nombre o razón social del proveedor."
                                state.nroComprobante.isBlank() ->
                                    "Ingresa el número de boleta, factura o folio."
                                state.isTopeExcedido && state.justificacionTope.trim().length < 10 ->
                                    "El gasto supera el tope permitido. Escribe una justificación de al menos 10 caracteres."
                                state.rutError != null ->
                                    "El RUT del proveedor no es válido."
                                state.fechaError != null ->
                                    state.fechaError ?: "La fecha del gasto no es válida."
                                else -> "Por favor completa todos los campos requeridos con asterisco (*)."
                            }
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(missing)
                            }
                        } else {
                            viewModel.saveGasto {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateBack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    cornerRadius = 14.dp
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Gasto en Terreno", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
