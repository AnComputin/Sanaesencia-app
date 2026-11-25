package com.example.sanaesenciaapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class ResultadoUbicacion(
    val latitud: Double,
    val longitud: Double,
    val distanciaKm: Float,
    val direccionAproximada: String?
)

object UbicacionHelper {
    // Coordenadas de la sede SanaEsencia: Caupolicán 220, Los Vilos
    const val LAT_CENTRO = -31.9135
    const val LON_CENTRO = -71.5168

    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionActual(context: Context): ResultadoUbicacion? {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val location = obtenerLocationConFallback(fusedClient) ?: return null

        val centro = Location("Centro").apply {
            latitude = LAT_CENTRO
            longitude = LON_CENTRO
        }
        val distanciaKm = location.distanceTo(centro) / 1000f
        val direccion = resolverDireccionAproximada(context, location.latitude, location.longitude)

        return ResultadoUbicacion(
            latitud = location.latitude,
            longitud = location.longitude,
            distanciaKm = distanciaKm,
            direccionAproximada = direccion
        )
    }

    suspend fun obtenerDistanciaACentro(context: Context): Float? =
        obtenerUbicacionActual(context)?.distanciaKm

    @SuppressLint("MissingPermission")
    private suspend fun obtenerLocationConFallback(
        fusedClient: com.google.android.gms.location.FusedLocationProviderClient
    ): Location? {
        val ultima = suspendCancellableCoroutine<Location?> { continuation ->
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }
        if (ultima != null) return ultima

        val tokenSource = CancellationTokenSource()
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { tokenSource.cancel() }
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun resolverDireccionAproximada(
        context: Context,
        latitud: Double,
        longitud: Double
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            if (!Geocoder.isPresent()) return@runCatching null
            val geocoder = Geocoder(context, Locale.forLanguageTag("es-CL"))
            val resultados = geocoder.getFromLocation(latitud, longitud, 1)
            resultados?.firstOrNull()?.let { addr ->
                listOfNotNull(
                    addr.thoroughfare?.takeIf { it.isNotBlank() },
                    addr.locality?.takeIf { it.isNotBlank() } ?: addr.subAdminArea?.takeIf { it.isNotBlank() },
                    addr.countryName?.takeIf { it.isNotBlank() }
                ).joinToString(", ").takeIf { it.isNotBlank() }
            }
        }.getOrNull()
    }
}