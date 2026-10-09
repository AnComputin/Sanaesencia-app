package com.example.sanaesenciaapp.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sanaesenciaapp.data.AppState

@Composable
fun HomeScreen(
    navController: NavController,
    appState: AppState
) {

    val usuario =
        appState.usuarioActual

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text =
                "Bienvenido a SanaEsencia",
            fontSize = 28.sp
        )

        Text(
            text =
                "Hola, ${usuario?.nombre ?: ""}",
            fontSize = 20.sp
        )

        Button(

            onClick = {

                appState.logout()

                navController.navigate("login") {

                    popUpTo("home") {
                        inclusive = true
                    }
                }
            }

        ) {

            Text("Cerrar sesión")
        }
    }
}