package com.example.teqgastos.model

enum class FormaPago(val label: String, val shortLabel: String) {
    REEMBOLSO_PROPIO("Dinero Propio (Solicita Reembolso)", "Reembolso Propio"),
    FONDO_POR_RENDIR("Fondo por Rendir (Anticipo)", "Fondo por Rendir"),
    TARJETA_CORPORATIVA("Tarjeta Corporativa TEQMED", "Tarjeta Corporativa")
}
