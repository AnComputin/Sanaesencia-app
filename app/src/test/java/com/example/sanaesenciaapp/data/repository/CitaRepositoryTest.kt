package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.CitaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test


class CitaRepositoryTest {

    private class FakeCitaDao : CitaDao() {
        val citasGuardadas = mutableListOf<CitaEntity>()
        var siguienteId = 1

        override fun obtenerTodasFlow(): Flow<List<CitaEntity>> {
            return flowOf(citasGuardadas.toList())
        }

        override fun obtenerPorUsuarioFlow(email: String): Flow<List<CitaEntity>> {
            return flowOf(citasGuardadas.filter { it.usuarioEmail == email })
        }

        override suspend fun obtenerTodas(): List<CitaEntity> {
            return citasGuardadas.toList()
        }

        override suspend fun contarSolapesEspecialista(
            especialistaId: Int, inicioMillis: Long, finMillis: Long
        ): Int {
            return citasGuardadas.count {
                it.especialistaId == especialistaId &&
                        it.estado == "CONFIRMADA" &&
                        it.inicioMillis < finMillis &&
                        it.finMillis > inicioMillis
            }
        }

        override suspend fun contarPresencialesSolapadas(
            inicioMillis: Long, finMillis: Long
        ): Int {
            return citasGuardadas.count {
                it.modalidad == "PRESENCIAL" &&
                        it.estado == "CONFIRMADA" &&
                        it.inicioMillis < finMillis &&
                        it.finMillis > inicioMillis
            }
        }

        override suspend fun obtenerBoxesOcupados(
            inicioMillis: Long, finMillis: Long
        ): List<Int> {
            return citasGuardadas.filter {
                it.modalidad == "PRESENCIAL" &&
                        it.estado == "CONFIRMADA" &&
                        it.boxAsignado != null &&
                        it.inicioMillis < finMillis &&
                        it.finMillis > inicioMillis
            }.mapNotNull { it.boxAsignado }
        }

        override suspend fun insertarInterno(cita: CitaEntity): Long {
            val citaConId = cita.copy(id = siguienteId++)
            citasGuardadas.add(citaConId)
            return citaConId.id.toLong()
        }

        override suspend fun actualizarEstado(id: Int, nuevoEstado: String) {
            val index = citasGuardadas.indexOfFirst { it.id == id }
            if (index != -1) {
                citasGuardadas[index] = citasGuardadas[index].copy(estado = nuevoEstado)
            }
        }
    }

    private lateinit var fakeDao: FakeCitaDao
    private lateinit var repository: CitaRepository

    @Before
    fun setup() {
        fakeDao = FakeCitaDao()
        repository = CitaRepository(fakeDao)
    }

    private fun crearCita(
        email: String = "test@mail.com",
        modalidad: String = "PRESENCIAL",
        inicio: Long = 0,
        fin: Long = 3600000,
        especialistaId: Int = 1
    ) = CitaEntity(
        id = 0,
        usuarioEmail = email,
        especialidadId = 1,
        especialidadNombre = "Psicología",
        especialistaId = especialistaId,
        especialistaNombre = "Dr. Test",
        modalidad = modalidad,
        inicioMillis = inicio,
        finMillis = fin,
        hora = "09:00",
        pacienteNombre = "Paciente Test",
        estado = "CONFIRMADA"
    )

    // RESERVA

    @Test
    fun `reserva presencial asigna box`() = runTest {
        val resultado = repository.reservarCita(crearCita())
        assertNotNull(resultado)
        assertNotNull(resultado?.boxAsignado)
    }

    @Test
    fun `reserva online no asigna box`() = runTest {
        val resultado = repository.reservarCita(crearCita(modalidad = "ONLINE"))
        assertNotNull(resultado)
        assertNull(resultado?.boxAsignado)
    }

    @Test
    fun `reserva solapada es rechazada`() = runTest {
        repository.reservarCita(crearCita(inicio = 0, fin = 3600000))
        val resultado = repository.reservarCita(crearCita(inicio = 1800000, fin = 5400000))
        assertNull(resultado)
    }

    // FILTRADO POR USUARIO

    @Test
    fun `filtrado por usuario retorna solo sus citas`() = runTest {
        repository.reservarCita(crearCita(email = "ana@mail.com"))
        repository.reservarCita(crearCita(email = "juan@mail.com"))

        val citasAna = mutableListOf<CitaEntity>()
        repository.obtenerCitasPorUsuario("ana@mail.com").collect {
            citasAna.addAll(it)
        }

        assertEquals(1, citasAna.size)
        assertEquals("ana@mail.com", citasAna[0].usuarioEmail)
    }

    // CAMBIO DE ESTADO

    @Test
    fun `cancelar cita cambia estado`() = runTest {
        val cita = repository.reservarCita(crearCita())!!
        repository.cancelarCita(cita.id)
        assertEquals("CANCELADA", fakeDao.citasGuardadas[0].estado)
    }

    @Test
    fun `completar cita cambia estado`() = runTest {
        val cita = repository.reservarCita(crearCita())!!
        repository.completarCita(cita.id)
        assertEquals("COMPLETADA", fakeDao.citasGuardadas[0].estado)
    }
}