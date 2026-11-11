package com.example.sanaesenciaapp.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MemoriceViewModelTest {

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
    fun `inicializacion del memorice crea 16 cartas y 8 parejas`() {
        val viewModel = MemoriceViewModel()
        val state = viewModel.uiState.value

        assertEquals(16, state.cartas.size)
        assertEquals(0, state.intentos)
        assertFalse(state.juegoTerminado)

        val parejasUnicas = state.cartas.map { it.parejaId }.toSet()
        assertEquals(8, parejasUnicas.size)
    }

    @Test
    fun `reiniciar juego restablece intentos y cartas`() {
        val viewModel = MemoriceViewModel()
        viewModel.reiniciarJuego()

        val state = viewModel.uiState.value
        assertEquals(16, state.cartas.size)
        assertEquals(0, state.intentos)
        assertFalse(state.juegoTerminado)
    }
}
