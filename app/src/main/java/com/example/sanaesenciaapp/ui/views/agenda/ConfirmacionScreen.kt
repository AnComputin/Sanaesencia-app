package com.example.sanaesenciaapp.ui.views.agenda

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.utils.CalendarioHelper
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.viewmodel.Modalidad
import kotlinx.coroutines.launch

@Composable
fun ConfirmacionScreen(
    citaViewModel: CitaViewModel,
    onVerMisCitas: () -> Unit
) {

    val ultimaCita by citaViewModel.ultimaCita.collectAsState()

    val snackbarHostState =
        remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var startAnimation by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val scale by animateFloatAsState(
        targetValue =
            if (startAnimation) 1f else 0f,

        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),

        label = "EscalaIconoConfirmacion"
    )

    Scaffold(
        containerColor =
            MaterialTheme.colorScheme.background,

        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }
    ) { paddingValues ->

        SanaBackground {

            val cita = ultimaCita

            if (cita == null) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Card(
                        shape =
                            RoundedCornerShape(22.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .surface
                            ),

                        modifier =
                            Modifier.padding(24.dp)
                    ) {

                        Text(
                            text =
                                "No hay una cita para mostrar.",

                            fontSize = 16.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,

                            modifier =
                                Modifier.padding(24.dp)
                        )
                    }
                }

            } else {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(
                            horizontal = 20.dp,
                            vertical = 30.dp
                        ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.spacedBy(18.dp)
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    // Confirmación animada
                    Surface(
                        shape =
                            RoundedCornerShape(50.dp),

                        color =
                            MaterialTheme
                                .colorScheme
                                .primaryContainer
                    ) {

                        Box(
                            modifier =
                                Modifier.padding(18.dp),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.CheckCircle,

                                contentDescription =
                                    "Cita confirmada",

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary,

                                modifier = Modifier
                                    .size(82.dp)
                                    .scale(scale)
                            )
                        }
                    }

                    Text(
                        text = "¡Cita confirmada!",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color =
                            MaterialTheme
                                .colorScheme
                                .secondary,

                        textAlign = TextAlign.Center
                    )

                    Text(
                        text =
                            "Tu hora fue reservada correctamente.",

                        fontSize = 15.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onBackground
                                .copy(alpha = 0.70f),

                        textAlign = TextAlign.Center
                    )

                    // Resumen de la cita
                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(22.dp),

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
                                Modifier.padding(20.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(
                                    13.dp
                                )
                        ) {

                            Text(
                                text =
                                    "Resumen de tu cita",

                                fontSize = 19.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .secondary
                            )

                            DatoConfirmacion(
                                titulo =
                                    "Especialidad",

                                valor =
                                    cita.especialidad
                            )

                            DatoConfirmacion(
                                titulo =
                                    "Profesional",

                                valor =
                                    cita.profesional
                            )

                            DatoConfirmacion(
                                titulo =
                                    "Fecha",

                                valor =
                                    citaViewModel
                                        .formatearFecha(
                                            cita.inicioMillis
                                        )
                            )

                            DatoConfirmacion(
                                titulo = "Hora",
                                valor = cita.hora
                            )

                            DatoConfirmacion(
                                titulo =
                                    "Paciente",

                                valor =
                                    cita.paciente
                            )

                            if (cita.modalidad == Modalidad.PRESENCIAL && cita.boxAsignado != null) {
                                DatoConfirmacion(
                                    titulo = "Box asignado",
                                    valor = "Box ${cita.boxAsignado}"
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(2.dp)
                            )

                            val modalidadTexto =
                                when (
                                    cita.modalidad
                                ) {

                                    Modalidad.PRESENCIAL ->
                                        "Presencial"

                                    Modalidad.ONLINE ->
                                        "Online"
                                }

                            Surface(
                                shape =
                                    RoundedCornerShape(
                                        14.dp
                                    ),

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .primaryContainer
                            ) {

                                Text(
                                    text =
                                        if (
                                            cita.modalidad ==
                                            Modalidad.PRESENCIAL
                                        )
                                            "📍 $modalidadTexto${cita.boxAsignado?.let { " · Box $it" } ?: ""}"
                                        else
                                            "💻 $modalidadTexto",

                                    fontSize = 13.sp,

                                    fontWeight =
                                        FontWeight
                                            .SemiBold,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onPrimaryContainer,

                                    modifier =
                                        Modifier.padding(
                                            horizontal =
                                                12.dp,
                                            vertical =
                                                8.dp
                                        )
                                )
                            }
                        }
                    }

                    // Mensaje informativo
                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(18.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .primaryContainer
                            )
                    ) {

                        Text(
                            text =
                                "Tu cita ya está registrada. Puedes agregarla a tu calendario para recordarla.",

                            fontSize = 13.sp,

                            textAlign =
                                TextAlign.Center,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onPrimaryContainer,

                            modifier =
                                Modifier.padding(16.dp)
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(2.dp)
                    )

                    OutlinedButton(
                        onClick = {

                            val exito =
                                CalendarioHelper
                                    .agregarACalendario(
                                        context =
                                            context,

                                        titulo =
                                            "Cita: ${cita.especialidad}",

                                        descripcion =
                                            "Cita médica de ${cita.especialidad} con ${cita.profesional}. Paciente: ${cita.paciente}.",

                                        inicioMillis =
                                            cita.inicioMillis,

                                        ubicacion =
                                            if (cita.modalidad == Modalidad.PRESENCIAL && cita.boxAsignado != null)
                                                "Presencial · Box ${cita.boxAsignado}"
                                            else
                                                "Modalidad: ${cita.modalidad.name}"
                                    )

                            if (!exito) {

                                scope.launch {

                                    snackbarHostState
                                        .showSnackbar(
                                            "No hay una app de calendario"
                                        )
                                }
                            }
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),

                        shape =
                            RoundedCornerShape(17.dp)
                    ) {

                        Text(
                            text =
                                "Agregar a mi calendario",

                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick =
                            onVerMisCitas,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),

                        shape =
                            RoundedCornerShape(18.dp)
                    ) {

                        Text(
                            text =
                                "Ver mis citas",

                            fontSize = 16.sp,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(20.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun DatoConfirmacion(
    titulo: String,
    valor: String
) {

    Column {

        Text(
            text = titulo,
            fontSize = 12.sp,
            fontWeight =
                FontWeight.SemiBold,
            color =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Spacer(
            modifier =
                Modifier.height(2.dp)
        )

        Text(
            text = valor,
            fontSize = 15.sp,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun ConfirmacionScreenPreview() {

    val viewModel =
        remember {
            CitaViewModel()
        }

    ConfirmacionScreen(
        citaViewModel =
            viewModel,

        onVerMisCitas = {}
    )
}