package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class TipoOponenteGato {
    DOS_JUGADORES,
    CPU_FACIL,
    CPU_DIFICIL
}

data class GatoUiState(
    val tablero: List<String?> = List(9) { null },
    val turno: String = "X", // "X" o "O"
    val ganador: String? = null, // "X", "O", "Empate" o null
    val oponente: TipoOponenteGato = TipoOponenteGato.CPU_DIFICIL,
    val puntajeX: Int = 0,
    val puntajeO: Int = 0,
    val empates: Int = 0,
    val pensandoCpu: Boolean = false
)

class GatoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GatoUiState())
    val uiState: StateFlow<GatoUiState> = _uiState.asStateFlow()

    fun setOponente(oponente: TipoOponenteGato) {
        _uiState.update { it.copy(oponente = oponente) }
        reiniciarJuego()
    }

    fun reiniciarJuego() {
        _uiState.update {
            it.copy(
                tablero = List(9) { null },
                turno = "X",
                ganador = null,
                pensandoCpu = false
            )
        }
    }

    fun seleccionarCelda(index: Int, esMovimientoCpu: Boolean = false) {
        val state = _uiState.value
        if (index !in 0..8 || state.tablero[index] != null || state.ganador != null) return

        // Para toques de jugador humano: rechazar si la CPU está pensando o es turno de la CPU
        if (!esMovimientoCpu) {
            if (state.pensandoCpu) return
            if (state.oponente != TipoOponenteGato.DOS_JUGADORES && state.turno == "O") return
        }

        val nuevoTablero = state.tablero.toMutableList()
        nuevoTablero[index] = state.turno

        val ganadorActual = verificarGanador(nuevoTablero)
        val esEmpate = ganadorActual == null && nuevoTablero.all { it != null }

        val nuevoGanador = when {
            ganadorActual != null -> ganadorActual
            esEmpate -> "Empate"
            else -> null
        }

        val nuevoPuntajeX = if (ganadorActual == "X") state.puntajeX + 1 else state.puntajeX
        val nuevoPuntajeO = if (ganadorActual == "O") state.puntajeO + 1 else state.puntajeO
        val nuevosEmpates = if (esEmpate) state.empates + 1 else state.empates

        val siguienteTurno = if (state.turno == "X") "O" else "X"

        _uiState.update {
            it.copy(
                tablero = nuevoTablero,
                turno = siguienteTurno,
                ganador = nuevoGanador,
                puntajeX = nuevoPuntajeX,
                puntajeO = nuevoPuntajeO,
                empates = nuevosEmpates
            )
        }

        // Si es turno de la CPU y el juego no ha terminado
        if (nuevoGanador == null && state.oponente != TipoOponenteGato.DOS_JUGADORES && siguienteTurno == "O") {
            ejecutarMovimientoCpu()
        }
    }

    private fun ejecutarMovimientoCpu() {
        _uiState.update { it.copy(pensandoCpu = true) }

        viewModelScope.launch {
            delay(500L) // Simular tiempo de respuesta del sistema
            val state = _uiState.value
            if (state.ganador != null || state.turno != "O") {
                _uiState.update { it.copy(pensandoCpu = false) }
                return@launch
            }

            val indiceCpu = when (state.oponente) {
                TipoOponenteGato.CPU_FACIL -> obtenerMovimientoAleatorio(state.tablero)
                TipoOponenteGato.CPU_DIFICIL -> obtenerMovimientoMinimax(state.tablero, "O")
                else -> -1
            }

            if (indiceCpu in 0..8 && state.tablero[indiceCpu] == null) {
                seleccionarCelda(indiceCpu, esMovimientoCpu = true)
            }
            _uiState.update { it.copy(pensandoCpu = false) }
        }
    }

    private fun obtenerMovimientoAleatorio(tablero: List<String?>): Int {
        val libres = tablero.indices.filter { tablero[it] == null }
        return if (libres.isNotEmpty()) libres[Random.nextInt(libres.size)] else -1
    }

    private fun obtenerMovimientoMinimax(tablero: List<String?>, jugadorCpu: String): Int {
        // Estrategia inteligente: 1. Ganar si puede, 2. Bloquear al jugador, 3. Centro, 4. Esquinas, 5. Aleatorio
        val jugadorHumano = if (jugadorCpu == "O") "X" else "O"

        // 1. Ver si CPU puede ganar en 1 movimiento
        for (i in tablero.indices) {
            if (tablero[i] == null) {
                val copia = tablero.toMutableList()
                copia[i] = jugadorCpu
                if (verificarGanador(copia) == jugadorCpu) return i
            }
        }

        // 2. Ver si humano va a ganar y bloquear
        for (i in tablero.indices) {
            if (tablero[i] == null) {
                val copia = tablero.toMutableList()
                copia[i] = jugadorHumano
                if (verificarGanador(copia) == jugadorHumano) return i
            }
        }

        // 3. Tomar centro si está libre
        if (tablero[4] == null) return 4

        // 4. Tomar esquinas libres
        val esquinas = listOf(0, 2, 6, 8).filter { tablero[it] == null }
        if (esquinas.isNotEmpty()) return esquinas[Random.nextInt(esquinas.size)]

        // 5. Cualquier libre
        return obtenerMovimientoAleatorio(tablero)
    }

    private fun verificarGanador(t: List<String?>): String? {
        val lineas = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Filas
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columnas
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonales
        )
        for (linea in lineas) {
            val (a, b, c) = linea
            if (t[a] != null && t[a] == t[b] && t[a] == t[c]) {
                return t[a]
            }
        }
        return null
    }
}
