package com.example.teqgastos.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.EstadoRendicion
import com.example.teqgastos.model.FormaPago
import com.example.teqgastos.model.TipoMoneda

@Entity(tableName = "categorias_gasto")
data class CategoriaGastoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val topeMaximoClp: Double,
    val requiereComprobante: Boolean = true,
    val icono: String
)

@Entity(tableName = "centros_costo")
data class CentroCostoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val ciudad: String
)

@Entity(
    tableName = "rendiciones",
    indices = [Index(value = ["tecnicoRut"]), Index(value = ["estado"])]
)
data class RendicionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val fechaInicio: String,
    val fechaFin: String,
    val tecnicoNombre: String,
    val tecnicoRut: String,
    val centroCostoId: Long,
    val centroCostoNombre: String,
    val fondoInicial: Double = 0.0,
    val estado: EstadoRendicion = EstadoRendicion.BORRADOR,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "gastos",
    foreignKeys = [
        ForeignKey(
            entity = RendicionEntity::class,
            parentColumns = ["id"],
            childColumns = ["rendicionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["rendicionId"]),
        Index(value = ["rutProveedor", "nroComprobante"]) // Para control de duplicidad
    ]
)
data class GastoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rendicionId: Long,
    val categoriaId: Long,
    val categoriaNombre: String,
    val moneda: TipoMoneda = TipoMoneda.CLP,
    val montoOriginal: Double,
    val montoClp: Double,
    val tipoCambio: Double = 1.0,
    val formaPago: FormaPago,
    val rutProveedor: String,
    val nombreProveedor: String,
    val nroComprobante: String,
    val fechaGasto: String,
    val fotoComprobanteUri: String? = null,
    val justificacionTope: String? = null,
    val estadoGasto: EstadoGasto = EstadoGasto.REGISTRADO,
    val observacionSupervisor: String? = null,
    val origenTeqRuta: Boolean = false,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// Bitácora de Auditoría requerida por la normativa vigente (Sección 5.1 del caso)
@Entity(
    tableName = "auditoria_rendicion",
    foreignKeys = [
        ForeignKey(
            entity = RendicionEntity::class,
            parentColumns = ["id"],
            childColumns = ["rendicionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["rendicionId"])]
)
data class AuditoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rendicionId: Long,
    val usuarioNombre: String,
    val usuarioRut: String,
    val accion: String, // CREACION, ENVIO, APROBACION_JEFE, OBSERVACION, PAGO
    val detalle: String,
    val fechaHora: String,
    val timestamp: Long = System.currentTimeMillis()
)

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
