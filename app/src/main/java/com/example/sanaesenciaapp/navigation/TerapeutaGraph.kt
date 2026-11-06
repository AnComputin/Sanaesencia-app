package com.example.sanaesenciaapp.navigation

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
    terapeutaViewModel: TerapeutaViewModel
) {
    composable("home_terapeuta") {
        HomeTerapeutaScreen(
            navController = navController,
            appState = appState
        )
    }

    composable("agenda_terapeuta") {
        AgendaDiaScreen(
            terapeutaViewModel = terapeutaViewModel
        )
    }

    composable("pacientes_terapeuta") {
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
        DetalleContinuidadScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            terapeutaViewModel = terapeutaViewModel
        )
    }
}
