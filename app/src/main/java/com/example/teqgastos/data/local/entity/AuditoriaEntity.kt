package com.example.teqgastos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Bitácora de Auditoría requerida por la normativa vigente (Sección 5.1 del caso)
@Entity(
    tableName = "auditoria_rendicion",
    foreignKeys = [
        ForeignKey(
            entity = RendicionEntity::class,
            parentColumns = ["id"],
            childColumns = ["rendicionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["rendicionId"])]
)
data class AuditoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rendicionId: Long,
    val usuarioNombre: String,
    val usuarioRut: String,
    val accion: String, // CREACION, ENVIO, APROBACION_JEFE, OBSERVACION, PAGO
    val detalle: String,
    val fechaHora: String,
    val timestamp: Long = System.currentTimeMillis()
)
