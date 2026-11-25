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

enum class FaseRespiracion(val texto: String, val duracionSegundos: Int) {
    INHALAR("Inhala suavemente", 4),
    MANTENER("Mantén el aire", 4),
    EXHALAR("Exhala despacio", 4)
}

data class RespiracionUiState(
    val tiempoRestanteTotal: Int = 60, // 1 minuto
    val fase: FaseRespiracion = FaseRespiracion.INHALAR,
    val segundosEnFase: Int = 4,
    val estaActivo: Boolean = false,
    val estaPausado: Boolean = false,
    val estaCompletado: Boolean = false
)

class RespiracionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RespiracionUiState())
    val uiState: StateFlow<RespiracionUiState> = _uiState.asStateFlow()

    private var sessionJob: Job? = null

    fun iniciarOSincronizar() {
        if (_uiState.value.estaPausado) {
            reanudar()
        } else if (!_uiState.value.estaActivo) {
            iniciar()
        }
    }

    fun iniciar() {
        sessionJob?.cancel()
        _uiState.value = RespiracionUiState(
            tiempoRestanteTotal = 60,
            fase = FaseRespiracion.INHALAR,
            segundosEnFase = 4,
            estaActivo = true,
            estaPausado = false,
            estaCompletado = false
        )

        iniciarBucleRespiracion()
    }

    fun pausar() {
        sessionJob?.cancel()
        _uiState.update { it.copy(estaPausado = true) }
    }

    fun reanudar() {
        if (!_uiState.value.estaPausado) return
        _uiState.update { it.copy(estaPausado = false) }
        iniciarBucleRespiracion()
    }

    fun reiniciar() {
        sessionJob?.cancel()
        _uiState.value = RespiracionUiState()
    }

    private fun iniciarBucleRespiracion() {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            while (_uiState.value.tiempoRestanteTotal > 0 && _uiState.value.estaActivo && !_uiState.value.estaPausado) {
                delay(1000L)
                val currentState = _uiState.value

                val nuevoTotal = currentState.tiempoRestanteTotal - 1
                if (nuevoTotal <= 0) {
                    _uiState.update {
                        it.copy(
                            tiempoRestanteTotal = 0,
                            estaActivo = false,
                            estaPausado = false,
                            estaCompletado = true
                        )
                    }
                    break
                }

                var nuevaSegFase = currentState.segundosEnFase - 1
                var nuevaFase = currentState.fase

                if (nuevaSegFase <= 0) {
                    nuevaFase = when (currentState.fase) {
                        FaseRespiracion.INHALAR -> FaseRespiracion.MANTENER
                        FaseRespiracion.MANTENER -> FaseRespiracion.EXHALAR
                        FaseRespiracion.EXHALAR -> FaseRespiracion.INHALAR
                    }
                    nuevaSegFase = nuevaFase.duracionSegundos
                }

                _uiState.update {
                    it.copy(
                        tiempoRestanteTotal = nuevoTotal,
                        fase = nuevaFase,
                        segundosEnFase = nuevaSegFase
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionJob?.cancel()
    }
}
