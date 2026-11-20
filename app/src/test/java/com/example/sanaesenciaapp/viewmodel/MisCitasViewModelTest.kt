package com.example.sanaesenciaapp.viewmodel

import com.example.sanaesenciaapp.data.room.CitaEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de MisCitasViewModel (estado y lógica pura).
 */
class MisCitasViewModelTest {

    private lateinit var viewModel: MisCitasViewModel

    @Before
    fun setup() {
        viewModel = MisCitasViewModel(
            citaRepository = com.example.sanaesenciaapp.data.repository.CitaRepository(
                object : com.example.sanaesenciaapp.data.room.CitaDao() {
                    override fun obtenerTodasFlow() = kotlinx.coroutines.flow.flowOf(emptyList<CitaEntity>())
                    override fun obtenerPorUsuarioFlow(email: String) = kotlinx.coroutines.flow.flowOf(emptyList<CitaEntity>())
                    override suspend fun obtenerTodas(): List<CitaEntity> = emptyList()
                    override suspend fun contarSolapesEspecialista(especialistaId: Int, inicioMillis: Long, finMillis: Long): Int = 0
                    override suspend fun contarPresencialesSolapadas(inicioMillis: Long, finMillis: Long): Int = 0
                    override suspend fun obtenerBoxesOcupados(inicioMillis: Long, finMillis: Long): List<Int> = emptyList()
                    override suspend fun insertarInterno(cita: CitaEntity): Long = 1
                    override suspend fun actualizarEstado(id: Int, nuevoEstado: String) {}
                }
            ),
            sesionRepository = com.example.sanaesenciaapp.data.repository.SesionRepository(
                com.example.sanaesenciaapp.data.repository.UsuarioRepository(
                    object : com.example.sanaesenciaapp.data.room.UsuarioDao {
                        override suspend fun insertar(usuario: com.example.sanaesenciaapp.data.room.UsuarioEntity): Long = 1
                        override suspend fun obtenerTodos(): List<com.example.sanaesenciaapp.data.room.UsuarioEntity> = emptyList()
                        override suspend fun obtenerPorEmail(email: String): com.example.sanaesenciaapp.data.room.UsuarioEntity? = null
                        override suspend fun actualizarPassword(email: String, nuevaPassword: String): Int = 0
                        override suspend fun contarCuentasSinFormatoPbkdf2(): Int = 0
                    }
                )
            )
        )
    }

    private fun crearCita(estado: String = "CONFIRMADA", inicioMillis: Long = System.currentTimeMillis() + 48 * 3600000L) = CitaEntity(
        usuarioEmail = "test@mail.com",
        especialidadId = 1,
        especialidadNombre = "Psicología",
        especialistaId = 1,
        especialistaNombre = "Dr. Test",
        modalidad = "PRESENCIAL",
        inicioMillis = inicioMillis,
        finMillis = inicioMillis + 3600000,
        hora = "09:00",
        pacienteNombre = "Paciente Test",
        estado = estado
    )

    @Test
    fun `estado inicial tiene lista vacia`() {
        assertEquals(0, viewModel.citas.value.size)
    }

    @Test
    fun `puedeCancelar retorna true para cita confirmada futura`() {
        assertTrue(viewModel.puedeCancelar(crearCita()))
    }

    @Test
    fun `puedeCancelar retorna false para cita pasada`() {
        assertFalse(viewModel.puedeCancelar(crearCita(inicioMillis = System.currentTimeMillis() - 3600000L)))
    }

    @Test
    fun `puedeCancelar retorna false para cita no confirmada`() {
        assertFalse(viewModel.puedeCancelar(crearCita(estado = "CANCELADA")))
    }

    @Test
    fun `formatearFecha retorna texto legible`() {
        val resultado = viewModel.formatearFecha(System.currentTimeMillis())
        assertNotNull(resultado)
        assertTrue(resultado.isNotBlank())
    }

    @Test
    fun `limpiarError resetea error`() {
        viewModel.limpiarError()
        assertEquals(null, viewModel.errorGeneral.value)
    }
}
