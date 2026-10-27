package com.example.teqgastos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.teqgastos.data.local.entity.AuditoriaEntity
import com.example.teqgastos.data.local.entity.CategoriaGastoEntity
import com.example.teqgastos.data.local.entity.CentroCostoEntity
import com.example.teqgastos.data.local.entity.GastoEntity
import com.example.teqgastos.data.local.entity.RendicionConGastos
import com.example.teqgastos.data.local.entity.RendicionEntity
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.EstadoRendicion
import kotlinx.coroutines.flow.Flow

@Dao
interface RendicionDao {
    @Query("SELECT * FROM rendiciones ORDER BY createdAt DESC")
    fun getAllRendicionesFlow(): Flow<List<RendicionEntity>>

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
}

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

@Dao
interface AuditoriaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditoria(registro: AuditoriaEntity): Long

    @Query("SELECT * FROM auditoria_rendicion WHERE rendicionId = :rendicionId ORDER BY timestamp DESC")
    fun getAuditoriaByRendicionFlow(rendicionId: Long): Flow<List<AuditoriaEntity>>
}

@Dao
interface CatalogDao {
    @Query("SELECT * FROM categorias_gasto ORDER BY nombre ASC")
    fun getCategoriasFlow(): Flow<List<CategoriaGastoEntity>>

    @Query("SELECT * FROM categorias_gasto WHERE id = :id")
    suspend fun getCategoriaById(id: Long): CategoriaGastoEntity?

    @Query("SELECT * FROM centros_costo ORDER BY nombre ASC")
    fun getCentrosCostoFlow(): Flow<List<CentroCostoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategorias(categorias: List<CategoriaGastoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCentrosCosto(centros: List<CentroCostoEntity>)

    @Query("SELECT COUNT(*) FROM categorias_gasto")
    suspend fun countCategorias(): Int
}
