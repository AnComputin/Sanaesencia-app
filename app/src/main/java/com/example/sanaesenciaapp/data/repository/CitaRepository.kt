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

    suspend fun sincronizarCitasDesdeServidor(email: String?) {
        if (!RetrofitClient.estaInicializado()) return
        try {
            val response = RetrofitClient.agendaApi.listarCitas(email)
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

    /* Reserva cita en agenda-service (:8084) y la guarda en Room; retorna null si el horario no está disponible */
    suspend fun reservarCita(cita: CitaEntity): CitaEntity? {
        if (!RetrofitClient.estaInicializado()) {
            return citaDao.reservarSiDisponible(cita)
        }
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
                boxAsignado = null
            )
            val response = RetrofitClient.agendaApi.reservarCita(request)

            when {
                response.isSuccessful -> {
                    val guardadaRemota = response.body() ?: cita
                    citaDao.reservarSiDisponible(guardadaRemota) ?: guardadaRemota
                }
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
        if (RetrofitClient.estaInicializado()) {
            try {
                RetrofitClient.agendaApi.cambiarEstadoCita(id = id, estado = nuevoEstado)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
        citaDao.actualizarEstado(id, nuevoEstado)
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