package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.SeleccionEspecialidadScreen
import com.example.sanaesenciaapp.ui.views.UbicacionScreen
import com.example.sanaesenciaapp.ui.views.catalogo.DetalleServicioScreen
import com.example.sanaesenciaapp.ui.views.catalogo.SeleccionEspecialistaScreen
import com.example.sanaesenciaapp.viewmodel.AppViewModelProvider
import com.example.sanaesenciaapp.viewmodel.CatalogoViewModel
import com.example.sanaesenciaapp.viewmodel.CitaViewModel

fun NavGraphBuilder.catalogoGraph(
    navController: NavHostController,
    appState: AppState,
    citaViewModel: CitaViewModel,
    appViewModelProvider: AppViewModelProvider
) {
    composable("ubicacion") {
        UbicacionScreen(onBackClick = { navController.popBackStack() })
    }

    composable(
        route = "detalle_servicio/{especialidadId}",
        arguments = listOf(navArgument("especialidadId") { type = NavType.IntType })
    ) { entry ->
        val catalogoViewModel: CatalogoViewModel =
            viewModel(viewModelStoreOwner = entry, factory = appViewModelProvider)
        val especialidades by catalogoViewModel.especialidades.collectAsStateWithLifecycle()
        val state by catalogoViewModel.uiState.collectAsStateWithLifecycle()
        val id = entry.arguments?.getInt("especialidadId")
        val especialidad = especialidades.firstOrNull { it.id == id }
        if (especialidad != null) {
            DetalleServicioScreen(
                especialidad = especialidad,
                onBackClick = { navController.popBackStack() },
                onReservarClick = {
                    if (appState.usuarioActual == null) {
                        navController.navigate("login")
                    } else if (!especialidad.agendaPausada) {
                        appState.especialidadSeleccionada = especialidad
                        appState.especialistaSeleccionado = null
                        appState.seleccionarBeneficiario(null)
                        navController.navigate("seleccionar_especialista/${especialidad.id}")
                    }
                }
            )
        } else if (state.errorEspecialidades) {
            androidx.compose.material3.Text(androidx.compose.ui.res.stringResource(com.example.sanaesenciaapp.R.string.catalogo_detalle_error_carga))
        } else {
            androidx.compose.material3.CircularProgressIndicator()
        }
    }

    composable("seleccionar_especialidad") { entry ->
        val catalogoViewModel: CatalogoViewModel =
            viewModel(viewModelStoreOwner = entry, factory = appViewModelProvider)
        val especialidades by catalogoViewModel.especialidades.collectAsStateWithLifecycle()
        val state by catalogoViewModel.uiState.collectAsStateWithLifecycle()
        SeleccionEspecialidadScreen(
            especialidades = especialidades,
            cargando = state.cargandoEspecialidades,
            error = state.errorEspecialidades,
            onEspecialidadClick = { id ->
                val selected = especialidades.firstOrNull { it.id == id } ?: return@SeleccionEspecialidadScreen
                appState.especialidadSeleccionada = selected
                appState.especialistaSeleccionado = null
                navController.navigate(
                    if (selected.agendaPausada) "detalle_servicio/$id"
                    else "seleccionar_especialista/$id"
                )
            }
        )
    }

    composable(
        route = "seleccionar_especialista/{especialidadId}",
        arguments = listOf(navArgument("especialidadId") { type = NavType.IntType })
    ) { entry ->
        val catalogoViewModel: CatalogoViewModel =
            viewModel(viewModelStoreOwner = entry, factory = appViewModelProvider)
        SeleccionEspecialistaScreen(
            catalogoViewModel = catalogoViewModel,
            citaViewModel = citaViewModel,
            onNavigateBack = { navController.popBackStack() },
            onEspecialistaSelected = { navController.navigate("agendar") }
        )
    }
}
