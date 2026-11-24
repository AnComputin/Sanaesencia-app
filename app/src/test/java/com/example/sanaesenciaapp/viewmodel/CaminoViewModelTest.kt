package com.example.sanaesenciaapp.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaminoViewModelTest {

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
    fun `inicializacion del camino tiene posicion 1 y 25 casillas`() {
        val viewModel = CaminoViewModel()
        val state = viewModel.uiState.value

        assertEquals(1, state.posicionActual)
        assertEquals(25, state.tablero.size)
        assertFalse(state.juegoTerminado)
        assertEquals(0, state.turnoCount)
    }

    @Test
    fun `tirar dado incrementa turnos y cambia posicion`() {
        val viewModel = CaminoViewModel()
        viewModel.tirarDado()

        val state = viewModel.uiState.value
        assertEquals(1, state.turnoCount)
        assertEquals(true, state.posicionActual > 1)
    }

    @Test
    fun `alcanzar la meta completa el juego y reiniciar restaura el estado`() {
        val dados = ArrayDeque(listOf(6, 6, 6, 6, 6))
        val viewModel = CaminoViewModel { dados.removeFirst() }

        repeat(5) { viewModel.tirarDado() }

        assertTrue(viewModel.uiState.value.juegoTerminado)
        assertEquals(25, viewModel.uiState.value.posicionActual)
        val turnosFinales = viewModel.uiState.value.turnoCount
        viewModel.tirarDado()
        assertEquals(turnosFinales, viewModel.uiState.value.turnoCount)

        viewModel.reiniciarJuego()
        assertFalse(viewModel.uiState.value.juegoTerminado)
        assertEquals(1, viewModel.uiState.value.posicionActual)
        assertEquals(0, viewModel.uiState.value.turnoCount)
    }
}
