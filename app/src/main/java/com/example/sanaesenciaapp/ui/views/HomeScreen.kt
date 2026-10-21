package com.example.sanaesenciaapp.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.components.SanaBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    appState: AppState
) {

    val usuario = appState.usuarioActual
    val hojaColor = MaterialTheme.colorScheme.primary

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

        SanaBackground {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                // Hoja decorativa grande del Home
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // Hoja principal
                    rotate(
                        degrees = -28f,
                        pivot = Offset(
                            x = size.width * 0.82f,
                            y = size.height * 0.19f
                        )
                    ) {

                        drawOval(
                            color = hojaColor.copy(alpha = 0.065f),
                            topLeft = Offset(
                                x = size.width * 0.58f,
                                y = size.height * 0.04f
                            ),
                            size = Size(
                                width = size.width * 0.36f,
                                height = size.height * 0.23f
                            )
                        )

                        // Nervadura central
                        drawLine(
                            color = hojaColor.copy(alpha = 0.085f),
                            start = Offset(
                                x = size.width * 0.76f,
                                y = size.height * 0.055f
                            ),
                            end = Offset(
                                x = size.width * 0.76f,
                                y = size.height * 0.255f
                            ),
                            strokeWidth = 4f
                        )

                        // Nervaduras pequeñas
                        drawLine(
                            color = hojaColor.copy(alpha = 0.055f),
                            start = Offset(
                                x = size.width * 0.76f,
                                y = size.height * 0.11f
                            ),
                            end = Offset(
                                x = size.width * 0.66f,
                                y = size.height * 0.09f
                            ),
                            strokeWidth = 3f
                        )

                        drawLine(
                            color = hojaColor.copy(alpha = 0.055f),
                            start = Offset(
                                x = size.width * 0.76f,
                                y = size.height * 0.15f
                            ),
                            end = Offset(
                                x = size.width * 0.86f,
                                y = size.height * 0.13f
                            ),
                            strokeWidth = 3f
                        )

                        drawLine(
                            color = hojaColor.copy(alpha = 0.055f),
                            start = Offset(
                                x = size.width * 0.76f,
                                y = size.height * 0.19f
                            ),
                            end = Offset(
                                x = size.width * 0.66f,
                                y = size.height * 0.18f
                            ),
                            strokeWidth = 3f
                        )
                    }
                }

                // CONTENIDO ORIGINAL
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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

                    if (usuario != null) {

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        // Botón Mis citas (visible solo si el usuario tiene sesión iniciada)
                        OutlinedButton(
                            onClick = {
                                navController.navigate("mis_citas")
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),

                            shape = RoundedCornerShape(16.dp)
                        ) {

                            Text(
                                text = "Mis citas",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
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
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        EspecialidadCard(
                            nombre = "Psicología\nInfanto-Juvenil",
                            icono = "🌱",
                            modifier = Modifier.weight(1f),

                            onClick = {

                                appState.especialidadSeleccionada =
                                    appState.especialidades.find {
                                        it.id == 1
                                    }

                                navController.navigate(
                                    "detalle_servicio"
                                )
                            }
                        )

                        EspecialidadCard(
                            nombre = "Psicología\nAdulto",
                            icono = "🧠",
                            modifier = Modifier.weight(1f),

                            onClick = {

                                appState.especialidadSeleccionada =
                                    appState.especialidades.find {
                                        it.id == 2
                                    }

                                navController.navigate(
                                    "detalle_servicio"
                                )
                            }
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        EspecialidadCard(
                            nombre = "Terapia\nOcupacional",
                            icono = "🧩",
                            modifier = Modifier.weight(1f),

                            onClick = {

                                appState.especialidadSeleccionada =
                                    appState.especialidades.find {
                                        it.id == 3
                                    }

                                navController.navigate(
                                    "detalle_servicio"
                                )
                            }
                        )

                        EspecialidadCard(
                            nombre = "Nutrición\nIntegrativa",
                            icono = "🥗",
                            modifier = Modifier.weight(1f),

                            onClick = {

                                appState.especialidadSeleccionada =
                                    appState.especialidades.find {
                                        it.id == 4
                                    }

                                navController.navigate(
                                    "detalle_servicio"
                                )
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
                            containerColor =
                                MaterialTheme.colorScheme.primaryContainer
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

                    // GPS
                    OutlinedButton(
                        onClick = {
                            navController.navigate("ubicacion")
                        },

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
                        modifier = Modifier.height(12.dp)
                    )

                    // Espacio de Bienestar
                    Button(
                        onClick = {
                            navController.navigate("juegos_hub")
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),

                        shape = RoundedCornerShape(16.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {

                        Text(
                            text = "🌿 Espacio de Bienestar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
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

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
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
