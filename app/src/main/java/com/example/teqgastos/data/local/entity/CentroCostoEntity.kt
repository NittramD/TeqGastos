package com.example.teqgastos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "centros_costo")
data class CentroCostoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val ciudad: String
)
