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

enum class TipoMensajeCamino {
    INICIO,
    META,
    CASILLA_NORMAL,
    CASILLA_AVANCE,
    CASILLA_RETROCESO,
    CASILLA_MEDITACION
}

data class MensajeCamino(
    val tipo: TipoMensajeCamino,
    val numeroCasilla: Int? = null
)

data class CasillaCamino(
    val numero: Int,
    val tipo: TipoCasillaCamino
)

data class CaminoUiState(
    val posicionActual: Int = 1,
    val ultimoDado: Int? = null,
    val mensajeEvento: MensajeCamino = MensajeCamino(TipoMensajeCamino.INICIO),
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
        val listaCasillas = (1..25).map { numero ->
            val tipo = when (numero) {
                3, 9, 17 -> TipoCasillaCamino.AVANCE
                7, 14, 21 -> TipoCasillaCamino.RETROCESO
                5, 12, 19 -> TipoCasillaCamino.MEDITACION
                else -> TipoCasillaCamino.NORMAL
            }
            CasillaCamino(numero = numero, tipo = tipo)
        }

        _uiState.value = CaminoUiState(tablero = listaCasillas)
    }

    fun tirarDado() {
        val state = _uiState.value
        if (state.juegoTerminado) return

        val dado = Random.nextInt(1, 7)
        var nuevaPosicion = state.posicionActual + dado

        if (nuevaPosicion >= 25) {
            _uiState.update {
                it.copy(
                    posicionActual = 25,
                    ultimoDado = dado,
                    mensajeEvento = MensajeCamino(TipoMensajeCamino.META),
                    turnoCount = it.turnoCount + 1,
                    juegoTerminado = true
                )
            }
            return
        }

        val casillaDestino = state.tablero.find { it.numero == nuevaPosicion }
        val tipoMensaje = when (casillaDestino?.tipo) {
            TipoCasillaCamino.AVANCE -> {
                nuevaPosicion = (nuevaPosicion + 2).coerceAtMost(25)
                TipoMensajeCamino.CASILLA_AVANCE
            }
            TipoCasillaCamino.RETROCESO -> {
                nuevaPosicion = (nuevaPosicion - 1).coerceAtLeast(1)
                TipoMensajeCamino.CASILLA_RETROCESO
            }
            TipoCasillaCamino.MEDITACION -> TipoMensajeCamino.CASILLA_MEDITACION
            TipoCasillaCamino.NORMAL, null -> TipoMensajeCamino.CASILLA_NORMAL
        }

        val gano = nuevaPosicion >= 25
        _uiState.update {
            it.copy(
                posicionActual = if (gano) 25 else nuevaPosicion,
                ultimoDado = dado,
                mensajeEvento = MensajeCamino(
                    tipo = tipoMensaje,
                    numeroCasilla = casillaDestino?.numero ?: nuevaPosicion
                ),
                turnoCount = it.turnoCount + 1,
                juegoTerminado = gano
            )
        }
    }
}
