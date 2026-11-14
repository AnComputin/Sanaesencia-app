package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.MyApplication
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.di.AppContainer
import com.example.sanaesenciaapp.ui.views.HomeScreen
import com.example.sanaesenciaapp.ui.views.InicioScreen
import com.example.sanaesenciaapp.ui.views.SplashScreen
import com.example.sanaesenciaapp.ui.views.admin.HomeAdminScreen
import com.example.sanaesenciaapp.viewmodel.AgendaViewModelModule
import com.example.sanaesenciaapp.viewmodel.AppViewModelProvider
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.viewmodel.CatalogoViewModel
import com.example.sanaesenciaapp.viewmodel.CatalogoViewModelModule
import com.example.sanaesenciaapp.viewmodel.BeneficiarioViewModelModule
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModelModule

@Composable
fun AppNavigation(
    navController: NavHostController,
    appState: AppState,
    appContainer: AppContainer = (LocalContext.current.applicationContext as MyApplication).appContainer
) {
    val appViewModelProvider = remember(appContainer) {
        AppViewModelProvider(
            appContainer = appContainer,
            modules = listOf(
                TerapeutaViewModelModule(),
                AgendaViewModelModule(),
                CatalogoViewModelModule(),
                BeneficiarioViewModelModule()
            )
        )
    }

    val terapeutaViewModel: TerapeutaViewModel = viewModel(factory = appViewModelProvider)
    val citaViewModel: CitaViewModel = viewModel(factory = appViewModelProvider)

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onContinueClick = {
                    navController.navigate("inicio") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("inicio") {
            InicioScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("registro") }
            )
        }

        composable("home") { entry ->
            val catalogoViewModel: CatalogoViewModel =
                viewModel(viewModelStoreOwner = entry, factory = appViewModelProvider)
            val especialidades = catalogoViewModel.especialidades.collectAsStateWithLifecycle().value
            val catalogoState = catalogoViewModel.uiState.collectAsStateWithLifecycle().value
            HomeScreen(
                appState = appState,
                especialidades = especialidades,
                cargandoCatalogo = catalogoState.cargandoEspecialidades,
                errorCatalogo = catalogoState.errorEspecialidades,
                onLoginClick = { navController.navigate("login") },
                onReserveClick = { navController.navigate("seleccionar_especialidad") },
                onMyAppointmentsClick = { navController.navigate("mis_citas") },
                onSpecialtyClick = { id -> navController.navigate("detalle_servicio/$id") },
                onLocationClick = { navController.navigate("ubicacion") },
                onGamesClick = { navController.navigate("juegos_hub") },
                onLogoutClick = {
                    appState.logout()
                    navController.navigate("inicio") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("home_admin") {
            HomeAdminScreen(
                navController = navController,
                appState = appState
            )
        }

        authGraph(navController, appState, appViewModelProvider)
        catalogoGraph(navController, appState, citaViewModel, appViewModelProvider)
        agendaGraph(navController, appState, citaViewModel)
        terapeutaGraph(navController, appState, terapeutaViewModel)
        juegosGraph(navController)
    }
}
