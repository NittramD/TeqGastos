package com.example.teqgastos.model

data class TrasladoTeqRuta(
    val id: String,
    val tramo: String,
    val kilometros: Double,
    val peajesClp: Double,
    val combustibleClp: Double,
    val fecha: String
)
