package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun `reiniciar cancela el ocultado pendiente de una pareja anterior`() = runTest(testDispatcher) {
        val viewModelStore = ViewModelStore()
        val viewModel = MemoriceViewModel().also { viewModelStore.put("memorice", it) }

        try {
            val cartas = viewModel.uiState.value.cartas
            val primera = cartas.first()
            val segunda = cartas.first { it.parejaId != primera.parejaId }

            viewModel.voltearCarta(primera.id)
            viewModel.voltearCarta(segunda.id)
            assertTrue(viewModel.uiState.value.bloqueado)

            viewModel.reiniciarJuego()
            val estadoReiniciado = viewModel.uiState.value
            assertFalse(estadoReiniciado.bloqueado)
            assertTrue(estadoReiniciado.cartas.none { it.volteada || it.encontrada })

            advanceTimeBy(800L)
            runCurrent()

            assertEquals(estadoReiniciado.cartas, viewModel.uiState.value.cartas)
            assertFalse(viewModel.uiState.value.bloqueado)
        } finally {
            viewModelStore.clear()
        }
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
