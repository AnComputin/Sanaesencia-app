package com.example.sanaesenciaapp.ui.views

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Context
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.ui.components.SanaButton
import com.example.sanaesenciaapp.ui.components.SanaCard
import com.example.sanaesenciaapp.utils.UbicacionHelper
import kotlinx.coroutines.launch

@Composable
fun UbicacionScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var distanciaKm by remember { mutableStateOf<Float?>(null) }
    var mensajePermisoRechazado by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            mensajePermisoRechazado = false
            scope.launch {
                distanciaKm = UbicacionHelper.obtenerDistanciaACentro(context)
            }
        } else {
            mensajePermisoRechazado = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Ubicación Sede Los Vilos", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Dirección: Caupolicán 220", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(24.dp))

        SanaCard {
            Column(modifier = Modifier.padding(20.dp)) {
                if (distanciaKm != null) {
                    Text(text = "Estás a **${String.format("%.1f", distanciaKm)} km** de la sede.", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    if (distanciaKm!! > 30.0f) {
                        Text(text = "💡 Estás lejos de la sede física. Te sugerimos agendar una **atención online**.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text(text = "📍 ¡Estás cerca! Te esperamos en nuestros boxes presenciales.", color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    Text(text = "Presiona el botón para calcular tu distancia a la sede presencial.")
                }

                if (mensajePermisoRechazado) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "⚠️ Permiso de ubicación denegado. Habilítalo en ajustes para calcular tu distancia.", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SanaButton(text = "Ver mi distancia", onClick = {
            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                scope.launch {
                    distanciaKm = UbicacionHelper.obtenerDistanciaACentro(context)
                }
            } else {
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        })

        Spacer(modifier = Modifier.height(12.dp))

        // Botón de WhatsApp (HU-19)
        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/56912345678?text=Hola,%20deseo%20consultar%20sobre%20las%20terapias%20de%20SanaEsencia"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "💬 Contactar por WhatsApp", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
    }
}