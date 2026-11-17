package com.example.teqgastos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.teqgastos.model.EstadoRendicion

@Entity(
    tableName = "rendiciones",
    indices = [Index(value = ["tecnicoRut"]), Index(value = ["estado"])]
)
data class RendicionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val fechaInicio: String,
    val fechaFin: String,
    val tecnicoNombre: String,
    val tecnicoRut: String,
    val centroCostoId: Long,
    val centroCostoNombre: String,
    val fondoInicial: Double = 0.0,
    val estado: EstadoRendicion = EstadoRendicion.BORRADOR,
    val createdAt: Long = System.currentTimeMillis()
)
