package com.example.sanaesenciaapp.ui.views.agenda

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.viewmodel.Horario
import com.example.sanaesenciaapp.viewmodel.Modalidad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendarScreen(
    citaViewModel: CitaViewModel,
    onContinuar: () -> Unit,
    onVolver: () -> Unit
) {
    val uiState by citaViewModel.uiState.collectAsState()
    val horarios by citaViewModel.horarios.collectAsState()
    val puedeContinuar by citaViewModel.puedeContinuar.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservar cita") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección Servicio y Profesional
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
                        text = "Detalles del servicio",
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
                }
            }

            // Sección Modalidad
            Text(
                text = "Modalidad de atención",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModalidadTarjeta(
                    titulo = "Presencial",
                    isSelected = uiState.modalidad == Modalidad.PRESENCIAL,
                    onClick = { citaViewModel.seleccionarModalidad(Modalidad.PRESENCIAL) },
                    modifier = Modifier.weight(1f)
                )
                ModalidadTarjeta(
                    titulo = "Online",
                    isSelected = uiState.modalidad == Modalidad.ONLINE,
                    onClick = { citaViewModel.seleccionarModalidad(Modalidad.ONLINE) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Sección Fechas
            Text(
                text = "Seleccionar fecha",
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(citaViewModel.fechasDisponibles) { fechaMillis ->
                    val esSeleccionado = uiState.fechaMillis == fechaMillis
                    FechaTarjeta(
                        textoFecha = citaViewModel.formatearFecha(fechaMillis),
                        isSelected = esSeleccionado,
                        onClick = { citaViewModel.seleccionarFecha(fechaMillis) }
                    )
                }
            }

            // Sección Horarios
            Text(
                text = "Seleccionar hora",
                style = MaterialTheme.typography.titleMedium
            )
            val filasHorarios = horarios.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filasHorarios.forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fila.forEach { horario ->
                            HoraTarjeta(
                                horario = horario,
                                isSelected = uiState.hora == horario.hora,
                                onClick = { citaViewModel.seleccionarHora(horario.hora) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - fila.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Datos del Paciente
            Text(
                text = "Datos del paciente",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = uiState.nombrePaciente,
                onValueChange = { citaViewModel.cambiarNombre(it) },
                label = { Text("Nombre del paciente") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                },
                isError = uiState.errorNombre != null,
                supportingText = if (uiState.errorNombre != null) {
                    { Text(uiState.errorNombre!!) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Continuar
            Button(
                onClick = onContinuar,
                enabled = puedeContinuar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuar")
            }
        }
    }
}

@Composable
private fun ModalidadTarjeta(
    titulo: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "ModalidadBackgroundColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "ModalidadContentColor"
    )

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun FechaTarjeta(
    textoFecha: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "FechaBackgroundColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "FechaContentColor"
    )

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = textoFecha,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun HoraTarjeta(
    horario: Horario,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !horario.disponible -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "HoraBackgroundColor"
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            !horario.disponible -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "HoraContentColor"
    )

    Card(
        onClick = onClick,
        enabled = horario.disponible,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor,
            disabledContentColor = contentColor
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = horario.hora,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AgendarScreenPreview() {
    AgendarScreen(
        citaViewModel = CitaViewModel(),
        onContinuar = {},
        onVolver = {}
    )
}
