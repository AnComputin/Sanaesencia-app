package com.example.sanaesenciaapp.data.remote

import com.example.sanaesenciaapp.data.room.CitaEntity

/**
 * DTO de citas para la API (VA-05).
 *
 * Refleja el JSON del contrato de agenda-service (entidad JPA Cita serializada
 * por Jackson vía getters). NO es CitaEntity: el mapeo entre ambas es manual
 * y explícito para no asumir que los campos coinciden eternamente.
 */
data class CitaDto(
    val id: Int? = null,
    val usuarioEmail: String,
    val especialidadId: Int,
    val especialidadNombre: String,
    val especialistaId: Int,
    val especialistaNombre: String,
    val modalidad: String,
    val inicioMillis: Long,
    val finMillis: Long,
    val hora: String,
    val pacienteNombre: String,
    val beneficiarioId: Int? = null,
    val estado: String,
    val boxAsignado: Int? = null
)

/** DTO → Room. id 0 (sin persistir) se traduce a null (aún sin id del servidor). */
fun CitaDto.toEntity(): CitaEntity = CitaEntity(
    id = id ?: 0,
    usuarioEmail = usuarioEmail,
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

/** Room → DTO. id 0 (fila local sin guardar) se traduce a null (sin id del servidor). */
fun CitaEntity.toDto(): CitaDto = CitaDto(
    id = id.takeIf { it > 0 },
    usuarioEmail = usuarioEmail,
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
