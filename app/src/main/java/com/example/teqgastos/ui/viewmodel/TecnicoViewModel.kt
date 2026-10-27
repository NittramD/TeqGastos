package com.example.teqgastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teqgastos.data.local.entity.CentroCostoEntity
import com.example.teqgastos.data.local.entity.GastoEntity
import com.example.teqgastos.data.local.entity.RendicionConGastos
import com.example.teqgastos.data.local.entity.RendicionEntity
import com.example.teqgastos.data.repository.TeqGastosRepository
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.EstadoRendicion
import com.example.teqgastos.model.FormaPago
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TrasladoTeqRuta(
    val id: String,
    val tramo: String,
    val kilometros: Double,
    val peajesClp: Double,
    val combustibleClp: Double,
    val fecha: String
)

class TecnicoViewModel(private val repository: TeqGastosRepository) : ViewModel() {

    val rendiciones: StateFlow<List<RendicionEntity>> = repository.getAllRendiciones().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val centrosCosto: StateFlow<List<CentroCostoEntity>> = repository.getCentrosCosto().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentRendicionConGastos = MutableStateFlow<RendicionConGastos?>(null)
    val currentRendicionConGastos: StateFlow<RendicionConGastos?> = _currentRendicionConGastos.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun loadRendicionDetalle(rendicionId: Long) {
        viewModelScope.launch {
            repository.getRendicionConGastosFlow(rendicionId).collect {
                _currentRendicionConGastos.value = it
            }
        }
    }

    fun createRendicion(
        titulo: String,
        fechaInicio: String,
        fechaFin: String,
        tecnicoNombre: String,
        tecnicoRut: String,
        centroCostoId: Long,
        centroCostoNombre: String,
        fondoInicial: Double,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.createRendicion(
                RendicionEntity(
                    titulo = titulo,
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin,
                    tecnicoNombre = tecnicoNombre,
                    tecnicoRut = tecnicoRut,
                    centroCostoId = centroCostoId,
                    centroCostoNombre = centroCostoNombre,
                    fondoInicial = fondoInicial,
                    estado = EstadoRendicion.BORRADOR
                )
            )
            onSuccess(id)
        }
    }

    fun enviarAJefatura(rendicionId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val rendicion = repository.getRendicionConGastos(rendicionId)
            if (rendicion == null) {
                _uiMessage.value = "Error al cargar la rendición."
                return@launch
            }
            if (rendicion.gastos.isEmpty()) {
                _uiMessage.value = "No puedes enviar una rendición sin gastos registrados."
                return@launch
            }
            if (rendicion.gastos.any { it.estadoGasto == EstadoGasto.OBSERVADO }) {
                _uiMessage.value = "Hay gastos observados. Debes corregirlos o eliminarlos antes de re-enviar."
                return@launch
            }

            repository.updateEstadoRendicion(rendicionId, EstadoRendicion.ENVIADA)
            // Registro inalterable de auditoría
            repository.registrarAuditoria(
                rendicionId = rendicionId,
                usuarioNombre = rendicion.rendicion.tecnicoNombre,
                usuarioRut = rendicion.rendicion.tecnicoRut,
                accion = "ENVIO_JEFATURA",
                detalle = "Envío formal a supervisión con ${rendicion.gastos.size} gastos por ${rendicion.totalRendidoClp}."
            )

            _uiMessage.value = "Rendición enviada exitosamente a Jefatura para aprobación."
            onSuccess()
        }
    }

    fun sincronizarManual() {
        viewModelScope.launch {
            repository.sincronizarGastosPendientes()
            repository.refrescarIndicadores()
            _uiMessage.value = "Sincronización completada con éxito."
        }
    }

    fun eliminarGasto(gasto: GastoEntity) {
        viewModelScope.launch {
            repository.deleteGasto(gasto)
            _uiMessage.value = "Gasto eliminado correctamente."
        }
    }

    fun importarTrasladosTeqRuta(rendicionId: Long, trasladosSeleccionados: List<TrasladoTeqRuta>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val gastosNuevos = mutableListOf<GastoEntity>()
            trasladosSeleccionados.forEach { traslado ->
                if (traslado.peajesClp > 0) {
                    gastosNuevos.add(
                        GastoEntity(
                            rendicionId = rendicionId,
                            categoriaId = 2,
                            categoriaNombre = "Peajes",
                            montoOriginal = traslado.peajesClp,
                            montoClp = traslado.peajesClp,
                            formaPago = FormaPago.REEMBOLSO_PROPIO,
                            rutProveedor = "96.812.300-8",
                            nombreProveedor = "Autopistas Chile (TeqRuta TAG)",
                            nroComprobante = "TR-TAG-${traslado.id}",
                            fechaGasto = traslado.fecha,
                            origenTeqRuta = true,
                            isSynced = false,
                            estadoGasto = EstadoGasto.REGISTRADO
                        )
                    )
                }
                if (traslado.combustibleClp > 0) {
                    gastosNuevos.add(
                        GastoEntity(
                            rendicionId = rendicionId,
                            categoriaId = 1,
                            categoriaNombre = "Combustible",
                            montoOriginal = traslado.combustibleClp,
                            montoClp = traslado.combustibleClp,
                            formaPago = FormaPago.TARJETA_CORPORATIVA,
                            rutProveedor = "99.148.000-K",
                            nombreProveedor = "Cálculo Ruta TeqRuta (${traslado.kilometros} km)",
                            nroComprobante = "TR-COMB-${traslado.id}",
                            fechaGasto = traslado.fecha,
                            origenTeqRuta = true,
                            isSynced = false,
                            estadoGasto = EstadoGasto.REGISTRADO
                        )
                    )
                }
            }
            repository.insertGastos(gastosNuevos)
            repository.registrarAuditoria(
                rendicionId = rendicionId,
                usuarioNombre = "Sistema TeqRuta",
                usuarioRut = "99.999.999-9",
                accion = "IMPORTACION_TEQRUTA",
                detalle = "Importados ${gastosNuevos.size} gastos calculados de traslados y peajes."
            )
            _uiMessage.value = "Se importaron ${gastosNuevos.size} gastos desde TeqRuta."
            onSuccess()
        }
    }

    class Factory(private val repository: TeqGastosRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TecnicoViewModel(repository) as T
        }
    }
}
