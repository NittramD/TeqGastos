package com.example.teqgastos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.FormaPago
import com.example.teqgastos.model.TipoMoneda

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
