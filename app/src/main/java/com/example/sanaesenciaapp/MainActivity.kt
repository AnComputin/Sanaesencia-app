package com.example.sanaesenciaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.navigation.AppNavigation
import com.example.sanaesenciaapp.ui.theme.SanaEsenciaAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val appContainer = (application as MyApplication).appContainer
        val db = appContainer.database
        val appState = AppState(db, appContainer.sesionRepository, appContainer.usuarioRepository)

        appState.cargarDatos()

        setContent {

            SanaEsenciaAppTheme {

                SanaEsenciaApp(
                    appState
                )
            }
        }
    }
}


@Composable
fun SanaEsenciaApp(
    appState: AppState
) {

    val navController =
        rememberNavController()

    MaterialTheme {

        AppNavigation(
            navController,
            appState
        )
    }
}