package com.example.sanaesenciaapp.ui.views.catalogo

import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.data.room.EspecialistaEntity
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.CitaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionEspecialistaScreen(
    appState: AppState,
    citaViewModel: CitaViewModel,
    onNavigateBack: () -> Unit,
    onEspecialistaSelected: () -> Unit
) {
    val especialidad = appState.especialidadSeleccionada

    LaunchedEffect(especialidad?.id) {
        appState.especialistas.clear()
        appState.especialistaSeleccionado = null
        especialidad?.let {
            appState.cargarEspecialistasPorEspecialidad(it.id)
        }
    }

    val especialistasCompatibles = appState.especialistas.filter { especialista ->
        modalidadCompatible(
            modalidadEspecialidad = especialidad?.modalidad ?: "Ambas",
            modalidadEspecialista = especialista.modalidad
        ) != null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.especialista_topbar),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.beneficiario_accessibility_volver)
                        )
                    }
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = especialidad?.nombre ?: stringResource(R.string.catalogo_error_especialidad_predeterminada),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = especialidad?.descripcion
                            ?: stringResource(R.string.especialista_subt),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.especialista_disponibles, especialistasCompatibles.size),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (especialidad == null) {
                    item {
                        MensajeEspecialistas(stringResource(R.string.especialista_error_cargar))
                    }
                } else if (especialidad.agendaPausada) {
                    item {
                        MensajeEspecialistas(
                            stringResource(R.string.especialista_error_pausada)
                        )
                    }
                } else if (appState.especialistas.isEmpty()) {
                    item {
                        MensajeEspecialistas(stringResource(R.string.especialista_cargando))
                    }
                } else if (especialistasCompatibles.isEmpty()) {
                    item {
                        MensajeEspecialistas(
                            stringResource(R.string.especialista_error_incompatibles)
                        )
                    }
                } else {
                    items(especialistasCompatibles, key = { it.id }) { especialista ->
                        EspecialistaCard(
                            especialista = especialista,
                            onClick = {
                                val modalidadPermitida = modalidadCompatible(
                                    modalidadEspecialidad = especialidad.modalidad,
                                    modalidadEspecialista = especialista.modalidad
                                )

                                if (modalidadPermitida != null) {
                                    appState.especialistaSeleccionado = especialista

                                    citaViewModel.seleccionarServicio(
                                        especialidad = especialidad.nombre,
                                        profesional = especialista.nombre,
                                        modalidadPermitida = modalidadPermitida,
                                        especialidadId = especialidad.id,
                                        especialistaId = especialista.id
                                    )

                                    onEspecialistaSelected()
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun MensajeEspecialistas(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(20.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EspecialistaCard(
    especialista: EspecialistaEntity,
    onClick: () -> Unit
) {
    val iniciales = especialista.nombre
        .replace("Dr.", "")
        .replace("Dra.", "")
        .replace("Psic.", "")
        .replace("TO.", "")
        .replace("Nut.", "")
        .replace("Esp.", "")
        .replace("Acup.", "")
        .replace("Quiro.", "")
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales.ifBlank { "SE" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = especialista.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.detalle_modalidad, especialista.modalidad),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Devuelve la intersección entre lo permitido por la especialidad y lo que ofrece
 * el especialista: "Presencial", "Online", "Ambas" o null si son incompatibles.
 */
private fun modalidadCompatible(
    modalidadEspecialidad: String,
    modalidadEspecialista: String
): String? {
    fun comoConjunto(valor: String): Set<String> = when {
        valor.equals("Presencial", ignoreCase = true) -> setOf("Presencial")
        valor.equals("Online", ignoreCase = true) -> setOf("Online")
        else -> setOf("Presencial", "Online")
    }

    val disponibles = comoConjunto(modalidadEspecialidad)
        .intersect(comoConjunto(modalidadEspecialista))

    return when {
        disponibles.size == 2 -> "Ambas"
        "Presencial" in disponibles -> "Presencial"
        "Online" in disponibles -> "Online"
        else -> null
    }
}
