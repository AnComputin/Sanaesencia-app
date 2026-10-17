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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacientesAsignadosScreen(
    onPacienteClick: (Int)-> Unit,
    terapeutaViewModel: TerapeutaViewModel = viewModel()
){
    val state by terapeutaViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text("Mis Pacientes") })
        }
    ) { padding ->
        if (state.pacientes.isEmpty()){
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No hay pacientes asignados")
            }
        }else{
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.pacientes){paciente ->
                    val enRiesgo = terapeutaViewModel.estaEnRiesgo(paciente)

                    Card(onClick = {onPacienteClick(paciente.id)},
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (enRiesgo)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(text = paciente.nombre,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp)

                                if (enRiesgo){
                                    Card(colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )) {Text(text = "EN RIESGO DE DESERCIÓN",
                                        color = MaterialTheme.colorScheme.onError,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(6.dp)) }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(text = "RUT: ${paciente.rut}")
                            Text(text = "Última atención: ${paciente.ultimaAtencion}")
                            Text(text = "Box asignado: ${paciente.boxAsignado}")
                        }
                    }
                }
            }
        }
    }
}