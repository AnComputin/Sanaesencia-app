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
                val locales = citaDao.obtenerTodas()
                response.body()?.forEach { remota ->
                    // Deduplica por (especialista, inicio, usuario): si ya está en
                    // Room solo sincroniza el estado por su id LOCAL, y así cada
                    // sincronización no vuelve a insertar la misma cita.
                    val existente = locales.find {
                        it.especialistaId == remota.especialistaId &&
                            it.inicioMillis == remota.inicioMillis &&
                            it.usuarioEmail.trim()
                                .equals(remota.usuarioEmail.trim(), ignoreCase = true)
                    }
                    if (existente != null) {
                        if (existente.estado != remota.estado) {
                            citaDao.actualizarEstado(existente.id, remota.estado)
                        }
                    } else {
                        // Conserva el id del servidor (los PATCH usan ese id).
                        // Si el id colisionara con una fila local, Room lanza y se
                        // reintenta con id local libre para no abortar toda la sync.
                        val insertada = runCatching {
                            citaDao.reservarSiDisponible(remota)
                        }.getOrNull()
                        if (insertada == null) {
                            runCatching {
                                citaDao.reservarSiDisponible(remota.copy(id = 0))
                            }
                        }
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
                    // Primero con el id del servidor (los PATCH dependen de él);
                    // si colisiona con una fila local, se reintenta con id libre.
                    runCatching {
                        citaDao.reservarSiDisponible(guardadaRemota)
                    }.getOrNull()
                        ?: runCatching {
                            citaDao.reservarSiDisponible(guardadaRemota.copy(id = 0))
                        }.getOrNull()
                        ?: guardadaRemota
                }
                else -> null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    /* Actualiza estado de cita en agenda-service (:8084) y en Room.
       Room solo se toca si el servidor confirmó (o si no hay backend):
       si el servidor rechaza (p. ej. 403 porque PATCH es solo ADMIN),
       marcarla igual en Room la desincroniza y el servidor sigue
       bloqueando el horario mientras la app lo muestra libre. */
    suspend fun actualizarEstadoCita(id: Int, nuevoEstado: String) {
        if (RetrofitClient.estaInicializado()) {
            val respuesta = try {
                RetrofitClient.agendaApi.cambiarEstadoCita(id = id, estado = nuevoEstado)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
            if (respuesta == null || !respuesta.isSuccessful) {
                return
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