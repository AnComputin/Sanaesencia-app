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
                AgendaViewModelModule()
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
            SplashScreen(navController)
        }

        composable("inicio") {
            InicioScreen(navController)
        }

        composable("home") {
            HomeScreen(
                navController = navController,
                appState = appState
            )
        }

        composable("home_admin") {
            HomeAdminScreen(
                navController = navController,
                appState = appState
            )
        }

        authGraph(navController, appState)
        catalogoGraph(navController, appState, citaViewModel)
        agendaGraph(navController, appState, citaViewModel)
        terapeutaGraph(navController, appState, terapeutaViewModel)
        juegosGraph(navController)
    }
}
