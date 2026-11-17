package com.example.teqgastos.model

enum class UserRole(val displayName: String, val description: String) {
    TECNICO("Técnico de Terreno", "Crea rendiciones, registra gastos y captura boletas en terreno."),
    SUPERVISOR("Jefatura de Servicio Técnico", "Revisa, aprueba, observa o rechaza gastos de su equipo."),
    FINANZAS("Finanzas y Contabilidad", "Revisión final, registro de pagos y exportación contable a ERP.")
}
