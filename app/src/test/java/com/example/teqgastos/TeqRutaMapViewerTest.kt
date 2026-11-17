package com.example.teqgastos

import com.example.teqgastos.model.CoordenadaGps
import com.example.teqgastos.model.TrasladoTeqRuta
import com.example.teqgastos.ui.screens.tecnico.TRAMOS_CATALOGO_TEQRUTA
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TeqRutaMapViewerTest {

    @Test
    fun catalogoTramos_hasValidRoutesAndCoordinatesInChile() {
        assertFalse(TRAMOS_CATALOGO_TEQRUTA.isEmpty())
        assertEquals(4, TRAMOS_CATALOGO_TEQRUTA.size)

        for (tramo in TRAMOS_CATALOGO_TEQRUTA) {
            assertNotNull(tramo.id)
            assertTrue("El tramo debe tener distancia mayor a 0 km", tramo.kilometros > 0)
            assertTrue("El costo de combustible debe ser positivo", tramo.combustibleClp > 0)
            assertTrue("Debe tener peajes definidos", tramo.peajesClp >= 0)

            // Validar coordenadas en territorio chileno Macrozona Sur (Lat: [-40, -35], Lng: [-74, -71])
            assertTrue("Latitud origen válida en Chile (${tramo.origenLat})", tramo.origenLat in -45.0..-30.0)
            assertTrue("Longitud origen válida en Chile (${tramo.origenLng})", tramo.origenLng in -76.0..-69.0)
            assertTrue("Latitud destino válida en Chile (${tramo.destinoLat})", tramo.destinoLat in -45.0..-30.0)
            assertTrue("Longitud destino válida en Chile (${tramo.destinoLng})", tramo.destinoLng in -76.0..-69.0)

            // Puntos intermedios de polilínea
            for (punto in tramo.puntosRuta) {
                assertTrue("Punto intermedio lat válida", punto.lat in -45.0..-30.0)
                assertTrue("Punto intermedio lng válida", punto.lng in -76.0..-69.0)
            }
        }
    }

    @Test
    fun tramoEspecifico_concepcionLosAngeles_detailsCorrect() {
        val tramo1 = TRAMOS_CATALOGO_TEQRUTA.first { it.id == "TR-01" }
        assertEquals("Concepción ➔ Hospital Los Ángeles", tramo1.tramo)
        assertEquals("Base TEQMED Concepción", tramo1.origenNombre)
        assertEquals("Hospital Dr. Víctor Ríos Ruiz (Los Ángeles)", tramo1.destinoNombre)
        assertEquals(132.5, tramo1.kilometros, 0.01)
        assertEquals(4900.0, tramo1.peajesClp, 0.01)
        assertEquals(23800.0, tramo1.combustibleClp, 0.01)
        assertTrue(tramo1.peajesDetalle.contains("Peaje Chaimávida"))
    }

    @Test
    fun coordenadaGps_dataClassEqualityAndLabel() {
        val coord1 = CoordenadaGps(-36.8270, -73.0503, "TEQMED")
        val coord2 = CoordenadaGps(-36.8270, -73.0503, "TEQMED")
        assertEquals(coord1, coord2)
        assertEquals("TEQMED", coord1.etiqueta)
    }
}
