package com.example.sanaesenciaapp.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sanaesenciaapp.data.AppState

@Composable
fun SeleccionEspecialidadScreen(
    navController: NavController,
    appState: AppState
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Selecciona una especialidad",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        appState.especialidades.forEach { especialidad ->

            Button(
                onClick = {

                    appState.especialidadSeleccionada =
                        especialidad

                    navController.navigate(
                        "detalle_servicio"
                    )
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = especialidad.nombre
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }
    }
}