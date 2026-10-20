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
import com.example.sanaesenciaapp.viewmodel.EstadoReserva
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.ui.components.SanaBackground

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
        citas.filter { it.estado == EstadoReserva.CONFIRMADA }
    }
    val historialCitas = remember(citas) {
        citas.filter { it.estado == EstadoReserva.COMPLETADA || it.estado == EstadoReserva.CANCELADA }
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
        SanaBackground {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 18.dp
                        )
                ) {

                    Text(
                        text = "Tus atenciones",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Revisa y administra tus próximas citas.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                            .copy(alpha = 0.7f)
                    )
                }

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {

                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = {
                            selectedTabIndex = 0
                        },
                        text = {
                            Text(
                                text = "Próximas",
                                fontWeight =
                                    if (selectedTabIndex == 0)
                                        FontWeight.Bold
                                    else
                                        FontWeight.Normal
                            )
                        }
                    )

                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            selectedTabIndex = 1
                        },
                        text = {
                            Text(
                                text = "Historial",
                                fontWeight =
                                    if (selectedTabIndex == 1)
                                        FontWeight.Bold
                                    else
                                        FontWeight.Normal
                            )
                        }
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

                            formatearFecha = {
                                citaViewModel.formatearFecha(it)
                            },

                            puedeCancelar = {
                                citaViewModel.puedeCancelar(it)
                            },

                            onAgregarCalendario = { cita ->

                                val exito =
                                    CalendarioHelper.agregarACalendario(
                                        context = context,
                                        titulo =
                                            "Cita: ${cita.especialidad}",

                                        descripcion =
                                            "Cita médica de ${cita.especialidad} " +
                                                    "con ${cita.profesional}. " +
                                                    "Paciente: ${cita.paciente}.",

                                        inicioMillis =
                                            cita.inicioMillis,

                                        ubicacion =
                                            "Modalidad: ${cita.modalidad.name}"
                                    )

                                if (!exito) {

                                    scope.launch {

                                        snackbarHostState.showSnackbar(
                                            "No hay una app de calendario"
                                        )
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

                            formatearFecha = {
                                citaViewModel.formatearFecha(it)
                            },

                            puedeCancelar = {
                                false
                            },

                            onAgregarCalendario = {},

                            onSolicitarCancelar = {}
                        )
                    }
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

        shape = RoundedCornerShape(22.dp),

        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),

            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            // Especialidad + estado
            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = cita.especialidad,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.secondary,

                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(50.dp),

                    color = when (cita.estado) {

                        EstadoReserva.CONFIRMADA ->
                            MaterialTheme.colorScheme
                                .primaryContainer

                        EstadoReserva.COMPLETADA ->
                            MaterialTheme.colorScheme
                                .surfaceVariant

                        EstadoReserva.CANCELADA ->
                            MaterialTheme.colorScheme
                                .errorContainer
                    }
                ) {

                    Text(
                        text = when (cita.estado) {

                            EstadoReserva.CONFIRMADA ->
                                "Confirmada"

                            EstadoReserva.COMPLETADA ->
                                "Completada"

                            EstadoReserva.CANCELADA ->
                                "Cancelada"
                        },

                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,

                        color = when (cita.estado) {

                            EstadoReserva.CONFIRMADA ->
                                MaterialTheme.colorScheme
                                    .onPrimaryContainer

                            EstadoReserva.COMPLETADA ->
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant

                            EstadoReserva.CANCELADA ->
                                MaterialTheme.colorScheme
                                    .onErrorContainer
                        },

                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        )
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            // Profesional
            Text(
                text = "Profesional",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = cita.profesional,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Fecha y hora
            Text(
                text = "Fecha y hora",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text =
                    "${formatearFecha(cita.inicioMillis)} · ${cita.hora}",

                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Surface(
                    shape = RoundedCornerShape(12.dp),

                    color =
                        MaterialTheme.colorScheme
                            .primaryContainer
                ) {

                    Text(
                        text =
                            if (
                                cita.modalidad.name ==
                                "PRESENCIAL"
                            )
                                "📍 Presencial"
                            else
                                "💻 Online",

                        fontSize = 13.sp,

                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 7.dp
                        ),

                        color =
                            MaterialTheme.colorScheme
                                .onPrimaryContainer
                    )
                }
            }

            Text(
                text = "Paciente: ${cita.paciente}",
                fontSize = 14.sp,
                color =
                    MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.75f)
            )

            if (esProxima) {

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                OutlinedButton(
                    onClick = onAgregarCalendario,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),

                    shape = RoundedCornerShape(15.dp)
                ) {

                    Text(
                        text = "Agregar a mi calendario"
                    )
                }

                if (puedeCancelar) {

                    Button(
                        onClick =
                            onSolicitarCancelar,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),

                        shape =
                            RoundedCornerShape(15.dp)
                    ) {

                        Text("Cancelar cita")
                    }

                } else {

                    Text(
                        text =
                            "Esta cita ya no puede cancelarse porque faltan menos de 24 horas.",

                        fontSize = 12.sp,

                        color =
                            MaterialTheme.colorScheme.error
                                .copy(alpha = 0.85f)
                    )
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
