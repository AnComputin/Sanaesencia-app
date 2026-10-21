package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.HomeScreen
import com.example.sanaesenciaapp.ui.views.InicioScreen
import com.example.sanaesenciaapp.ui.views.LoginScreen
import com.example.sanaesenciaapp.ui.views.RegistroScreen
import com.example.sanaesenciaapp.ui.views.SeleccionEspecialidadScreen
import com.example.sanaesenciaapp.ui.views.SplashScreen
import com.example.sanaesenciaapp.ui.views.UbicacionScreen
import com.example.sanaesenciaapp.ui.views.admin.HomeAdminScreen
import com.example.sanaesenciaapp.ui.views.agenda.AgendarScreen
import com.example.sanaesenciaapp.ui.views.agenda.ConfirmacionScreen
import com.example.sanaesenciaapp.ui.views.agenda.PagoScreen
import com.example.sanaesenciaapp.ui.views.auth.FichaBeneficiarioScreen
import com.example.sanaesenciaapp.ui.views.catalogo.DetalleServicioScreen
import com.example.sanaesenciaapp.ui.views.catalogo.SeleccionEspecialistaScreen
import com.example.sanaesenciaapp.ui.views.citas.MisCitasScreen
import com.example.sanaesenciaapp.ui.views.terapeuta.AgendaDiaScreen
import com.example.sanaesenciaapp.ui.views.terapeuta.DetalleContinuidadScreen
import com.example.sanaesenciaapp.ui.views.terapeuta.HomeTerapeutaScreen
import com.example.sanaesenciaapp.ui.views.terapeuta.PacientesAsignadosScreen
import com.example.sanaesenciaapp.ui.views.juegos.JuegosHubScreen
import com.example.sanaesenciaapp.ui.views.juegos.MemoriceScreen
import com.example.sanaesenciaapp.ui.views.juegos.GatoScreen
import com.example.sanaesenciaapp.ui.views.juegos.CaminoBienestarScreen
import com.example.sanaesenciaapp.ui.views.juegos.RespiracionGuiadaScreen
import com.example.sanaesenciaapp.ui.views.juegos.PausaConscienteScreen
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.viewmodel.CitaViewModelFactory
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    appState: AppState
) {
    val terapeutaViewModel: TerapeutaViewModel = viewModel()
    val citaViewModel: CitaViewModel = viewModel(
        factory = CitaViewModelFactory(appState.db.citaDao())
    )

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("inicio") {
            InicioScreen(navController)
        }

        composable("home") {
            HomeScreen(
                navController = navController,
                appState = appState
            )
        }

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

        composable("home_admin") {
            HomeAdminScreen(
                navController = navController,
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

        composable("ficha_beneficiario") {
            FichaBeneficiarioScreen(
                appState = appState,
                onNavigateBack = {
                    navController.popBackStack()
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
            MisCitasScreen(
                citaViewModel = citaViewModel,
                usuarioEmail = appState.usuarioActual?.email ?: "",
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

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

        composable("juegos_hub") {
            JuegosHubScreen(navController = navController)
        }

        composable("memorice_zen") {
            MemoriceScreen(navController = navController)
        }

        composable("gato_armonico") {
            GatoScreen(navController = navController)
        }

        composable("camino_bienestar") {
            CaminoBienestarScreen(navController = navController)
        }

        composable("respiracion_guiada") {
            RespiracionGuiadaScreen(navController = navController)
        }

        composable("pausa_consciente") {
            PausaConscienteScreen(navController = navController)
        }
    }
}
