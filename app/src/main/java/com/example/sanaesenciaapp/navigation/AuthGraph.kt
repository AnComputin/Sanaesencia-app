package com.example.sanaesenciaapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.LoginScreen
import com.example.sanaesenciaapp.ui.views.RegistroScreen
import com.example.sanaesenciaapp.ui.views.auth.FichaBeneficiarioScreen
import com.example.sanaesenciaapp.viewmodel.AppViewModelProvider
import com.example.sanaesenciaapp.viewmodel.BeneficiarioViewModel

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    appState: AppState,
    appViewModelProvider: AppViewModelProvider
) {
    composable("login") {
        LoginScreen(
            onNavigateToHome = {
                val destino = when (appState.usuarioActual?.rol) {
                    "ADMIN" -> "home_admin"
                    "TERAPEUTA" -> "home_terapeuta"
                    else -> "home"
                }

                navController.navigate(destino) {
                    popUpTo("login") { inclusive = true }
                }
            },
            onNavigateToRegistro = {
                navController.navigate("registro")
            },
            appState = appState
        )
    }

    composable("registro") {
        RegistroScreen(
            onRegistroExitoso = {
                navController.navigate("login") {
                    popUpTo("registro") { inclusive = true }
                    launchSingleTop = true
                }
            },
            appState = appState
        )
    }

    composable("ficha_beneficiario") { entry ->
        val beneficiarioViewModel: BeneficiarioViewModel =
            viewModel(viewModelStoreOwner = entry, factory = appViewModelProvider)
        FichaBeneficiarioScreen(
            beneficiarioViewModel = beneficiarioViewModel,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}
