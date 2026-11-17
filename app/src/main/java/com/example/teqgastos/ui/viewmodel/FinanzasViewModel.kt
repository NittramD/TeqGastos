package com.example.teqgastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teqgastos.data.local.entity.AuditoriaEntity
import com.example.teqgastos.data.local.entity.RendicionConGastos
import com.example.teqgastos.data.repository.TeqGastosRepository
import com.example.teqgastos.model.EstadoRendicion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanzasViewModel(private val repository: TeqGastosRepository) : ViewModel() {

    val rendicionesFinanzas: StateFlow<List<RendicionConGastos>> = repository.getRendicionesByEstados(
        listOf(
            EstadoRendicion.APROBADA_JEFATURA,
            EstadoRendicion.APROBADA_FINANZAS,
            EstadoRendicion.REEMBOLSADA
        )
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun getAuditoriaFlow(rendicionId: Long): Flow<List<AuditoriaEntity>> =
        repository.getAuditoriaFlow(rendicionId)

    fun registrarPagoReembolso(rendicionId: Long) {
        viewModelScope.launch {
            repository.updateEstadoRendicion(rendicionId, EstadoRendicion.REEMBOLSADA)
            
            // Registro de auditoría
            repository.registrarAuditoria(
                rendicionId = rendicionId,
                usuarioNombre = "Roberto Cabrera",
                usuarioRut = "14.209.811-3",
                accion = "PAGO_REEMBOLSO",
                detalle = "Reembolso procesado y transferido al técnico. Rendición cerrada contablemente."
            )

            _uiMessage.value = "Reembolso procesado y rendición cerrada contablemente."
        }
    }

    fun generarReporteErpCsv(rendiciones: List<RendicionConGastos>): String {
        val sb = StringBuilder()
        sb.append("ID_RENDICION;TITULO;FECHA_INICIO;TECNICO_RUT;CENTRO_COSTO;ID_GASTO;CATEGORIA;PROVEEDOR_RUT;PROVEEDOR;NRO_DOC;MONEDA;MONTO_CLP;FORMA_PAGO;ESTADO\n")
        rendiciones.forEach { r ->
            r.gastos.forEach { g ->
                sb.append("${r.rendicion.id};${r.rendicion.titulo};${r.rendicion.fechaInicio};${r.rendicion.tecnicoRut};${r.rendicion.centroCostoNombre};")
                sb.append("${g.id};${g.categoriaNombre};${g.rutProveedor};${g.nombreProveedor};${g.nroComprobante};")
                sb.append("${g.moneda.name};${g.montoClp.toInt()};${g.formaPago.shortLabel};${g.estadoGasto.name}\n")
            }
        }
        return sb.toString()
    }

    class Factory(private val repository: TeqGastosRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FinanzasViewModel(repository) as T
        }
    }
}
