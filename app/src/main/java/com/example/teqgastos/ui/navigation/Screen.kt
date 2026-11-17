package com.example.teqgastos.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    
    // Rutas Técnico
    object TecnicoHome : Screen("tecnico_home")
    object NuevaRendicion : Screen("nueva_rendicion")
    object DetalleRendicion : Screen("detalle_rendicion/{rendicionId}") {
        fun createRoute(rendicionId: Long) = "detalle_rendicion/$rendicionId"
    }
    object RegistroGasto : Screen("registro_gasto/{rendicionId}?gastoId={gastoId}") {
        fun createRoute(rendicionId: Long, gastoId: Long = 0L) = "registro_gasto/$rendicionId?gastoId=$gastoId"
    }
    object ImportarTeqRuta : Screen("importar_teqruta/{rendicionId}") {
        fun createRoute(rendicionId: Long) = "importar_teqruta/$rendicionId"
    }
    object MapaRuta : Screen("mapa_ruta/{rendicionId}?tramoId={tramoId}") {
        fun createRoute(rendicionId: Long, tramoId: String? = null): String {
            return if (tramoId != null) {
                "mapa_ruta/$rendicionId?tramoId=$tramoId"
            } else {
                "mapa_ruta/$rendicionId"
            }
        }
    }

    // Rutas Supervisor
    object SupervisorBandeja : Screen("supervisor_bandeja")
    object RevisionRendicion : Screen("revision_rendicion/{rendicionId}") {
        fun createRoute(rendicionId: Long) = "revision_rendicion/$rendicionId"
    }

    // Rutas Finanzas
    object FinanzasBandeja : Screen("finanzas_bandeja")
    object ExportarErp : Screen("exportar_erp")

    // Rutas Comunes
    object VisorComprobante : Screen("visor_comprobante/{gastoId}") {
        fun createRoute(gastoId: Long) = "visor_comprobante/$gastoId"
    }
    object Configuraciones : Screen("configuraciones")
}
