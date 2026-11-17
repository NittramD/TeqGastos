package com.example.teqgastos

import com.example.teqgastos.data.local.Converters
import com.example.teqgastos.data.local.entity.GastoEntity
import com.example.teqgastos.data.local.entity.RendicionEntity
import com.example.teqgastos.model.EstadoRendicion
import com.example.teqgastos.model.FormaPago
import com.example.teqgastos.model.SyncStatus
import com.example.teqgastos.sync.SyncEngineState
import com.example.teqgastos.sync.SyncSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Pruebas unitarias para persistencia y sincronización robusta offline-first (Fase 2).
 * Verifica Outbox Pattern, idempotencia mediante UUIDs, y TypeConverters en Room.
 */
class SyncOutboxPatternTest {

    private val converters = Converters()

    @Test
    fun testSyncStatusTypeConverters() {
        for (status in SyncStatus.values()) {
            val serialized = converters.fromSyncStatus(status)
            assertEquals(status.name, serialized)
            val deserialized = converters.toSyncStatus(serialized)
            assertEquals(status, deserialized)
        }

        // Caso inválido o desconocido retorna SYNCED de forma segura
        assertEquals(SyncStatus.SYNCED, converters.toSyncStatus("UNKNOWN_STATUS_XYZ"))
    }

    @Test
    fun testGastoEntityOutboxDefaults() {
        val gasto1 = GastoEntity(
            rendicionId = 1L,
            categoriaId = 1L,
            categoriaNombre = "Combustible",
            montoOriginal = 25000.0,
            montoClp = 25000.0,
            formaPago = FormaPago.REEMBOLSO_PROPIO,
            rutProveedor = "99.148.000-K",
            nombreProveedor = "Copec S.A.",
            nroComprobante = "FAC-12345",
            fechaGasto = "2026-11-17"
        )

        val gasto2 = GastoEntity(
            rendicionId = 1L,
            categoriaId = 1L,
            categoriaNombre = "Combustible",
            montoOriginal = 15000.0,
            montoClp = 15000.0,
            formaPago = FormaPago.REEMBOLSO_PROPIO,
            rutProveedor = "99.148.000-K",
            nombreProveedor = "Copec S.A.",
            nroComprobante = "FAC-12346",
            fechaGasto = "2026-11-17"
        )

        assertEquals(SyncStatus.SYNCED, gasto1.syncStatus)
        assertNotNull(gasto1.idempotencyKey)
        assertTrue(gasto1.lastModified > 0L)

        // Cada entidad debe generar su propia clave de idempotencia UUID única
        assertNotEquals(gasto1.idempotencyKey, gasto2.idempotencyKey)
        UUID.fromString(gasto1.idempotencyKey) // Debe parsear como UUID válido sin lanzar excepción
    }

    @Test
    fun testRendicionEntityOutboxDefaults() {
        val rendicion = RendicionEntity(
            titulo = "Mantención Hospital Clínico del Sur",
            fechaInicio = "2026-11-15",
            fechaFin = "2026-11-17",
            tecnicoNombre = "Gerardo Galán",
            tecnicoRut = "17.482.913-K",
            centroCostoId = 4L,
            centroCostoNombre = "Hospital Clínico del Sur",
            fondoInicial = 60000.0,
            estado = EstadoRendicion.BORRADOR
        )

        assertEquals(SyncStatus.SYNCED, rendicion.syncStatus)
        assertNotNull(rendicion.idempotencyKey)
        assertTrue(rendicion.lastModified > 0L)
        UUID.fromString(rendicion.idempotencyKey)
    }

    @Test
    fun testSyncEngineStateAndSummary() {
        val summary = SyncSummary(
            gastosSincronizados = 3,
            rendicionesSincronizadas = 1,
            gastosEliminados = 1,
            indicadoresActualizados = true,
            timestamp = System.currentTimeMillis()
        )

        assertEquals(3, summary.gastosSincronizados)
        assertEquals(1, summary.rendicionesSincronizadas)
        assertEquals(1, summary.gastosEliminados)
        assertTrue(summary.indicadoresActualizados)

        val successState = SyncEngineState.Success(summary.timestamp, summary)
        assertEquals(summary, successState.summary)

        val syncingState = SyncEngineState.Syncing("Gasto #12345", 0.5f)
        assertEquals(0.5f, syncingState.progress, 0.001f)
        assertEquals("Gasto #12345", syncingState.item)
    }
}
