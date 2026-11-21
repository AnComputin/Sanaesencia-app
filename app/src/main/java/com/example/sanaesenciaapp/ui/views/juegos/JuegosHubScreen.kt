package com.example.sanaesenciaapp.ui.views.juegos

import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R

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
import com.example.sanaesenciaapp.ui.components.SanaBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuegosHubScreen(
    onBackClick: () -> Unit,
    onRespiracionGuiadaClick: () -> Unit,
    onPausaConscienteClick: () -> Unit,
    onMemoriceClick: () -> Unit,
    onGatoClick: () -> Unit,
    onCaminoBienestarClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.juegos_topbar),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.juegos_accessibility_volver),
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
                    text = stringResource(R.string.juegos_titulo_principal),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(R.string.juegos_subt_principal),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                // SECCIÓN 1: PAUSAS DE BIENESTAR
                Text(
                    text = stringResource(R.string.juegos_seccion_pausas),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                ActividadCard(
                    titulo = stringResource(R.string.juegos_respiracion_titulo),
                    subtitulo = stringResource(R.string.juegos_respiracion_subt),
                    icono = "🫁",
                    duracion = stringResource(R.string.juegos_respiracion_duracion),
                    onClick = onRespiracionGuiadaClick
                )

                ActividadCard(
                    titulo = stringResource(R.string.juegos_pausa_titulo),
                    subtitulo = stringResource(R.string.juegos_pausa_subt),
                    icono = "🌿",
                    duracion = stringResource(R.string.juegos_pausa_duracion),
                    onClick = onPausaConscienteClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                // SECCIÓN 2: JUEGOS MENTALES
                Text(
                    text = stringResource(R.string.juegos_seccion_mentales),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                ActividadCard(
                    titulo = stringResource(R.string.juegos_memorice_titulo),
                    subtitulo = stringResource(R.string.juegos_memorice_subt),
                    icono = "🎴",
                    duracion = stringResource(R.string.juegos_memorice_duracion),
                    onClick = onMemoriceClick
                )

                ActividadCard(
                    titulo = stringResource(R.string.juegos_gato_titulo),
                    subtitulo = stringResource(R.string.juegos_gato_subt),
                    icono = "⭕",
                    duracion = stringResource(R.string.juegos_gato_duracion),
                    onClick = onGatoClick
                )

                ActividadCard(
                    titulo = stringResource(R.string.juegos_camino_titulo),
                    subtitulo = stringResource(R.string.juegos_camino_subt),
                    icono = "🎲",
                    duracion = stringResource(R.string.juegos_camino_duracion),
                    onClick = onCaminoBienestarClick
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
