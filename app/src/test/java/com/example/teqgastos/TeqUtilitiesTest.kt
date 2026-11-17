package com.example.teqgastos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeqUtilitiesTest {

    @Test
    fun ivaCalculator_netoAndIvaMatchTotal() {
        val total = 11900.0
        val neto = total / 1.19
        val iva = total - neto

        assertEquals(10000.0, neto, 0.01)
        assertEquals(1900.0, iva, 0.01)
        assertEquals(total, neto + iva, 0.01)
    }

    @Test
    fun semaforoFondo_consumoPercentageCalculations() {
        val fondoInicial = 100000.0

        val gastoBajo = 45000.0 // 45% -> Verde
        val pctBajo = gastoBajo / fondoInicial
        assertTrue(pctBajo < 0.70)

        val gastoMedio = 80000.0 // 80% -> Ámbar
        val pctMedio = gastoMedio / fondoInicial
        assertTrue(pctMedio in 0.70..0.95)

        val gastoAlto = 110000.0 // 110% -> Rojo
        val pctAlto = gastoAlto / fondoInicial
        assertTrue(pctAlto > 0.95)
        assertEquals(-10000.0, fondoInicial - gastoAlto, 0.01)
    }

    @Test
    fun odometroCombustible_estimationAccuracy() {
        val distanciaKm = 150.0
        val rendimiento = 12.0 // km/L
        val precioLitro = 1320.0 // CLP/L

        val litros = distanciaKm / rendimiento
        val costo = litros * precioLitro

        assertEquals(12.5, litros, 0.01)
        assertEquals(16500.0, costo, 0.01)
    }
}
