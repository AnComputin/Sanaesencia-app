package com.example.sanaesenciaapp.viewmodel

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
class PausasViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ════════════════════════════════════════════
    // RESPIRACIÓN GUIADA
    // ════════════════════════════════════════════

    @Test
    fun `inicializacion de respiracion esta inactiva y en 60 segundos`() {
        val viewModel = RespiracionViewModel()
        val state = viewModel.uiState.value

        assertEquals(60, state.tiempoRestanteTotal)
        assertFalse(state.estaActivo)
        assertFalse(state.estaPausado)
        assertFalse(state.estaCompletado)
        assertEquals(FaseRespiracion.INHALAR, state.fase)
    }

    @Test
    fun `iniciar respiracion activa la sesion`() {
        val viewModel = RespiracionViewModel()
        viewModel.iniciar()

        val state = viewModel.uiState.value
        assertTrue(state.estaActivo)
        assertFalse(state.estaPausado)
    }

    @Test
    fun `pausar respiracion actualiza el estado a pausado`() {
        val viewModel = RespiracionViewModel()
        viewModel.iniciar()
        viewModel.pausar()

        val state = viewModel.uiState.value
        assertTrue(state.estaPausado)
    }

    // ════════════════════════════════════════════
    // PAUSA CONSCIENTE
    // ════════════════════════════════════════════

    @Test
    fun `inicializacion de pausa consciente tiene 4 pasos`() {
        val viewModel = PausaConscienteViewModel()
        val state = viewModel.uiState.value

        assertEquals(4, state.pasos.size)
        assertEquals(0, state.pasoIndex)
        assertFalse(state.estaCompletado)
    }

    @Test
    fun `siguiente paso avanza el indice`() {
        val viewModel = PausaConscienteViewModel()
        viewModel.iniciar()
        viewModel.siguientePaso()

        val state = viewModel.uiState.value
        assertEquals(1, state.pasoIndex)
        assertTrue(state.estaActivo)
        assertFalse(state.estaPausado)
    }

    @Test
    fun `completar todos los pasos marca la sesion como completada`() {
        val viewModel = PausaConscienteViewModel()
        viewModel.iniciar()
        viewModel.siguientePaso() // a paso 2 (index 1)
        viewModel.siguientePaso() // a paso 3 (index 2)
        viewModel.siguientePaso() // a paso 4 (index 3)
        viewModel.siguientePaso() // fin

        val state = viewModel.uiState.value
        assertTrue(state.estaCompletado)

        viewModel.reiniciar()
        assertEquals(0, viewModel.uiState.value.pasoIndex)
        assertFalse(viewModel.uiState.value.estaCompletado)
        assertFalse(viewModel.uiState.value.estaActivo)
    }

    @Test
    fun `respiracion avanza fases pausa y completa al finalizar el minuto`() = runTest(testDispatcher) {
        val store = androidx.lifecycle.ViewModelStore()
        val viewModel = RespiracionViewModel().also { store.put("respiracion", it) }
        try {
            viewModel.iniciar()
            advanceTimeBy(4_000L)
            runCurrent()
            assertEquals(FaseRespiracion.MANTENER, viewModel.uiState.value.fase)

            viewModel.pausar()
            val segundosPausados = viewModel.uiState.value.tiempoRestanteTotal
            advanceTimeBy(5_000L)
            runCurrent()
            assertEquals(segundosPausados, viewModel.uiState.value.tiempoRestanteTotal)

            viewModel.reanudar()
            advanceTimeBy(60_000L)
            runCurrent()
            assertTrue(viewModel.uiState.value.estaCompletado)
            assertEquals(0, viewModel.uiState.value.tiempoRestanteTotal)

            viewModel.reiniciar()
            assertFalse(viewModel.uiState.value.estaCompletado)
            assertEquals(60, viewModel.uiState.value.tiempoRestanteTotal)
        } finally {
            store.clear()
        }
    }
}
