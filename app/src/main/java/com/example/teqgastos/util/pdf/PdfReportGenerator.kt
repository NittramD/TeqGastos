package com.example.teqgastos.util.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.teqgastos.data.local.entity.RendicionConGastos
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object PdfReportGenerator {
    private const val TAG = "PdfReportGenerator"
    private const val PAGE_WIDTH = 595 // A4 standard width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 standard height in points

    private fun formatClp(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        return format.format(amount)
    }

    /**
     * Genera un informe formal en PDF para una rendición médica de TEQMED SpA.
     */
    fun generatePdf(
        context: Context,
        rendicionConGastos: RendicionConGastos,
        signatureBitmap: Bitmap? = null
    ): File? {
        val r = rendicionConGastos.rendicion
        val gastos = rendicionConGastos.gastos

        // Intentar recuperar firma de archivo si no viene en el parámetro
        val firmBmp: Bitmap? = signatureBitmap ?: run {
            val possibleFiles = listOf(
                File(context.filesDir, "signatures/supervisor_signature_${r.id}.png"),
                File(context.filesDir, "signatures/sig_jefatura_${r.id}.png"),
                File(context.cacheDir, "signatures/supervisor_signature_${r.id}.png")
            )
            possibleFiles.firstOrNull { it.exists() }?.let {
                try {
                    BitmapFactory.decodeFile(it.absolutePath)
                } catch (e: Exception) {
                    null
                }
            }
        }

        val document = PdfDocument()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Configuración de paginación
        val itemsPerPage = 16
        val totalGastos = gastos.size
        val totalPages = if (totalGastos == 0) 1 else ((totalGastos - 1) / itemsPerPage) + 1

        val clpFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

        for (pageNumber in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = 40f

            // 1. BANNER SUPERIOR CORPORATIVO (TEQMED SpA)
            paint.color = Color.rgb(15, 23, 42) // Slate 900
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 75f, paint)

            paint.color = Color.rgb(0, 150, 136) // TeqPrimary / Teal Accent
            canvas.drawRect(0f, 75f, PAGE_WIDTH.toFloat(), 79f, paint)

            textPaint.color = Color.WHITE
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 18f
            canvas.drawText("TEQMED SpA", 40f, 38f, textPaint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 9.5f
            canvas.drawText("R.U.T. 76.845.120-K · Soluciones Médicas y Soporte Clínico en Terreno", 40f, 54f, textPaint)
            canvas.drawText("Concepción · Chillán · Los Ángeles · Temuco", 40f, 66f, textPaint)

            // Folio de Informe a la derecha
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 11f
            canvas.drawText("INFORME OFICIAL", PAGE_WIDTH - 40f, 38f, textPaint)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 9f
            canvas.drawText("Folio Rendición: #${r.id}", PAGE_WIDTH - 40f, 52f, textPaint)
            val nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            canvas.drawText("Emitido: $nowStr", PAGE_WIDTH - 40f, 65f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            y = 100f

            // 2. METADATOS DE LA COMISIÓN MÉDICA
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 14f
            textPaint.color = Color.rgb(15, 23, 42)
            canvas.drawText("RENDICIÓN DE GASTOS: ${r.titulo.uppercase()}", 40f, y, textPaint)

            y += 18f
            paint.color = Color.rgb(241, 245, 249) // Slate 100
            val metaRect = RectF(40f, y, PAGE_WIDTH - 40f, y + 62f)
            canvas.drawRoundRect(metaRect, 6f, 6f, paint)

            paint.color = Color.rgb(203, 213, 225) // Slate 300
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(metaRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            // Datos estructurados en 2 columnas
            val col1X = 52f
            val col2X = 310f
            var metaY = y + 18f

            textPaint.textSize = 9.5f
            textPaint.color = Color.rgb(71, 85, 105)
            canvas.drawText("Técnico Responsable:", col1X, metaY, textPaint)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = Color.rgb(15, 23, 42)
            canvas.drawText("${r.tecnicoNombre} (${r.tecnicoRut})", col1X + 115f, metaY, textPaint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = Color.rgb(71, 85, 105)
            canvas.drawText("Centro de Costo:", col2X, metaY, textPaint)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = Color.rgb(15, 23, 42)
            val ccNombre = if (r.centroCostoNombre.length > 25) r.centroCostoNombre.take(24) + "..." else r.centroCostoNombre
            canvas.drawText(ccNombre, col2X + 85f, metaY, textPaint)

            metaY += 16f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = Color.rgb(71, 85, 105)
            canvas.drawText("Período de Comisión:", col1X, metaY, textPaint)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = Color.rgb(15, 23, 42)
            canvas.drawText("${r.fechaInicio} al ${r.fechaFin}", col1X + 115f, metaY, textPaint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = Color.rgb(71, 85, 105)
            canvas.drawText("Estado Oficial:", col2X, metaY, textPaint)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = Color.rgb(0, 128, 96)
            canvas.drawText(r.estado.name, col2X + 85f, metaY, textPaint)

            metaY += 16f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = Color.rgb(71, 85, 105)
            canvas.drawText("Fondo Asignado:", col1X, metaY, textPaint)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = Color.rgb(15, 23, 42)
            canvas.drawText(formatClp(r.fondoInicial), col1X + 115f, metaY, textPaint)

            y += 80f

            // 3. TABLA DE DETALLE DE GASTOS
            val tableTop = y
            val rowHeight = 22f

            // Encabezado de la tabla
            paint.color = Color.rgb(30, 41, 59) // Slate 800
            canvas.drawRect(40f, tableTop, PAGE_WIDTH - 40f, tableTop + rowHeight, paint)

            textPaint.color = Color.WHITE
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 8.5f

            canvas.drawText("FECHA", 46f, tableTop + 14f, textPaint)
            canvas.drawText("CATEGORÍA", 108f, tableTop + 14f, textPaint)
            canvas.drawText("PROVEEDOR / RAZÓN SOCIAL", 195f, tableTop + 14f, textPaint)
            canvas.drawText("FOLIO / N°", 365f, tableTop + 14f, textPaint)
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("MONTO CLP", PAGE_WIDTH - 46f, tableTop + 14f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            y += rowHeight

            // Filas de gastos de la página actual
            val startIndex = (pageNumber - 1) * itemsPerPage
            val endIndex = kotlin.math.min(startIndex + itemsPerPage, totalGastos)

            for (i in startIndex until endIndex) {
                val gasto = gastos[i]

                // Color alternado de fondo
                paint.color = if (i % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(40f, y, PAGE_WIDTH - 40f, y + rowHeight, paint)

                paint.color = Color.rgb(226, 232, 240)
                paint.strokeWidth = 0.5f
                canvas.drawLine(40f, y + rowHeight, PAGE_WIDTH - 40f, y + rowHeight, paint)

                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textPaint.textSize = 8.5f
                textPaint.color = Color.rgb(51, 65, 85)

                canvas.drawText(gasto.fechaGasto, 46f, y + 14f, textPaint)

                val catText = if (gasto.categoriaNombre.length > 15) gasto.categoriaNombre.take(14) + "." else gasto.categoriaNombre
                canvas.drawText(catText, 108f, y + 14f, textPaint)

                val provText = if (gasto.nombreProveedor.length > 28) gasto.nombreProveedor.take(27) + "." else gasto.nombreProveedor
                canvas.drawText(provText, 195f, y + 14f, textPaint)

                val docText = if (gasto.nroComprobante.length > 12) gasto.nroComprobante.take(11) + "." else gasto.nroComprobante
                canvas.drawText(docText, 365f, y + 14f, textPaint)

                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textPaint.color = Color.rgb(15, 23, 42)
                canvas.drawText(formatClp(gasto.montoClp), PAGE_WIDTH - 46f, y + 14f, textPaint)
                textPaint.textAlign = Paint.Align.LEFT

                y += rowHeight
            }

            // Si es la última página, dibujamos totales y bloque de firmas
            if (pageNumber == totalPages) {
                y += 10f

                // 4. RESUMEN DE TOTALES Y SALDOS
                val totalRendido = rendicionConGastos.totalRendidoClp
                val fondoInicial = r.fondoInicial
                val diferencia = totalRendido - fondoInicial

                val summaryW = 230f
                val summaryX = PAGE_WIDTH - 40f - summaryW
                val summaryH = 68f

                paint.color = Color.rgb(241, 245, 249)
                val summaryRect = RectF(summaryX, y, summaryX + summaryW, y + summaryH)
                canvas.drawRoundRect(summaryRect, 6f, 6f, paint)

                paint.color = Color.rgb(203, 213, 225)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRoundRect(summaryRect, 6f, 6f, paint)
                paint.style = Paint.Style.FILL

                var sumY = y + 16f
                textPaint.textSize = 9f
                textPaint.color = Color.rgb(71, 85, 105)
                canvas.drawText("Fondo Inicial Entregado:", summaryX + 12f, sumY, textPaint)
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.color = Color.rgb(15, 23, 42)
                canvas.drawText(formatClp(fondoInicial), summaryX + summaryW - 12f, sumY, textPaint)
                textPaint.textAlign = Paint.Align.LEFT

                sumY += 16f
                textPaint.color = Color.rgb(71, 85, 105)
                canvas.drawText("Total Gastado en Comisión:", summaryX + 12f, sumY, textPaint)
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textPaint.color = Color.rgb(15, 23, 42)
                canvas.drawText(formatClp(totalRendido), summaryX + summaryW - 12f, sumY, textPaint)
                textPaint.textAlign = Paint.Align.LEFT

                sumY += 18f
                paint.color = Color.rgb(203, 213, 225)
                canvas.drawLine(summaryX + 10f, sumY - 6f, summaryX + summaryW - 10f, sumY - 6f, paint)

                val saldoEtiqueta = when {
                    diferencia > 0 -> "Saldo a Reembolsar al Técnico:"
                    diferencia < 0 -> "Saldo a Reintegrar a TEQMED:"
                    else -> "Saldo Final:"
                }
                val saldoColor = when {
                    diferencia > 0 -> Color.rgb(2, 132, 199) // Azul
                    diferencia < 0 -> Color.rgb(194, 65, 12)  // Naranja
                    else -> Color.rgb(0, 128, 96)            // Verde
                }
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textPaint.textSize = 9.5f
                textPaint.color = saldoColor
                canvas.drawText(saldoEtiqueta, summaryX + 12f, sumY, textPaint)
                textPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(formatClp(kotlin.math.abs(diferencia)), summaryX + summaryW - 12f, sumY, textPaint)
                textPaint.textAlign = Paint.Align.LEFT

                y += summaryH + 24f

                // 5. SECCIÓN DE FIRMAS DIGITALES
                val sigWidth = 220f
                val sigHeight = 85f

                // Firma 1: Técnico Responsable
                val tX = 50f
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRoundRect(RectF(tX, y, tX + sigWidth, y + sigHeight), 4f, 4f, paint)
                paint.color = Color.rgb(203, 213, 225)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRoundRect(RectF(tX, y, tX + sigWidth, y + sigHeight), 4f, 4f, paint)
                paint.style = Paint.Style.FILL

                // Línea de rúbrica
                paint.color = Color.rgb(148, 163, 184)
                canvas.drawLine(tX + 20f, y + 54f, tX + sigWidth - 20f, y + 54f, paint)

                textPaint.textAlign = Paint.Align.CENTER
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textPaint.textSize = 8.5f
                textPaint.color = Color.rgb(30, 41, 59)
                canvas.drawText(r.tecnicoNombre, tX + (sigWidth / 2), y + 66f, textPaint)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textPaint.textSize = 7.5f
                textPaint.color = Color.rgb(100, 116, 139)
                canvas.drawText("Técnico Clínico Responsable (${r.tecnicoRut})", tX + (sigWidth / 2), y + 76f, textPaint)

                // Firma 2: Jefatura de Operaciones / Supervisor (Con rúbrica digital estampar)
                val jX = PAGE_WIDTH - 50f - sigWidth
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRoundRect(RectF(jX, y, jX + sigWidth, y + sigHeight), 4f, 4f, paint)
                paint.color = Color.rgb(203, 213, 225)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRoundRect(RectF(jX, y, jX + sigWidth, y + sigHeight), 4f, 4f, paint)
                paint.style = Paint.Style.FILL

                // Si disponemos del Bitmap de la firma manuscrita, lo estampamos aquí
                if (firmBmp != null) {
                    val destRect = RectF(jX + 35f, y + 6f, jX + sigWidth - 35f, y + 50f)
                    canvas.drawBitmap(firmBmp, null, destRect, paint)
                } else {
                    paint.color = Color.rgb(148, 163, 184)
                    canvas.drawLine(jX + 20f, y + 54f, jX + sigWidth - 20f, y + 54f, paint)
                }

                textPaint.textAlign = Paint.Align.CENTER
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textPaint.textSize = 8.5f
                textPaint.color = Color.rgb(30, 41, 59)
                canvas.drawText("Dra. Marcela Fuentes", jX + (sigWidth / 2), y + 66f, textPaint)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textPaint.textSize = 7.5f
                textPaint.color = Color.rgb(100, 116, 139)
                canvas.drawText("Aprobación Jefatura Operaciones (12.839.210-4)", jX + (sigWidth / 2), y + 76f, textPaint)
                textPaint.textAlign = Paint.Align.LEFT
            }

            // 6. PIE DE PÁGINA
            paint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(40f, PAGE_HEIGHT - 35f, PAGE_WIDTH - 40f, PAGE_HEIGHT - 35f, paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 8f
            textPaint.color = Color.rgb(148, 163, 184)
            canvas.drawText("TEQMED SpA · Sistema de Rendición de Gastos y Viáticos Clínicos", 40f, PAGE_HEIGHT - 22f, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Página $pageNumber de $totalPages", PAGE_WIDTH - 40f, PAGE_HEIGHT - 22f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            document.finishPage(page)
        }

        // Guardar archivo PDF en cache de reportes
        return try {
            val reportDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val file = File(reportDir, "informe_rendicion_${r.id}_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            Log.d(TAG, "Informe PDF formal generado exitosamente: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error al escribir archivo PDF: ${e.message}", e)
            document.close()
            null
        }
    }

    /**
     * Obtiene el content Uri compatible con FileProvider para compartir o abrir el PDF.
     */
    fun getUriForPdf(context: Context, pdfFile: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
    }

    /**
     * Lanza intent para compartir el PDF a través de apps externas (WhatsApp, Gmail, Drive).
     */
    fun sharePdf(context: Context, pdfFile: File, titulo: String = "Informe Rendición TEQMED") {
        val uri = getUriForPdf(context, pdfFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, titulo)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Informe PDF TEQMED"))
    }

    /**
     * Lanza intent para visualizar el PDF en un visor nativo instalado en el dispositivo.
     */
    fun viewPdf(context: Context, pdfFile: File) {
        val uri = getUriForPdf(context, pdfFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si no hay visor dedicado, recurrir al chooser de compartir
            sharePdf(context, pdfFile)
        }
    }
}
