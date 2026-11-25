package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.terapeuta.AgendaDiaScreen
import com.example.sanaesenciaapp.ui.views.terapeuta.DetalleContinuidadScreen
import com.example.sanaesenciaapp.ui.views.terapeuta.HomeTerapeutaScreen
import com.example.sanaesenciaapp.ui.views.terapeuta.PacientesAsignadosScreen
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel

fun NavGraphBuilder.terapeutaGraph(
    navController: NavHostController,
    appState: AppState,
    terapeutaViewModel: TerapeutaViewModel,
    onLimpiarSesion: () -> Unit = {}
) {
    composable("home_terapeuta") {
        LaunchedEffect(appState.usuarioActual) {
            if (!appState.usuarioActual?.rol.equals("TERAPEUTA", ignoreCase = true)) {
                navController.navigate(if (appState.usuarioActual == null) "login" else "home") {
                    popUpTo("home_terapeuta") { inclusive = true }
                }
            }
        }
        HomeTerapeutaScreen(
            nombreUsuario = appState.usuarioActual?.nombre,
            onAgendaClick = { navController.navigate("agenda_terapeuta") },
            onPacientesClick = { navController.navigate("pacientes_terapeuta") },
            onLogoutClick = {
                onLimpiarSesion()
                appState.logout()
                navController.navigate("home") {
                    popUpTo("home_terapeuta") {
                        inclusive = true
                    }
                }
            }
        )
    }

    composable("agenda_terapeuta") {
        LaunchedEffect(appState.usuarioActual) {
            if (!appState.usuarioActual?.rol.equals("TERAPEUTA", ignoreCase = true)) {
                navController.navigate("login") {
                    popUpTo("agenda_terapeuta") { inclusive = true }
                }
            }
        }
        AgendaDiaScreen(
            terapeutaViewModel = terapeutaViewModel
        )
    }

    composable("pacientes_terapeuta") {
        LaunchedEffect(appState.usuarioActual) {
            if (!appState.usuarioActual?.rol.equals("TERAPEUTA", ignoreCase = true)) {
                navController.navigate("login") {
                    popUpTo("pacientes_terapeuta") { inclusive = true }
                }
            }
        }
        PacientesAsignadosScreen(
            onPacienteClick = { pacienteId ->
                val paciente = terapeutaViewModel.uiState.value.pacientes.find {
                    it.id == pacienteId
                }

                if (paciente != null) {
                    terapeutaViewModel.seleccionarPaciente(paciente)
                    navController.navigate("detalle_continuidad")
                }
            },
            terapeutaViewModel = terapeutaViewModel
        )
    }

    composable("detalle_continuidad") {
        LaunchedEffect(appState.usuarioActual) {
            if (!appState.usuarioActual?.rol.equals("TERAPEUTA", ignoreCase = true)) {
                navController.navigate("login") {
                    popUpTo("detalle_continuidad") { inclusive = true }
                }
            }
        }
        DetalleContinuidadScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            terapeutaViewModel = terapeutaViewModel
        )
    }
}
