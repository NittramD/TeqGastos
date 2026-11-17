package com.example.teqgastos

import com.example.teqgastos.util.camera.ReceiptOcrScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptOcrScannerTest {

    @Test
    fun parseVisionText_boletaCopecCombustible_extraeDatosCorrectos() {
        val sampleCopecText = """
            COPEC S.A.
            ESTACION DE SERVICIO RUTA 5 SUR KM 480
            R.U.T.: 99.520.000-7
            BOLETA ELECTRONICA N°: 849201
            FECHA: 15/11/2026 14:32
            
            DIESEL ULTRA GRADO 2
            45.2 LITROS X $1.150
            
            TOTAL: $51.980
            FORMA DE PAGO: TARJETA DEBITO
        """.trimIndent()

        val result = ReceiptOcrScanner.parseVisionText(sampleCopecText)

        assertEquals("99.520.000-7", result.rutProveedor)
        assertTrue(result.nombreProveedor?.contains("COPEC") == true)
        assertEquals("849201", result.nroComprobante)
        assertEquals(51980.0, result.montoClp ?: 0.0, 0.01)
        assertEquals("2026-11-15", result.fechaGasto)
    }

    @Test
    fun parseVisionText_peajeAutopista_extraeFolioYMonto() {
        val samplePeajeText = """
            AUTOPISTA DEL ITATA S.A.
            RUT: 96.845.210-3
            PLAZA AGUA AMARILLA
            FOLIO: 1948203
            FECHA: 14-11-2026
            CATEGORIA 1 VEHICULO LIVIANO
            TOTAL A PAGAR: $4.850
            GRACIAS POR SU PREFERENCIA
        """.trimIndent()

        val result = ReceiptOcrScanner.parseVisionText(samplePeajeText)

        assertEquals("96.845.210-3", result.rutProveedor)
        assertTrue(result.nombreProveedor?.contains("AUTOPISTA") == true)
        assertEquals("1948203", result.nroComprobante)
        assertEquals(4850.0, result.montoClp ?: 0.0, 0.01)
        assertEquals("2026-11-14", result.fechaGasto)
    }

    @Test
    fun parseVisionText_almuerzoClinico_extraeFechaYTotal() {
        val sampleRestaurant = """
            RESTAURANT EL TABLON BIOBIO
            RUT 76.123.456-K
            BOLETA N: 48120
            FECHA: 2026-11-12
            1 MENU EJECUTIVO CLINICO
            TOTAL $ 18.500
        """.trimIndent()

        val result = ReceiptOcrScanner.parseVisionText(sampleRestaurant)

        assertEquals("76.123.456-K", result.rutProveedor)
        assertEquals("48120", result.nroComprobante)
        assertEquals(18500.0, result.montoClp ?: 0.0, 0.01)
        assertEquals("2026-11-12", result.fechaGasto)
    }
}
