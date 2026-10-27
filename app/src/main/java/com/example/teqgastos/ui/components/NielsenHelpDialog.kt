package com.example.teqgastos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.teqgastos.ui.theme.NeuBackground
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSecondary
import com.example.teqgastos.ui.theme.TeqSuccess

data class NielsenHeuristicItem(
    val number: Int,
    val title: String,
    val englishName: String,
    val principle: String,
    val implementationInApp: String
)

val nielsenHeuristicsList = listOf(
    NielsenHeuristicItem(
        number = 1,
        title = "Visibilidad del estado del sistema",
        englishName = "Visibility of system status",
        principle = "El sistema debe mantener informado al usuario sobre lo que ocurre, mediante retroalimentación oportuna y adecuada.",
        implementationInApp = "Indicadores de sincronización en vivo, banner de modo sin conexión (offline), badges de estado dinámicos en rendiciones y gastos, y confirmaciones snackbar en cada acción realizada."
    ),
    NielsenHeuristicItem(
        number = 2,
        title = "Relación entre el sistema y el mundo real",
        englishName = "Match between system and real world",
        principle = "El sistema debe usar el lenguaje, palabras y conceptos familiares para el usuario y su contexto profesional.",
        implementationInApp = "Formato de moneda chilena (CLP \$), validación y formato de RUT (XX.XXX.XXX-X), conceptos de boleta/factura/folio, centros de costo con hospitales reales y términos de viáticos y comisiones de terreno."
    ),
    NielsenHeuristicItem(
        number = 3,
        title = "Control y libertad del usuario",
        englishName = "User control and freedom",
        principle = "El usuario necesita salidas de emergencia claras cuando realiza acciones por error, con capacidad de deshacer o confirmar antes de actos irreversibles.",
        implementationInApp = "Diálogos de confirmación antes de eliminar gastos o enviar rendiciones a jefatura, y confirmación para descartar cambios en formularios al presionar Volver."
    ),
    NielsenHeuristicItem(
        number = 4,
        title = "Consistencia y estándares",
        englishName = "Consistency and standards",
        principle = "Las palabras, situaciones y acciones deben mantener la misma convención en toda la aplicación.",
        implementationInApp = "Patrón de barras superiores coherente, paleta semántica estandarizada para estados (Borrador: Gris, Enviada: Azul, Aprobada: Verde, Observada: Naranja), y botones primarios unificados con acento Glassmorphism."
    ),
    NielsenHeuristicItem(
        number = 5,
        title = "Prevención de errores",
        englishName = "Error prevention",
        principle = "Es mejor evitar que los errores ocurran mediante validaciones proactivas antes de que el usuario envíe la información.",
        implementationInApp = "Algoritmo Módulo 11 que verifica el dígito verificador del RUT, bloqueo de folios duplicados para el mismo proveedor, control del plazo de 30 días, y campo de justificación obligatorio si se supera el tope."
    ),
    NielsenHeuristicItem(
        number = 6,
        title = "Reconocimiento antes que recuerdo",
        englishName = "Recognition rather than recall",
        principle = "Hacer visibles las opciones y elementos para que el usuario no tenga que memorizar información entre pantallas.",
        implementationInApp = "Selector de categorías que muestra el tope diario permitido en pantalla, visor de tasa de cambio UF/USD en tiempo real sin requerir cálculos mentales, y tarjetas de resumen que desglosan saldos continuamente."
    ),
    NielsenHeuristicItem(
        number = 7,
        title = "Flexibilidad y eficiencia de uso",
        englishName = "Flexibility and efficiency of use",
        principle = "Proporcionar aceleradores para usuarios frecuentes o avanzados sin entorpecer a usuarios nuevos.",
        implementationInApp = "Buscador instantáneo por hospital/comisión, chips de filtro rápido por estado de rendición, e importación en 1 toque de trayectos GPS desde la plataforma TeqRuta."
    ),
    NielsenHeuristicItem(
        number = 8,
        title = "Diseño estético y minimalista",
        englishName = "Aesthetic and minimalist design",
        principle = "No incluir información innecesaria o irrelevante que compita con la visibilidad del contenido clave.",
        implementationInApp = "Diseño base Neumorphism suave que reduce la fatiga visual, botones clave en Glassmorphism de alta jerarquía, y revelación progresiva de campos complejos (conversión de moneda y justificación de sobregastos)."
    ),
    NielsenHeuristicItem(
        number = 9,
        title = "Ayudar a reconocer, diagnosticar y recuperarse de errores",
        englishName = "Help users recognize, diagnose, and recover from errors",
        principle = "Mensajes de error en lenguaje sencillo (sin códigos crípticos), precisos y con sugerencias de solución constructiva.",
        implementationInApp = "Explicaciones claras bajo cada campo ('El RUT es incorrecto, verifique el dígito verificador', 'Debe justificar el sobregasto de \$25.000') y sugerencias rápidas predefinidas para supervisores al observar comprobantes."
    ),
    NielsenHeuristicItem(
        number = 10,
        title = "Ayuda y documentación",
        englishName = "Help and documentation",
        principle = "Proporcionar documentación fácil de consultar, centrada en la tarea y accesible cuando el usuario lo necesite.",
        implementationInApp = "Este Centro de Ayuda transversal con las políticas oficiales de rendición de TEQMED SpA (topes, plazos y reembolsos) y el desglose de cumplimiento de las 10 heurísticas de Nielsen en la app."
    )
)

