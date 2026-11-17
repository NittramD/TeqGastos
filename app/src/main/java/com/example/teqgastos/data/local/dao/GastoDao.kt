package com.example.teqgastos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.teqgastos.data.local.entity.GastoEntity
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Query("SELECT * FROM gastos WHERE rendicionId = :rendicionId AND syncStatus != 'PENDING_DELETE' ORDER BY createdAt DESC")
    fun getGastosByRendicionFlow(rendicionId: Long): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE id = :id")
    suspend fun getGastoById(id: Long): GastoEntity?

    @Query("SELECT * FROM gastos WHERE id = :id")
    fun getGastoByIdFlow(id: Long): Flow<GastoEntity?>

    @Query("SELECT * FROM gastos WHERE idempotencyKey = :key LIMIT 1")
    suspend fun getGastoByIdempotencyKey(key: String): GastoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGasto(gasto: GastoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGastos(gastos: List<GastoEntity>): List<Long>

    @Update
    suspend fun updateGasto(gasto: GastoEntity)

    @Delete
    suspend fun deleteGasto(gasto: GastoEntity)

    // Control de duplicidad: mismo RUT y folio
    @Query("SELECT * FROM gastos WHERE rutProveedor = :rutProveedor AND nroComprobante = :nroComprobante AND id != :currentId LIMIT 1")
    suspend fun findDuplicado(rutProveedor: String, nroComprobante: String, currentId: Long = 0): GastoEntity?

    @Query("UPDATE gastos SET estadoGasto = :nuevoEstado, observacionSupervisor = :observacion WHERE id = :id")
    suspend fun updateEstadoGasto(id: Long, nuevoEstado: EstadoGasto, observacion: String? = null)

    // Consultas y operaciones de sincronización (Outbox Pattern)
    @Query("SELECT * FROM gastos WHERE syncStatus = :status ORDER BY lastModified ASC")
    suspend fun getGastosBySyncStatus(status: SyncStatus): List<GastoEntity>

    @Query("SELECT * FROM gastos WHERE syncStatus IN ('PENDING_INSERT', 'PENDING_UPDATE', 'PENDING_DELETE') ORDER BY lastModified ASC")
    suspend fun getGastosPendientes(): List<GastoEntity>

    @Query("UPDATE gastos SET syncStatus = :syncStatus, lastModified = :lastModified, isSynced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(
        id: Long,
        syncStatus: SyncStatus,
        lastModified: Long = System.currentTimeMillis(),
        isSynced: Boolean = (syncStatus == SyncStatus.SYNCED)
    )

    @Query("SELECT COUNT(*) FROM gastos WHERE syncStatus != 'SYNCED'")
    fun countGastosPendientes(): Flow<Int>

    @Query("SELECT COUNT(*) FROM gastos WHERE syncStatus != 'SYNCED'")
    suspend fun countGastosPendientesSync(): Int

    // Compatibilidad retrospectiva con WorkManager
    @Query("SELECT * FROM gastos WHERE isSynced = 0 OR syncStatus != 'SYNCED'")
    suspend fun getGastosPendientesSincronizacion(): List<GastoEntity>

    @Query("UPDATE gastos SET syncStatus = 'SYNCED', isSynced = 1, lastModified = :time WHERE syncStatus != 'SYNCED' OR isSynced = 0")
    suspend fun marcarTodosSincronizados(time: Long = System.currentTimeMillis())
}
