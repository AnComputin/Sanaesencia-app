package com.example.sanaesenciaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.navigation.AppNavigation
import com.example.sanaesenciaapp.ui.theme.SanaEsenciaAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        val appState = AppState(db)
        appState.cargarDatos()

        setContent {
            SanaEsenciaAppTheme {
                SanaEsenciaApp(appState)
            }
        }
    }
}

@Composable
fun SanaEsenciaApp(appState: AppState) {
    val navController = rememberNavController()
    AppNavigation(
        navController = navController,
        appState = appState
    )
}
