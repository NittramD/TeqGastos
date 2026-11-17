package com.example.teqgastos.model

enum class EstadoRendicion(val label: String) {
    BORRADOR("Borrador"),
    ENVIADA("Enviada a Jefatura"),
    OBSERVADA("Con Observaciones"),
    APROBADA_JEFATURA("Aprobada por Jefatura"),
    APROBADA_FINANZAS("Aprobada por Finanzas"),
    REEMBOLSADA("Reembolsada / Pagada"),
    RECHAZADA("Rechazada")
}
