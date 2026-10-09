package com.example.sanaesenciaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.sanaesenciaapp.navigation.AppNavigation
import com.example.sanaesenciaapp.ui.theme.SanaEsenciaAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SanaEsenciaAppTheme {
                SanaEsenciaApp()
            }
        }
    }
}

@Composable
fun SanaEsenciaApp() {

    val navController = rememberNavController()

    MaterialTheme {
        AppNavigation(navController)
    }
}