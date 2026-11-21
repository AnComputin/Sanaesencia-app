package com.example.sanaesenciaapp.ui.views.terapeuta

import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.EstadoCita
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaDiaScreen(
    terapeutaViewModel: TerapeutaViewModel = viewModel()
) {

    val state by terapeutaViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.terapeuta_agenda_titulo),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
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

                // Encabezado
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 18.dp
                        )
                ) {

                    Text(
                        text = stringResource(R.string.terapeuta_agenda_subtitulo),
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = stringResource(R.string.terapeuta_agenda_instruccion),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                            .copy(alpha = 0.70f)
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    if (state.fechaSeleccionada.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = stringResource(R.string.terapeuta_agenda_fecha, state.fechaSeleccionada),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = 14.dp,
                                    vertical = 10.dp
                                )
                            )
                        }
                    }
                }

                if (state.citas.isEmpty()) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "🌿",
                                fontSize = 42.sp
                            )

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Text(
                                text = stringResource(R.string.terapeuta_agenda_vacia_titulo),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = stringResource(R.string.terapeuta_agenda_vacia_mensaje),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                                    .copy(alpha = 0.65f)
                            )
                        }
                    }

                } else {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),

                        verticalArrangement =
                            Arrangement.spacedBy(14.dp)
                    ) {

                        items(
                            items = state.citas,
                            key = { it.id }
                        ) { cita ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),

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
                                        .padding(20.dp)
                                ) {

                                    // Hora + estado
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),

                                        horizontalArrangement =
                                            Arrangement.SpaceBetween,

                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Text(
                                            text = cita.hora,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color =
                                                MaterialTheme.colorScheme.secondary
                                        )

                                        Surface(
                                            shape =
                                                RoundedCornerShape(50.dp),

                                            color = when (cita.estado) {

                                                EstadoCita.ASISTIDA ->
                                                    MaterialTheme.colorScheme
                                                        .primaryContainer

                                                EstadoCita.INASISTENTE ->
                                                    MaterialTheme.colorScheme
                                                        .errorContainer

                                                EstadoCita.CANCELADA_TERAPEUTA ->
                                                    MaterialTheme.colorScheme
                                                        .surfaceVariant

                                                EstadoCita.PENDIENTE ->
                                                    MaterialTheme.colorScheme
                                                        .secondaryContainer

                                                else ->
                                                    MaterialTheme.colorScheme
                                                        .surfaceVariant
                                            }
                                        ) {

                                            Text(
                                                text = when (cita.estado) {

                                                    EstadoCita.ASISTIDA ->
                                                        stringResource(R.string.terapeuta_estado_asistida)

                                                    EstadoCita.INASISTENTE ->
                                                        stringResource(R.string.terapeuta_estado_inasistente)

                                                    EstadoCita.CANCELADA_TERAPEUTA ->
                                                        stringResource(R.string.terapeuta_estado_cancelada)

                                                    EstadoCita.PENDIENTE ->
                                                        stringResource(R.string.terapeuta_estado_pendiente)

                                                    else ->
                                                        cita.estado.name
                                                },

                                                fontSize = 12.sp,
                                                fontWeight =
                                                    FontWeight.SemiBold,

                                                modifier =
                                                    Modifier.padding(
                                                        horizontal = 10.dp,
                                                        vertical = 6.dp
                                                    )
                                            )
                                        }
                                    }

                                    Spacer(
                                        modifier = Modifier.height(14.dp)
                                    )

                                    Text(
                                        text = stringResource(R.string.terapeuta_label_paciente),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color =
                                            MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        text = cita.paciente,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(
                                        modifier = Modifier.height(12.dp)
                                    )

                                    Text(
                                        text = stringResource(R.string.terapeuta_label_box_atencion),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color =
                                            MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        text = cita.box,
                                        fontSize = 15.sp
                                    )

                                    // Acciones
                                    if (
                                        cita.estado ==
                                        EstadoCita.PENDIENTE
                                    ) {

                                        Spacer(
                                            modifier =
                                                Modifier.height(20.dp)
                                        )

                                        Text(
                                            text = stringResource(R.string.terapeuta_btn_registrar_atencion),
                                            fontSize = 13.sp,
                                            fontWeight =
                                                FontWeight.SemiBold,
                                            color =
                                                MaterialTheme.colorScheme
                                                    .onSurface
                                                    .copy(alpha = 0.65f)
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(10.dp)
                                        )

                                        Button(
                                            onClick = {

                                                terapeutaViewModel
                                                    .actualizarEstadoCita(
                                                        cita.id,
                                                        EstadoCita.ASISTIDA
                                                    )
                                            },

                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp),

                                            shape =
                                                RoundedCornerShape(15.dp)
                                        ) {

                                            Text(
                                                text = stringResource(R.string.terapeuta_btn_marcar_asistida),
                                                fontWeight =
                                                    FontWeight.SemiBold
                                            )
                                        }

                                        Spacer(
                                            modifier =
                                                Modifier.height(8.dp)
                                        )

                                        Row(
                                            modifier =
                                                Modifier.fillMaxWidth(),

                                            horizontalArrangement =
                                                Arrangement.spacedBy(8.dp)
                                        ) {

                                            OutlinedButton(
                                                onClick = {

                                                    terapeutaViewModel
                                                        .actualizarEstadoCita(
                                                            cita.id,
                                                            EstadoCita.INASISTENTE
                                                        )
                                                },

                                                modifier =
                                                    Modifier.weight(1f),

                                                shape =
                                                    RoundedCornerShape(15.dp)
                                            ) {

                                                Text(
                                                    text = stringResource(R.string.terapeuta_estado_inasistente),
                                                    fontSize = 12.sp
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = {

                                                    terapeutaViewModel
                                                        .actualizarEstadoCita(
                                                            cita.id,
                                                            EstadoCita.CANCELADA_TERAPEUTA
                                                        )
                                                },

                                                modifier =
                                                    Modifier.weight(1f),

                                                shape =
                                                    RoundedCornerShape(15.dp),

                                                colors =
                                                    ButtonDefaults
                                                        .outlinedButtonColors(
                                                            contentColor =
                                                                MaterialTheme
                                                                    .colorScheme
                                                                    .error
                                                        )
                                            ) {

                                                Text(
                                                    text = stringResource(R.string.terapeuta_btn_cancelar),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}