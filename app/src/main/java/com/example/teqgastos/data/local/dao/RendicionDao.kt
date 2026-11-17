package com.example.teqgastos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.teqgastos.data.local.entity.RendicionConGastos
import com.example.teqgastos.data.local.entity.RendicionEntity
import com.example.teqgastos.model.EstadoRendicion
import com.example.teqgastos.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RendicionDao {
    @Query("SELECT * FROM rendiciones ORDER BY createdAt DESC")
    fun getAllRendicionesFlow(): Flow<List<RendicionEntity>>

    @Query("SELECT * FROM rendiciones WHERE id = :id")
    suspend fun getRendicionById(id: Long): RendicionEntity?

    @Query("SELECT * FROM rendiciones WHERE tecnicoRut = :rut ORDER BY createdAt DESC")
    fun getRendicionesByTecnico(rut: String): Flow<List<RendicionEntity>>

    @Transaction
    @Query("SELECT * FROM rendiciones WHERE id = :id")
    fun getRendicionConGastosFlow(id: Long): Flow<RendicionConGastos?>

    @Transaction
    @Query("SELECT * FROM rendiciones WHERE id = :id")
    suspend fun getRendicionConGastos(id: Long): RendicionConGastos?

    @Transaction
    @Query("SELECT * FROM rendiciones WHERE estado IN (:estados) ORDER BY createdAt DESC")
    fun getRendicionesByEstadosFlow(estados: List<EstadoRendicion>): Flow<List<RendicionConGastos>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRendicion(rendicion: RendicionEntity): Long

    @Update
    suspend fun updateRendicion(rendicion: RendicionEntity)

    @Query("UPDATE rendiciones SET estado = :nuevoEstado WHERE id = :id")
    suspend fun updateEstadoRendicion(id: Long, nuevoEstado: EstadoRendicion)

    @Delete
    suspend fun deleteRendicion(rendicion: RendicionEntity)

    // Métodos de Sincronización Outbox Pattern
    @Query("SELECT * FROM rendiciones WHERE syncStatus != 'SYNCED' ORDER BY lastModified ASC")
    suspend fun getRendicionesPendientes(): List<RendicionEntity>

    @Query("SELECT * FROM rendiciones WHERE syncStatus != 'SYNCED' ORDER BY lastModified ASC")
    fun getRendicionesPendientesFlow(): Flow<List<RendicionEntity>>

    @Query("SELECT COUNT(*) FROM rendiciones WHERE syncStatus != 'SYNCED'")
    fun countRendicionesPendientes(): Flow<Int>

    @Query("UPDATE rendiciones SET syncStatus = :syncStatus, lastModified = :lastModified WHERE id = :id")
    suspend fun updateSyncStatus(
        id: Long,
        syncStatus: SyncStatus,
        lastModified: Long = System.currentTimeMillis()
    )

    @Query("UPDATE rendiciones SET syncStatus = 'SYNCED', lastModified = :time WHERE syncStatus != 'SYNCED'")
    suspend fun marcarTodasSincronizadas(time: Long = System.currentTimeMillis())
}
