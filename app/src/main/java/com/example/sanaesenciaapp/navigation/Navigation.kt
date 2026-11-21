package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.sanaesenciaapp.viewmodel.AgendarViewModel
import com.example.sanaesenciaapp.viewmodel.PagoViewModel
import com.example.sanaesenciaapp.viewmodel.MisCitasViewModel
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModelModule
import com.example.sanaesenciaapp.viewmodel.TiendaViewModel
import com.example.sanaesenciaapp.viewmodel.TiendaViewModelFactory

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
                AgendaViewModelModule()
            )
        )
    }

    val terapeutaViewModel: TerapeutaViewModel = viewModel(factory = appViewModelProvider)
    val citaViewModel: CitaViewModel = viewModel(factory = appViewModelProvider)
    val agendarViewModel: AgendarViewModel = viewModel(factory = appViewModelProvider)
    val pagoViewModel: PagoViewModel = viewModel(factory = appViewModelProvider)
    val misCitasViewModel: MisCitasViewModel = viewModel(factory = appViewModelProvider)
    val tiendaViewModel: TiendaViewModel = viewModel(factory = TiendaViewModelFactory(appContainer.database))

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("inicio") {
            InicioScreen(navController)
        }

        composable("home") {
            HomeScreen(
                navController = navController,
                appState = appState,
                onLogout = {
                    tiendaViewModel.limpiarCarrito()
                    appState.logout()
                }
            )
        }

        composable("home_admin") {
            HomeAdminScreen(
                navController = navController,
                appState = appState,
                tiendaViewModel = tiendaViewModel
            )
        }

        authGraph(navController, appState)
        catalogoGraph(navController, appState, citaViewModel)
        agendaGraph(navController, appContainer.sesionRepository, appContainer.beneficiarioRepository, citaViewModel, agendarViewModel, pagoViewModel, misCitasViewModel)
        terapeutaGraph(navController, appState, terapeutaViewModel)
        juegosGraph(navController)
        tiendaGraph(navController, appState, tiendaViewModel)
    }
}
