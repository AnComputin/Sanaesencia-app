package com.example.sanaesenciaapp.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sanaesenciaapp.R
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.components.SanaBackground
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
        val id by catalogoViewModel.especialidadId.collectAsStateWithLifecycle()
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
        } else if (state.errorEspecialidades || !state.cargandoEspecialidades) {
            SanaBackground {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.catalogo_detalle_error_carga),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Button(
                                onClick = { catalogoViewModel.reintentarEspecialidades() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.catalogo_btn_reintentar))
                            }
                        }
                    }
                }
            }
        } else {
            SanaBackground {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
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
            },
            onReintentarClick = { catalogoViewModel.reintentarEspecialidades() }
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
