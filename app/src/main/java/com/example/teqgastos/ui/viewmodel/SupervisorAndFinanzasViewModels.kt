package com.example.teqgastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teqgastos.data.local.entity.AuditoriaEntity
import com.example.teqgastos.data.local.entity.RendicionConGastos
import com.example.teqgastos.data.repository.TeqGastosRepository
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.EstadoRendicion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SupervisorViewModel(private val repository: TeqGastosRepository) : ViewModel() {

    val rendicionesPendientes: StateFlow<List<RendicionConGastos>> = repository.getRendicionesByEstados(
        listOf(EstadoRendicion.ENVIADA, EstadoRendicion.OBSERVADA)
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentRendicion = MutableStateFlow<RendicionConGastos?>(null)
    val currentRendicion: StateFlow<RendicionConGastos?> = _currentRendicion.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun loadRendicion(id: Long) {
        viewModelScope.launch {
            repository.getRendicionConGastosFlow(id).collect {
                _currentRendicion.value = it
            }
        }
    }

    fun getAuditoriaFlow(rendicionId: Long): Flow<List<AuditoriaEntity>> =
        repository.getAuditoriaFlow(rendicionId)

    fun aprobarGasto(gastoId: Long) {
        viewModelScope.launch {
            repository.updateEstadoGasto(gastoId, EstadoGasto.APROBADO, null)
            _uiMessage.value = "Gasto marcado como aprobado."
        }
    }

    fun observarGasto(gastoId: Long, rendicionId: Long, comentario: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateEstadoGasto(gastoId, EstadoGasto.OBSERVADO, comentario)
            repository.updateEstadoRendicion(rendicionId, EstadoRendicion.OBSERVADA)
            
            // Registro de auditoría
            repository.registrarAuditoria(
                rendicionId = rendicionId,
                usuarioNombre = "Dra. Marcela Fuentes",
                usuarioRut = "12.839.210-4",
                accion = "OBSERVACION",
                detalle = "Gasto ID #$gastoId devuelto con observación: $comentario"
            )

            _uiMessage.value = "Gasto observado. Se ha devuelto al técnico para corrección."
            onComplete()
        }
    }

    fun rechazarGasto(gastoId: Long, motivo: String) {
        viewModelScope.launch {
            repository.updateEstadoGasto(gastoId, EstadoGasto.RECHAZADO, motivo)
            _uiMessage.value = "Gasto rechazado."
        }
    }

    fun aprobarTodaLaRendicion(rendicionId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            val rendicion = repository.getRendicionConGastos(rendicionId) ?: return@launch
            rendicion.gastos.forEach { gasto ->
                if (gasto.estadoGasto == EstadoGasto.REGISTRADO) {
                    repository.updateEstadoGasto(gasto.id, EstadoGasto.APROBADO)
                }
            }
            repository.updateEstadoRendicion(rendicionId, EstadoRendicion.APROBADA_JEFATURA)
            
            // Registro de auditoría
            repository.registrarAuditoria(
                rendicionId = rendicionId,
                usuarioNombre = "Dra. Marcela Fuentes",
                usuarioRut = "12.839.210-4",
                accion = "APROBACION_SUPERVISOR",
                detalle = "Aprobación de la rendición completa por ${rendicion.totalRendidoClp}."
            )

            _uiMessage.value = "¡Rendición aprobada exitosamente y remitida a Finanzas!"
            onComplete()
        }
    }

    class Factory(private val repository: TeqGastosRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SupervisorViewModel(repository) as T
        }
    }
}

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
                usuarioNombre = "Roberto Pinochet",
                usuarioRut = "14.209.811-1",
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
