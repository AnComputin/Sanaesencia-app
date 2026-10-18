package com.example.sanaesenciaapp.ui.views.citas

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sanaesenciaapp.utils.CalendarioHelper
import com.example.sanaesenciaapp.viewmodel.CitaItem
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.viewmodel.EstadoCita
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisCitasScreen(
    citaViewModel: CitaViewModel,
    onVolver: () -> Unit
) {
    val citas by citaViewModel.citas.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var citaACancelar by remember { mutableStateOf<CitaItem?>(null) }

    val proximasCitas = remember(citas) {
        citas.filter { it.estado == EstadoCita.CONFIRMADA }
    }
    val historialCitas = remember(citas) {
        citas.filter { it.estado == EstadoCita.COMPLETADA || it.estado == EstadoCita.CANCELADA }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis citas") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Próximas") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Historial") }
                )
            }

            Crossfade(
                targetState = selectedTabIndex,
                label = "PestañasCitas"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> ListaCitas(
                        citas = proximasCitas,
                        esProxima = true,
                        formatearFecha = { citaViewModel.formatearFecha(it) },
                        puedeCancelar = { citaViewModel.puedeCancelar(it) },
                        onAgregarCalendario = { cita ->
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
                        onSolicitarCancelar = { cita ->
                            citaACancelar = cita
                        }
                    )
                    1 -> ListaCitas(
                        citas = historialCitas,
                        esProxima = false,
                        formatearFecha = { citaViewModel.formatearFecha(it) },
                        puedeCancelar = { false },
                        onAgregarCalendario = {},
                        onSolicitarCancelar = {}
                    )
                }
            }
        }

        citaACancelar?.let { cita ->
            AlertDialog(
                onDismissRequest = { citaACancelar = null },
                title = { Text("Cancelar cita") },
                text = {
                    Text("¿Estás seguro de que deseas cancelar la cita de ${cita.especialidad} con ${cita.profesional}?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val id = cita.id
                            citaACancelar = null
                            val cancelada = citaViewModel.cancelarCita(id)
                            if (cancelada) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Cita cancelada")
                                }
                            }
                        }
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { citaACancelar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun ListaCitas(
    citas: List<CitaItem>,
    esProxima: Boolean,
    formatearFecha: (Long) -> String,
    puedeCancelar: (CitaItem) -> Boolean,
    onAgregarCalendario: (CitaItem) -> Unit,
    onSolicitarCancelar: (CitaItem) -> Unit
) {
    if (citas.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tienes citas",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(citas, key = { it.id }) { cita ->
                CitaTarjeta(
                    cita = cita,
                    esProxima = esProxima,
                    formatearFecha = formatearFecha,
                    puedeCancelar = puedeCancelar(cita),
                    onAgregarCalendario = { onAgregarCalendario(cita) },
                    onSolicitarCancelar = { onSolicitarCancelar(cita) }
                )
            }
        }
    }
}

@Composable
private fun CitaTarjeta(
    cita: CitaItem,
    esProxima: Boolean,
    formatearFecha: (Long) -> String,
    puedeCancelar: Boolean,
    onAgregarCalendario: () -> Unit,
    onSolicitarCancelar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cita.especialidad,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (cita.estado) {
                        EstadoCita.CONFIRMADA -> "Confirmada"
                        EstadoCita.COMPLETADA -> "Completada"
                        EstadoCita.CANCELADA -> "Cancelada"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when (cita.estado) {
                        EstadoCita.CONFIRMADA -> MaterialTheme.colorScheme.primary
                        EstadoCita.COMPLETADA -> MaterialTheme.colorScheme.outline
                        EstadoCita.CANCELADA -> MaterialTheme.colorScheme.error
                    }
                )
            }

            Text(
                text = "Profesional: ${cita.profesional}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Fecha: ${formatearFecha(cita.inicioMillis)} - ${cita.hora}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Modalidad: ${if (cita.modalidad.name == "PRESENCIAL") "Presencial" else "Online"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Paciente: ${cita.paciente}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (esProxima) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAgregarCalendario,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Agregar a mi calendario")
                    }

                    Button(
                        onClick = onSolicitarCancelar,
                        enabled = puedeCancelar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (puedeCancelar) "Cancelar cita" else "No se puede cancelar con menos de 24 h"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MisCitasScreenPreview() {
    val viewModel = remember { CitaViewModel() }
    MisCitasScreen(
        citaViewModel = viewModel,
        onVolver = {}
    )
}
