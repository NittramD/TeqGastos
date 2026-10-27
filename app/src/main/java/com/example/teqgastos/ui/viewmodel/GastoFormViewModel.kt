package com.example.teqgastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teqgastos.data.local.entity.CategoriaGastoEntity
import com.example.teqgastos.data.local.entity.GastoEntity
import com.example.teqgastos.data.remote.IndicadoresValores
import com.example.teqgastos.data.repository.TeqGastosRepository
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.FormaPago
import com.example.teqgastos.model.TipoMoneda
import com.example.teqgastos.ui.components.RutValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class GastoFormState(
    val gastoId: Long = 0,
    val rendicionId: Long = 0,
    val categoria: CategoriaGastoEntity? = null,
    val montoInput: String = "",
    val tipoMoneda: TipoMoneda = TipoMoneda.CLP,
    val tasaConversion: Double = 1.0,
    val rutProveedor: String = "",
    val nombreProveedor: String = "",
    val nroComprobante: String = "",
    val fechaGasto: String = LocalDate.now().toString(),
    val formaPago: FormaPago = FormaPago.REEMBOLSO_PROPIO,
    val fotoComprobanteUri: String? = null,
    val justificacionTope: String = "",
    
    // Errores y validaciones reactivas
    val rutError: String? = null,
    val montoError: String? = null,
    val fechaError: String? = null,
    val duplicadoError: String? = null,
    val justificacionError: String? = null,
    val comprobanteError: String? = null,
    
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val montoNumero: Double
        get() = montoInput.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0

    val montoClp: Double
        get() = montoNumero * tasaConversion

    val isTopeExcedido: Boolean
        get() = categoria != null && montoClp > categoria.topeMaximoClp

    val isFormValid: Boolean
        get() = categoria != null &&
                montoNumero > 0 &&
                montoError == null &&
                rutError == null &&
                fechaError == null &&
                duplicadoError == null &&
                nombreProveedor.isNotBlank() &&
                nroComprobante.isNotBlank() &&
                (!isTopeExcedido || justificacionTope.trim().length >= 10) &&
                (!categoria.requiereComprobante || fotoComprobanteUri != null)
}

