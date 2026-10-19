package com.example.sanaesenciaapp.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun HomeScreen(
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
                },

                actions = {

                    if (usuario == null) {

                        TextButton(
                            onClick = {
                                navController.navigate("login")
                            }
                        ) {

                            Text(
                                text = "Ingresar",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                    } else {

                        Text(
                            text = "Hola, ${usuario.nombre}",
                            modifier = Modifier.padding(end = 16.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Presentación
            Text(
                text = "Bienestar integral",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Acompañamos tu bienestar físico, emocional y mental.",
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // Botón reservar
            Button(
                onClick = {

                    if (usuario == null) {

                        navController.navigate("login")

                    } else {

                        navController.navigate(
                            "seleccionar_especialidad"
                        )
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),

                shape = RoundedCornerShape(18.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {

                Text(
                    text = "Reservar hora",
                    fontSize = 17.sp
                )
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = "Especialidades",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                EspecialidadCard(
                    nombre = "Psicología",
                    icono = "🧠",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        appState.especialidadSeleccionada = appState.especialidades.find { it.id == 1 }
                        navController.navigate("detalle_servicio")
                    }
                )

                EspecialidadCard(
                    nombre = "Fonoaudiología",
                    icono = "💬",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        appState.especialidadSeleccionada = appState.especialidades.find { it.id == 2 }
                        navController.navigate("detalle_servicio")
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                EspecialidadCard(
                    nombre = "Terapia\nOcupacional",
                    icono = "🖐️",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        appState.especialidadSeleccionada = appState.especialidades.find { it.id == 3 }
                        navController.navigate("detalle_servicio")
                    }
                )

                EspecialidadCard(
                    nombre = "Nutrición",
                    icono = "🍏",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        appState.especialidadSeleccionada = appState.especialidades.find { it.id == 4 }
                        navController.navigate("detalle_servicio")
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(20.dp),

                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Atención presencial y online",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Elige la modalidad que más se adapte a ti."
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // Botón de acceso a la ubicación y GPS
            OutlinedButton(
                onClick = { navController.navigate("ubicacion") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "📍 Ver Sede y Distancia (GPS)",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            if (usuario != null) {

                TextButton(
                    onClick = {

                        appState.logout()

                    },

                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                ) {

                    Text("Cerrar sesión")
                }
            }
        }
    }
}

@Composable
fun EspecialidadCard(
    nombre: String,
    icono: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    Card(
        modifier = modifier
            .height(125.dp)
            .clickable { onClick() },

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),

            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = icono,
                fontSize = 30.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = nombre,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}