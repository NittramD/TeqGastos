package com.example.teqgastos.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.FormaPago

data class RendicionConGastos(
    @Embedded val rendicion: RendicionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "rendicionId"
    )
    val gastos: List<GastoEntity>
) {
    val totalRendidoClp: Double
        get() = gastos.sumOf { it.montoClp }

    val totalReembolsoTecnicoClp: Double
        get() = gastos.filter { it.formaPago == FormaPago.REEMBOLSO_PROPIO }.sumOf { it.montoClp }

    val totalTarjetaCorporativaClp: Double
        get() = gastos.filter { it.formaPago == FormaPago.TARJETA_CORPORATIVA }.sumOf { it.montoClp }

    val totalFondoRendirClp: Double
        get() = gastos.filter { it.formaPago == FormaPago.FONDO_POR_RENDIR }.sumOf { it.montoClp }

    val saldoFondoPorRendir: Double
        get() = rendicion.fondoInicial - totalFondoRendirClp

    val tieneObservaciones: Boolean
        get() = gastos.any { it.estadoGasto == EstadoGasto.OBSERVADO }

    val cantidadGastosObservados: Int
        get() = gastos.count { it.estadoGasto == EstadoGasto.OBSERVADO }

    val gastosPendientesSincronizar: Int
        get() = gastos.count { !it.isSynced }
}
