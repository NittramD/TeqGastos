package com.example.teqgastos.ui.screens.tecnico

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.teqgastos.model.FormaPago
import com.example.teqgastos.model.TipoMoneda
import com.example.teqgastos.ui.components.GlassmorphicButton
import com.example.teqgastos.ui.components.NielsenHelpDialog
import com.example.teqgastos.ui.components.RutValidator
import com.example.teqgastos.ui.components.TeqConfirmationDialog
import com.example.teqgastos.ui.components.TeqIvaCalculatorRow
import com.example.teqgastos.ui.components.TeqQuickExpenseTemplates
import com.example.teqgastos.ui.components.formatClp
import com.example.teqgastos.ui.theme.NeuBackground
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSuccess
import com.example.teqgastos.ui.theme.TeqWarning
import com.example.teqgastos.ui.viewmodel.GastoFormViewModel
import com.example.teqgastos.util.ImageCompressor
import com.example.teqgastos.util.camera.ReceiptOcrScanner
import com.example.teqgastos.util.location.LocationTracker
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
    var isScanningOcr by remember { mutableStateOf(false) }
    var isLocatingGps by remember { mutableStateOf(false) }

    // Parámetros de presentación y visualización nativa (IE 2.4.2)
    var contentScaleMode by remember { mutableStateOf(ContentScale.Crop) }
    var cornerRadiusDp by remember { mutableStateOf(14.dp) }
    var showPreviewModal by remember { mutableStateOf(false) }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var esFacturaAfecta by remember { mutableStateOf(false) }

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

    val procesarOcrConImagen: (Uri) -> Unit = { imageUri ->
        coroutineScope.launch {
            isScanningOcr = true
            try {
                val ocrResult = ReceiptOcrScanner.parseReceipt(context, imageUri)
                viewModel.applyOcrResult(
                    rut = ocrResult.rutProveedor,
                    nombre = ocrResult.nombreProveedor,
                    folio = ocrResult.nroComprobante,
                    monto = ocrResult.montoClp,
                    fecha = ocrResult.fechaGasto
                )
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val resumen = buildString {
                    append("IA detectó datos: ")
                    val parts = mutableListOf<String>()
                    ocrResult.montoClp?.let { parts.add("Monto $${it.toLong()}") }
                    ocrResult.rutProveedor?.let { parts.add("RUT $it") }
                    ocrResult.nroComprobante?.let { parts.add("Folio $it") }
                    append(if (parts.isNotEmpty()) parts.joinToString(" · ") else "Revisar campos.")
                }
                snackbarHostState.showSnackbar(resumen)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error al procesar boleta: ${e.localizedMessage}")
            } finally {
                isScanningOcr = false
            }
        }
    }

    // Gestión Segura de Permisos Nativos en Runtime (IE 2.4.1)
    var pendingCameraAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingCameraAction?.invoke()
            pendingCameraAction = null
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permiso de Cámara denegado. Es requerido para capturar boletas físicas.")
            }
        }
    }

    val ejecutarConPermisoCamara: (() -> Unit) -> Unit = { accion ->
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            accion()
        } else {
            pendingCameraAction = accion
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Recurso Nativo 1: Cámara simple
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

    // Recurso Nativo 1 (avanzado): Cámara con OCR de IA
    val ocrCameraLauncher = rememberLauncherForActivityResult(
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
                    procesarOcrConImagen(Uri.parse(compressedUri))
                }
            }
        }
    }

    // Recurso Nativo 2: Selector de Galería / Almacenamiento (Photo Picker nativo)
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isCompressingImage = true
                val compressedUri = ImageCompressor.compressAndSaveImage(context, uri)
                isCompressingImage = false
                if (compressedUri != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onFotoCaptured(compressedUri)
                    procesarOcrConImagen(Uri.parse(compressedUri))
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                isLocatingGps = true
                val loc = LocationTracker.getCurrentLocation(context)
                isLocatingGps = false
                if (loc != null) {
                    viewModel.onUbicacionGpsChanged(loc.formattedDescription)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    snackbarHostState.showSnackbar("Ubicación GPS registrada: ${loc.formattedDescription}")
                } else {
                    snackbarHostState.showSnackbar("No fue posible capturar señal GPS precisa.")
                }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Se requiere permiso de ubicación para adjuntar coordenadas.")
            }
        }
    }

    val solicitarUbicacionGps: () -> Unit = {
        if (LocationTracker.hasLocationPermission(context)) {
            coroutineScope.launch {
                isLocatingGps = true
                val loc = LocationTracker.getCurrentLocation(context)
                isLocatingGps = false
                if (loc != null) {
                    viewModel.onUbicacionGpsChanged(loc.formattedDescription)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    snackbarHostState.showSnackbar("Ubicación GPS registrada: ${loc.formattedDescription}")
                } else {
                    snackbarHostState.showSnackbar("No fue posible capturar señal GPS precisa.")
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
            // SECCIÓN 0: Plantillas Rápidas (1-Tap) para Terreno
            item {
                TeqQuickExpenseTemplates(
                    onTemplateSelected = { template ->
                        viewModel.onNombreProveedorChanged(template.proveedor)
                        viewModel.onRutChanged(template.rut)
                        viewModel.onMontoChanged(template.montoSugerido.toLong().toString())
                        categorias.firstOrNull { it.nombre.contains(template.categoria, ignoreCase = true) }?.let {
                            viewModel.onCategoriaChanged(it)
                        }
                    }
                )
            }

            // SECCIÓN 1: Captura de Foto con IA (OCR ML Kit) y Asistente GPS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comprobante de Respaldo *",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TeqPrimary
                    )
                    if (state.fotoComprobanteUri != null) {
                        Surface(
                            color = TeqSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Comprobante adjunto",
                                color = TeqSuccess,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                if (isCompressingImage || isScanningOcr) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(130.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                CircularProgressIndicator(color = TeqPrimary, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isScanningOcr) "Analizando boleta con Inteligencia Artificial ML Kit..." else "Optimizando y comprimiendo imagen ligera...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TeqPrimary
                                )
                                Text(
                                    text = if (isScanningOcr) "Extrayendo RUT, Total CLP, Proveedor y Folio" else "Ajustando resolución para eficiencia en terreno",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else if (state.fotoComprobanteUri == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // BOTÓN DESTACADO: Escanear con IA (OCR) con gestión segura de permisos en runtime (IE 2.4.1)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    ejecutarConPermisoCamara {
                                        val file = File(context.cacheDir, "ocr_temp_${System.currentTimeMillis()}.jpg")
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        photoUriTemp = uri
                                        ocrCameraLauncher.launch(uri)
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = TeqPrimary.copy(alpha = 0.08f)),
                            border = BorderStroke(1.5.dp, TeqPrimary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(TeqPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Escanear Boleta con IA (OCR)",
                                        fontWeight = FontWeight.Bold,
                                        color = TeqPrimary,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "Toma la foto y autocompleta RUT, Monto, Proveedor y Folio",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        // Recursos Nativos Secundarios: Cámara Simple y Galería
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    ejecutarConPermisoCamara {
                                        val file = File(context.cacheDir, "camera_temp_${System.currentTimeMillis()}.jpg")
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        photoUriTemp = uri
                                        cameraLauncher.launch(uri)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp), tint = TeqPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cámara Simple", style = MaterialTheme.typography.bodySmall, color = TeqPrimary)
                            }

                            OutlinedButton(
                                onClick = {
                                    galleryPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp), tint = TeqPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Galería Fotos", style = MaterialTheme.typography.bodySmall, color = TeqPrimary)
                            }
                        }
                    }
                } else {
                    // Contenedor Visual del Recurso Nativo con Borde y Escala Ajustable (IE 2.4.2)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(cornerRadiusDp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = state.fotoComprobanteUri,
                                contentDescription = "Comprobante",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { showPreviewModal = true },
                                contentScale = contentScaleMode
                            )

                            // Badge superior de compresión y optimización
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(bottomEnd = 10.dp),
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TeqSuccess, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("JPEG Optimizado (80% · Ligero)", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            // Botones de acción sobre el recurso
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { showPreviewModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Zoom", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = {
                                        state.fotoComprobanteUri?.let {
                                            procesarOcrConImagen(Uri.parse(it))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TeqPrimary.copy(alpha = 0.85f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("IA OCR", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = {
                                        ejecutarConPermisoCamara {
                                            val file = File(context.cacheDir, "camera_temp_${System.currentTimeMillis()}.jpg")
                                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                            photoUriTemp = uri
                                            ocrCameraLauncher.launch(uri)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Repetir", style = MaterialTheme.typography.labelSmall)
                                }

                                IconButton(
                                    onClick = { viewModel.onFotoCaptured("") },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // BARRA DE AJUSTE DE PRESENTACIÓN EN TIEMPO REAL (IE 2.4.2)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ajuste de presentación:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = contentScaleMode == ContentScale.Crop,
                                    onClick = {
                                        contentScaleMode = if (contentScaleMode == ContentScale.Crop) ContentScale.Fit else ContentScale.Crop
                                    },
                                    label = {
                                        Text(
                                            if (contentScaleMode == ContentScale.Crop) "Recortar (Crop)" else "Ajustar (Fit)",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (contentScaleMode == ContentScale.Crop) Icons.Default.Crop else Icons.Default.FitScreen,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )

                                FilterChip(
                                    selected = cornerRadiusDp == 24.dp,
                                    onClick = {
                                        cornerRadiusDp = if (cornerRadiusDp == 14.dp) 24.dp else 14.dp
                                    },
                                    label = {
                                        Text(
                                            if (cornerRadiusDp == 24.dp) "Borde: 24dp" else "Borde: 14dp",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Diálogo Modal para Inspección de Imagen en Detalle
                    if (showPreviewModal) {
                        Dialog(onDismissRequest = { showPreviewModal = false }) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(440.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Black)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = state.fotoComprobanteUri,
                                        contentDescription = "Vista ampliada de comprobante",
                                        modifier = Modifier.fillMaxSize().padding(12.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    IconButton(
                                        onClick = { showPreviewModal = false },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // Banner Animado de Confirmación de Datos Autocompletados con IA
                AnimatedVisibility(
                    visible = state.ocrAutocompletado,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "¡Boleta Leída con IA Exitosamente!",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.dismissOcrBanner() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Se autocompletaron los siguientes datos. Por favor revíselos y confirme:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (state.montoInput.isNotBlank()) {
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.5.dp, Color(0xFF86EFAC))
                                    ) {
                                        Text(
                                            text = "Monto: $${state.montoInput}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (state.rutProveedor.isNotBlank()) {
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.5.dp, Color(0xFF86EFAC))
                                    ) {
                                        Text(
                                            text = "RUT: ${state.rutProveedor}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (state.nroComprobante.isNotBlank()) {
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.5.dp, Color(0xFF86EFAC))
                                    ) {
                                        Text(
                                            text = "Folio: ${state.nroComprobante}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { viewModel.dismissOcrBanner() }) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF15803D))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Confirmar Datos Autocompletados", color = Color(0xFF15803D), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ASISTENTE DE GEOLOCALIZACIÓN GPS PARA TRASLADOS MÉDICOS
                if (state.ubicacionGps == null) {
                    OutlinedButton(
                        onClick = { solicitarUbicacionGps() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLocatingGps
                    ) {
                        if (isLocatingGps) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = TeqPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Capturando señal GPS en terreno...", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = TeqPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Adjuntar Ubicación GPS (Combustible / Peaje)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = TeqPrimary)
                        }
                    }
                } else {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Ubicación GPS Adjunta al Gasto:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                                        Text(state.ubicacionGps ?: "", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1E40AF), fontWeight = FontWeight.Medium)
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.onUbicacionGpsChanged(null) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar GPS", tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
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
                        trailingIcon = {
                            if (state.montoError != null) {
                                Icon(Icons.Default.Error, contentDescription = "Error en monto", tint = MaterialTheme.colorScheme.error)
                            } else if (state.montoNumero > 0) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Monto válido", tint = TeqSuccess)
                            }
                        },
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

                // Calculadora de IVA 19% SII (Boleta Exenta vs. Factura Afecta con Crédito Fiscal)
                Spacer(modifier = Modifier.height(8.dp))
                TeqIvaCalculatorRow(
                    montoTotal = state.montoNumero,
                    esFacturaAfecta = esFacturaAfecta,
                    onTipoCambiado = { esFacturaAfecta = it }
                )
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
                                trailingIcon = {
                                    if (state.justificacionError != null) {
                                        Icon(Icons.Default.Error, contentDescription = "Error justificación", tint = MaterialTheme.colorScheme.error)
                                    } else if (state.justificacionTope.trim().length >= 10) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Justificación válida", tint = TeqSuccess)
                                    }
                                },
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
                    trailingIcon = {
                        if (state.fechaError != null) {
                            Icon(Icons.Default.Error, contentDescription = "Error fecha", tint = MaterialTheme.colorScheme.error)
                        } else if (state.fechaGasto.isNotBlank()) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Fecha válida", tint = TeqSuccess)
                        }
                    },
                    supportingText = {
                        state.fechaError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        } ?: Text("Los gastos deben rendirse dentro de los últimos 30 días.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // SECCIÓN 6: Proveedor, RUT y Folio con Control de Duplicidad e Íconos de Estado (IE 2.1.1)
            item {
                OutlinedTextField(
                    value = state.rutProveedor,
                    onValueChange = { viewModel.onRutChanged(it) },
                    label = { Text("RUT Proveedor *") },
                    placeholder = { Text("76.123.456-7") },
                    isError = state.rutError != null,
                    trailingIcon = {
                        if (state.rutError != null) {
                            Icon(Icons.Default.Error, contentDescription = "Error RUT", tint = MaterialTheme.colorScheme.error)
                        } else if (RutValidator.isValid(state.rutProveedor)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "RUT válido", tint = TeqSuccess)
                        }
                    },
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
                    isError = state.nombreProveedorError != null,
                    trailingIcon = {
                        if (state.nombreProveedorError != null) {
                            Icon(Icons.Default.Error, contentDescription = "Error proveedor", tint = MaterialTheme.colorScheme.error)
                        } else if (state.nombreProveedor.trim().length >= 3) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Proveedor válido", tint = TeqSuccess)
                        }
                    },
                    supportingText = { state.nombreProveedorError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
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
                    isError = state.duplicadoError != null || state.nroComprobanteError != null,
                    trailingIcon = {
                        if (state.duplicadoError != null || state.nroComprobanteError != null) {
                            Icon(Icons.Default.Error, contentDescription = "Error comprobante", tint = MaterialTheme.colorScheme.error)
                        } else if (state.nroComprobante.isNotBlank()) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Comprobante válido", tint = TeqSuccess)
                        }
                    },
                    supportingText = {
                        val err = state.duplicadoError ?: state.nroComprobanteError
                        err?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
                    },
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

            // SECCIÓN 8: Botón Guardar con Validación Integral e Íconos (IE 2.1.1)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                GlassmorphicButton(
                    onClick = {
                        val isValid = viewModel.validateAllFields()
                        if (!isValid) {
                            val missing = when {
                                state.categoria?.requiereComprobante == true && state.fotoComprobanteUri == null ->
                                    "Por favor toma una fotografía o adjunta el comprobante físico."
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
                                else -> "Por favor completa o corrige los campos marcados en rojo (*)."
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
