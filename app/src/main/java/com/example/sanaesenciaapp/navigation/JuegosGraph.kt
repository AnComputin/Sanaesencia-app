package com.example.sanaesenciaapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.sanaesenciaapp.ui.views.juegos.CaminoBienestarScreen
import com.example.sanaesenciaapp.ui.views.juegos.GatoScreen
import com.example.sanaesenciaapp.ui.views.juegos.JuegosHubScreen
import com.example.sanaesenciaapp.ui.views.juegos.MemoriceScreen
import com.example.sanaesenciaapp.ui.views.juegos.PausaConscienteScreen
import com.example.sanaesenciaapp.ui.views.juegos.RespiracionGuiadaScreen

fun NavGraphBuilder.juegosGraph(
    navController: NavHostController
) {
    composable("juegos_hub") {
        JuegosHubScreen(
            onBackClick = { navController.popBackStack() },
            onRespiracionGuiadaClick = { navController.navigate("respiracion_guiada") },
            onPausaConscienteClick = { navController.navigate("pausa_consciente") },
            onMemoriceClick = { navController.navigate("memorice_zen") },
            onGatoClick = { navController.navigate("gato_armonico") },
            onCaminoBienestarClick = { navController.navigate("camino_bienestar") }
        )
    }

    composable("memorice_zen") {
        MemoriceScreen(
            onBackClick = { navController.popBackStack() }
        )
    }

    composable("gato_armonico") {
        GatoScreen(
            onBackClick = { navController.popBackStack() }
        )
    }

    composable("camino_bienestar") {
        CaminoBienestarScreen(
            onBackClick = { navController.popBackStack() }
        )
    }

    composable("respiracion_guiada") {
        RespiracionGuiadaScreen(
            onBackClick = { navController.popBackStack() }
        )
    }

    composable("pausa_consciente") {
        PausaConscienteScreen(
            onBackClick = { navController.popBackStack() }
        )
    }
}
