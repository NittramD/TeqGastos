package com.example.teqgastos.util.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.teqgastos.ui.components.RutValidator
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern
import kotlin.coroutines.resume

data class OcrReceiptResult(
    val rutProveedor: String?,
    val nombreProveedor: String?,
    val nroComprobante: String?,
    val montoClp: Double?,
    val fechaGasto: String?,
    val rawText: String
)

object ReceiptOcrScanner {
    private const val TAG = "ReceiptOcrScanner"

    // Regex para RUT Chileno: 12.345.678-9 o 12345678-9 o 7.890.123-K
    private val RUT_REGEX = Pattern.compile(
        "(?:R\\.?U\\.?T\\.?:?\\s*)?\\b(\\d{1,2}\\.?\\d{3}\\.?\\d{3}-[\\dkK])\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Regex para Folio / N° Comprobante en Chile
    private val FOLIO_REGEX = Pattern.compile(
        "(?:FOLIO|BOLETA(?:\\s+ELECTR[OÓ]NICA)?|FACTURA(?:\\s+ELECTR[OÓ]NICA)?|N[°º]|NRO\\.?|NUMERO)\\s*[:#]?\\s*([0-9]{1,12})\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Regex para Fecha (dd/MM/yyyy o dd-MM-yyyy o yyyy-MM-dd)
    private val FECHA_REGEX_DMY = Pattern.compile(
        "\\b(0?[1-9]|[12][0-9]|3[01])[/.-](0?[1-9]|1[0-2])[/.-](20\\d{2})\\b"
    )
    private val FECHA_REGEX_YMD = Pattern.compile(
        "\\b(20\\d{2})[/.-](0?[1-9]|1[0-2])[/.-](0?[1-9]|[12][0-9]|3[01])\\b"
    )

    // Palabras clave para montos totales
    private val TOTAL_KEYWORDS = listOf(
        "TOTAL A PAGAR",
        "TOTAL PAGAR",
        "TOTAL CLP",
        "TOTAL BOLETA",
        "VALOR TOTAL",
        "IMPORTE TOTAL",
        "MONTO TOTAL",
        "TOTAL",
        "PAGO",
        "SALDO"
    )

    // Marcas comerciales frecuentes en servicios en terreno / traslados médicos
    private val KNOWN_BRANDS = listOf(
        "COPEC", "SHELL", "PETROBRAS", "ENEX", "PRONTO COPEC", "UPITA", "SPACIO 1",
        "AUTOPISTA CENTRAL", "COSTANERA NORTE", "VESPUCIO SUR", "VESPUCIO NORTE",
        "AUTOPISTA DEL ITATA", "RUTAS DEL PACIFICO", "RUTAS DEL BOSQUE", "SURVIAS",
        "CRUZ VERDE", "SALCOBRAND", "AHUMADA", "REDBUDGET", "HOTEL", "RESTAURANT",
        "LIDER", "JUMBO", "UNIMARC", "SODIMAC", "EASY"
    )

    /**
     * Procesa una imagen de boleta a partir de su Uri y extrae los campos estructurados.
     */
    suspend fun parseReceipt(context: Context, imageUri: Uri): OcrReceiptResult = withContext(Dispatchers.IO) {
        val image = try {
            InputImage.fromFilePath(context, imageUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando imagen desde Uri: ${e.message}", e)
            return@withContext OcrReceiptResult(null, null, null, null, null, "")
        }
        processInputImage(image)
    }

    /**
     * Procesa una imagen de boleta a partir de un Bitmap en memoria.
     */
    suspend fun parseReceipt(bitmap: Bitmap): OcrReceiptResult = withContext(Dispatchers.Default) {
        val image = InputImage.fromBitmap(bitmap, 0)
        processInputImage(image)
    }

    private suspend fun processInputImage(image: InputImage): OcrReceiptResult {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        return suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    try {
                        val result = parseVisionText(visionText.text)
                        continuation.resume(result)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error analizando texto OCR: ${e.message}", e)
                        continuation.resume(
                            OcrReceiptResult(null, null, null, null, null, visionText.text)
                        )
                    } finally {
                        recognizer.close()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Fallo en reconocimiento ML Kit: ${e.message}", e)
                    continuation.resume(
                        OcrReceiptResult(null, null, null, null, null, "")
                    )
                    recognizer.close()
                }
        }
    }

    /**
     * Extrae información estructurada a partir del texto plano reconocido.
     */
    fun parseVisionText(rawText: String): OcrReceiptResult {
        if (rawText.isBlank()) {
            return OcrReceiptResult(null, null, null, null, null, "")
        }

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val rut = extractRut(lines, rawText)
        val nroComprobante = extractFolio(lines, rawText)
        val fecha = extractFecha(rawText)
        val monto = extractMonto(lines, rawText)
        val proveedor = extractProveedor(lines)

        return OcrReceiptResult(
            rutProveedor = rut,
            nombreProveedor = proveedor,
            nroComprobante = nroComprobante,
            montoClp = monto,
            fechaGasto = fecha,
            rawText = rawText
        )
    }

    /**
     * Extrae y formatea el RUT chileno del proveedor emisor.
     */
    private fun extractRut(lines: List<String>, fullText: String): String? {
        val matcher = RUT_REGEX.matcher(fullText)
        while (matcher.find()) {
            val candidate = matcher.group(1) ?: continue
            val formatted = RutValidator.formatRut(candidate)
            // Validamos que sea un RUT válido con algoritmo Módulo 11
            if (RutValidator.isValid(formatted)) {
                // Descartamos si por error se captura el RUT de TEQMED como emisor
                if (!formatted.startsWith("76.845.120")) {
                    return formatted
                }
            }
        }

        // Búsqueda línea por línea si el matcher global omitió saltos
        for (line in lines) {
            val lineMatcher = RUT_REGEX.matcher(line)
            if (lineMatcher.find()) {
                val candidate = lineMatcher.group(1) ?: continue
                val formatted = RutValidator.formatRut(candidate)
                return formatted
            }
        }
        return null
    }

    /**
     * Extrae el número de folio o comprobante (Boleta / Factura).
     */
    private fun extractFolio(lines: List<String>, fullText: String): String? {
        val matcher = FOLIO_REGEX.matcher(fullText)
        if (matcher.find()) {
            val folio = matcher.group(1)
            if (!folio.isNullOrBlank() && folio.length in 1..10) {
                return folio
            }
        }

        // Búsqueda en líneas individuales buscando palabras clave
        for ((index, line) in lines.withIndex()) {
            val upper = line.uppercase(Locale.getDefault())
            if (upper.contains("FOLIO") || upper.contains("BOLETA") || upper.contains("N°") || upper.contains("NRO")) {
                val digitsOnly = line.filter { it.isDigit() }
                if (digitsOnly.length in 2..9) {
                    return digitsOnly
                }
                // Mirar la línea siguiente si el número viene abajo
                if (index + 1 < lines.size) {
                    val nextDigits = lines[index + 1].filter { it.isDigit() }
                    if (nextDigits.length in 2..9) {
                        return nextDigits
                    }
                }
            }
        }
        return null
    }

    /**
     * Extrae y normaliza la fecha al formato ISO yyyy-MM-dd.
     */
    private fun extractFecha(fullText: String): String? {
        // Formato dd/MM/yyyy
        val dmyMatcher = FECHA_REGEX_DMY.matcher(fullText)
        if (dmyMatcher.find()) {
            val day = dmyMatcher.group(1)?.toIntOrNull() ?: 1
            val month = dmyMatcher.group(2)?.toIntOrNull() ?: 1
            val year = dmyMatcher.group(3)?.toIntOrNull() ?: 2026
            return try {
                val date = LocalDate.of(year, month, day)
                date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                null
            }
        }

        // Formato yyyy/MM/dd
        val ymdMatcher = FECHA_REGEX_YMD.matcher(fullText)
        if (ymdMatcher.find()) {
            val year = ymdMatcher.group(1)?.toIntOrNull() ?: 2026
            val month = ymdMatcher.group(2)?.toIntOrNull() ?: 1
            val day = ymdMatcher.group(3)?.toIntOrNull() ?: 1
            return try {
                val date = LocalDate.of(year, month, day)
                date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    /**
     * Extrae el monto total en CLP de la boleta.
     */
    private fun extractMonto(lines: List<String>, fullText: String): Double? {
        // Estrategia 1: Buscar líneas con palabras clave de "TOTAL"
        for ((index, line) in lines.withIndex()) {
            val upper = line.uppercase(Locale.getDefault())
            for (keyword in TOTAL_KEYWORDS) {
                if (upper.contains(keyword)) {
                    // Extraer montos de esta misma línea
                    val montoEnLinea = parseMontoFromLine(line.substring(upper.indexOf(keyword) + keyword.length))
                    if (montoEnLinea != null && montoEnLinea > 0) {
                        return montoEnLinea
                    }
                    // Si no está en la misma línea, mirar la siguiente
                    if (index + 1 < lines.size) {
                        val montoNext = parseMontoFromLine(lines[index + 1])
                        if (montoNext != null && montoNext > 0) {
                            return montoNext
                        }
                    }
                }
            }
        }

        // Estrategia 2: Buscar símbolos de moneda $ y tomar el monto razonable más alto
        val montosEncontrados = mutableListOf<Double>()
        val dollarRegex = Pattern.compile("\\$\\s*([0-9]{1,3}(?:\\.[0-9]{3})*|[0-9]{3,8})")
        val dollarMatcher = dollarRegex.matcher(fullText)
        while (dollarMatcher.find()) {
            val numStr = dollarMatcher.group(1)?.replace(".", "") ?: ""
            numStr.toDoubleOrNull()?.let { montosEncontrados.add(it) }
        }

        if (montosEncontrados.isNotEmpty()) {
            // El total suele ser el número más alto en una boleta de consumo
            return montosEncontrados.filter { it in 500.0..5000000.0 }.maxOrNull()
        }

        return null
    }

    private fun parseMontoFromLine(line: String): Double? {
        // Limpiar texto para aislar dígitos y separadores de miles
        val regex = Pattern.compile("[$]?\\s*([0-9]{1,3}(?:\\.[0-9]{3})+|[0-9]{3,8})")
        val matcher = regex.matcher(line)
        if (matcher.find()) {
            val clean = matcher.group(1)?.replace(".", "")?.trim() ?: ""
            return clean.toDoubleOrNull()
        }
        return null
    }

    /**
     * Extrae el nombre o razón social del proveedor emisor.
     */
    private fun extractProveedor(lines: List<String>): String? {
        // 1. Verificar si coincide con alguna marca conocida de terreno
        for (line in lines.take(10)) {
            val upper = line.uppercase(Locale.getDefault())
            for (brand in KNOWN_BRANDS) {
                if (upper.contains(brand)) {
                    return line.take(40).trim()
                }
            }
        }

        // 2. Extraer de los primeros encabezados que no sean títulos genéricos
        for (line in lines.take(5)) {
            val upper = line.uppercase(Locale.getDefault())
            if (upper.contains("BOLETA") || upper.contains("FACTURA") ||
                upper.contains("S.I.I.") || upper.contains("R.U.T") ||
                upper.contains("RUT") || upper.contains("ELECTRONICA") ||
                upper.contains("ORIGINAL") || upper.contains("CLIENTE") ||
                upper.all { it.isDigit() || it.isWhitespace() }
            ) {
                continue
            }
            if (line.length in 4..45) {
                return line.trim()
            }
        }

        return null
    }
}
