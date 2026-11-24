package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.data.remote.CrearCitaRequest
import com.example.sanaesenciaapp.data.remote.RetrofitClient
import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.CitaEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class CitaRepository(
    private val citaDao: CitaDao
) {
    fun obtenerCitasPorUsuario(email: String): Flow<List<CitaEntity>> {
        val correoLimpio = email.trim().lowercase()
        return citaDao.obtenerPorUsuarioFlow(correoLimpio)
            .onStart { sincronizarCitasDesdeServidor(correoLimpio) }
    }

    /* Obtiene todas las citas (para administrativo y terapeuta) */
    fun obtenerTodasLasCitas(): Flow<List<CitaEntity>> {
        return citaDao.obtenerTodasFlow()
            .onStart { sincronizarCitasDesdeServidor(null) }
    }

    private fun agendaApiOrNull() = runCatching { RetrofitClient.agendaApi }.getOrNull()

    suspend fun sincronizarCitasDesdeServidor(email: String?) {
        val api = agendaApiOrNull() ?: return
        try {
            val response = api.listarCitas(email)
            if (response.isSuccessful) {
                response.body()?.forEach { remota ->
                    citaDao.reservarSiDisponible(remota)
                    if (remota.id > 0) {
                        citaDao.actualizarEstado(remota.id, remota.estado)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Si no hay conexión, Room mantiene disponibles las citas locales
        }
    }

    suspend fun retenerCita(cita: CitaEntity): CitaEntity? {
        val api = agendaApiOrNull() ?: return cita
        return try {
            val response = api.retenerCita(cita.toRequest("EN_PAGO"))
            if (response.isSuccessful) response.body() else null
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    suspend fun confirmarRetencion(idRemoto: Int): CitaEntity? {
        val api = agendaApiOrNull()
        if (api == null) {
            return citaDao.obtenerTodas().firstOrNull { it.id == idRemoto }
                ?.copy(estado = "CONFIRMADA")
        }
        return try {
            val response = api.confirmarRetencion(idRemoto)
            if (response.isSuccessful) response.body() else null
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    suspend fun liberarRetencionRemota(idRemoto: Int) {
        val api = agendaApiOrNull() ?: return
        try {
            api.liberarRetencion(idRemoto)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // La expiración en backend libera el horario aunque el dispositivo esté desconectado.
        }
    }

    private fun CitaEntity.toRequest(estado: String) = CrearCitaRequest(
        usuarioEmail = usuarioEmail.trim().lowercase(),
        especialidadId = especialidadId,
        especialidadNombre = especialidadNombre,
        especialistaId = especialistaId,
        especialistaNombre = especialistaNombre,
        modalidad = modalidad,
        inicioMillis = inicioMillis,
        finMillis = finMillis,
        hora = hora,
        pacienteNombre = pacienteNombre,
        beneficiarioId = beneficiarioId,
        estado = estado,
        boxAsignado = boxAsignado
    )

    /* Reserva cita en agenda-service (:8084) y la guarda en Room; retorna null si el horario no está disponible */
    suspend fun reservarCita(cita: CitaEntity): CitaEntity? {
        val api = agendaApiOrNull()
        if (api == null) return citaDao.reservarSiDisponible(cita)
        return try {
            val request = CrearCitaRequest(
                usuarioEmail = cita.usuarioEmail.trim().lowercase(),
                especialidadId = cita.especialidadId,
                especialidadNombre = cita.especialidadNombre,
                especialistaId = cita.especialistaId,
                especialistaNombre = cita.especialistaNombre,
                modalidad = cita.modalidad,
                inicioMillis = cita.inicioMillis,
                finMillis = cita.finMillis,
                hora = cita.hora,
                pacienteNombre = cita.pacienteNombre,
                beneficiarioId = cita.beneficiarioId,
                estado = cita.estado,
                boxAsignado = cita.boxAsignado
            )
            val response = api.reservarCita(request)

            when {
                response.isSuccessful -> {
                    val guardadaRemota = response.body() ?: cita
                    citaDao.reservarSiDisponible(guardadaRemota) ?: guardadaRemota
                }
                response.code() == 400 || response.code() == 409 -> null
                else -> null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    /* Actualiza estado de cita en agenda-service (:8084) y en Room */
    suspend fun actualizarEstadoCita(id: Int, nuevoEstado: String) {
        val api = agendaApiOrNull()
        if (api == null) {
            citaDao.actualizarEstado(id, nuevoEstado)
            return
        }
        try {
            val response = api.cambiarEstadoCita(id = id, estado = nuevoEstado)
            if (response.isSuccessful) citaDao.actualizarEstado(id, nuevoEstado)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Room no se marca como actualizado si la fuente remota rechazó el cambio.
        }
    }

    /* Cancela una cita */
    suspend fun cancelarCita(id: Int) {
        actualizarEstadoCita(id, "CANCELADA")
    }

    /* Marca cita como completada */
    suspend fun completarCita(id: Int) {
        actualizarEstadoCita(id, "COMPLETADA")
    }
}