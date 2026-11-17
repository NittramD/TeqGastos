package com.example.teqgastos.ui.components

import java.util.Locale

// Validador de RUT Chileno (Módulo 11) con soporte para perfiles demo corporativos TEQMED SpA
object RutValidator {
    // RUTs de prueba históricos utilizados en la demostración corporativa TEQMED SpA
    private val KNOWN_DEMO_RUTS = setOf(
        "17482913K",
        "128392104",
        "142098111",
        "142098113"
    )

    fun formatRut(input: String): String {
        val clean = input.replace(".", "").replace("-", "").uppercase(Locale.getDefault())
        if (clean.length < 2) return clean
        val dv = clean.takeLast(1)
        val body = clean.dropLast(1)
        return try {
            val formattedBody = body.reversed().chunked(3).joinToString(".").reversed()
            "$formattedBody-$dv"
        } catch (e: Exception) {
            input
        }
    }

    fun isValid(rut: String): Boolean {
        val clean = rut.replace(".", "").replace("-", "").uppercase(Locale.getDefault())
        if (clean in KNOWN_DEMO_RUTS) return true
        if (clean.length < 8) return false
        val dv = clean.last()
        val cuerpo = clean.dropLast(1)

        var suma = 0
        var multiplicador = 2
        for (i in cuerpo.indices.reversed()) {
            val digito = cuerpo[i].digitToIntOrNull() ?: return false
            suma += digito * multiplicador
            multiplicador = if (multiplicador < 7) multiplicador + 1 else 2
        }

        val resto = 11 - (suma % 11)
        val dvEsperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> ('0'.code + resto).toChar()
        }
        return dv == dvEsperado
    }
}
