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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacientesAsignadosScreen(
    onPacienteClick: (Int) -> Unit,
    terapeutaViewModel: TerapeutaViewModel = viewModel()
) {

    val state by terapeutaViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.terapeuta_pacientes_topbar),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            )
        }
    ) { padding ->

        SanaBackground {

            if (state.pacientes.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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
                            text = stringResource(R.string.terapeuta_pacientes_vacio_titulo),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = stringResource(R.string.terapeuta_pacientes_vacio_mensaje),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                                .copy(alpha = 0.65f)
                        )
                    }
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),

                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp,
                        vertical = 20.dp
                    ),

                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    item {

                        Text(
                            text = stringResource(R.string.terapeuta_pacientes_seguimiento_titulo),
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = stringResource(R.string.terapeuta_pacientes_seguimiento_desc),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                                .copy(alpha = 0.70f)
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }

                    items(
                        items = state.pacientes,
                        key = { it.id }
                    ) { paciente ->

                        val enRiesgo =
                            terapeutaViewModel.estaEnRiesgo(paciente)

                        Card(
                            onClick = {
                                onPacienteClick(paciente.id)
                            },

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

                                Row(
                                    modifier = Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = paciente.nombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp,
                                        color =
                                            MaterialTheme.colorScheme.secondary,

                                        modifier = Modifier.weight(1f)
                                    )

                                    Surface(
                                        shape =
                                            RoundedCornerShape(50.dp),

                                        color =
                                            if (enRiesgo)
                                                MaterialTheme.colorScheme
                                                    .errorContainer
                                            else
                                                MaterialTheme.colorScheme
                                                    .primaryContainer
                                    ) {

                                        Text(
                                            text =
                                                if (enRiesgo)
                                                    stringResource(R.string.terapeuta_estado_en_riesgo)
                                                else
                                                    stringResource(R.string.terapeuta_estado_seguimiento_activo),

                                            fontSize = 11.sp,
                                            fontWeight =
                                                FontWeight.SemiBold,

                                            color =
                                                if (enRiesgo)
                                                    MaterialTheme.colorScheme
                                                        .onErrorContainer
                                                else
                                                    MaterialTheme.colorScheme
                                                        .onPrimaryContainer,

                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 6.dp
                                            )
                                        )
                                    }
                                }

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
                                    text = paciente.rut,
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
                                    text = paciente.ultimaAtencion,
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
                                    text = paciente.boxAsignado,
                                    fontSize = 15.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(14.dp)
                                )

                                Text(
                                    text =
                                        if (enRiesgo)
                                            stringResource(R.string.terapeuta_estado_requiere_revision)
                                        else
                                            stringResource(R.string.terapeuta_estado_tocar_seguimiento),

                                    fontSize = 13.sp,

                                    color =
                                        if (enRiesgo)
                                            MaterialTheme.colorScheme.error
                                        else
                                            MaterialTheme.colorScheme
                                                .onSurface
                                                .copy(alpha = 0.60f)
                                )
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