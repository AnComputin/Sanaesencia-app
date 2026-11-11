package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.HomeScreen
import com.example.sanaesenciaapp.ui.views.InicioScreen
import com.example.sanaesenciaapp.ui.views.SplashScreen
import com.example.sanaesenciaapp.ui.views.admin.HomeAdminScreen
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.viewmodel.CitaViewModelFactory
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    appState: AppState
) {
    val terapeutaViewModel: TerapeutaViewModel = viewModel()
    val citaViewModel: CitaViewModel = viewModel(
        factory = CitaViewModelFactory(appState.db.citaDao())
    )

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
