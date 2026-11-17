package com.example.teqgastos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.teqgastos.data.local.entity.GastoEntity
import com.example.teqgastos.model.EstadoGasto
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Query("SELECT * FROM gastos WHERE rendicionId = :rendicionId ORDER BY createdAt DESC")
    fun getGastosByRendicionFlow(rendicionId: Long): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE id = :id")
    suspend fun getGastoById(id: Long): GastoEntity?

    @Query("SELECT * FROM gastos WHERE id = :id")
    fun getGastoByIdFlow(id: Long): Flow<GastoEntity?>

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

    // Sincronización en segundo plano (WorkManager)
    @Query("SELECT * FROM gastos WHERE isSynced = 0")
    suspend fun getGastosPendientesSincronizacion(): List<GastoEntity>

    @Query("UPDATE gastos SET isSynced = 1 WHERE isSynced = 0")
    suspend fun marcarTodosSincronizados()
}
