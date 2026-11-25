package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.data.remote.CitaDto

/**
 * Contrato de la fuente remota de citas (VA-05, Fase 1).
 *
 * La implementación real se conecta cuando Anaís registre el cliente Retrofit
 * + interceptor JWT en AppContainer. La identidad NO viaja en los argumentos:
 * el backend decide el propietario desde el claim sub del JWT.
 */
interface CitaFuente {

    /** POST /api/citas — reserva; éxito = 201 con la cita creada (id del servidor). */
    suspend fun reservar(cita: CitaDto): Result<CitaDto>

    /** GET /api/citas — citas del usuario autenticado (sin parámetro de email). */
    suspend fun listarPropias(): Result<List<CitaDto>>
}

/**
 * Error HTTP de la API de citas.
 * El contrato actual devuelve 400 en conflictos de horario (no 409);
 * 409 se acepta igual en el mapeo por si Mark lo incorpora.
 */
class CitaApiException(
    val codigoHttp: Int,
    mensaje: String? = null
) : Exception(mensaje ?: "Error HTTP $codigoHttp")
