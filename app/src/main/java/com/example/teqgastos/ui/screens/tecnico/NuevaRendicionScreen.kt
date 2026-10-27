package com.example.teqgastos.ui.screens.tecnico

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.data.local.entity.CentroCostoEntity
import com.example.teqgastos.ui.components.GlassmorphicButton
import com.example.teqgastos.ui.components.NielsenHelpDialog
import com.example.teqgastos.ui.components.TeqConfirmationDialog
import com.example.teqgastos.ui.theme.NeuBackground
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.viewmodel.TecnicoViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaRendicionScreen(
    viewModel: TecnicoViewModel,
    onNavigateBack: () -> Unit,
    onRendicionCreated: (Long) -> Unit
) {
    val centrosCosto by viewModel.centrosCosto.collectAsState()

    var titulo by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf(LocalDate.now().toString()) }
    var fechaFin by remember { mutableStateOf(LocalDate.now().plusDays(2).toString()) }
    var selectedCentroCosto by remember { mutableStateOf<CentroCostoEntity?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var fondoInicialInput by remember { mutableStateOf("") }

    var tituloError by remember { mutableStateOf<String?>(null) }
    var centroError by remember { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val isDirty = titulo.isNotBlank() || selectedCentroCosto != null || fondoInicialInput.isNotBlank()
    val handleBack = {
        if (isDirty) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = isDirty, onBack = handleBack)

    if (showHelpDialog) {
        NielsenHelpDialog(onDismissRequest = { showHelpDialog = false })
    }

    if (showDiscardDialog) {
        TeqConfirmationDialog(
            title = "¿Descartar Comisión de Servicio?",
            message = "Tienes datos ingresados sin guardar. Si abandonas la pantalla ahora, se perderán.",
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

    Scaffold(
        containerColor = NeuBackground,
        topBar = {
            TopAppBar(
                title = { Text("Nueva Comisión de Servicio", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Datos de la Comisión en Terreno",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary
                )
                Text(
                    text = "Cree una rendición para agrupar los gastos de viaje y servicio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Título o Motivo
            item {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        tituloError = if (it.trim().length < 5) "Ingrese un motivo descriptivo (mín. 5 letras)" else null
                    },
                    label = { Text("Título / Motivo de la Comisión *") },
                    placeholder = { Text("Ej: Mantención Tomógrafo Hospital Las Higueras") },
                    isError = tituloError != null,
                    supportingText = { tituloError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Selector de Centro de Costo (ExposedDropdownMenu)
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCentroCosto?.let { "${it.codigo} - ${it.nombre}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Centro de Costo Asignado *") },
                        placeholder = { Text("Seleccionar clínica u hospital") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        isError = centroError != null,
                        supportingText = { centroError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        centrosCosto.forEach { centro ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(centro.nombre, fontWeight = FontWeight.SemiBold)
                                        Text("${centro.codigo} · ${centro.ciudad}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                },
                                onClick = {
                                    selectedCentroCosto = centro
                                    centroError = null
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Rango de Fechas
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = { fechaInicio = it },
                        label = { Text("Fecha Inicio (AAAA-MM-DD)") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = { fechaFin = it },
                        label = { Text("Fecha Término") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Fondo por Rendir Inicial
            item {
                OutlinedTextField(
                    value = fondoInicialInput,
                    onValueChange = { fondoInicialInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Fondo por Rendir Inicial (Opcional)") },
                    placeholder = { Text("Ej: 80000") },
                    prefix = { Text("$ ") },
                    supportingText = { Text("Indique el monto si TEQMED le entregó dinero por adelantado.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Botón de Guardar
            item {
                Spacer(modifier = Modifier.height(16.dp))
                GlassmorphicButton(
                    onClick = {
                        var isValid = true
                        if (titulo.trim().length < 5) {
                            tituloError = "El título debe tener al menos 5 caracteres."
                            isValid = false
                        }
                        if (selectedCentroCosto == null) {
                            centroError = "Debe seleccionar un centro de costo."
                            isValid = false
                        }

                        if (isValid) {
                            val centro = selectedCentroCosto!!
                            val fondo = fondoInicialInput.toDoubleOrNull() ?: 0.0
                            viewModel.createRendicion(
                                titulo = titulo.trim(),
                                fechaInicio = fechaInicio.trim(),
                                fechaFin = fechaFin.trim(),
                                tecnicoNombre = "Gerardo Galán",
                                tecnicoRut = "17.482.913-K",
                                centroCostoId = centro.id,
                                centroCostoNombre = centro.nombre,
                                fondoInicial = fondo,
                                onSuccess = { newId ->
                                    onRendicionCreated(newId)
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    cornerRadius = 14.dp
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear Rendición y Cargar Gastos", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
