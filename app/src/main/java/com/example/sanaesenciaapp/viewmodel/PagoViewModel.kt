package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.repository.CitaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PagoUiState(
    val citaId: Int = 0,
    val especialidadNombre: String="",
    val especialistaNombre: String = "",
    val modalidad: String = "",
    val fechaHora: String = "",
    val nombrePaciente: String = "",
    val totalAPagar: Int = 35000,
    val tiempoRestanteSegundos: Int = 600,
    val estaExpirado: Boolean = false,
    val isLoading: Boolean = false,
    val pagoExitoso: Boolean = false,
    val errorGeneral: String? = null
)

class PagoViewModel (
    private val citaRepository: CitaRepository
): ViewModel(){
    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    private var temporizadorActivo = false

    fun iniciarTemporizador(){
        if (temporizadorActivo) return
        temporizadorActivo = true

        viewModelScope.launch {
            while (_uiState.value.tiempoRestanteSegundos > 0 && !_uiState.value.pagoExitoso){
                delay(1000)
                _uiState.update {
                    val nuevoTiempo = it.tiempoRestanteSegundos -1
                    it.copy(
                        tiempoRestanteSegundos = nuevoTiempo,
                        estaExpirado = nuevoTiempo <=0
                    )
                }
            }
        }
    }
    //procesa el pago(simulado)
    fun procesarPago(){
        val state = _uiState.value

        if (state.estaExpirado){
            _uiState.update { it.copy(errorGeneral = "El tiempo de pago expiró") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorGeneral = null) }

        viewModelScope.launch {
            //simula proceso pago
            delay(1500)

            _uiState.update {
                it.copy(isLoading = false, pagoExitoso = true)
            }
        }
    }
    // reinicia temporizador
    fun reiniciarTemporizador(){
        temporizadorActivo = false
        _uiState.update {
            it.copy(tiempoRestanteSegundos = 600, estaExpirado = false)
        }
        iniciarTemporizador()
    }

    //carga datos de cita para mostrar en el resumen
    fun cargarDatosCita(
        citaId: Int,
        especialidad: String,
        especialista: String,
        modalidad: String,
        fechaHora: String,
        paciente: String
    ){
        _uiState.update {
            it.copy(
                citaId = citaId,
                especialidadNombre = especialidad,
                especialistaNombre = especialista,
                modalidad = modalidad,
                fechaHora = fechaHora,
                nombrePaciente = paciente
            )
        }
    }
}