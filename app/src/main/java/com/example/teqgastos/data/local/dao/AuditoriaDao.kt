package com.example.teqgastos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.teqgastos.data.local.entity.AuditoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditoriaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditoria(registro: AuditoriaEntity): Long

    @Query("SELECT * FROM auditoria_rendicion WHERE rendicionId = :rendicionId ORDER BY timestamp DESC")
    fun getAuditoriaByRendicionFlow(rendicionId: Long): Flow<List<AuditoriaEntity>>
}
