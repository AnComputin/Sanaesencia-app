package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.data.repository.BeneficiarioRepository
import com.example.sanaesenciaapp.data.repository.SesionRepository
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
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
    sesionRepository: SesionRepository,
    beneficiarioRepository: BeneficiarioRepository,
    citaViewModel: CitaViewModel,
    agendarViewModel: AgendarViewModel,
    pagoViewModel: PagoViewModel,
    misCitasViewModel: MisCitasViewModel
) {
    composable("agendar") {
        val sesion = sesionRepository.sesionActual.value
        var beneficiarioSeleccionado by remember { mutableStateOf<BeneficiarioEntity?>(null) }

        AgendarScreen(
            agendarViewModel = agendarViewModel,
            onContinuar = {
                navController.navigate("pago")
            },
            onVolver = {
                navController.popBackStack()
            },
            nombreTitular = sesion?.nombre.orEmpty(),
            beneficiarios = emptyList(), // Se cargará desde BeneficiarioRepository
            beneficiarioSeleccionadoId = beneficiarioSeleccionado?.id,
            onPacienteTitularSeleccionado = {
                beneficiarioSeleccionado = null
            },
            onBeneficiarioSeleccionado = { beneficiario ->
                beneficiarioSeleccionado = beneficiario
            },
            onRegistrarBeneficiario = {
                navController.navigate("ficha_beneficiario")
            }
        )
    }

    composable("pago") {
        val sesion = sesionRepository.sesionActual.value
        PagoScreen(
            pagoViewModel = pagoViewModel,
            usuarioEmail = sesion?.email ?: "",
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
        val sesion = sesionRepository.sesionActual.value
        MisCitasScreen(
            misCitasViewModel = misCitasViewModel,
            usuarioEmail = sesion?.email ?: "",
            onVolver = {
                navController.popBackStack()
            }
        )
    }
}
