package com.example.sanaesenciaapp.ui.views.terapeuta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesenciaapp.viewmodel.EstadoCita
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaDiaScreen(terapeutaViewModel: TerapeutaViewModel = viewModel()){
    val state by terapeutaViewModel.uiState.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text("Agenda del Día") })
        }) {padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Fecha: ${state.fechaSeleccionada}",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.citas){cita ->
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when(cita.estado){
                                EstadoCita.ASISTIDA ->
                                    MaterialTheme.colorScheme.primaryContainer
                                EstadoCita.INASISTENTE ->
                                    MaterialTheme.colorScheme.errorContainer
                                EstadoCita.CANCELADA_TERAPEUTA ->
                                    MaterialTheme.colorScheme.surfaceVariant
                                EstadoCita.PENDIENTE ->
                                    MaterialTheme.colorScheme.surface
                                else ->
                                    MaterialTheme.colorScheme.surface
                            }
                        )) {Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = cita.hora,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp)
                        Text(text = cita.box)
                    }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = cita.paciente)
                        Text(text = "Estado: ${cita.estado.name}",
                            fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        //botones de accion
                        if (cita.estado == EstadoCita.PENDIENTE){
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    terapeutaViewModel.actualizarEstadoCita(cita.id, EstadoCita.ASISTIDA)
                                },
                                    modifier = Modifier.weight(1f)) {
                                    Text("Asistida", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = {terapeutaViewModel.actualizarEstadoCita(cita.id,
                                        EstadoCita.INASISTENTE)},
                                    modifier = Modifier.weight(1f)
                                ) {Text("Inasistente", fontSize = 12.sp) }
                                OutlinedButton(
                                    onClick = {terapeutaViewModel.actualizarEstadoCita(cita.id,
                                        EstadoCita.CANCELADA_TERAPEUTA)},
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {Text("Cancelada", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}