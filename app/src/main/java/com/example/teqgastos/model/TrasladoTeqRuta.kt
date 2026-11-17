package com.example.teqgastos.model

data class CoordenadaGps(
    val lat: Double,
    val lng: Double,
    val etiqueta: String? = null
)

data class TrasladoTeqRuta(
    val id: String,
    val tramo: String,
    val kilometros: Double,
    val peajesClp: Double,
    val combustibleClp: Double,
    val fecha: String,
    val origenNombre: String = "Concepción",
    val origenLat: Double = -36.8270,
    val origenLng: Double = -73.0503,
    val destinoNombre: String = "Los Ángeles",
    val destinoLat: Double = -37.4697,
    val destinoLng: Double = -72.3537,
    val puntosRuta: List<CoordenadaGps> = emptyList(),
    val peajesDetalle: List<String> = emptyList(),
    val tiempoEstimado: String = "1h 45m"
)
