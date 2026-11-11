package com.example.sanaesenciaapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object UbicacionHelper {
    // Coordenadas de la sede SanaEsencia: Caupolicán 220, Los Vilos
    const val LAT_CENTRO = -31.9135
    const val LON_CENTRO = -71.5168

    @SuppressLint("MissingPermission")
    suspend fun obtenerDistanciaACentro(context: Context): Float? {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        return suspendCancellableCoroutine { continuation ->
            fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val centro = Location("Centro").apply {
                        latitude = LAT_CENTRO
                        longitude = LON_CENTRO
                    }
                    val distanciaMetros = location.distanceTo(centro)
                    continuation.resume(distanciaMetros / 1000f) // Retorna en kilómetros
                } else {
                    continuation.resume(null)
                }
            }.addOnFailureListener {
                continuation.resume(null)
            }
        }
    }
}