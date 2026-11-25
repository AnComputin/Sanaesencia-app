package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.agenda.AgendarScreen
import com.example.sanaesenciaapp.ui.views.agenda.ConfirmacionScreen
import com.example.sanaesenciaapp.ui.views.agenda.PagoScreen
import com.example.sanaesenciaapp.ui.views.citas.MisCitasScreen
import com.example.sanaesenciaapp.viewmodel.CitaViewModel

fun NavGraphBuilder.agendaGraph(
    navController: NavHostController,
    appState: AppState,
    citaViewModel: CitaViewModel
) {
    composable("agendar") {
        LaunchedEffect(Unit) {
            if (appState.usuarioActual == null) {
                navController.navigate("login") {
                    popUpTo("agendar") { inclusive = true }
                }
            }
        }

        AgendarScreen(
            citaViewModel = citaViewModel,
            onContinuar = {
                navController.navigate("pago")
            },
            onVolver = {
                navController.popBackStack()
            },
            nombreTitular = appState.usuarioActual?.nombre.orEmpty(),
            beneficiarios = appState.beneficiarios,
            beneficiarioSeleccionadoId = appState.beneficiarioSeleccionado?.id,
            onPacienteTitularSeleccionado = {
                appState.seleccionarBeneficiario(null)
            },
            onBeneficiarioSeleccionado = { beneficiario ->
                appState.seleccionarBeneficiario(beneficiario)
            },
            onRegistrarBeneficiario = {
                navController.navigate("ficha_beneficiario")
            }
        )
    }

    composable("pago") {
        LaunchedEffect(Unit) {
            if (appState.usuarioActual == null) {
                navController.navigate("login") {
                    popUpTo("pago") { inclusive = true }
                }
            }
        }

        PagoScreen(
            citaViewModel = citaViewModel,
            usuarioEmail = appState.usuarioActual?.email ?: "",
            onPagoExitoso = {
                navController.navigate("confirmacion") {
                    popUpTo("pago") { inclusive = true }
                }
            },
            onExpirado = {
                navController.popBackStack()
            }
        )
    }

    composable("confirmacion") {
        ConfirmacionScreen(
            citaViewModel = citaViewModel,
            onVerMisCitas = {
                navController.navigate("mis_citas") {
                    popUpTo("confirmacion") { inclusive = true }
                }
            }
        )
    }

    composable("mis_citas") {
        LaunchedEffect(Unit) {
            if (appState.usuarioActual == null) {
                navController.navigate("login") {
                    popUpTo("mis_citas") { inclusive = true }
                }
            }
        }

        MisCitasScreen(
            citaViewModel = citaViewModel,
            usuarioEmail = appState.usuarioActual?.email ?: "",
            onVolver = {
                navController.popBackStack()
            }
        )
    }
}
