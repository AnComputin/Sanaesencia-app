package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

enum class TipoCasillaCamino {
    NORMAL,
    AVANCE,
    RETROCESO,
    MEDITACION
}

data class CasillaCamino(
    val numero: Int,
    val tipo: TipoCasillaCamino,
    val mensaje: String
)

data class CaminoUiState(
    val posicionActual: Int = 1, // de 1 a 25
    val ultimoDado: Int? = null,
    val mensajeEvento: String = "¡Bienvenido al Camino del Bienestar! Lanza el dado para comenzar.",
    val turnoCount: Int = 0,
    val juegoTerminado: Boolean = false,
    val tablero: List<CasillaCamino> = emptyList()
)

class CaminoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CaminoUiState())
    val uiState: StateFlow<CaminoUiState> = _uiState.asStateFlow()

    init {
        reiniciarJuego()
    }

    fun reiniciarJuego() {
        val listaCasillas = mutableListOf<CasillaCamino>()
        for (i in 1..25) {
            val tipo = when (i) {
                3, 9, 17 -> TipoCasillaCamino.AVANCE
                7, 14, 21 -> TipoCasillaCamino.RETROCESO
                5, 12, 19 -> TipoCasillaCamino.MEDITACION
                else -> TipoCasillaCamino.NORMAL
            }
            val mensaje = when (tipo) {
                TipoCasillaCamino.AVANCE -> "Casilla $i: ¡Impulso de energía! Avanzas 2 casillas extra."
                TipoCasillaCamino.RETROCESO -> "Casilla $i: Pausa reflexiva. Retrocedes 1 casilla."
                TipoCasillaCamino.MEDITACION -> "Casilla $i: Momento de paz interior. Respira hondo."
                TipoCasillaCamino.NORMAL -> "Casilla $i: Un paso firme hacia tu bienestar."
            }
            listaCasillas.add(CasillaCamino(numero = i, tipo = tipo, mensaje = mensaje))
        }

        _uiState.value = CaminoUiState(
            posicionActual = 1,
            ultimoDado = null,
            mensajeEvento = "¡Bienvenido al Camino del Bienestar! Lanza el dado para comenzar.",
            turnoCount = 0,
            juegoTerminado = false,
            tablero = listaCasillas
        )
    }

    fun tirarDado() {
        val state = _uiState.value
        if (state.juegoTerminado) return

        val dado = Random.nextInt(1, 7) // 1 al 6
        var nuevaPosicion = state.posicionActual + dado
        var mensajeExtra = ""

        if (nuevaPosicion >= 25) {
            nuevaPosicion = 25
            _uiState.update {
                it.copy(
                    posicionActual = 25,
                    ultimoDado = dado,
                    mensajeEvento = "¡Has llegado a la meta (Casilla 25)! Tu bienestar integral es completo.",
                    turnoCount = it.turnoCount + 1,
                    juegoTerminado = true
                )
            }
            return
        }

        // Evaluar casilla especial
        val casillaDestino = state.tablero.find { it.numero == nuevaPosicion }
        if (casillaDestino != null) {
            when (casillaDestino.tipo) {
                TipoCasillaCamino.AVANCE -> {
                    nuevaPosicion = (nuevaPosicion + 2).coerceAtMost(25)
                    mensajeExtra = " ⚡ ¡Casilla de Avance! Te impulsas +2 casillas."
                }
                TipoCasillaCamino.RETROCESO -> {
                    nuevaPosicion = (nuevaPosicion - 1).coerceAtLeast(1)
                    mensajeExtra = " 🧘 ¡Pausa reflexiva! Retrocedes -1 casilla."
                }
                TipoCasillaCamino.MEDITACION -> {
                    mensajeExtra = " 🌿 ¡Meditación profunda! Mantienes tu posición y meditas."
                }
                TipoCasillaCamino.NORMAL -> {}
            }
        }

        val mensajeFinal = (casillaDestino?.mensaje ?: "") + mensajeExtra

        val ganado = nuevaPosicion >= 25
        val posFinal = if (ganado) 25 else nuevaPosicion

        _uiState.update {
            it.copy(
                posicionActual = posFinal,
                ultimoDado = dado,
                mensajeEvento = mensajeFinal,
                turnoCount = it.turnoCount + 1,
                juegoTerminado = ganado
            )
        }
    }
}
