package com.example.sanaesenciaapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.HomeScreen
import com.example.sanaesenciaapp.ui.views.InicioScreen
import com.example.sanaesenciaapp.ui.views.LoginScreen
import com.example.sanaesenciaapp.ui.views.RegistroScreen
import com.example.sanaesenciaapp.ui.views.SplashScreen
import com.example.sanaesenciaapp.ui.views.UbicacionScreen
import com.example.sanaesenciaapp.ui.views.catalogo.DetalleServicioScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesenciaapp.ui.views.admin.HomeAdminScreen
import com.example.sanaesenciaapp.ui.views.terapeuta.AgendaDiaScreen
import com.example.sanaesenciaapp.ui.views.terapeuta.DetalleContinuidadScreen
import com.example.sanaesenciaapp.ui.views.terapeuta.HomeTerapeutaScreen
import com.example.sanaesenciaapp.ui.views.terapeuta.PacientesAsignadosScreen
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel
import com.example.sanaesenciaapp.ui.views.agenda.AgendarScreen
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.ui.views.SeleccionEspecialidadScreen
import com.example.sanaesenciaapp.ui.views.agenda.PagoScreen
import com.example.sanaesenciaapp.ui.views.agenda.ConfirmacionScreen
import com.example.sanaesenciaapp.ui.views.citas.MisCitasScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    appState: AppState
) {
    val terapeutaViewModel: TerapeutaViewModel = viewModel()
    val citaViewModel: CitaViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen(navController)
        }

        // 1. Ruta agregada para InicioScreen
        composable("inicio") {
            InicioScreen(navController)
        }

        composable("home") {
            HomeScreen(
                navController,
                appState
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
                        popUpTo("login") {
                            inclusive = true
                        }
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
                        popUpTo("registro") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                appState = appState
            )
        }

        // 2. Le pasamos navController a UbicacionScreen para permitir volver atrás
        composable("ubicacion") {
            UbicacionScreen(onBackClick = { navController.popBackStack() }
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

                        } else {

                            citaViewModel.seleccionarServicio(
                                especialidad = espActual.nombre,
                                profesional = espActual.profesional,
                                modalidadPermitida = espActual.modalidad
                            )

                            navController.navigate("agendar")
                        }
                    }
                )
            }
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
                    val paciente =
                        terapeutaViewModel
                            .uiState
                            .value
                            .pacientes
                            .find {
                                it.id == pacienteId
                            }
                    if (paciente != null) {
                        terapeutaViewModel
                            .seleccionarPaciente(
                                paciente
                            )
                        navController.navigate(
                            "detalle_continuidad"
                        )
                    }
                },

                terapeutaViewModel =
                    terapeutaViewModel
            )
        }

        composable("detalle_continuidad") {

            DetalleContinuidadScreen(

                onNavigateBack = {
                    navController.popBackStack()
                },

                terapeutaViewModel =
                    terapeutaViewModel
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
                }
            )
        }

        composable("seleccionar_especialidad") {

            SeleccionEspecialidadScreen(
                navController = navController,
                appState = appState
            )
        }

        composable("pago") {

            PagoScreen(
                citaViewModel = citaViewModel,

                onPagoExitoso = {
                    navController.navigate("confirmacion") {
                        popUpTo("pago") {
                            inclusive = true
                        }
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
                        popUpTo("confirmacion") {
                            inclusive = true
                        }
                    }
                },

            )
        }

        composable("mis_citas") {

            MisCitasScreen(
                citaViewModel = citaViewModel,

                onVolver = {
                    navController.popBackStack()
                }
            )
        }




    }
}