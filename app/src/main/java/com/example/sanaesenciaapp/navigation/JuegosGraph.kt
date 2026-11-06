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
