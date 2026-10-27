package com.example.teqgastos.data.repository

import android.content.Context
import com.example.teqgastos.data.local.AppDatabase
import com.example.teqgastos.data.local.entity.AuditoriaEntity
import com.example.teqgastos.data.local.entity.CategoriaGastoEntity
import com.example.teqgastos.data.local.entity.CentroCostoEntity
import com.example.teqgastos.data.local.entity.GastoEntity
import com.example.teqgastos.data.local.entity.RendicionConGastos
import com.example.teqgastos.data.local.entity.RendicionEntity
import com.example.teqgastos.data.remote.IndicadoresService
import com.example.teqgastos.data.remote.IndicadoresValores
import com.example.teqgastos.model.EstadoGasto
import com.example.teqgastos.model.EstadoRendicion
import com.example.teqgastos.model.FormaPago
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TeqGastosRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val rendicionDao = db.rendicionDao()
    private val gastoDao = db.gastoDao()
    private val catalogDao = db.catalogDao()
    private val auditoriaDao = db.auditoriaDao()

    @Volatile
    var indicadoresActuales: IndicadoresValores = IndicadoresValores()

    suspend fun refrescarIndicadores(): IndicadoresValores = withContext(Dispatchers.IO) {
        val nuevos = IndicadoresService.obtenerIndicadores()
        indicadoresActuales = nuevos
        nuevos
    }

    // Semilla inicial de datos para TEQMED SpA
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        refrescarIndicadores()

        if (catalogDao.countCategorias() == 0) {
            val categorias = listOf(
                CategoriaGastoEntity(nombre = "Combustible", topeMaximoClp = 60000.0, icono = "local_gas_station"),
                CategoriaGastoEntity(nombre = "Peajes", topeMaximoClp = 25000.0, icono = "toll"),
                CategoriaGastoEntity(nombre = "Estacionamiento", topeMaximoClp = 15000.0, icono = "local_parking"),
                CategoriaGastoEntity(nombre = "Alimentación", topeMaximoClp = 22000.0, icono = "restaurant"),
                CategoriaGastoEntity(nombre = "Alojamiento", topeMaximoClp = 75000.0, icono = "hotel"),
                CategoriaGastoEntity(nombre = "Insumos Urgentes", topeMaximoClp = 150000.0, icono = "medical_services")
            )
            catalogDao.insertCategorias(categorias)

            val centros = listOf(
                CentroCostoEntity(codigo = "CC-HRC-01", nombre = "Hospital Regional Guillermo Grant Benavente", ciudad = "Concepción"),
                CentroCostoEntity(codigo = "CC-HLH-02", nombre = "Hospital Las Higueras", ciudad = "Talcahuano"),
                CentroCostoEntity(codigo = "CC-CAS-03", nombre = "Clínica Andes Salud Concepción", ciudad = "Concepción"),
                CentroCostoEntity(codigo = "CC-HCS-04", nombre = "Hospital Clínico del Sur", ciudad = "Concepción"),
                CentroCostoEntity(codigo = "CC-HLA-05", nombre = "Hospital Víctor Ríos Ruiz", ciudad = "Los Ángeles"),
                CentroCostoEntity(codigo = "CC-HRT-06", nombre = "Hospital Dr. Hernán Henríquez Aravena", ciudad = "Temuco")
            )
            catalogDao.insertCentrosCosto(centros)

            // Rendición 1: Borrador
            val idR1 = rendicionDao.insertRendicion(
                RendicionEntity(
                    titulo = "Mantención Pabellón Quirúrgico Los Ángeles",
                    fechaInicio = "2026-10-18",
                    fechaFin = "2026-10-20",
                    tecnicoNombre = "Gerardo Galán",
                    tecnicoRut = "17.482.913-K",
                    centroCostoId = 5,
                    centroCostoNombre = "Hospital Víctor Ríos Ruiz",
                    fondoInicial = 80000.0,
                    estado = EstadoRendicion.BORRADOR
                )
            )
            registrarAuditoriaInterna(idR1, "Gerardo Galán", "17.482.913-K", "CREACION", "Creación de rendición en borrador.")

            gastoDao.insertGasto(
                GastoEntity(
                    rendicionId = idR1,
                    categoriaId = 2,
                    categoriaNombre = "Peajes",
                    montoOriginal = 4900.0,
                    montoClp = 4900.0,
                    formaPago = FormaPago.REEMBOLSO_PROPIO,
                    rutProveedor = "96.812.300-8",
                    nombreProveedor = "Autopista del Itata",
                    nroComprobante = "BOL-104921",
                    fechaGasto = "2026-10-18",
                    isSynced = true,
                    estadoGasto = EstadoGasto.REGISTRADO
                )
            )

            // Rendición 2: Enviada a Jefatura
            val idR2 = rendicionDao.insertRendicion(
                RendicionEntity(
                    titulo = "Calibración Ecógrafos Hospital Las Higueras",
                    fechaInicio = "2026-10-14",
                    fechaFin = "2026-10-15",
                    tecnicoNombre = "Carlos Mendoza",
                    tecnicoRut = "16.291.844-5",
                    centroCostoId = 2,
                    centroCostoNombre = "Hospital Las Higueras",
                    fondoInicial = 50000.0,
                    estado = EstadoRendicion.ENVIADA
                )
            )
            registrarAuditoriaInterna(idR2, "Carlos Mendoza", "16.291.844-5", "CREACION", "Creación de rendición.")
            registrarAuditoriaInterna(idR2, "Carlos Mendoza", "16.291.844-5", "ENVIO_JEFATURA", "Rendición enviada a jefatura para revisión.")

            gastoDao.insertGasto(
                GastoEntity(
                    rendicionId = idR2,
                    categoriaId = 1,
                    categoriaNombre = "Combustible",
                    montoOriginal = 45000.0,
                    montoClp = 45000.0,
                    formaPago = FormaPago.TARJETA_CORPORATIVA,
                    rutProveedor = "99.148.000-K",
                    nombreProveedor = "Copec S.A.",
                    nroComprobante = "FAC-901844",
                    fechaGasto = "2026-10-14",
                    isSynced = true,
                    estadoGasto = EstadoGasto.REGISTRADO
                )
            )
        }
    }

    // Auditoría
    private suspend fun registrarAuditoriaInterna(
        rendicionId: Long,
        usuarioNombre: String,
        usuarioRut: String,
        accion: String,
        detalle: String
    ) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fechaHora = LocalDateTime.now().format(formatter)
        auditoriaDao.insertAuditoria(
            AuditoriaEntity(
                rendicionId = rendicionId,
                usuarioNombre = usuarioNombre,
                usuarioRut = usuarioRut,
                accion = accion,
                detalle = detalle,
                fechaHora = fechaHora
            )
        )
    }

    suspend fun registrarAuditoria(
        rendicionId: Long,
        usuarioNombre: String,
        usuarioRut: String,
        accion: String,
        detalle: String
    ) = withContext(Dispatchers.IO) {
        registrarAuditoriaInterna(rendicionId, usuarioNombre, usuarioRut, accion, detalle)
    }

    fun getAuditoriaFlow(rendicionId: Long): Flow<List<AuditoriaEntity>> =
        auditoriaDao.getAuditoriaByRendicionFlow(rendicionId)

    // Catálogos
    fun getCategorias(): Flow<List<CategoriaGastoEntity>> = catalogDao.getCategoriasFlow()
    fun getCentrosCosto(): Flow<List<CentroCostoEntity>> = catalogDao.getCentrosCostoFlow()

    // Rendiciones
    fun getAllRendiciones(): Flow<List<RendicionEntity>> = rendicionDao.getAllRendicionesFlow()
    fun getRendicionesByTecnico(rut: String): Flow<List<RendicionEntity>> = rendicionDao.getRendicionesByTecnico(rut)
    fun getRendicionConGastosFlow(id: Long): Flow<RendicionConGastos?> = rendicionDao.getRendicionConGastosFlow(id)
    suspend fun getRendicionConGastos(id: Long): RendicionConGastos? = rendicionDao.getRendicionConGastos(id)
    fun getRendicionesByEstados(estados: List<EstadoRendicion>): Flow<List<RendicionConGastos>> =
        rendicionDao.getRendicionesByEstadosFlow(estados)

    suspend fun createRendicion(rendicion: RendicionEntity): Long {
        val id = rendicionDao.insertRendicion(rendicion)
        registrarAuditoria(id, rendicion.tecnicoNombre, rendicion.tecnicoRut, "CREACION", "Creación inicial de la comisión.")
        return id
    }

    suspend fun updateRendicion(rendicion: RendicionEntity) = rendicionDao.updateRendicion(rendicion)
    suspend fun updateEstadoRendicion(id: Long, estado: EstadoRendicion) = rendicionDao.updateEstadoRendicion(id, estado)
    suspend fun deleteRendicion(rendicion: RendicionEntity) = rendicionDao.deleteRendicion(rendicion)

    // Gastos
    fun getGastosByRendicion(rendicionId: Long): Flow<List<GastoEntity>> = gastoDao.getGastosByRendicionFlow(rendicionId)
    suspend fun getGastoById(id: Long): GastoEntity? = gastoDao.getGastoById(id)
    fun getGastoByIdFlow(id: Long): Flow<GastoEntity?> = gastoDao.getGastoByIdFlow(id)
    suspend fun saveGasto(gasto: GastoEntity): Long {
        return if (gasto.id == 0L) {
            gastoDao.insertGasto(gasto)
        } else {
            gastoDao.updateGasto(gasto)
            gasto.id
        }
    }
    suspend fun insertGastos(gastos: List<GastoEntity>) = gastoDao.insertGastos(gastos)
    suspend fun deleteGasto(gasto: GastoEntity) = gastoDao.deleteGasto(gasto)
    suspend fun checkDuplicado(rut: String, folio: String, currentId: Long = 0): Boolean {
        if (rut.isBlank() || folio.isBlank()) return false
        return gastoDao.findDuplicado(rut.trim(), folio.trim(), currentId) != null
    }

    suspend fun updateEstadoGasto(id: Long, nuevoEstado: EstadoGasto, observacion: String? = null) {
        gastoDao.updateEstadoGasto(id, nuevoEstado, observacion)
    }

    suspend fun sincronizarGastosPendientes() = withContext(Dispatchers.IO) {
        gastoDao.marcarTodosSincronizados()
    }
}
