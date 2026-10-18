package com.example.sanaesenciaapp.ui.views.agenda

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.viewmodel.Modalidad
import kotlinx.coroutines.delay
import java.util.Locale

// TODO mover a ReglasNegocio
private const val PAGO_SEGUNDOS = 600

@Composable
fun PagoScreen(
    citaViewModel: CitaViewModel,
    onPagoExitoso: () -> Unit,
    onExpirado: () -> Unit
) {
    val uiState by citaViewModel.uiState.collectAsState()

    var tiempoRestante by remember { mutableIntStateOf(PAGO_SEGUNDOS) }
    var estaExpirado by remember { mutableStateOf(false) }

    LaunchedEffect(tiempoRestante) {
        if (tiempoRestante > 0) {
            delay(1000L)
            tiempoRestante -= 1
        } else {
            estaExpirado = true
        }
    }

    val progreso = (tiempoRestante.toFloat() / PAGO_SEGUNDOS.toFloat()).coerceIn(0f, 1f)
    val progresoAnimado by animateFloatAsState(
        targetValue = progreso,
        label = "ProgresoTiempoPago"
    )

    val minutos = tiempoRestante / 60
    val segundos = tiempoRestante % 60
    val tiempoTexto = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos)

    val colorBarraProgreso = if (tiempoRestante < 60) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Resumen del Pago",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Resumen de la cita
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
                    text = "Detalles de la cita",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Especialidad: ${if (uiState.especialidad.isNotBlank()) uiState.especialidad else "-"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Profesional: ${if (uiState.profesional.isNotBlank()) uiState.profesional else "-"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                val modalidadTexto = when (uiState.modalidad) {
                    Modalidad.PRESENCIAL -> "Presencial"
                    Modalidad.ONLINE -> "Online"
                    null -> "-"
                }
                Text(
                    text = "Modalidad: $modalidadTexto",
                    style = MaterialTheme.typography.bodyMedium
                )
                val fechaTexto = uiState.fechaMillis?.let { citaViewModel.formatearFecha(it) } ?: "-"
                Text(
                    text = "Fecha: $fechaTexto",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Hora: ${uiState.hora ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Paciente: ${if (uiState.nombrePaciente.isNotBlank()) uiState.nombrePaciente else "-"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Monto ficticio y leyenda
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Pago simulado",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$35.000",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Temporizador regresivo y barra de progreso
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tiempo restante para pagar:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = tiempoTexto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (tiempoRestante < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                )
            }

            LinearProgressIndicator(
                progress = { progresoAnimado },
                modifier = Modifier.fillMaxWidth(),
                color = colorBarraProgreso,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Pagar
        Button(
            onClick = {
                citaViewModel.confirmarCita()
                onPagoExitoso()
            },
            enabled = !estaExpirado && tiempoRestante > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pagar")
        }
    }

    // Modal de tiempo agotado
    if (estaExpirado) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Tiempo agotado") },
            text = { Text("El tiempo para realizar el pago ha expirado.") },
            confirmButton = {
                Button(onClick = onExpirado) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PagoScreenPreview() {
    PagoScreen(
        citaViewModel = CitaViewModel(),
        onPagoExitoso = {},
        onExpirado = {}
    )
}
