package com.example.sanaesenciaapp.ui.views

import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sanaesenciaapp.data.remote.ClimaRepository
import com.example.sanaesenciaapp.data.remote.ClimaSede
import com.example.sanaesenciaapp.ui.components.SanaButton
import com.example.sanaesenciaapp.ui.components.SanaCard
import com.example.sanaesenciaapp.utils.ResultadoUbicacion
import com.example.sanaesenciaapp.utils.UbicacionHelper
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbicacionScreen(
    onBackClick: () -> Unit = {},
    climaRepository: ClimaRepository = remember { ClimaRepository() }
) {
    val context = LocalContext.current
    val whatsappMessage = stringResource(R.string.ubicacion_whatsapp_mensaje)
    val scope = rememberCoroutineScope()
    var ubicacionActual by remember { mutableStateOf<ResultadoUbicacion?>(null) }
    var cargandoUbicacion by remember { mutableStateOf(false) }
    var mensajePermisoRechazado by remember { mutableStateOf(false) }
    var mensajeErrorGps by remember { mutableStateOf<String?>(null) }

    var climaSede by remember { mutableStateOf<ClimaSede?>(null) }
    var cargandoClima by remember { mutableStateOf(true) }
    var errorClima by remember { mutableStateOf<String?>(null) }

    fun cargarClima() {
        cargandoClima = true
        errorClima = null
        scope.launch {
            climaRepository.obtenerClimaSede()
                .onSuccess {
                    climaSede = it
                    cargandoClima = false
                }
                .onFailure {
                    errorClima = "No se pudo consultar el clima en línea. Verifica tu conexión."
                    cargandoClima = false
                }
        }
    }

    LaunchedEffect(Unit) {
        cargarClima()
    }

    fun consultarUbicacion() {
        cargandoUbicacion = true
        mensajeErrorGps = null
        scope.launch {
            val resultado = UbicacionHelper.obtenerUbicacionActual(context)
            cargandoUbicacion = false
            if (resultado != null) {
                ubicacionActual = resultado
            } else {
                mensajeErrorGps = "No se pudo obtener la ubicación del dispositivo. Verifica que el GPS esté activado e intenta nuevamente."
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val fineGranted = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            mensajePermisoRechazado = false
            consultarUbicacion()
        } else {
            mensajePermisoRechazado = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.inicio_titulo_sede),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(
                            text = stringResource(R.string.inicio_volver),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📍", fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.inicio_sede_nombre),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(R.string.ubicacion_direccion),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                SanaCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when {
                            cargandoUbicacion -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Obteniendo ubicación GPS...",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            ubicacionActual != null -> {
                                val u = ubicacionActual!!
                                Text(
                                    text = stringResource(R.string.ubicacion_distancia, u.distanciaKm),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "Coordenadas: %.4f, %.4f",
                                        u.latitud,
                                        u.longitud
                                    ),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                if (!u.direccionAproximada.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tu ubicación: ${u.direccionAproximada}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                if (u.distanciaKm > 30.0f) {
                                    Text(
                                        text = stringResource(R.string.inicio_sede_lejos),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.inicio_sede_cerca),
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            else -> {
                                Text(
                                    text = stringResource(R.string.inicio_sede_instruccion),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        if (mensajePermisoRechazado) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.inicio_sede_permiso_denegado),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        mensajeErrorGps?.let { err ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SanaCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Clima en Sede Los Vilos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            TextButton(
                                onClick = { cargarClima() },
                                enabled = !cargandoClima
                            ) {
                                Text("Actualizar", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when {
                            cargandoClima -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Consultando clima actual (Open-Meteo)...",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }

                            climaSede != null -> {
                                val c = climaSede!!
                                Text(
                                    text = "${c.icono} ${String.format(Locale.US, "%.1f", c.temperaturaC)} °C · ${c.descripcion}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "Sensación %.1f °C · Humedad %d%% · Viento %.0f km/h",
                                        c.sensacionTermicaC,
                                        c.humedadPorcentaje,
                                        c.vientoKmh
                                    ),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = c.recomendacionBienestar,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )
                            }

                            else -> {
                                Text(
                                    text = errorClima ?: "Clima no disponible en este momento.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(onClick = { cargarClima() }) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column {
                // 1. Botón GPS para calcular la distancia
                SanaButton(
                    text = stringResource(R.string.inicio_btn_distancia_gps),
                    enabled = !cargandoUbicacion,
                    onClick = {
                        val fineGranted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val coarseGranted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (fineGranted || coarseGranted) {
                            mensajePermisoRechazado = false
                            consultarUbicacion()
                        } else {
                            launcher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Botón para abrir Google Maps de forma nativa (con respaldo web)
                Button(
                    onClick = {
                        val mapUri = Uri.parse(
                            "geo:${UbicacionHelper.LAT_CENTRO},${UbicacionHelper.LON_CENTRO}?q=Caupolic%C3%A1n+220,+Los+Vilos,+Coquimbo,+Chile"
                        )
                        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (_: ActivityNotFoundException) {
                            val webUri = Uri.parse(
                                "https://www.google.com/maps/search/?api=1&query=${UbicacionHelper.LAT_CENTRO},${UbicacionHelper.LON_CENTRO}"
                            )
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, "No se encontró una app de mapas", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.inicio_btn_google_maps),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Botón de WhatsApp
                OutlinedButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://wa.me/56912345678")
                                .buildUpon()
                                .appendQueryParameter("text", whatsappMessage)
                                .build()
                        )
                        try {
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(context, "No se encontró una aplicación para abrir WhatsApp", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ubicacion_btn_whatsapp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}