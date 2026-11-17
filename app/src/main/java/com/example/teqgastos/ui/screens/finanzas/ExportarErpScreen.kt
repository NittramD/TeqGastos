package com.example.teqgastos.ui.screens.finanzas

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.data.local.entity.RendicionConGastos
import com.example.teqgastos.ui.components.GlassmorphicButton
import com.example.teqgastos.ui.components.NielsenHelpDialog
import com.example.teqgastos.ui.components.formatClp
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSuccess
import com.example.teqgastos.ui.viewmodel.FinanzasViewModel
import com.example.teqgastos.util.export.TeqPayrollExportUtil
import com.example.teqgastos.util.pdf.PdfReportGenerator
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportarErpScreen(
    viewModel: FinanzasViewModel,
    onNavigateBack: () -> Unit
) {
    val rendiciones by viewModel.rendicionesFinanzas.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val csvContent = remember(rendiciones) { viewModel.generarReporteErpCsv(rendiciones) }
    var showHelpDialog by remember { mutableStateOf(false) }

    var isGeneratingPdf by remember { mutableStateOf(false) }
    var lastGeneratedPdf by remember { mutableStateOf<File?>(null) }
    var rendicionSeleccionadaPdf by remember { mutableStateOf<RendicionConGastos?>(null) }

    if (showHelpDialog) {
        NielsenHelpDialog(onDismissRequest = { showHelpDialog = false })
    }

    val generarInformePdf: (RendicionConGastos) -> Unit = { rItem ->
        isGeneratingPdf = true
        rendicionSeleccionadaPdf = rItem
        coroutineScope.launch {
            try {
                val pdf = PdfReportGenerator.generatePdf(context, rItem)
                isGeneratingPdf = false
                if (pdf != null) {
                    lastGeneratedPdf = pdf
                    snackbarHostState.showSnackbar("Informe PDF generado: ${pdf.name}")
                } else {
                    snackbarHostState.showSnackbar("No fue posible generar el informe PDF.")
                }
            } catch (e: Exception) {
                isGeneratingPdf = false
                snackbarHostState.showSnackbar("Error al generar PDF: ${e.localizedMessage}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Exportación e Informes TEQMED", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Ayuda y Políticas", tint = TeqPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 6.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // BOTÓN PRINCIPAL: Generar y Previsualizar Informe PDF
                    GlassmorphicButton(
                        onClick = {
                            if (rendiciones.isNotEmpty()) {
                                generarInformePdf(rendiciones.first())
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("No hay rendiciones aprobadas para generar PDF.")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        cornerRadius = 14.dp
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generando Informe PDF Formal...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generar Informe Formal PDF (TEQMED)", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // BOTÓN SECUNDARIO: Compartir CSV para ERP
                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csvContent)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Exportar Archivo Contable TEQMED"))
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = TeqPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar Archivo CSV para ERP", fontWeight = FontWeight.SemiBold, color = TeqPrimary)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // TARJETA ANIMADA DE RESULTADO PDF GENERADO
            item {
                AnimatedVisibility(
                    visible = lastGeneratedPdf != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    lastGeneratedPdf?.let { file ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TeqSuccess, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Informe PDF TEQMED Listo",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFF15803D)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Rendición: ${rendicionSeleccionadaPdf?.rendicion?.titulo ?: "Comisión Médica"} · Total: ${rendicionSeleccionadaPdf?.totalRendidoClp?.formatClp() ?: "$0"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "Archivo: ${file.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4B5563)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { PdfReportGenerator.viewPdf(context, file) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TeqPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Previsualizar", style = MaterialTheme.typography.labelMedium)
                                    }
                                    Button(
                                        onClick = { PdfReportGenerator.sharePdf(context, file) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TeqSuccess),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Compartir", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECCIÓN: RENDICIONES DISPONIBLES PARA EMISIÓN FORMAL DE PDF
            item {
                Text(
                    text = "Rendiciones Aprobadas para Emisión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary
                )
                Text(
                    text = "Seleccione una rendición para emitir su informe oficial en PDF con firmas de jefatura.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (rendiciones.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "No hay rendiciones aprobadas por jefatura pendientes de informe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(rendiciones, key = { it.rendicion.id }) { rItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rItem.rendicion.titulo,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${rItem.rendicion.tecnicoNombre} · ${rItem.rendicion.centroCostoNombre}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "Total: ${rItem.totalRendidoClp.formatClp()} (${rItem.gastos.size} comprobantes)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TeqPrimary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = {
                                        val csv = TeqPayrollExportUtil.generatePayrollCsv(context, rItem.rendicion, rItem.totalReembolsoTecnicoClp)
                                        TeqPayrollExportUtil.shareFileWithSystem(context, csv, "text/csv", "Exportar Nómina Bancaria SAP")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = TeqPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CSV", style = MaterialTheme.typography.labelSmall, color = TeqPrimary)
                                }
                                Button(
                                    onClick = { generarInformePdf(rItem) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TeqPrimary),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PDF", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Estructura de Carga Contable (ERP)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary
                )
                Text(
                    text = "Formato CSV delimitado por ';' para carga en sistemas ERP contables.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Vista Previa CSV (Delimitado por ';'):", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = csvContent.ifBlank { "No hay datos para exportar." },
                            color = Color(0xFF4ADE80),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
