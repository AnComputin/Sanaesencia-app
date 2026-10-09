package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.ui.views.InicioScreen
import com.example.sanaesenciaapp.ui.views.LoginScreen
import com.example.sanaesenciaapp.ui.views.RegistroScreen

@Composable
fun AppNavigation(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "inicio"
    ) {

        composable("inicio") {
            InicioScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("registro") {
            RegistroScreen(navController)
        }
    }
}