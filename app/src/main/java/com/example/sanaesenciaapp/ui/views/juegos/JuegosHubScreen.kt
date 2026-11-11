package com.example.sanaesenciaapp.ui.views.juegos

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sanaesenciaapp.ui.components.SanaBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuegosHubScreen(
    navController: NavController
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🌿 Espacio de Bienestar",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        SanaBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tu momento de autocuidado",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Dedica unos minutos a tu bienestar. Explora actividades para ejercitar la mente, relajarte y conectar con el presente.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                // SECCIÓN 1: PAUSAS DE BIENESTAR
                Text(
                    text = "🧘 Pausas de bienestar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                ActividadCard(
                    titulo = "Respiración Guiada",
                    subtitulo = "Sincroniza tu respiración en 1 minuto para reducir el estrés y calmar la mente.",
                    icono = "🫁",
                    duracion = "1 min",
                    onClick = { navController.navigate("respiracion_guiada") }
                )

                ActividadCard(
                    titulo = "Pausa Consciente",
                    subtitulo = "Actividad guiada paso a paso para hacer una pausa, conectar con tu cuerpo y entorno.",
                    icono = "🌿",
                    duracion = "2 min",
                    onClick = { navController.navigate("pausa_consciente") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // SECCIÓN 2: JUEGOS MENTALES
                Text(
                    text = "🧠 Juegos mentales",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                ActividadCard(
                    titulo = "Memorice Zen",
                    subtitulo = "Entrena tu memoria encontrando parejas de cartas con temática de bienestar.",
                    icono = "🎴",
                    duracion = "Estimulación visual",
                    onClick = { navController.navigate("memorice_zen") }
                )

                ActividadCard(
                    titulo = "Gato Armónico",
                    subtitulo = "El clásico tres en raya para ejercitar la estrategia (Individual o contra IA).",
                    icono = "⭕",
                    duracion = "Estrategia cognitiva",
                    onClick = { navController.navigate("gato_armonico") }
                )

                ActividadCard(
                    titulo = "Camino del Bienestar",
                    subtitulo = "Avanza por el tablero de 25 casillas descubriendo hábitos y mensajes positivos.",
                    icono = "🎲",
                    duracion = "Recorrido lúdico",
                    onClick = { navController.navigate("camino_bienestar") }
                )
            }
        }
    }
}

@Composable
fun ActividadCard(
    titulo: String,
    subtitulo: String,
    icono: String,
    duracion: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = icono,
                fontSize = 36.sp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = titulo,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = duracion,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitulo,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }
    }
}
