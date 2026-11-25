package com.example.sanaesenciaapp.data.remote

import com.example.sanaesenciaapp.data.room.CitaEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas del mapeo explícito CitaDto (API) <-> CitaEntity (Room) — VA-05.
 */
class CitaDtoTest {

    private fun dtoCompleto() = CitaDto(
        id = 45,
        usuarioEmail = "paciente@correo.cl",
        especialidadId = 3,
        especialidadNombre = "Masoterapia",
        especialistaId = 12,
        especialistaNombre = "Nombre Especialista",
        modalidad = "PRESENCIAL",
        inicioMillis = 1795608000000L,
        finMillis = 1795611600000L,
        hora = "10:00 - 11:00",
        pacienteNombre = "Nombre Paciente",
        beneficiarioId = 7,
        estado = "CONFIRMADA",
        boxAsignado = 2
    )

    @Test
    fun `dto sin id mapea a entity con id 0`() {
        val entity = dtoCompleto().copy(id = null).toEntity()
        assertEquals(0, entity.id)
    }

    @Test
    fun `entity con id 0 mapea a dto sin id`() {
        val entity = CitaEntity(
            id = 0,
            usuarioEmail = "paciente@correo.cl",
            especialidadId = 3,
            especialidadNombre = "Masoterapia",
            especialistaId = 12,
            especialistaNombre = "Dr. Test",
            modalidad = "ONLINE",
            inicioMillis = 1000L,
            finMillis = 2000L,
            hora = "09:00",
            pacienteNombre = "Paciente",
            estado = "CONFIRMADA"
        )
        assertNull(entity.toDto().id)
    }

    @Test
    fun `roundtrip dto entity dto preserva todos los campos`() {
        val original = dtoCompleto()

        val resultado = original.toEntity().toDto()

        assertEquals(original, resultado)
    }

    @Test
    fun `opcionales nulos se preservan en ambos sentidos`() {
        val sinOpcionales = dtoCompleto().copy(
            id = 1,
            beneficiarioId = null,
            boxAsignado = null
        )

        val entity = sinOpcionales.toEntity()
        assertNull(entity.beneficiarioId)
        assertNull(entity.boxAsignado)

        val deVuelta = entity.toDto()
        assertNull(deVuelta.beneficiarioId)
        assertNull(deVuelta.boxAsignado)
        assertEquals(1, deVuelta.id)
    }

    @Test
    fun `entity con id positivo mapea a dto con ese id`() {
        val entity = dtoCompleto().toEntity()
        assertEquals(45, entity.id)
        assertEquals(45, entity.toDto().id)
    }
}
