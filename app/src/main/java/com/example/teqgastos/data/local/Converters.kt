package com.example.teqgastos.data.local

import androidx.room.TypeConverter
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.EstadoRendicion
import com.example.teqgastos.model.FormaPago
import com.example.teqgastos.model.TipoMoneda

class Converters {
    @TypeConverter
    fun fromEstadoRendicion(value: EstadoRendicion): String = value.name

    @TypeConverter
    fun toEstadoRendicion(value: String): EstadoRendicion = try {
        EstadoRendicion.valueOf(value)
    } catch (e: Exception) {
        EstadoRendicion.BORRADOR
    }

    @TypeConverter
    fun fromEstadoGasto(value: EstadoGasto): String = value.name

    @TypeConverter
    fun toEstadoGasto(value: String): EstadoGasto = try {
        EstadoGasto.valueOf(value)
    } catch (e: Exception) {
        EstadoGasto.REGISTRADO
    }

    @TypeConverter
    fun fromFormaPago(value: FormaPago): String = value.name

    @TypeConverter
    fun toFormaPago(value: String): FormaPago = try {
        FormaPago.valueOf(value)
    } catch (e: Exception) {
        FormaPago.REEMBOLSO_PROPIO
    }

    @TypeConverter
    fun fromTipoMoneda(value: TipoMoneda): String = value.name

    @TypeConverter
    fun toTipoMoneda(value: String): TipoMoneda = try {
        TipoMoneda.valueOf(value)
    } catch (e: Exception) {
        TipoMoneda.CLP
    }
}
