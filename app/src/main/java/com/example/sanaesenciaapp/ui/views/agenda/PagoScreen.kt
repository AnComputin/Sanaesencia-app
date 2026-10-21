package com.example.sanaesenciaapp.ui.views.agenda

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.viewmodel.Modalidad
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun PagoScreen(
    citaViewModel: CitaViewModel,
    usuarioEmail: String = "",
    onPagoExitoso: () -> Unit,
    onExpirado: () -> Unit
) {

    val uiState by citaViewModel.uiState.collectAsState()
    val puedeConfirmar by citaViewModel.puedeContinuar.collectAsState()

    LaunchedEffect(Unit) {
        citaViewModel.iniciarPago()
    }

    var tiempoRestante by remember {
        mutableIntStateOf(
            citaViewModel.segundosRestantesPago()
        )
    }

    var estaExpirado by remember {
        mutableStateOf(false)
    }

    var procesandoPago by remember {
        mutableStateOf(false)
    }

    var errorReserva by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(tiempoRestante) {

        if (tiempoRestante > 0) {

            delay(1000L)

            val nuevoTiempo =
                citaViewModel.segundosRestantesPago()

            tiempoRestante = nuevoTiempo

            if (nuevoTiempo <= 0) {
                estaExpirado = true
            }

        } else {
            estaExpirado = true
        }
    }

    val progreso =
        (
                tiempoRestante.toFloat() /
                        citaViewModel.duracionPagoSegundos.toFloat()
                ).coerceIn(0f, 1f)

    val progresoAnimado by animateFloatAsState(
        targetValue = progreso,
        label = "ProgresoTiempoPago"
    )

    val minutos = tiempoRestante / 60
    val segundos = tiempoRestante % 60

    val tiempoTexto = String.format(
        Locale.getDefault(),
        "%02d:%02d",
        minutos,
        segundos
    )

    val colorBarraProgreso =
        if (tiempoRestante < 60) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        }

    val modalidadTexto =
        when (uiState.modalidad) {

            Modalidad.PRESENCIAL ->
                "Presencial"

            Modalidad.ONLINE ->
                "Online"

            null ->
                "-"
        }

    val fechaTexto =
        uiState.fechaMillis?.let {
            citaViewModel.formatearFecha(it)
        } ?: "-"

    SanaBackground {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 20.dp,
                    vertical = 26.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {

            // Encabezado
            Column {

                Text(
                    text = "Confirma tu reserva",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.secondary
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = "Revisa los datos antes de realizar el pago.",
                    fontSize = 14.sp,
                    color =
                        MaterialTheme.colorScheme
                            .onBackground
                            .copy(alpha = 0.70f)
                )
            }

            // Resumen
            Card(
                modifier = Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
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
                        Arrangement.spacedBy(13.dp)
                ) {

                    Text(
                        text = "Resumen de la cita",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color =
                            MaterialTheme.colorScheme.secondary
                    )

                    DatoPago(
                        titulo = "Especialidad",
                        valor =
                            if (
                                uiState.especialidad
                                    .isNotBlank()
                            )
                                uiState.especialidad
                            else
                                "-"
                    )

                    DatoPago(
                        titulo = "Profesional",
                        valor =
                            if (
                                uiState.profesional
                                    .isNotBlank()
                            )
                                uiState.profesional
                            else
                                "-"
                    )

                    DatoPago(
                        titulo = "Fecha",
                        valor = fechaTexto
                    )

                    DatoPago(
                        titulo = "Hora",
                        valor =
                            uiState.hora ?: "-"
                    )

                    DatoPago(
                        titulo = "Paciente",
                        valor =
                            if (
                                uiState.nombrePaciente
                                    .isNotBlank()
                            )
                                uiState.nombrePaciente
                            else
                                "-"
                    )

                    Spacer(
                        modifier =
                            Modifier.height(2.dp)
                    )

                    Surface(
                        shape =
                            RoundedCornerShape(14.dp),

                        color =
                            MaterialTheme.colorScheme
                                .primaryContainer
                    ) {

                        Text(
                            text =
                                if (
                                    uiState.modalidad ==
                                    Modalidad.PRESENCIAL
                                )
                                    "📍 $modalidadTexto"
                                else
                                    "💻 $modalidadTexto",

                            fontSize = 13.sp,
                            fontWeight =
                                FontWeight.SemiBold,

                            color =
                                MaterialTheme.colorScheme
                                    .onPrimaryContainer,

                            modifier =
                                Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                )
                        )
                    }
                }
            }

            // Monto
            Card(
                modifier = Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(24.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme
                                .primaryContainer
                    )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Total a pagar",
                        fontSize = 14.sp,
                        fontWeight =
                            FontWeight.SemiBold,
                        color =
                            MaterialTheme.colorScheme
                                .onPrimaryContainer
                                .copy(alpha = 0.75f)
                    )

                    Spacer(
                        modifier =
                            Modifier.height(5.dp)
                    )

                    Text(
                        text = "$35.000",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color =
                            MaterialTheme.colorScheme
                                .secondary
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text = "Pago simulado",
                        fontSize = 12.sp,
                        color =
                            MaterialTheme.colorScheme
                                .onPrimaryContainer
                                .copy(alpha = 0.60f)
                    )
                }
            }

            // Tiempo de pago
            Card(
                modifier = Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
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
                        Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Tiempo para completar el pago",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color =
                            MaterialTheme.colorScheme.secondary
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text =
                                if (tiempoRestante < 60)
                                    "El tiempo está por finalizar"
                                else
                                    "Reserva temporal de la hora",

                            fontSize = 13.sp,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurface
                                    .copy(alpha = 0.65f)
                        )

                        Text(
                            text = tiempoTexto,
                            fontSize = 21.sp,
                            fontWeight =
                                FontWeight.Bold,

                            color =
                                if (
                                    tiempoRestante < 60
                                )
                                    MaterialTheme
                                        .colorScheme
                                        .error
                                else
                                    MaterialTheme
                                        .colorScheme
                                        .secondary
                        )
                    }

                    LinearProgressIndicator(
                        progress = {
                            progresoAnimado
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        color =
                            colorBarraProgreso,

                        trackColor =
                            MaterialTheme.colorScheme
                                .surfaceVariant
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Button(
                onClick = {
                    procesandoPago = true
                    errorReserva = null

                    citaViewModel.confirmarCita(usuarioEmail) { exito ->
                        procesandoPago = false

                        if (exito) {
                            onPagoExitoso()
                        } else {
                            errorReserva =
                                "La hora dejó de estar disponible. Vuelve a seleccionar un horario."
                        }
                    }
                },

                enabled =
                    !estaExpirado &&
                            tiempoRestante > 0 &&
                            !procesandoPago &&
                            usuarioEmail.isNotBlank() &&
                            puedeConfirmar,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                shape =
                    RoundedCornerShape(18.dp)
            ) {

                Text(
                    text = if (procesandoPago) "Confirmando reserva..." else "Pagar $35.000",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            errorReserva?.let { mensaje ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Text(
                text = "Este pago es una simulación utilizada para el prototipo.",
                fontSize = 12.sp,
                color =
                    MaterialTheme.colorScheme
                        .onBackground
                        .copy(alpha = 0.55f),

                modifier =
                    Modifier.align(
                        Alignment.CenterHorizontally
                    )
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }

    // Tiempo agotado
    if (estaExpirado) {

        AlertDialog(
            onDismissRequest = {},

            title = {
                Text(
                    text = "Tiempo agotado",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    "El tiempo para realizar el pago ha expirado. La hora deberá seleccionarse nuevamente."
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        citaViewModel
                            .reiniciarPago()

                        onExpirado()
                    }
                ) {

                    Text("Aceptar")
                }
            }
        )
    }
}


@Composable
private fun DatoPago(
    titulo: String,
    valor: String
) {

    Column {

        Text(
            text = titulo,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color =
                MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = valor,
            fontSize = 15.sp,
            color =
                MaterialTheme.colorScheme.onSurface
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun PagoScreenPreview() {

    PagoScreen(
        citaViewModel = CitaViewModel(),
        onPagoExitoso = {},
        onExpirado = {}
    )
}