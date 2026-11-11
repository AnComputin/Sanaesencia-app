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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.AdminViewModel
import com.example.sanaesenciaapp.viewmodel.AdminViewModelFactory
import com.example.sanaesenciaapp.viewmodel.BoxEstadoAdmin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAdminScreen(
    navController: NavController,
    appState: AppState,
    adminViewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(appState.db))
) {
    val especialidades by adminViewModel.especialidades.collectAsState()
    val usuario = appState.usuarioActual
    var pestañaSeleccionada by remember { mutableStateOf(0) }

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
                },
                actions = {
                    TextButton(
                        onClick = {
                            appState.logout()
                            navController.navigate("home") {
                                popUpTo("home_admin") { inclusive = true }
                            }
                        }
                    ) {
                        Text("Salir")
                    }
                }
            )
        }
    ) { padding ->
        SanaBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 14.dp,
                        bottom = 10.dp
                    )
                ) {
                    Text(
                        text = "Administración del centro",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Hola, ${usuario?.nombre ?: "Administrador"}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TabRow(
                    selectedTabIndex = pestañaSeleccionada,
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    Tab(
                        selected = pestañaSeleccionada == 0,
                        onClick = { pestañaSeleccionada = 0 },
                        text = { Text("Especialidades") }
                    )
                    Tab(
                        selected = pestañaSeleccionada == 1,
                        onClick = { pestañaSeleccionada = 1 },
                        text = { Text("Boxes") }
                    )
                }

                when (pestañaSeleccionada) {
                    0 -> EspecialidadesAdminContent(
                        especialidades = especialidades,
                        adminViewModel = adminViewModel,
                        modifier = Modifier.weight(1f)
                    )

                    else -> BoxesAdminContent(
                        adminViewModel = adminViewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EspecialidadesAdminContent(
    especialidades: List<EspecialidadEntity>,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Gestión de especialidades",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Ajusta la modalidad y la disponibilidad de la agenda. Los cambios se guardan automáticamente en Room.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
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
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = especialidad.icono,
                            fontSize = 32.sp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
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

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    adminViewModel.actualizarModalidad(
                                        especialidad.id,
                                        "Presencial"
                                    )
                                },
                                label = { Text("Presencial") },
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = especialidad.modalidad.equals("Online", ignoreCase = true),
                                onClick = {
                                    adminViewModel.actualizarModalidad(
                                        especialidad.id,
                                        "Online"
                                    )
                                },
                                label = { Text("Online") },
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = especialidad.modalidad.equals("Ambas", ignoreCase = true),
                                onClick = {
                                    adminViewModel.actualizarModalidad(
                                        especialidad.id,
                                        "Ambas"
                                    )
                                },
                                label = { Text("Ambas") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Estado de agenda",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val agendaDisponible = !especialidad.agendaPausada
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = if (agendaDisponible) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                }
                            ) {
                                Text(
                                    text = if (agendaDisponible) "Disponible (Activa)" else "Pausada",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (agendaDisponible) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    },
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = !especialidad.agendaPausada,
                            onCheckedChange = { disponible ->
                                adminViewModel.actualizarEstadoAgenda(
                                    especialidad.id,
                                    disponible
                                )
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun BoxesAdminContent(
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val estadoBoxes by adminViewModel.boxesUiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Ocupación de boxes",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Consulta en vivo los 8 boxes físicos del centro. Las citas online no ocupan box.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
        }

        item {
            Text(
                text = "Fecha",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(adminViewModel.fechasBoxes) { fecha ->
                    FilterChip(
                        selected = fecha == estadoBoxes.fechaMillis,
                        onClick = { adminViewModel.seleccionarFechaBoxes(fecha) },
                        label = { Text(formatearFechaAdmin(fecha)) }
                    )
                }
            }
        }

        item {
            Text(
                text = "Hora",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(adminViewModel.horasBoxes) { hora ->
                    FilterChip(
                        selected = hora == estadoBoxes.hora,
                        onClick = { adminViewModel.seleccionarHoraBoxes(hora) },
                        label = { Text(hora) }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (estadoBoxes.capacidadCompleta) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "${estadoBoxes.ocupados} / 8 boxes ocupados",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estadoBoxes.capacidadCompleta) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )

                    Text(
                        text = if (estadoBoxes.capacidadCompleta) {
                            "Capacidad completa para ${estadoBoxes.hora}. No se pueden agregar más citas presenciales en este intervalo."
                        } else {
                            "${estadoBoxes.disponibles} boxes disponibles para ${estadoBoxes.hora}."
                        },
                        fontSize = 13.sp,
                        color = if (estadoBoxes.capacidadCompleta) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
        }

        items(
            items = estadoBoxes.boxes.chunked(2)
        ) { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                fila.forEach { box ->
                    BoxAdminCard(
                        box = box,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (fila.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = "El estado se actualiza desde Room. Solo cuentan citas PRESENCIAL con estado CONFIRMADA que se superponen con el horario seleccionado.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun BoxAdminCard(
    box: BoxEstadoAdmin,
    modifier: Modifier = Modifier
) {
    val cita = box.citas.firstOrNull()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (box.ocupado) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "BOX ${box.numero}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (box.ocupado) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
            ) {
                Text(
                    text = if (box.ocupado) "Ocupado" else "Disponible",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (box.ocupado) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.padding(
                        horizontal = 9.dp,
                        vertical = 4.dp
                    )
                )
            }

            if (cita != null) {
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = cita.especialidadNombre,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                Text(
                    text = cita.especialistaNombre,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.82f)
                )

                Text(
                    text = "Paciente: ${cita.pacienteNombre}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.72f)
                )

                Text(
                    text = "Hora: ${cita.hora}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                if (box.tieneConflicto) {
                    Text(
                        text = "⚠ ${box.citas.size} citas superpuestas en este box",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Text(
                    text = "Libre para una atención presencial.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                )
            }
        }
    }
}

private fun formatearFechaAdmin(millis: Long): String {
    val formato = SimpleDateFormat("EEE d MMM", Locale("es", "CL"))
    return formato.format(Date(millis))
        .replaceFirstChar { caracter ->
            if (caracter.isLowerCase()) caracter.titlecase(Locale("es", "CL")) else caracter.toString()
        }
}
