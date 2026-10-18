package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.HomeScreen
import com.example.sanaesenciaapp.ui.views.LoginScreen
import com.example.sanaesenciaapp.ui.views.RegistroScreen
import com.example.sanaesenciaapp.ui.views.SplashScreen
import com.example.sanaesenciaapp.ui.views.UbicacionScreen
import com.example.sanaesenciaapp.ui.views.catalogo.DetalleServicioScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    appState: AppState
) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("home") {
            HomeScreen(navController, appState)
        }

        composable("login") {
            LoginScreen(navController, appState)
        }

        composable("registro") {
            RegistroScreen(navController, appState)
        }

        composable("ubicacion") {
            UbicacionScreen(onBackClick = { navController.popBackStack() })        }

        // Pantalla de detalle de servicio
        composable("detalle_servicio") {
            val esp = appState.especialidadSeleccionada
            if (esp != null) {
                DetalleServicioScreen(
                    especialidad = esp,
                    onBackClick = { navController.popBackStack() },
                    onReservarClick = { navController.navigate("home") }
                )
            }
        }
    }
}