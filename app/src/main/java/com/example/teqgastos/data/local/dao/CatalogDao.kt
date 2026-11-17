package com.example.teqgastos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.teqgastos.data.local.entity.CategoriaGastoEntity
import com.example.teqgastos.data.local.entity.CentroCostoEntity
import kotlinx.coroutines.flow.Flow

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
