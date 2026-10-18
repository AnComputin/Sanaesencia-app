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

            SplashScreen(
                navController
            )
        }

        composable("home") {

            HomeScreen(
                navController,
                appState
            )
        }

        composable("login") {

            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegistro = {
                    navController.navigate("registro")
                }
            )
        }

        composable("registro") {

            RegistroScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("ubicacion") {
            UbicacionScreen()
        }
        composable("detalle_servicio") {
            val esp = appState.especialidadSeleccionada
            if (esp != null) {
                DetalleServicioScreen(
                    especialidad = esp,
                    onReservarClick = { navController.navigate("home") }
                )
            }
        }
    }
}