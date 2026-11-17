package com.example.teqgastos.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.teqgastos.TeqGastosApp
import com.example.teqgastos.model.UserRole
import com.example.teqgastos.ui.screens.auth.LoginScreen
import com.example.teqgastos.ui.screens.common.ConfiguracionesScreen
import com.example.teqgastos.ui.screens.common.SplashScreen
import com.example.teqgastos.ui.screens.common.VisorComprobanteScreen
import com.example.teqgastos.ui.screens.finanzas.ExportarErpScreen
import com.example.teqgastos.ui.screens.finanzas.FinanzasBandejaScreen
import com.example.teqgastos.ui.screens.supervisor.RevisionRendicionScreen
import com.example.teqgastos.ui.screens.supervisor.SupervisorBandejaScreen
import com.example.teqgastos.ui.screens.tecnico.DetalleRendicionScreen
import com.example.teqgastos.ui.screens.tecnico.ImportarTeqRutaScreen
import com.example.teqgastos.ui.screens.tecnico.MapaRutaScreen
import com.example.teqgastos.ui.screens.tecnico.NuevaRendicionScreen
import com.example.teqgastos.ui.screens.tecnico.RegistroGastoScreen
import com.example.teqgastos.ui.screens.tecnico.TecnicoHomeScreen
import com.example.teqgastos.ui.viewmodel.AuthViewModel
import com.example.teqgastos.ui.viewmodel.FinanzasViewModel
import com.example.teqgastos.ui.viewmodel.GastoFormViewModel
import com.example.teqgastos.ui.viewmodel.SupervisorViewModel
import com.example.teqgastos.ui.viewmodel.TecnicoViewModel

