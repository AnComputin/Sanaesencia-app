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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.ui.components.SanaBackground
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
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reservar cita",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },

                navigationIcon = {
                    IconButton(
                        onClick = onVolver
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        SanaBackground {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 20.dp,
                        vertical = 18.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {

                // Encabezado
                Column {

                    Text(
                        text = "Agenda tu atención",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "Selecciona modalidad, fecha y horario.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                            .copy(alpha = 0.7f)
                    )
                }

                // Resumen del servicio
                Card(
                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(22.dp),

                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = "Tu atención",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color =
                                MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text =
                                if (uiState.especialidad.isNotBlank())
                                    uiState.especialidad
                                else
                                    "Especialidad",

                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme.secondary
                        )

                        Text(
                            text =
                                if (uiState.profesional.isNotBlank())
                                    uiState.profesional
                                else
                                    "Profesional por definir",

                            fontSize = 15.sp,
                            color =
                                MaterialTheme.colorScheme
                                    .onPrimaryContainer
                        )
                    }
                }

                // Modalidad
                SeccionTarjeta(
                    titulo = "Modalidad de atención",
                    subtitulo = "Elige cómo deseas recibir tu atención."
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        if (
                            uiState.modalidadPermitida.equals(
                                "Presencial",
                                ignoreCase = true
                            ) ||
                            uiState.modalidadPermitida.equals(
                                "Ambas",
                                ignoreCase = true
                            )
                        ) {

                            ModalidadTarjeta(
                                titulo = "Presencial",

                                isSelected =
                                    uiState.modalidad ==
                                            Modalidad.PRESENCIAL,

                                onClick = {
                                    citaViewModel
                                        .seleccionarModalidad(
                                            Modalidad.PRESENCIAL
                                        )
                                },

                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (
                            uiState.modalidadPermitida.equals(
                                "Online",
                                ignoreCase = true
                            ) ||
                            uiState.modalidadPermitida.equals(
                                "Ambas",
                                ignoreCase = true
                            )
                        ) {

                            ModalidadTarjeta(
                                titulo = "Online",

                                isSelected =
                                    uiState.modalidad ==
                                            Modalidad.ONLINE,

                                onClick = {
                                    citaViewModel
                                        .seleccionarModalidad(
                                            Modalidad.ONLINE
                                        )
                                },

                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Fecha
                SeccionTarjeta(
                    titulo = "Seleccionar fecha",
                    subtitulo = "Elige uno de los próximos días disponibles."
                ) {

                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(9.dp),

                        contentPadding =
                            PaddingValues(vertical = 4.dp)
                    ) {

                        items(
                            citaViewModel.fechasDisponibles
                        ) { fechaMillis ->

                            val esSeleccionado =
                                uiState.fechaMillis ==
                                        fechaMillis

                            FechaTarjeta(
                                textoFecha =
                                    citaViewModel
                                        .formatearFecha(
                                            fechaMillis
                                        ),

                                isSelected =
                                    esSeleccionado,

                                onClick = {
                                    citaViewModel
                                        .seleccionarFecha(
                                            fechaMillis
                                        )
                                }
                            )
                        }
                    }
                }

                // Horario
                SeccionTarjeta(
                    titulo = "Seleccionar hora",
                    subtitulo = "Los horarios deshabilitados ya no están disponibles."
                ) {

                    val filasHorarios =
                        horarios.chunked(3)

                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(9.dp)
                    ) {

                        filasHorarios.forEach { fila ->

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {

                                fila.forEach { horario ->

                                    HoraTarjeta(
                                        horario = horario,

                                        isSelected =
                                            uiState.hora ==
                                                    horario.hora,

                                        onClick = {
                                            citaViewModel
                                                .seleccionarHora(
                                                    horario.hora
                                                )
                                        },

                                        modifier =
                                            Modifier.weight(1f)
                                    )
                                }

                                repeat(
                                    3 - fila.size
                                ) {
                                    Spacer(
                                        modifier =
                                            Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Paciente
                SeccionTarjeta(
                    titulo = "Datos del paciente",
                    subtitulo = "Indica quién recibirá la atención."
                ) {

                    OutlinedTextField(
                        value =
                            uiState.nombrePaciente,

                        onValueChange = {
                            citaViewModel
                                .cambiarNombre(it)
                        },

                        label = {
                            Text(
                                "Nombre del paciente"
                            )
                        },

                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Default.Person,

                                contentDescription = null
                            )
                        },

                        isError =
                            uiState.errorNombre != null,

                        supportingText =
                            if (
                                uiState.errorNombre != null
                            ) {
                                {
                                    Text(
                                        uiState.errorNombre!!
                                    )
                                }
                            } else {
                                null
                            },

                        singleLine = true,

                        shape =
                            RoundedCornerShape(16.dp),

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = onContinuar,

                    enabled = puedeContinuar,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    shape =
                        RoundedCornerShape(18.dp)
                ) {

                    Text(
                        text = "Continuar al pago",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Podrás revisar todos los datos antes de confirmar.",
                    fontSize = 12.sp,

                    color =
                        MaterialTheme.colorScheme
                            .onBackground
                            .copy(alpha = 0.6f),

                    modifier =
                        Modifier.align(
                            Alignment.CenterHorizontally
                        )
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )
            }
        }
    }
}


@Composable
private fun SeccionTarjeta(
    titulo: String,
    subtitulo: String,
    contenido: @Composable () -> Unit
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
            modifier = Modifier.padding(20.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Column {

                Text(
                    text = titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.secondary
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = subtitulo,
                    fontSize = 12.sp,

                    color =
                        MaterialTheme.colorScheme
                            .onSurface
                            .copy(alpha = 0.62f)
                )
            }

            contenido()
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

    val backgroundColor by
    animateColorAsState(
        targetValue =
            if (isSelected)
                MaterialTheme.colorScheme
                    .primaryContainer
            else
                MaterialTheme.colorScheme
                    .surfaceVariant,

        label = "ModalidadBackgroundColor"
    )

    val contentColor by
    animateColorAsState(
        targetValue =
            if (isSelected)
                MaterialTheme.colorScheme
                    .onPrimaryContainer
            else
                MaterialTheme.colorScheme
                    .onSurfaceVariant,

        label = "ModalidadContentColor"
    )

    Card(
        onClick = onClick,

        shape =
            RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),

        modifier = modifier
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    if (titulo == "Presencial")
                        "📍"
                    else
                        "💻",

                fontSize = 22.sp
            )

            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )

            Text(
                text = titulo,
                fontSize = 14.sp,

                fontWeight =
                    if (isSelected)
                        FontWeight.Bold
                    else
                        FontWeight.Medium
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

    val backgroundColor by
    animateColorAsState(
        targetValue =
            if (isSelected)
                MaterialTheme.colorScheme
                    .primaryContainer
            else
                MaterialTheme.colorScheme
                    .surfaceVariant,

        label = "FechaBackgroundColor"
    )

    val contentColor by
    animateColorAsState(
        targetValue =
            if (isSelected)
                MaterialTheme.colorScheme
                    .onPrimaryContainer
            else
                MaterialTheme.colorScheme
                    .onSurfaceVariant,

        label = "FechaContentColor"
    )

    Card(
        onClick = onClick,

        shape =
            RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(
            containerColor =
                backgroundColor,
            contentColor =
                contentColor
        )
    ) {

        Box(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 13.dp
            ),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = textoFecha,

                fontSize = 13.sp,

                fontWeight =
                    if (isSelected)
                        FontWeight.Bold
                    else
                        FontWeight.Medium
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

    val backgroundColor by
    animateColorAsState(
        targetValue = when {

            !horario.disponible ->
                MaterialTheme.colorScheme
                    .surfaceVariant
                    .copy(alpha = 0.45f)

            isSelected ->
                MaterialTheme.colorScheme
                    .primaryContainer

            else ->
                MaterialTheme.colorScheme
                    .surfaceVariant
        },

        label = "HoraBackgroundColor"
    )

    val contentColor by
    animateColorAsState(
        targetValue = when {

            !horario.disponible ->
                MaterialTheme.colorScheme
                    .onSurface
                    .copy(alpha = 0.35f)

            isSelected ->
                MaterialTheme.colorScheme
                    .onPrimaryContainer

            else ->
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        },

        label = "HoraContentColor"
    )

    Card(
        onClick = onClick,

        enabled =
            horario.disponible,

        shape =
            RoundedCornerShape(14.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    backgroundColor,

                contentColor =
                    contentColor,

                disabledContainerColor =
                    backgroundColor,

                disabledContentColor =
                    contentColor
            ),

        modifier = modifier
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 13.dp
                ),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = horario.hora,

                fontSize = 14.sp,

                fontWeight =
                    if (isSelected)
                        FontWeight.Bold
                    else
                        FontWeight.Medium
            )
        }
    }
}


@Preview(
    showBackground = true
)
@Composable
private fun AgendarScreenPreview() {

    AgendarScreen(
        citaViewModel =
            CitaViewModel(),

        onContinuar = {},
        onVolver = {}
    )
}