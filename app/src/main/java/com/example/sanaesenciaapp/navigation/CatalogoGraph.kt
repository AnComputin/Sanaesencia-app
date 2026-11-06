package com.example.sanaesenciaapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.SeleccionEspecialidadScreen
import com.example.sanaesenciaapp.ui.views.UbicacionScreen
import com.example.sanaesenciaapp.ui.views.catalogo.DetalleServicioScreen
import com.example.sanaesenciaapp.ui.views.catalogo.SeleccionEspecialistaScreen
import com.example.sanaesenciaapp.viewmodel.CitaViewModel

fun NavGraphBuilder.catalogoGraph(
    navController: NavHostController,
    appState: AppState,
    citaViewModel: CitaViewModel
) {
    composable("ubicacion") {
        UbicacionScreen(
            onBackClick = { navController.popBackStack() }
        )
    }

    composable("detalle_servicio") {
        val esp = appState.especialidadSeleccionada
        if (esp != null) {
            val espActual = appState.especialidades.find { it.id == esp.id } ?: esp

            DetalleServicioScreen(
                especialidad = espActual,
                onBackClick = { navController.popBackStack() },
                onReservarClick = {
                    if (appState.usuarioActual == null) {
                        navController.navigate("login")
                    } else if (!espActual.agendaPausada) {
                        appState.especialidadSeleccionada = espActual
                        appState.especialistaSeleccionado = null
                        appState.seleccionarBeneficiario(null)
                        navController.navigate("seleccionar_especialista")
                    }
                }
            )
        }
    }

    composable("seleccionar_especialidad") {
        SeleccionEspecialidadScreen(
            navController = navController,
            appState = appState
        )
    }

    composable("seleccionar_especialista") {
        SeleccionEspecialistaScreen(
            navController = navController,
            appState = appState,
            citaViewModel = citaViewModel
        )
    }
}