class GastoFormViewModel(
    private val repository: TeqGastosRepository,
    private val rendicionId: Long,
    private val gastoId: Long
) : ViewModel() {

    val categorias: StateFlow<List<CategoriaGastoEntity>> = repository.getCategorias().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _formState = MutableStateFlow(GastoFormState(rendicionId = rendicionId, gastoId = gastoId))
    val formState: StateFlow<GastoFormState> = _formState.asStateFlow()

    private val _indicadores = MutableStateFlow(repository.indicadoresActuales)
    val indicadores: StateFlow<IndicadoresValores> = _indicadores.asStateFlow()

    init {
        // Cargar indicadores de la API pública en vivo
        viewModelScope.launch {
            val ind = repository.refrescarIndicadores()
            _indicadores.value = ind
            actualizarTasaConversion(_formState.value.tipoMoneda, ind)
        }

        if (gastoId > 0) {
            loadGasto(gastoId)
        }
    }

    private fun loadGasto(id: Long) {
        viewModelScope.launch {
            val gasto = repository.getGastoById(id) ?: return@launch
            _formState.value = _formState.value.copy(
                gastoId = gasto.id,
                rendicionId = gasto.rendicionId,
                montoInput = gasto.montoOriginal.toInt().toString(),
                tipoMoneda = gasto.moneda,
                tasaConversion = gasto.tipoCambio,
                rutProveedor = gasto.rutProveedor,
                nombreProveedor = gasto.nombreProveedor,
                nroComprobante = gasto.nroComprobante,
                fechaGasto = gasto.fechaGasto,
                formaPago = gasto.formaPago,
                fotoComprobanteUri = gasto.fotoComprobanteUri,
                justificacionTope = gasto.justificacionTope ?: ""
            )
            validarFecha(gasto.fechaGasto)
        }
    }

    fun onCategoriaChanged(categoria: CategoriaGastoEntity) {
        _formState.value = _formState.value.copy(categoria = categoria)
        validateTope()
    }

    fun onMontoChanged(nuevoMonto: String) {
        val clean = nuevoMonto.filter { it.isDigit() }
        _formState.value = _formState.value.copy(
            montoInput = clean,
            montoError = if (clean.isEmpty() || clean.toLongOrNull() == 0L) "El monto debe ser mayor a 0" else null
        )
        validateTope()
    }

    fun onMonedaChanged(moneda: TipoMoneda) {
        actualizarTasaConversion(moneda, _indicadores.value)
    }

    private fun actualizarTasaConversion(moneda: TipoMoneda, ind: IndicadoresValores) {
        val tasa = when (moneda) {
            TipoMoneda.CLP -> 1.0
            TipoMoneda.UF -> ind.valorUf
            TipoMoneda.USD -> ind.valorDolar
        }
        _formState.value = _formState.value.copy(tipoMoneda = moneda, tasaConversion = tasa)
        validateTope()
    }

    fun onRutChanged(nuevoRut: String) {
        val formatted = RutValidator.formatRut(nuevoRut)
        val error = if (formatted.length >= 8 && !RutValidator.isValid(formatted)) {
            "RUT inválido (revise dígito verificador)"
        } else {
            null
        }
        _formState.value = _formState.value.copy(rutProveedor = formatted, rutError = error)
        checkDuplicadoAsync()
    }

    fun onNombreProveedorChanged(nombre: String) {
        _formState.value = _formState.value.copy(nombreProveedor = nombre)
    }

    fun onNroComprobanteChanged(nro: String) {
        _formState.value = _formState.value.copy(nroComprobante = nro)
        checkDuplicadoAsync()
    }

    fun onFechaChanged(fecha: String) {
        _formState.value = _formState.value.copy(fechaGasto = fecha)
        validarFecha(fecha)
    }

    // Regla de Negocio: Control de Gastos Fuera de Plazo (Sección 2.3 del caso)
    private fun validarFecha(fechaStr: String) {
        try {
            val fecha = LocalDate.parse(fechaStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
            val hoy = LocalDate.now()
            val diasDiferencia = ChronoUnit.DAYS.between(fecha, hoy)

            val error = when {
                fecha.isAfter(hoy) -> "La fecha no puede ser futura."
                diasDiferencia > 30 -> "Gasto fuera de plazo legal: tiene más de 30 días de antigüedad."
                else -> null
            }
            _formState.value = _formState.value.copy(fechaError = error)
        } catch (e: Exception) {
            _formState.value = _formState.value.copy(fechaError = "Formato de fecha inválido (AAAA-MM-DD).")
        }
    }

    fun onFormaPagoChanged(forma: FormaPago) {
        _formState.value = _formState.value.copy(formaPago = forma)
    }

    fun onFotoCaptured(uriString: String) {
        _formState.value = _formState.value.copy(fotoComprobanteUri = uriString, comprobanteError = null)
    }

    fun onJustificacionChanged(justificacion: String) {
        _formState.value = _formState.value.copy(
            justificacionTope = justificacion,
            justificacionError = if (_formState.value.isTopeExcedido && justificacion.trim().length < 10) {
                "Debe indicar una justificación de al menos 10 caracteres"
            } else {
                null
            }
        )
    }

    private fun validateTope() {
        val state = _formState.value
        val cat = state.categoria ?: return
        if (state.montoClp > cat.topeMaximoClp && state.justificacionTope.trim().length < 10) {
            _formState.value = state.copy(
                justificacionError = "El gasto excede el tope de ${cat.nombre}. Requiere justificación."
            )
        } else {
            _formState.value = state.copy(justificacionError = null)
        }
    }

    private fun checkDuplicadoAsync() {
        val state = _formState.value
        if (state.rutProveedor.isNotBlank() && state.nroComprobante.isNotBlank()) {
            viewModelScope.launch {
                val isDuplicado = repository.checkDuplicado(
                    rut = state.rutProveedor,
                    folio = state.nroComprobante,
                    currentId = state.gastoId
                )
                _formState.value = _formState.value.copy(
                    duplicadoError = if (isDuplicado) "Alerta: Este comprobante ya fue rendido anteriormente en TEQMED." else null
                )
            }
        }
    }

    fun saveGasto(onSuccess: () -> Unit) {
        val state = _formState.value
        if (!state.isFormValid) {
            _formState.value = state.copy(errorMessage = "Por favor verifique los campos requeridos y advertencias.")
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isSaving = true)
            try {
                val categoria = state.categoria ?: return@launch
                val gasto = GastoEntity(
                    id = state.gastoId,
                    rendicionId = rendicionId,
                    categoriaId = categoria.id,
                    categoriaNombre = categoria.nombre,
                    moneda = state.tipoMoneda,
                    montoOriginal = state.montoNumero,
                    montoClp = state.montoClp,
                    tipoCambio = state.tasaConversion,
                    formaPago = state.formaPago,
                    rutProveedor = state.rutProveedor.trim(),
                    nombreProveedor = state.nombreProveedor.trim(),
                    nroComprobante = state.nroComprobante.trim(),
                    fechaGasto = state.fechaGasto,
                    fotoComprobanteUri = state.fotoComprobanteUri,
                    justificacionTope = if (state.isTopeExcedido) state.justificacionTope.trim() else null,
                    isSynced = false, // Pendiente de sincronización automática
                    estadoGasto = EstadoGasto.REGISTRADO
                )
                repository.saveGasto(gasto)
                _formState.value = state.copy(isSaving = false, saveSuccess = true)
                onSuccess()
            } catch (e: Exception) {
                _formState.value = state.copy(isSaving = false, errorMessage = "Error al guardar gasto: ${e.localizedMessage}")
            }
        }
    }

    class Factory(
        private val repository: TeqGastosRepository,
        private val rendicionId: Long,
        private val gastoId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GastoFormViewModel(repository, rendicionId, gastoId) as T
        }
    }
}
