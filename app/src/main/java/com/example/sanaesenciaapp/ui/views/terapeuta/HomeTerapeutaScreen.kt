package com.example.sanaesenciaapp.ui.views.terapeuta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sanaesenciaapp.data.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTerapeutaScreen(
    navController: NavController,
    appState: AppState
) {

    val usuario = appState.usuarioActual

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🌿 SanaEsencia",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Panel del terapeuta",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Hola, ${usuario?.nombre ?: "Terapeuta"}",
                fontSize = 18.sp
            )

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Button(
                onClick = {
                    navController.navigate("agenda_terapeuta")
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),

                shape = RoundedCornerShape(18.dp)
            ) {

                Text("Agenda del día")
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = {
                    navController.navigate("pacientes_terapeuta")
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),

                shape = RoundedCornerShape(18.dp)
            ) {

                Text("Mis pacientes")
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            OutlinedButton(
                onClick = {

                    appState.logout()

                    navController.navigate("home") {

                        popUpTo("home_terapeuta") {
                            inclusive = true
                        }
                    }
                }
            ) {

                Text("Cerrar sesión")
            }
        }
    }
}

