package com.example.sanaesenciaapp.viewmodel

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de AgendarViewModel (estado y lógica pura).
 * No depende de Room ni Android.
 */
class AgendarViewModelTest {

    private lateinit var viewModel: AgendarViewModel

    @Before
    fun setup() {
        viewModel = AgendarViewModel(
            citaRepository = com.example.sanaesenciaapp.data.repository.CitaRepository(
                object : com.example.sanaesenciaapp.data.room.CitaDao() {
                    override fun obtenerTodasFlow() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.sanaesenciaapp.data.room.CitaEntity>())
                    override fun obtenerPorUsuarioFlow(email: String) = kotlinx.coroutines.flow.flowOf(emptyList<com.example.sanaesenciaapp.data.room.CitaEntity>())
                    override suspend fun obtenerTodas(): List<com.example.sanaesenciaapp.data.room.CitaEntity> = emptyList()
                    override suspend fun contarSolapesEspecialista(especialistaId: Int, inicioMillis: Long, finMillis: Long): Int = 0
                    override suspend fun contarPresencialesSolapadas(inicioMillis: Long, finMillis: Long): Int = 0
                    override suspend fun obtenerBoxesOcupados(inicioMillis: Long, finMillis: Long): List<Int> = emptyList()
                    override suspend fun insertarInterno(cita: com.example.sanaesenciaapp.data.room.CitaEntity): Long = 1
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

    // ════════════════════════════════════════════
    // ESTADO INICIAL
    // ════════════════════════════════════════════

    @Test
    fun `estado inicial tiene valores por defecto`() {
        val state = viewModel.uiState.value
        assertEquals("", state.modalidad)
        assertNull(state.fechaMillis)
        assertEquals("", state.hora)
        assertEquals("", state.nombrePaciente)
        assertFalse(state.puedeContinuar)
        assertFalse(state.isLoading)
        assertFalse(state.reservaExitosa)
    }

    // ════════════════════════════════════════════
    // SELECCIÓN
    // ════════════════════════════════════════════

    @Test
    fun `seleccionarServicio actualiza estado`() {
        viewModel.seleccionarServicio(1, "Psicología")
        assertEquals(1, viewModel.uiState.value.especialidadId)
        assertEquals("Psicología", viewModel.uiState.value.especialidadNombre)
    }

    @Test
    fun `seleccionarEspecialista actualiza estado`() {
        viewModel.seleccionarEspecialista(2, "Dr. Test")
        assertEquals(2, viewModel.uiState.value.especialistaId)
        assertEquals("Dr. Test", viewModel.uiState.value.especialistaNombre)
    }

    @Test
    fun `seleccionarModalidad valida y actualiza`() {
        viewModel.seleccionarModalidad(Modalidad.PRESENCIAL)
        assertEquals("PRESENCIAL", viewModel.uiState.value.modalidad)
    }

    // ════════════════════════════════════════════
    // NOMBRE
    // ════════════════════════════════════════════

    @Test
    fun `cambiarNombre con menos de 3 caracteres muestra error`() {
        viewModel.cambiarNombre("Ab")
        assertNotNull(viewModel.uiState.value.errorNombre)
    }

    @Test
    fun `cambiarNombre valido limpia error`() {
        viewModel.cambiarNombre("Juan Pérez")
        assertNull(viewModel.uiState.value.errorNombre)
    }

    // ════════════════════════════════════════════
    // PUEDE CONTINUAR
    // ════════════════════════════════════════════

    @Test
    fun `puedeContinuar es false sin datos`() {
        assertFalse(viewModel.uiState.value.puedeContinuar)
    }

    @Test
    fun `puedeContinuar es true con todos los datos`() {
        viewModel.seleccionarModalidad(Modalidad.PRESENCIAL)
        viewModel.seleccionarFecha(0)
        viewModel.seleccionarHora("09:00")
        viewModel.cambiarNombre("Juan Pérez")
        assertTrue(viewModel.uiState.value.puedeContinuar)
    }

    // ════════════════════════════════════════════
    // FECHAS
    // ════════════════════════════════════════════

    @Test
    fun `generarFechasDisponibles crea 7 fechas`() {
        viewModel.generarFechasDisponibles()
        assertEquals(7, viewModel.uiState.value.fechasDisponibles.size)
    }
}
