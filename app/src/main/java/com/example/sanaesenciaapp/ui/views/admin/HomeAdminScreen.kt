package com.example.sanaesenciaapp.ui.views.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.navigation.NavController
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.AdminViewModel
import com.example.sanaesenciaapp.viewmodel.AdminViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAdminScreen(
    navController: NavController,
    appState: AppState,
    adminViewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(appState.db))
) {
    val especialidades by adminViewModel.especialidades.collectAsState()
    val usuario = appState.usuarioActual

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🌿 Panel Administrador",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            )
        }
    ) { padding ->

        SanaBackground {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Gestión de Especialidades",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Hola, ${usuario?.nombre ?: "Administrador"}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Ajusta la modalidad y la disponibilidad de la agenda en tiempo real. Los cambios se guardan automáticamente en Room.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(
                    items = especialidades,
                    key = { it.id }
                ) { especialidad ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {

                            // Header de la tarjeta de especialidad
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = especialidad.icono,
                                    fontSize = 32.sp
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = especialidad.nombre,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )

                                    Text(
                                        text = especialidad.profesional,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Selección de Modalidad
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Modalidad de atención",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = especialidad.modalidad.equals("Presencial", ignoreCase = true),
                                        onClick = {
                                            adminViewModel.actualizarModalidad(especialidad.id, "Presencial")
                                        },
                                        label = { Text("Presencial") },
                                        modifier = Modifier.weight(1f)
                                    )

                                    FilterChip(
                                        selected = especialidad.modalidad.equals("Online", ignoreCase = true),
                                        onClick = {
                                            adminViewModel.actualizarModalidad(especialidad.id, "Online")
                                        },
                                        label = { Text("Online") },
                                        modifier = Modifier.weight(1f)
                                    )

                                    FilterChip(
                                        selected = especialidad.modalidad.equals("Ambas", ignoreCase = true),
                                        onClick = {
                                            adminViewModel.actualizarModalidad(especialidad.id, "Ambas")
                                        },
                                        label = { Text("Ambas") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Estado de la Agenda (Switch: activado = agenda disponible, es decir agendaPausada = false)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Estado de agenda",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    val agendaDisponible = !especialidad.agendaPausada
                                    Surface(
                                        shape = RoundedCornerShape(50.dp),
                                        color = if (agendaDisponible)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            text = if (agendaDisponible) "Disponible (Activa)" else "Pausada",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (agendaDisponible)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Switch(
                                    checked = !especialidad.agendaPausada,
                                    onCheckedChange = { disponible ->
                                        adminViewModel.actualizarEstadoAgenda(especialidad.id, disponible)
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            appState.logout()
                            navController.navigate("home") {
                                popUpTo("home_admin") {
                                    inclusive = true
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar sesión")
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}
