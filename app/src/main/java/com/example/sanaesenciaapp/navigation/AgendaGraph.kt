package com.example.sanaesenciaapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.agenda.AgendarScreen
import com.example.sanaesenciaapp.ui.views.agenda.ConfirmacionScreen
import com.example.sanaesenciaapp.ui.views.agenda.PagoScreen
import com.example.sanaesenciaapp.ui.views.citas.MisCitasScreen
import com.example.sanaesenciaapp.viewmodel.AgendarViewModel
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.viewmodel.MisCitasViewModel
import com.example.sanaesenciaapp.viewmodel.PagoViewModel

fun NavGraphBuilder.agendaGraph(
    navController: NavHostController,
    appState: AppState,
    citaViewModel: CitaViewModel,
    agendarViewModel: AgendarViewModel,
    pagoViewModel: PagoViewModel,
    misCitasViewModel: MisCitasViewModel
) {
    composable("agendar") {
        AgendarScreen(
            agendarViewModel = agendarViewModel,
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
            pagoViewModel = pagoViewModel,
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
            misCitasViewModel = misCitasViewModel,
            usuarioEmail = appState.usuarioActual?.email ?: "",
            onVolver = {
                navController.popBackStack()
            }
        )
    }
}
