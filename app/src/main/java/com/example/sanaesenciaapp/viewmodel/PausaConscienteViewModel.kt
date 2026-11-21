package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ContenidoPasoConsciente {
    DETENERSE,
    RESPIRACION,
    ENTORNO,
    RECONEXION
}

data class PasoConsciente(
    val numero: Int,
    val contenido: ContenidoPasoConsciente,
    val icono: String
)

data class PausaConscienteUiState(
    val pasoIndex: Int = 0,
    val estaActivo: Boolean = false,
    val estaPausado: Boolean = false,
    val estaCompletado: Boolean = false,
    val pasos: List<PasoConsciente> = emptyList()
)

class PausaConscienteViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PausaConscienteUiState())
    val uiState: StateFlow<PausaConscienteUiState> = _uiState.asStateFlow()

    init {
        reiniciar()
    }

    fun reiniciar() {
        val listaPasos = listOf(
            PasoConsciente(1, ContenidoPasoConsciente.DETENERSE, "🛑"),
            PasoConsciente(2, ContenidoPasoConsciente.RESPIRACION, "🌬️"),
            PasoConsciente(3, ContenidoPasoConsciente.ENTORNO, "👀"),
            PasoConsciente(4, ContenidoPasoConsciente.RECONEXION, "✨")
        )

        _uiState.value = PausaConscienteUiState(pasos = listaPasos)
    }

    fun iniciar() {
        _uiState.update { it.copy(estaActivo = true, estaPausado = false, pasoIndex = 0) }
    }

    fun siguientePaso() {
        val state = _uiState.value
        if (state.pasoIndex + 1 < state.pasos.size) {
            _uiState.update {
                it.copy(pasoIndex = it.pasoIndex + 1, estaActivo = true, estaPausado = false)
            }
        } else {
            _uiState.update { it.copy(estaCompletado = true, estaActivo = false) }
        }
    }

    fun pasoAnterior() {
        val state = _uiState.value
        if (state.pasoIndex > 0) {
            _uiState.update { it.copy(pasoIndex = it.pasoIndex - 1) }
        }
    }

    fun pausarOReanudar() {
        _uiState.update { it.copy(estaPausado = !it.estaPausado) }
    }
}
