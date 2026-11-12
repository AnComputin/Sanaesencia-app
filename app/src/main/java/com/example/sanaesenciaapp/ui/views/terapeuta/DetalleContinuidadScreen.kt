package com.example.sanaesenciaapp.ui.views.terapeuta

import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.EstadoCita
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleContinuidadScreen(
    onNavigateBack: () -> Unit,
    terapeutaViewModel: TerapeutaViewModel = viewModel()
) {

    val state by terapeutaViewModel.uiState.collectAsState()
    val paciente = state.pacienteSeleccionado

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.terapeuta_continuidad_topbar),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },

                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.terapeuta_accessibility_volver)
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
                    .padding(padding),

                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp,
                        vertical = 20.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                // Encabezado
                item {

                    Text(
                        text = stringResource(R.string.terapeuta_continuidad_titulo),
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = stringResource(R.string.terapeuta_continuidad_desc),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                            .copy(alpha = 0.70f)
                    )
                }

                // Ficha del paciente
                item {

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
                            modifier = Modifier.padding(20.dp)
                        ) {

                            Text(
                                text =
                                    paciente?.nombre
                                        ?: stringResource(R.string.terapeuta_paciente_no_seleccionado),

                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color =
                                    MaterialTheme.colorScheme.secondary
                            )

                            Spacer(
                                modifier = Modifier.height(16.dp)
                            )

                            Text(
                                text = stringResource(R.string.terapeuta_label_rut),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color =
                                    MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = paciente?.rut ?: "-",
                                fontSize = 15.sp
                            )

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Text(
                                text = stringResource(R.string.terapeuta_label_ultima_atencion),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color =
                                    MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text =
                                    paciente?.ultimaAtencion ?: "-",
                                fontSize = 15.sp
                            )

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Text(
                                text = stringResource(R.string.terapeuta_label_box_asignado),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color =
                                    MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text =
                                    paciente?.boxAsignado ?: "-",
                                fontSize = 15.sp
                            )

                            if (
                                paciente != null &&
                                terapeutaViewModel.estaEnRiesgo(
                                    paciente
                                )
                            ) {

                                Spacer(
                                    modifier =
                                        Modifier.height(16.dp)
                                )

                                Surface(
                                    shape =
                                        RoundedCornerShape(14.dp),

                                    color =
                                        MaterialTheme.colorScheme
                                            .errorContainer
                                ) {

                                    Column(
                                        modifier =
                                            Modifier.padding(14.dp)
                                    ) {

                                        Text(
                                            text =
                                                stringResource(R.string.terapeuta_seguimiento_requerido),
                                            fontWeight =
                                                FontWeight.Bold,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onErrorContainer
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(4.dp)
                                        )

                                        Text(
                                            text =
                                                stringResource(R.string.terapeuta_riesgo_discontinuidad),
                                            fontSize = 13.sp,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Notas de evolución
                item {

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
                            modifier = Modifier.padding(20.dp)
                        ) {

                            Text(
                                text = stringResource(R.string.terapeuta_notas_evolucion),
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color =
                                    MaterialTheme.colorScheme.secondary
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = stringResource(R.string.terapeuta_notas_desc),
                                fontSize = 13.sp,
                                color =
                                    MaterialTheme.colorScheme.onSurface
                                        .copy(alpha = 0.65f)
                            )

                            Spacer(
                                modifier = Modifier.height(14.dp)
                            )

                            OutlinedTextField(
                                value = state.notasEvolucion,

                                onValueChange = {
                                    terapeutaViewModel
                                        .onNotasEvolucionChange(it)
                                },

                                label = {
                                    Text(
                                        stringResource(R.string.terapeuta_notas_label)
                                    )
                                },

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),

                                maxLines = 6,

                                shape =
                                    RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }

                // Historial
                item {

                    Text(
                        text = stringResource(R.string.terapeuta_historial_titulo),
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    Text(
                        text = stringResource(R.string.terapeuta_historial_desc),
                        fontSize = 13.sp,
                        color =
                            MaterialTheme.colorScheme.onBackground
                                .copy(alpha = 0.65f)
                    )
                }

                if (state.historialCitas.isEmpty()) {

                    item {

                        Card(
                            modifier = Modifier.fillMaxWidth(),

                            shape =
                                RoundedCornerShape(20.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme
                                            .colorScheme
                                            .surface
                                )
                        ) {

                            Text(
                                text =
                                    stringResource(R.string.terapeuta_historial_vacio),

                                modifier =
                                    Modifier.padding(20.dp),

                                color =
                                    MaterialTheme.colorScheme
                                        .onSurface
                                        .copy(alpha = 0.65f)
                            )
                        }
                    }

                } else {

                    items(
                        items = state.historialCitas,
                        key = { it.id }
                    ) { cita ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),

                            shape =
                                RoundedCornerShape(20.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme
                                            .colorScheme
                                            .surface
                                ),

                            elevation =
                                CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                )
                        ) {

                            Column(
                                modifier =
                                    Modifier.padding(18.dp)
                            ) {

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = cita.hora,
                                        fontSize = 20.sp,
                                        fontWeight =
                                            FontWeight.Bold,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .secondary
                                    )

                                    Surface(
                                        shape =
                                            RoundedCornerShape(
                                                50.dp
                                            ),

                                        color =
                                            when (cita.estado) {

                                                EstadoCita.ASISTIDA ->
                                                    MaterialTheme
                                                        .colorScheme
                                                        .primaryContainer

                                                EstadoCita.INASISTENTE ->
                                                    MaterialTheme
                                                        .colorScheme
                                                        .errorContainer

                                                EstadoCita.CANCELADA_TERAPEUTA ->
                                                    MaterialTheme
                                                        .colorScheme
                                                        .surfaceVariant

                                                EstadoCita.PENDIENTE ->
                                                    MaterialTheme
                                                        .colorScheme
                                                        .secondaryContainer

                                                else ->
                                                    MaterialTheme
                                                        .colorScheme
                                                        .surfaceVariant
                                            }
                                    ) {

                                        Text(
                                            text =
                                                when (cita.estado) {

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

                                            fontSize = 11.sp,

                                            fontWeight =
                                                FontWeight.SemiBold,

                                            modifier =
                                                Modifier.padding(
                                                    horizontal = 9.dp,
                                                    vertical = 5.dp
                                                )
                                        )
                                    }
                                }

                                Spacer(
                                    modifier =
                                        Modifier.height(14.dp)
                                )

                                Text(
                                    text = stringResource(R.string.terapeuta_label_fecha),
                                    fontSize = 12.sp,
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                )

                                Text(
                                    text =
                                        state.fechaSeleccionada,
                                    fontSize = 15.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)
                                )

                                Text(
                                    text = stringResource(R.string.terapeuta_label_box),
                                    fontSize = 12.sp,
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                )

                                Text(
                                    text = cita.box,
                                    fontSize = 15.sp
                                )
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