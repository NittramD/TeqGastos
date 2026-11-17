package com.example.teqgastos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias_gasto")
data class CategoriaGastoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val topeMaximoClp: Double,
    val requiereComprobante: Boolean = true,
    val icono: String
)