@Composable
fun AppNavHost(
    app: TeqGastosApp,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(app.userPreferences, app.tokenManager)
    )
    val tecnicoViewModel: TecnicoViewModel = viewModel(factory = TecnicoViewModel.Factory(app.repository))
    val supervisorViewModel: SupervisorViewModel = viewModel(factory = SupervisorViewModel.Factory(app.repository))
    val finanzasViewModel: FinanzasViewModel = viewModel(factory = FinanzasViewModel.Factory(app.repository))

    val handleLogout: () -> Unit = {
        authViewModel.logout {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val handleSwitchAccount: (UserRole) -> Unit = { targetRole ->
        authViewModel.switchAccountAndLogin(targetRole) { rol ->
            when (rol) {
                UserRole.TECNICO -> navController.navigate(Screen.TecnicoHome.route) {
                    popUpTo(0) { inclusive = true }
                }
                UserRole.SUPERVISOR -> navController.navigate(Screen.SupervisorBandeja.route) {
                    popUpTo(0) { inclusive = true }
                }
                UserRole.FINANZAS -> navController.navigate(Screen.FinanzasBandeja.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(280)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(280)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(280)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(280)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(280)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(280)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(280)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(280)
            )
        }
    ) {
        // Pantalla 00: Splash Screen (Visualización inicial breve del Logotipo)
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla 01: Login & Selección de Rol
        composable(Screen.Login.route) {
            LaunchedEffect(Unit) {
                authViewModel.resetAuthState()
            }
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { rol ->
                    when (rol) {
                        UserRole.TECNICO -> navController.navigate(Screen.TecnicoHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        UserRole.SUPERVISOR -> navController.navigate(Screen.SupervisorBandeja.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        UserRole.FINANZAS -> navController.navigate(Screen.FinanzasBandeja.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Pantalla 02: Home Técnico (Rendiciones con NetworkMonitor)
        composable(Screen.TecnicoHome.route) {
            TecnicoHomeScreen(
                viewModel = tecnicoViewModel,
                networkMonitor = app.networkMonitor,
                onNavigateToNuevaRendicion = { navController.navigate(Screen.NuevaRendicion.route) },
                onNavigateToDetalle = { rendicionId -> navController.navigate(Screen.DetalleRendicion.createRoute(rendicionId)) },
                onNavigateToConfiguraciones = { navController.navigate(Screen.Configuraciones.route) },
                onLogout = handleLogout,
                onSwitchAccount = handleSwitchAccount
            )
        }

        // Pantalla 03: Nueva Rendición
        composable(Screen.NuevaRendicion.route) {
            NuevaRendicionScreen(
                viewModel = tecnicoViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRendicionCreated = { rendicionId ->
                    navController.navigate(Screen.DetalleRendicion.createRoute(rendicionId)) {
                        popUpTo(Screen.NuevaRendicion.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla 04: Detalle de Rendición
        composable(
            route = Screen.DetalleRendicion.route,
            arguments = listOf(navArgument("rendicionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val rendicionId = backStackEntry.arguments?.getLong("rendicionId") ?: 0L
            DetalleRendicionScreen(
                rendicionId = rendicionId,
                viewModel = tecnicoViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNuevoGasto = { id -> navController.navigate(Screen.RegistroGasto.createRoute(id, 0L)) },
                onNavigateToEditarGasto = { rId, gId -> navController.navigate(Screen.RegistroGasto.createRoute(rId, gId)) },
                onNavigateToVisorFoto = { gastoId -> navController.navigate(Screen.VisorComprobante.createRoute(gastoId)) },
                onNavigateToImportarTeqRuta = { rId -> navController.navigate(Screen.ImportarTeqRuta.createRoute(rId)) },
                onNavigateToMapa = { rId -> navController.navigate(Screen.MapaRuta.createRoute(rId)) }
            )
        }

        // Pantalla 05: Formulario de Registro / Edición de Gasto
        composable(
            route = Screen.RegistroGasto.route,
            arguments = listOf(
                navArgument("rendicionId") { type = NavType.LongType },
                navArgument("gastoId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val rendicionId = backStackEntry.arguments?.getLong("rendicionId") ?: 0L
            val gastoId = backStackEntry.arguments?.getLong("gastoId") ?: 0L
            val formViewModel: GastoFormViewModel = viewModel(
                factory = GastoFormViewModel.Factory(app.repository, rendicionId, gastoId)
            )
            RegistroGastoScreen(
                viewModel = formViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla 06: Visor de Comprobante / Auditoría
        composable(
            route = Screen.VisorComprobante.route,
            arguments = listOf(navArgument("gastoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gastoId = backStackEntry.arguments?.getLong("gastoId") ?: 0L
            VisorComprobanteScreen(
                gastoId = gastoId,
                repository = app.repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla 07: Importar desde TeqRuta
        composable(
            route = Screen.ImportarTeqRuta.route,
            arguments = listOf(navArgument("rendicionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val rendicionId = backStackEntry.arguments?.getLong("rendicionId") ?: 0L
            ImportarTeqRutaScreen(
                rendicionId = rendicionId,
                viewModel = tecnicoViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMapa = { rId, tramoId ->
                    navController.navigate(Screen.MapaRuta.createRoute(rId, tramoId))
                }
            )
        }

        // Pantalla 07B: Visualizador de Mapa de Ruta TeqRuta (Macrozona Sur)
        composable(
            route = Screen.MapaRuta.route,
            arguments = listOf(
                navArgument("rendicionId") { type = NavType.LongType },
                navArgument("tramoId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val rendicionId = backStackEntry.arguments?.getLong("rendicionId") ?: 0L
            val tramoId = backStackEntry.arguments?.getString("tramoId")
            MapaRutaScreen(
                rendicionId = rendicionId,
                initialTramoId = tramoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla 08: Bandeja de Aprobaciones del Supervisor
        composable(Screen.SupervisorBandeja.route) {
            SupervisorBandejaScreen(
                viewModel = supervisorViewModel,
                onNavigateToRevision = { rendicionId -> navController.navigate(Screen.RevisionRendicion.createRoute(rendicionId)) },
                onNavigateToConfiguraciones = { navController.navigate(Screen.Configuraciones.route) },
                onLogout = handleLogout,
                onSwitchAccount = handleSwitchAccount
            )
        }

        // Pantalla 09: Revisión Detallada de Rendición por Supervisor
        composable(
            route = Screen.RevisionRendicion.route,
            arguments = listOf(navArgument("rendicionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val rendicionId = backStackEntry.arguments?.getLong("rendicionId") ?: 0L
            RevisionRendicionScreen(
                rendicionId = rendicionId,
                viewModel = supervisorViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVisorFoto = { gastoId -> navController.navigate(Screen.VisorComprobante.createRoute(gastoId)) }
            )
        }

        // Pantalla 10: Bandeja de Finanzas
        composable(Screen.FinanzasBandeja.route) {
            FinanzasBandejaScreen(
                viewModel = finanzasViewModel,
                onNavigateToExportarErp = { navController.navigate(Screen.ExportarErp.route) },
                onNavigateToConfiguraciones = { navController.navigate(Screen.Configuraciones.route) },
                onLogout = handleLogout,
                onSwitchAccount = handleSwitchAccount
            )
        }

        // Pantalla 11: Exportación a ERP Contable
        composable(Screen.ExportarErp.route) {
            ExportarErpScreen(
                viewModel = finanzasViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pantalla 12: Configuraciones (Tema, Modo Offline Simulado, Info)
        composable(Screen.Configuraciones.route) {
            ConfiguracionesScreen(
                app = app,
                onNavigateBack = { navController.popBackStack() },
                onLogout = handleLogout,
                onSwitchAccount = handleSwitchAccount
            )
        }
    }
}
