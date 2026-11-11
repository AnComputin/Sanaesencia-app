package com.example.sanaesenciaapp.ui.views.juegos

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.GatoViewModel
import com.example.sanaesenciaapp.viewmodel.TipoOponenteGato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatoScreen(
    navController: NavController,
    viewModel: GatoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "⭕ Gato Armónico",
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selector de modo / dificultad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = uiState.oponente == TipoOponenteGato.DOS_JUGADORES,
                        onClick = { viewModel.setOponente(TipoOponenteGato.DOS_JUGADORES) },
                        label = { Text("2 Jugadores") }
                    )
                    FilterChip(
                        selected = uiState.oponente == TipoOponenteGato.CPU_FACIL,
                        onClick = { viewModel.setOponente(TipoOponenteGato.CPU_FACIL) },
                        label = { Text("CPU Fácil") }
                    )
                    FilterChip(
                        selected = uiState.oponente == TipoOponenteGato.CPU_DIFICIL,
                        onClick = { viewModel.setOponente(TipoOponenteGato.CPU_DIFICIL) },
                        label = { Text("CPU Experto") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Marcador
                val etiquetaX = if (uiState.oponente == TipoOponenteGato.DOS_JUGADORES) "Jugador X" else "Tú (X)"
                val etiquetaO = if (uiState.oponente == TipoOponenteGato.DOS_JUGADORES) "Jugador O" else "Sistema (O)"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(etiquetaX, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                            Text("${uiState.puntajeX}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Empates", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                            Text("${uiState.empates}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(etiquetaO, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                            Text("${uiState.puntajeO}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Estado del turno
                val textoEstado = when {
                    uiState.ganador == "Empate" -> "¡Empate armónico!"
                    uiState.ganador != null -> {
                        if (uiState.oponente == TipoOponenteGato.DOS_JUGADORES) {
                            "¡Ganador: Jugador ${uiState.ganador}!"
                        } else {
                            if (uiState.ganador == "X") "¡Has ganado la partida!" else "¡El Sistema (CPU) ha ganado!"
                        }
                    }
                    uiState.pensandoCpu -> "Sistema (CPU) pensando..."
                    else -> {
                        if (uiState.oponente == TipoOponenteGato.DOS_JUGADORES) {
                            "Turno del Jugador ${uiState.turno}"
                        } else {
                            if (uiState.turno == "X") "Tu turno (X)" else "Turno del Sistema (O)"
                        }
                    }
                }
                Text(
                    text = textoEstado,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tablero 3x3
                val deshabilitarTablero = uiState.pensandoCpu || (uiState.oponente != TipoOponenteGato.DOS_JUGADORES && uiState.turno == "O")

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(uiState.tablero) { index, valor ->
                        CeldaGato(
                            valor = valor,
                            habilitado = !deshabilitarTablero && valor == null
                        ) {
                            viewModel.seleccionarCelda(index)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.reiniciarJuego() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Reiniciar Partida", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (uiState.ganador != null) {
            AlertDialog(
                onDismissRequest = { viewModel.reiniciarJuego() },
                title = {
                    Text(
                        text = if (uiState.ganador == "Empate") "🤝 ¡Empate!" else if (uiState.ganador == "X" || uiState.oponente == TipoOponenteGato.DOS_JUGADORES) "🏆 ¡Victoria!" else "🤖 Fin del Juego",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                text = {
                    val msg = when {
                        uiState.ganador == "Empate" -> "El juego terminó en empate. Excelente equilibrio mental."
                        uiState.oponente == TipoOponenteGato.DOS_JUGADORES -> "El Jugador ${uiState.ganador} ha ganado la partida."
                        uiState.ganador == "X" -> "¡Felicitaciones! Has derrotado al Sistema en esta partida."
                        else -> "El Sistema (CPU) ha ganado esta vez. ¡Sigue practicando tu estrategia!"
                    }
                    Text(text = msg, fontSize = 16.sp)
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.reiniciarJuego() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continuar jugando")
                    }
                }
            )
        }
    }
}

@Composable
fun CeldaGato(
    valor: String?,
    habilitado: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(enabled = habilitado) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (valor == null) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (valor != null) {
                Text(
                    text = valor,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (valor == "X") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "·",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
