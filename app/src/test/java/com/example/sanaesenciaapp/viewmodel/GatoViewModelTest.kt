package com.example.sanaesenciaapp.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GatoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `inicializacion del gato tiene tablero vacio y turno X`() {
        val viewModel = GatoViewModel()
        val state = viewModel.uiState.value

        assertEquals(9, state.tablero.size)
        assertEquals(true, state.tablero.all { it == null })
        assertEquals("X", state.turno)
        assertNull(state.ganador)
    }

    @Test
    fun `seleccionar celda alterna el turno correctamente`() {
        val viewModel = GatoViewModel()
        viewModel.setOponente(TipoOponenteGato.DOS_JUGADORES)

        viewModel.seleccionarCelda(0) // X en 0
        var state = viewModel.uiState.value
        assertEquals("X", state.tablero[0])
        assertEquals("O", state.turno)

        viewModel.seleccionarCelda(1) // O en 1
        state = viewModel.uiState.value
        assertEquals("O", state.tablero[1])
        assertEquals("X", state.turno)
    }

    @Test
    fun `en modo CPU dificil la computadora responde automaticamente`() = runTest {
        val viewModel = GatoViewModel()
        viewModel.setOponente(TipoOponenteGato.CPU_DIFICIL)

        // Humano juega en casilla 0
        viewModel.seleccionarCelda(0)

        // Avanzar el tiempo virtual (500ms delay de la CPU)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // La CPU debe haber colocado 'O' en alguna casilla (ej. centro 4 en modo dificil)
        val casillasOcupadas = state.tablero.count { it != null }
        assertEquals(2, casillasOcupadas) // 1 de X y 1 de O
        assertEquals("O", state.tablero[4]) // Minimax elige el centro 4
        assertEquals("X", state.turno) // Vuelve a ser turno de X
        assertFalse(state.pensandoCpu)
    }

    @Test
    fun `en modo CPU facil la computadora responde automaticamente`() = runTest {
        val viewModel = GatoViewModel()
        viewModel.setOponente(TipoOponenteGato.CPU_FACIL)

        // Humano juega en casilla 0
        viewModel.seleccionarCelda(0)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        val casillasOcupadas = state.tablero.count { it != null }
        assertEquals(2, casillasOcupadas)
        assertEquals("X", state.turno)
        assertFalse(state.pensandoCpu)
    }
}
