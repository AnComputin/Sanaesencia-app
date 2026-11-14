package com.example.sanaesenciaapp.ui.views.juegos

import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.ContenidoPasoConsciente
import com.example.sanaesenciaapp.viewmodel.PausaConscienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PausaConscienteScreen(
    onBackClick: () -> Unit,
    viewModel: PausaConscienteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val pasoActual = uiState.pasos.getOrNull(uiState.pasoIndex)
    val totalPasos = uiState.pasos.size
    val progreso = if (totalPasos > 0) (uiState.pasoIndex + 1).toFloat() / totalPasos.toFloat() else 0f

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.juegos_pausa_titulo),
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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Indicador de Progreso
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.juegos_pausa_paso, uiState.pasoIndex + 1, totalPasos),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "${(progreso * 100).toInt()}%",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }

                // Tarjeta del Paso Actual
                if (pasoActual != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = pasoActual.icono,
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(
                                    when (pasoActual.contenido) {
                                        ContenidoPasoConsciente.DETENERSE ->
                                            R.string.juegos_pausa_detenerse_titulo
                                        ContenidoPasoConsciente.RESPIRACION ->
                                            R.string.juegos_pausa_respiracion_titulo
                                        ContenidoPasoConsciente.ENTORNO ->
                                            R.string.juegos_pausa_entorno_titulo
                                        ContenidoPasoConsciente.RECONEXION ->
                                            R.string.juegos_pausa_reconexion_titulo
                                    }
                                ),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(
                                    when (pasoActual.contenido) {
                                        ContenidoPasoConsciente.DETENERSE ->
                                            R.string.juegos_pausa_detenerse_descripcion
                                        ContenidoPasoConsciente.RESPIRACION ->
                                            R.string.juegos_pausa_respiracion_descripcion
                                        ContenidoPasoConsciente.ENTORNO ->
                                            R.string.juegos_pausa_entorno_descripcion
                                        ContenidoPasoConsciente.RECONEXION ->
                                            R.string.juegos_pausa_reconexion_descripcion
                                    }
                                ),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Botones de Navegación
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.pasoIndex > 0) {
                            OutlinedButton(
                                onClick = { viewModel.pasoAnterior() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.juegos_pausa_anterior),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.siguientePaso() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = stringResource(if (uiState.pasoIndex + 1 == totalPasos) R.string.juegos_pausa_finalizar else R.string.juegos_pausa_siguiente),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Diálogo de finalización
        if (uiState.estaCompletado) {
            AlertDialog(
                onDismissRequest = { viewModel.reiniciar() },
                title = {
                    Text(
                        text = stringResource(R.string.juegos_pausa_finalizada),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.juegos_pausa_completada),
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.reiniciar()
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.juegos_pausa_volver_inicio))
                    }
                }
            )
        }
    }
}
