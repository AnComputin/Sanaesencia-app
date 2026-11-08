package com.example.sanaesenciaapp.navigation

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

    // Ruta con parámetro citaId (para futura integración)
    composable("confirmacion/{citaId}") {
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
        MisCitasScreen(
            citaViewModel = citaViewModel,
            usuarioEmail = appState.usuarioActual?.email ?: "",
            onVolver = {
                navController.popBackStack()
            }
        )
    }
}
