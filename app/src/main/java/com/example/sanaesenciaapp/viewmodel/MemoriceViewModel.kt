package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartaZen(
    val id: Int,
    val parejaId: Int,
    val simbolo: String,
    val volteada: Boolean = false,
    val encontrada: Boolean = false
)

data class MemoriceUiState(
    val cartas: List<CartaZen> = emptyList(),
    val intentos: Int = 0,
    val segundosTranscurridos: Int = 0,
    val juegoTerminado: Boolean = false,
    val bloqueado: Boolean = false
)

class MemoriceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MemoriceUiState())
    val uiState: StateFlow<MemoriceUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var primeraCartaSeleccionadaId: Int? = null

    init {
        reiniciarJuego()
    }

    fun reiniciarJuego() {
        timerJob?.cancel()
        primeraCartaSeleccionadaId = null

        val simbolos = listOf("🌿", "🌸", "🧠", "🧩", "🥗", "🧘", "🍵", "🕯️")
        val listaCartas = mutableListOf<CartaZen>()
        var idCounter = 0

        simbolos.forEachIndexed { index, simbolo ->
            // Dos cartas por cada par
            listaCartas.add(CartaZen(id = idCounter++, parejaId = index, simbolo = simbolo))
            listaCartas.add(CartaZen(id = idCounter++, parejaId = index, simbolo = simbolo))
        }

        _uiState.value = MemoriceUiState(
            cartas = listaCartas.shuffled(),
            intentos = 0,
            segundosTranscurridos = 0,
            juegoTerminado = false,
            bloqueado = false
        )

        iniciarCronometro()
    }

    private fun iniciarCronometro() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (!_uiState.value.juegoTerminado) {
                    _uiState.update { it.copy(segundosTranscurridos = it.segundosTranscurridos + 1) }
                }
            }
        }
    }

    fun voltearCarta(cartaId: Int) {
        val currentState = _uiState.value
        if (currentState.bloqueado || currentState.juegoTerminado) return

        val cartaIndex = currentState.cartas.indexOfFirst { it.id == cartaId }
        if (cartaIndex == -1) return

        val cartaClicked = currentState.cartas[cartaIndex]
        if (cartaClicked.volteada || cartaClicked.encontrada) return

        // Voltear la carta seleccionada
        val nuevasCartas = currentState.cartas.toMutableList()
        nuevasCartas[cartaIndex] = cartaClicked.copy(volteada = true)

        val primeraId = primeraCartaSeleccionadaId

        if (primeraId == null) {
            // Es la primera carta del turno
            primeraCartaSeleccionadaId = cartaId
            _uiState.value = currentState.copy(cartas = nuevasCartas)
        } else {
            // Es la segunda carta del turno
            val primeraCarta = nuevasCartas.first { it.id == primeraId }
            val segundaCarta = nuevasCartas[cartaIndex]

            val nuevosIntentos = currentState.intentos + 1
            primeraCartaSeleccionadaId = null

            if (primeraCarta.parejaId == segundaCarta.parejaId) {
                // Coincidencia
                val cartasActualizadas = nuevasCartas.map {
                    if (it.id == primeraId || it.id == cartaId) {
                        it.copy(encontrada = true)
                    } else {
                        it
                    }
                }

                val juegoGanado = cartasActualizadas.all { it.encontrada }
                if (juegoGanado) {
                    timerJob?.cancel()
                }

                _uiState.value = currentState.copy(
                    cartas = cartasActualizadas,
                    intentos = nuevosIntentos,
                    juegoTerminado = juegoGanado
                )
            } else {
                // No hay coincidencia: bloquear y ocultar después de un breve retraso
                _uiState.value = currentState.copy(
                    cartas = nuevasCartas,
                    intentos = nuevosIntentos,
                    bloqueado = true
                )

                viewModelScope.launch {
                    delay(800L)
                    val ocultadas = _uiState.value.cartas.map {
                        if (it.id == primeraId || it.id == cartaId) {
                            it.copy(volteada = false)
                        } else {
                            it
                        }
                    }
                    _uiState.update { it.copy(cartas = ocultadas, bloqueado = false) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
