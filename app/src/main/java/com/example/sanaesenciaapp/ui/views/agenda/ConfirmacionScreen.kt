package com.example.sanaesenciaapp.ui.views.agenda

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sanaesenciaapp.utils.CalendarioHelper
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.viewmodel.Modalidad
import kotlinx.coroutines.launch

@Composable
fun ConfirmacionScreen(
    citaViewModel: CitaViewModel,
    onVerMisCitas: () -> Unit
) {
    val ultimaCita by citaViewModel.ultimaCita.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "EscalaIconoConfirmacion"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        val cita = ultimaCita
        if (cita == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay cita para mostrar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "¡Cita confirmada!",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(96.dp)
                        .scale(scale)
                )

                Text(
                    text = "¡Cita confirmada!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Resumen de la cita",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Especialidad: ${cita.especialidad}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Profesional: ${cita.profesional}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Fecha: ${citaViewModel.formatearFecha(cita.inicioMillis)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Hora: ${cita.hora}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val modalidadTexto = when (cita.modalidad) {
                            Modalidad.PRESENCIAL -> "Presencial"
                            Modalidad.ONLINE -> "Online"
                        }
                        Text(
                            text = "Modalidad: $modalidadTexto",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Paciente: ${cita.paciente}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val exito = CalendarioHelper.agregarACalendario(
                            context = context,
                            titulo = "Cita: ${cita.especialidad}",
                            descripcion = "Cita médica de ${cita.especialidad} con ${cita.profesional}. Paciente: ${cita.paciente}.",
                            inicioMillis = cita.inicioMillis,
                            ubicacion = "Modalidad: ${cita.modalidad.name}"
                        )
                        if (!exito) {
                            scope.launch {
                                snackbarHostState.showSnackbar("No hay una app de calendario")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agregar a mi calendario")
                }

                Button(
                    onClick = onVerMisCitas,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver mis citas")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmacionScreenPreview() {
    val viewModel = remember { CitaViewModel() }
    ConfirmacionScreen(
        citaViewModel = viewModel,
        onVerMisCitas = {}
    )
}