@Composable
fun NielsenHelpDialog(
    onDismissRequest: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            color = NeuBackground,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TeqPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = TeqPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Centro de Ayuda y Usabilidad",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TeqPrimary
                            )
                            Text(
                                text = "TeqGastos · TEQMED SpA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFDCE5EE))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("10 Leyes de Nielsen", "Políticas TEQMED")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TeqPrimary else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contenido de los tabs
                if (selectedTab == 0) {
                    // TAB 1: 10 LEYES DE NIELSEN
                    Text(
                        text = "Implementación de las 10 Heurísticas de Jakob Nielsen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Toca cada heurística para ver cómo se cumple en TeqGastos:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(nielsenHeuristicsList) { index, item ->
                            val isExpanded = expandedIndex == index
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedIndex = if (isExpanded) null else index
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = TeqSuccess.copy(alpha = 0.15f),
                                                shape = CircleShape,
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${item.number}",
                                                        fontWeight = FontWeight.Bold,
                                                        color = TeqSuccess,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = item.englishName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    AnimatedVisibility(visible = isExpanded) {
                                        Column(modifier = Modifier.padding(top = 10.dp)) {
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = "Principio Heurístico:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TeqPrimary
                                            )
                                            Text(
                                                text = item.principle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = "Aplicación en TeqGastos:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TeqSuccess
                                            )
                                            Text(
                                                text = item.implementationInApp,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 2: POLÍTICAS DE RENDICIÓN TEQMED
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Policy, contentDescription = null, tint = TeqPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Regla de Plazo Legal (30 Días)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Todos los comprobantes de gastos en terreno deben ser ingresados dentro de los 30 días posteriores a la fecha de emisión del documento tributario (boleta o factura). Si supera dicho plazo, el sistema requerirá una justificación extraordinaria para el comité de Finanzas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Topes Máximos por Categoría", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = TeqPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    PolicyTopeRow("Alimentación (Almuerzo/Cena)", "\$ 25.000 / día")
                                    PolicyTopeRow("Alojamiento (Hotel/Hostal)", "\$ 60.000 / noche")
                                    PolicyTopeRow("Combustible", "Según kilometraje y ruta")
                                    PolicyTopeRow("Peajes / TAG", "Rendición con comprobante")
                                    PolicyTopeRow("Materiales / Ferretería", "Previa autorización jefatura")
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Formas de Pago y Reembolso", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = TeqPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• Reembolso a Técnico: Gasto pagado con fondos particulares del técnico. Finanzas efectúa transferencia bancaria a su cuenta al aprobarse.", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("• Tarjeta Corporativa: Pagado con tarjeta bancaria de la empresa TEQMED. No genera reembolso a favor del técnico.", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("• Fondo por Rendir: Pagado con dinero en efectivo o anticipo entregado antes de iniciar la comisión de servicio.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Soporte y Mesa de Ayuda TEQMED", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = TeqPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("¿Dudas con tu rendición? Contacta a Finanzas TEQMED:", style = MaterialTheme.typography.bodySmall)
                                    Text("Correo: finanzas@teqmed.cl · Anexo interno: 302", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = TeqPrimary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botón Entendido
                GlassmorphicButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    cornerRadius = 14.dp
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Entendido", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PolicyTopeRow(categoria: String, tope: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(categoria, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Text(tope, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TeqPrimary)
    }
}
