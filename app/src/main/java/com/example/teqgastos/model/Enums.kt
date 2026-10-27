package com.example.teqgastos.model

enum class UserRole(val displayName: String, val description: String) {
    TECNICO("Técnico de Terreno", "Crea rendiciones, registra gastos y captura boletas en terreno."),
    SUPERVISOR("Jefatura de Servicio Técnico", "Revisa, aprueba, observa o rechaza gastos de su equipo."),
    FINANZAS("Finanzas y Contabilidad", "Revisión final, registro de pagos y exportación contable a ERP.")
}

enum class EstadoRendicion(val label: String) {
    BORRADOR("Borrador"),
    ENVIADA("Enviada a Jefatura"),
    OBSERVADA("Con Observaciones"),
    APROBADA_JEFATURA("Aprobada por Jefatura"),
    APROBADA_FINANZAS("Aprobada por Finanzas"),
    REEMBOLSADA("Reembolsada / Pagada"),
    RECHAZADA("Rechazada")
}

enum class EstadoGasto(val label: String) {
    REGISTRADO("Registrado"),
    APROBADO("Aprobado"),
    OBSERVADO("Observado"),
    RECHAZADO("Rechazado")
}

enum class FormaPago(val label: String, val shortLabel: String) {
    REEMBOLSO_PROPIO("Dinero Propio (Solicita Reembolso)", "Reembolso Propio"),
    FONDO_POR_RENDIR("Fondo por Rendir (Anticipo)", "Fondo por Rendir"),
    TARJETA_CORPORATIVA("Tarjeta Corporativa TEQMED", "Tarjeta Corporativa")
}

enum class TipoMoneda(val symbol: String, val tasaConversionClp: Double) {
    CLP("$", 1.0),
    UF("UF", 38250.0), // Indicador económico referencial actualizado
    USD("USD$", 945.0)  // Indicador económico referencial actualizado
}
