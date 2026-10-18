package com.example.sanaesenciaapp.ui.views.terapeuta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleContinuidadScreen(
    onNavigateBack: ()-> Unit,
    terapeutaViewModel: TerapeutaViewModel = viewModel()
){
    val state by terapeutaViewModel.uiState.collectAsState()
    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopAppBar(title = { Text("Detalle de Continuidad") }) })
    {padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {

            //ficha de paciente
            item { Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = state.pacienteSeleccionado?.nombre ?: "Paciente no seleccionado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "RUT: ${state.pacienteSeleccionado?.rut ?: "-"}")
                    Text(text = "Box asignado: ${state.pacienteSeleccionado?.boxAsignado ?: "-"}")
                    Text(text = "Última atención: ${state.pacienteSeleccionado?.ultimaAtencion ?: "-"}")
                }
            } }

            //notas de evolucion
            item { Text(text = "Notas de evolución",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = state.notasEvolucion,
                    onValueChange = {terapeutaViewModel.onNotasEvolucionChange(it)},
                    label = {Text("Escribir notas clínicas/holísticas...")},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5)
            }

            //historial de citas
            item {
                Text(text = "Historial de citas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp)
            }
            if (state.historialCitas.isEmpty()){
                item { Text("No hay citas registradas") }
            }else{
                items(state.historialCitas){cita ->
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Fecha ${state.fechaSeleccionada}")
                            Text(text = "Hora: ${cita.hora}")
                            Text(text = "Box: ${cita.box}")
                            Text(text = "Estado: ${cita.estado.name}")
                        }
                    }
                }
            }
        }
    }
}