package com.example.teqgastos.model

/**
 * Estados del patrón Outbox para la sincronización robusta offline-first en TEQMED SpA.
 */
enum class SyncStatus(val label: String) {
    /** El registro coincide perfectamente con el backend central ERP */
    SYNCED("Sincronizado"),

    /** El registro fue creado localmente y está pendiente de ser insertado en el ERP */
    PENDING_INSERT("Pendiente creación"),

    /** El registro fue editado localmente y debe actualizarse en el backend */
    PENDING_UPDATE("Pendiente actualización"),

    /** El registro fue marcado para eliminación y debe ser borrado en el ERP */
    PENDING_DELETE("Pendiente eliminación"),

    /** Falló la transmisión al backend tras varios reintentos */
    FAILED("Error de sincronización")
}
