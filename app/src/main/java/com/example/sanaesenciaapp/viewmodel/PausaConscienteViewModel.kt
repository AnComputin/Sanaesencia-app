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

data class PasoConsciente(
    val numero: Int,
    val titulo: String,
    val descripcion: String,
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

    private var autoTimerJob: Job? = null

    init {
        reiniciar()
    }

    fun reiniciar() {
        autoTimerJob?.cancel()
        val listaPasos = listOf(
            PasoConsciente(
                numero = 1,
                titulo = "Detente un momento",
                descripcion = "Tómate un instante para hacer una pausa en lo que estás haciendo. Ajusta tu postura, apoya los pies firmemente en el suelo y relaja los hombros.",
                icono = "🛑"
            ),
            PasoConsciente(
                numero = 2,
                titulo = "Observa tu respiración",
                descripcion = "Lleva tu atención suavemente al aire al entrar y salir. No necesitas cambiar nada, solo siente el movimiento de tu pecho y abdomen.",
                icono = "🌬️"
            ),
            PasoConsciente(
                numero = 3,
                titulo = "Reconoce tu entorno",
                descripcion = "Abre ligeramente tus sentidos. Identifica 3 sonidos lejanos, siente la temperatura del ambiente o nota el contacto de tu cuerpo con la silla.",
                icono = "👀"
            ),
            PasoConsciente(
                numero = 4,
                titulo = "Reconecta y continúa",
                descripcion = "Agradece este pequeño espacio de serenidad que te has regalado. Inhala profundo una última vez y vuelve a tu rutina con mente renovada.",
                icono = "✨"
            )
        )

        _uiState.value = PausaConscienteUiState(
            pasoIndex = 0,
            estaActivo = false,
            estaPausado = false,
            estaCompletado = false,
            pasos = listaPasos
        )
    }

    fun iniciar() {
        _uiState.update { it.copy(estaActivo = true, estaPausado = false, pasoIndex = 0) }
    }

    fun siguientePaso() {
        val state = _uiState.value
        if (state.pasoIndex + 1 < state.pasos.size) {
            _uiState.update { it.copy(pasoIndex = it.pasoIndex + 1) }
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

    override fun onCleared() {
        super.onCleared()
        autoTimerJob?.cancel()
    }
}
