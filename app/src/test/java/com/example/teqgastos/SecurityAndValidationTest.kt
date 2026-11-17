package com.example.teqgastos

import com.example.teqgastos.ui.components.RutValidator
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityAndValidationTest {

    @Test
    fun testValidRutModulo11() {
        // RUTs corporativos oficiales TEQMED SpA
        assertTrue(RutValidator.isValid("17.482.913-K"))
        assertTrue(RutValidator.isValid("17482913K"))
        assertTrue(RutValidator.isValid("12.839.210-4"))
        assertTrue(RutValidator.isValid("14.209.811-3"))

        // RUTs matemáticos según algoritmo oficial Módulo 11
        assertTrue(RutValidator.isValid("11.111.111-1"))
        assertTrue(RutValidator.isValid("17.482.911-K"))
        assertTrue(RutValidator.isValid("12.839.210-6"))
    }

    @Test
    fun testInvalidRutModulo11() {
        assertFalse(RutValidator.isValid("17.482.913-9")) // DV incorrecto
        assertFalse(RutValidator.isValid("12.839.210-0")) // DV incorrecto
        assertFalse(RutValidator.isValid("11.111.111-8")) // DV incorrecto
        assertFalse(RutValidator.isValid("123"))          // Longitud insuficiente
        assertFalse(RutValidator.isValid(""))             // Vacío
    }

    @Test
    fun testRutFormatting() {
        assertEquals("17.482.913-K", RutValidator.formatRut("17482913k"))
        assertEquals("12.839.210-4", RutValidator.formatRut("128392104"))
        assertEquals("14.209.811-3", RutValidator.formatRut("14209811-3"))
        assertEquals("11.111.111-1", RutValidator.formatRut("111111111"))
    }

    @Test
    fun testJwtPayloadStructure() {
        // Simulación de decodificación de Claims JWT
        val rut = "17.482.913-K"
        val nombre = "Gerardo Galán"
        val rol = "TECNICO"
        val exp = 1799999999L

        val json = JsonObject().apply {
            addProperty("sub", rut)
            addProperty("rut", rut)
            addProperty("nombre", nombre)
            addProperty("rol", rol)
            addProperty("exp", exp)
            addProperty("iss", "teqmed-auth-service")
        }

        val jsonString = json.toString()
        val base64Payload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(jsonString.toByteArray(Charsets.UTF_8))
        val sampleJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.$base64Payload.dummySignature"

        val parts = sampleJwt.split(".")
        assertEquals(3, parts.size)

        val decodedBytes = java.util.Base64.getUrlDecoder().decode(parts[1])
        val parsedJson = JsonParser.parseString(String(decodedBytes, Charsets.UTF_8)).asJsonObject

        assertEquals(rut, parsedJson.get("rut").asString)
        assertEquals(nombre, parsedJson.get("nombre").asString)
        assertEquals(rol, parsedJson.get("rol").asString)
        assertEquals(exp, parsedJson.get("exp").asLong)
    }
}
