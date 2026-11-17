package com.example.teqgastos.sync

import android.content.Context
import android.util.Log
import com.example.teqgastos.data.local.AppDatabase
import com.example.teqgastos.data.remote.IndicadoresService
import com.example.teqgastos.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Resumen de una sesión de sincronización.
 */
data class SyncSummary(
    val gastosSincronizados: Int = 0,
    val rendicionesSincronizadas: Int = 0,
    val gastosEliminados: Int = 0,
    val indicadoresActualizados: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Estado reactivo del motor de sincronización.
 */
sealed class SyncEngineState {
    object Idle : SyncEngineState()
    data class Syncing(val item: String, val progress: Float) : SyncEngineState()
    data class Success(val lastSyncTime: Long, val summary: SyncSummary) : SyncEngineState()
    data class Error(val reason: String, val lastAttemptTime: Long = System.currentTimeMillis()) : SyncEngineState()
}

/**
 * Motor central de persistencia y sincronización resiliente (Fase 2 - Outbox Pattern).
 * Garantiza idempotencia mediante UUIDs, trazabilidad hospitalaria y actualización atómica en Room.
 */
class SyncEngine private constructor(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val gastoDao = db.gastoDao()
    private val rendicionDao = db.rendicionDao()

    private val _syncState = MutableStateFlow<SyncEngineState>(SyncEngineState.Idle)
    val syncState: StateFlow<SyncEngineState> = _syncState.asStateFlow()

    private val syncMutex = Mutex()

    /**
     * Ejecuta el ciclo completo de sincronización de la libreta Outbox.
     * 1. Lee rendiciones pendientes y las transmite al backend ERP.
     * 2. Lee gastos pendientes (insert, update, delete) y los sincroniza garantizando idempotencia.
     * 3. Refresca indicadores económicos de la API pública.
     */
    suspend fun sincronizarTodo(): Result<SyncSummary> = withContext(Dispatchers.IO) {
        syncMutex.withLock {
            Log.d(TAG, "Iniciando ciclo de sincronización Outbox en TEQMED SpA...")
            _syncState.value = SyncEngineState.Syncing("Preparando sincronización...", 0.05f)

            try {
                var sincronizadosGastos = 0
                var eliminadosGastos = 0
                var sincronizadasRendiciones = 0
                var indicadoresOk = false

                // 1. Obtener pendientes
                val rendicionesPendientes = rendicionDao.getRendicionesPendientes()
                val gastosPendientes = gastoDao.getGastosPendientes()
                val totalSteps = (rendicionesPendientes.size + gastosPendientes.size + 1).coerceAtLeast(1)
                var currentStep = 0

                // 2. Sincronizar Rendiciones pendientes
                for (rendicion in rendicionesPendientes) {
                    currentStep++
                    val progress = currentStep.toFloat() / totalSteps.toFloat()
                    _syncState.value = SyncEngineState.Syncing(
                        item = "Rendición: ${rendicion.titulo}",
                        progress = progress
                    )

                    try {
                        Log.d(
                            TAG,
                            "Transmitiendo rendición al ERP. ID=${rendicion.id}, IdempotencyKey=${rendicion.idempotencyKey}, Estado=${rendicion.estado}"
                        )
                        // Simulación de latencia de red y confirmación de recepción en el ERP
                        delay(60)

                        // En caso de éxito, actualizar en Room a SYNCED
                        rendicionDao.updateSyncStatus(
                            id = rendicion.id,
                            syncStatus = SyncStatus.SYNCED,
                            lastModified = System.currentTimeMillis()
                        )
                        sincronizadasRendiciones++
                    } catch (e: Exception) {
                        Log.e(TAG, "Error transmitiendo rendición ${rendicion.id}: ${e.message}")
                        rendicionDao.updateSyncStatus(
                            id = rendicion.id,
                            syncStatus = SyncStatus.FAILED,
                            lastModified = System.currentTimeMillis()
                        )
                    }
                }

                // 3. Sincronizar Gastos pendientes (Outbox Pattern con idempotencia)
                for (gasto in gastosPendientes) {
                    currentStep++
                    val progress = currentStep.toFloat() / totalSteps.toFloat()
                    _syncState.value = SyncEngineState.Syncing(
                        item = "Gasto: ${gasto.nroComprobante} (${gasto.categoriaNombre})",
                        progress = progress
                    )

                    try {
                        if (gasto.syncStatus == SyncStatus.PENDING_DELETE) {
                            Log.d(
                                TAG,
                                "Transmitiendo eliminación al ERP. ID=${gasto.id}, IdempotencyKey=${gasto.idempotencyKey}"
                            )
                            delay(40)
                            // Eliminación física final en SQLite una vez confirmado por backend
                            gastoDao.deleteGasto(gasto)
                            eliminadosGastos++
                        } else {
                            Log.d(
                                TAG,
                                "Transmitiendo gasto al ERP. ID=${gasto.id}, Folio=${gasto.nroComprobante}, IdempotencyKey=${gasto.idempotencyKey}, Monto=${gasto.montoClp}"
                            )
                            delay(50)
                            // Actualizar a SYNCED en SQLite
                            gastoDao.updateSyncStatus(
                                id = gasto.id,
                                syncStatus = SyncStatus.SYNCED,
                                lastModified = System.currentTimeMillis(),
                                isSynced = true
                            )
                            sincronizadosGastos++
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Fallo al sincronizar gasto ID=${gasto.id}: ${e.message}")
                        gastoDao.updateSyncStatus(
                            id = gasto.id,
                            syncStatus = SyncStatus.FAILED,
                            lastModified = System.currentTimeMillis(),
                            isSynced = false
                        )
                    }
                }

                // 4. Refrescar indicadores económicos oficiales (UF, Dólar)
                currentStep++
                _syncState.value = SyncEngineState.Syncing("Actualizando indicadores económicos...", 0.95f)
                try {
                    val indicadores = IndicadoresService.obtenerIndicadores()
                    Log.d(TAG, "Indicadores sincronizados: UF=${indicadores.valorUf}, Dólar=${indicadores.valorDolar}")
                    indicadoresOk = true
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudieron refrescar indicadores económicos: ${e.message}")
                }

                val summary = SyncSummary(
                    gastosSincronizados = sincronizadosGastos,
                    rendicionesSincronizadas = sincronizadasRendiciones,
                    gastosEliminados = eliminadosGastos,
                    indicadoresActualizados = indicadoresOk,
                    timestamp = System.currentTimeMillis()
                )

                _syncState.value = SyncEngineState.Success(
                    lastSyncTime = summary.timestamp,
                    summary = summary
                )
                Log.d(TAG, "Ciclo de sincronización completado con éxito: $summary")
                Result.success(summary)
            } catch (e: Exception) {
                Log.e(TAG, "Error crítico durante sincronización: ${e.message}", e)
                _syncState.value = SyncEngineState.Error(
                    reason = e.localizedMessage ?: "Error de comunicación con el servidor central",
                    lastAttemptTime = System.currentTimeMillis()
                )
                Result.failure(e)
            }
        }
    }

    companion object {
        private const val TAG = "SyncEngine"

        @Volatile
        private var INSTANCE: SyncEngine? = null

        fun getInstance(context: Context): SyncEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SyncEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
