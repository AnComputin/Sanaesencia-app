package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.HomeScreen
import com.example.sanaesenciaapp.ui.views.InicioScreen
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

        // 1. Ruta agregada para InicioScreen
        composable("inicio") {
            InicioScreen(navController)
        }

        composable("home") {
            HomeScreen(
                navController,
                appState
            )
        }

        composable("login") {
            LoginScreen(
                navController,
                appState
            )
        }

        composable("registro") {
            RegistroScreen(
                navController,
                appState
            )
        }

        // 2. Le pasamos navController a UbicacionScreen para permitir volver atrás
        composable("ubicacion") {
            UbicacionScreen(onBackClick = { navController.popBackStack() }
            )
        }


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