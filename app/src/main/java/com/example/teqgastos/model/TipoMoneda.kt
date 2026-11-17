package com.example.teqgastos.model

enum class TipoMoneda(val symbol: String, val tasaConversionClp: Double) {
    CLP("$", 1.0),
    UF("UF", 38250.0), // Indicador económico referencial actualizado
    USD("USD$", 945.0)  // Indicador económico referencial actualizado
}
