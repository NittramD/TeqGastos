package com.example.teqgastos.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.Canvas as AndroidCanvas
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSuccess
import java.io.File
import java.io.FileOutputStream

data class SignaturePath(
    val points: List<Offset>
)

/**
 * Componente de lienzo interactivo para captura de firma digital manuscrita en Jetpack Compose.
 */
@Composable
fun SignatureCanvas(
    modifier: Modifier = Modifier,
    strokeColor: Color = Color(0xFF0F172A),
    strokeWidthDp: Float = 4f,
    onSignatureChanged: (hasSignature: Boolean) -> Unit = {},
    onCanvasReady: (SignatureExporter) -> Unit = {}
) {
    val paths = remember { mutableStateListOf<SignaturePath>() }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val exporter = remember(canvasSize, paths) {
        object : SignatureExporter {
            override fun isEmpty(): Boolean = paths.isEmpty() && currentPoints.isEmpty()

            override fun clear() {
                paths.clear()
                currentPoints = emptyList()
                onSignatureChanged(false)
            }

            override fun exportToBitmap(width: Int, height: Int): Bitmap? {
                val targetW = if (width > 0) width else if (canvasSize.width > 0) canvasSize.width else 600
                val targetH = if (height > 0) height else if (canvasSize.height > 0) canvasSize.height else 300

                val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                val canvas = AndroidCanvas(bitmap)
                canvas.drawColor(android.graphics.Color.WHITE) // Fondo blanco para documentos PDF formales

                val paint = AndroidPaint().apply {
                    color = android.graphics.Color.BLACK
                    isAntiAlias = true
                    style = AndroidPaint.Style.STROKE
                    strokeWidth = 6f
                    strokeCap = AndroidPaint.Cap.ROUND
                    strokeJoin = AndroidPaint.Join.ROUND
                }

                val scaleX = if (canvasSize.width > 0) targetW.toFloat() / canvasSize.width else 1f
                val scaleY = if (canvasSize.height > 0) targetH.toFloat() / canvasSize.height else 1f

                val allPaths = paths.toList() + if (currentPoints.isNotEmpty()) listOf(SignaturePath(currentPoints)) else emptyList()

                for (stroke in allPaths) {
                    if (stroke.points.size < 2) continue
                    val androidPath = AndroidPath()
                    val p0 = stroke.points[0]
                    androidPath.moveTo(p0.x * scaleX, p0.y * scaleY)

                    for (i in 1 until stroke.points.size) {
                        val p1 = stroke.points[i]
                        androidPath.lineTo(p1.x * scaleX, p1.y * scaleY)
                    }
                    canvas.drawPath(androidPath, paint)
                }

                return bitmap
            }

            override fun saveToFile(subDir: String, fileName: String): File? {
                val bmp = exportToBitmap(600, 300) ?: return null
                return try {
                    val dir = File(context.filesDir, subDir).apply { if (!exists()) mkdirs() }
                    val file = File(dir, fileName)
                    FileOutputStream(file).use { out ->
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    file
                } catch (e: Exception) {
                    Log.e("SignatureCanvas", "Error guardando firma en archivo: ${e.message}", e)
                    null
                }
            }
        }
    }

    onCanvasReady(exporter)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentPoints = listOf(offset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentPoints = currentPoints + change.position
                        onSignatureChanged(true)
                    },
                    onDragEnd = {
                        if (currentPoints.isNotEmpty()) {
                            paths.add(SignaturePath(currentPoints))
                            currentPoints = emptyList()
                            onSignatureChanged(true)
                        }
                    },
                    onDragCancel = {
                        currentPoints = emptyList()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Línea guía tenue para firma
            val guideY = size.height * 0.78f
            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(size.width * 0.08f, guideY),
                end = Offset(size.width * 0.92f, guideY),
                strokeWidth = 1.5.dp.toPx()
            )

            // Dibuja trazos ya finalizados
            for (stroke in paths) {
                if (stroke.points.size > 1) {
                    val path = Path().apply {
                        moveTo(stroke.points[0].x, stroke.points[0].y)
                        for (i in 1 until stroke.points.size) {
                            lineTo(stroke.points[i].x, stroke.points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = strokeColor,
                        style = Stroke(
                            width = strokeWidthDp.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Dibuja el trazo actual en movimiento
            if (currentPoints.size > 1) {
                val currentPath = Path().apply {
                    moveTo(currentPoints[0].x, currentPoints[0].y)
                    for (i in 1 until currentPoints.size) {
                        lineTo(currentPoints[i].x, currentPoints[i].y)
                    }
                }
                drawPath(
                    path = currentPath,
                    color = strokeColor,
                    style = Stroke(
                        width = strokeWidthDp.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // Texto de ayuda cuando el lienzo está vacío
        if (paths.isEmpty() && currentPoints.isEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Draw,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Firme aquí con el dedo o stylus",
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

interface SignatureExporter {
    fun isEmpty(): Boolean
    fun clear()
    fun exportToBitmap(width: Int = 600, height: Int = 300): Bitmap?
    fun saveToFile(subDir: String = "signatures", fileName: String = "signature.png"): File?
}

/**
 * Diálogo modal con lienzo interactivo para estampar la firma digital formal de la jefatura/supervisor.
 */
@Composable
fun SignatureDialog(
    titulo: String = "Firma de Aprobación de Jefatura",
    subtitulo: String = "Estampe su rúbrica digital manuscrita para validar y derivar formalmente esta rendición médica a Finanzas.",
    nombreFirmante: String = "Jefatura de Operaciones Clínicas TEQMED",
    rutFirmante: String = "TEQMED SpA",
    onDismissRequest: () -> Unit,
    onSignatureConfirmed: (bitmap: Bitmap, signatureFile: File) -> Unit
) {
    var hasSignature by remember { mutableStateOf(false) }
    var exporter by remember { mutableStateOf<SignatureExporter?>(null) }
    val haptic = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = null,
                    tint = TeqPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = nombreFirmante,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = rutFirmante,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Lienzo de firma táctil
                SignatureCanvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    onSignatureChanged = { hasSignature = it },
                    onCanvasReady = { exporter = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            exporter?.clear()
                            hasSignature = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = hasSignature
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Borrar Lienzo", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val exp = exporter
                    if (exp != null && !exp.isEmpty()) {
                        val bmp = exp.exportToBitmap(600, 300)
                        val fileName = "sig_jefatura_${System.currentTimeMillis()}.png"
                        val file = exp.saveToFile("signatures", fileName)
                        if (bmp != null && file != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSignatureConfirmed(bmp, file)
                        }
                    }
                },
                enabled = hasSignature,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TeqSuccess)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirmar y Aprobar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancelar")
            }
        }
    )
}
