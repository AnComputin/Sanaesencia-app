package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

//Modelos de datos del módulo terapeuta

data class Paciente(
    val id: Int,
    val nombre: String,
    val rut: String,
    val ultimaAtencion: String,  // Formato DD/MM/AAAA
    val boxAsignado: String
)

data class Cita(
    val id: Int,
    val paciente: String,
    val hora: String,
    val box: String,
    val estado: EstadoCita = EstadoCita.PENDIENTE
)

enum class EstadoCita {
    PENDIENTE,
    ASISTIDA,
    INASISTENTE,
    CANCELADA_TERAPEUTA
}

// Estado del portal. No hay una asociación persistida entre la cuenta y especialistaId,
// por lo que los datos clínicos permanecen vacíos hasta que exista ese contrato.
data class TerapeutaUiState(
    val pacientes: List<Paciente> = emptyList(),
    val pacientesEnRiesgo: Set<Int> = emptySet(),
    val citas: List<Cita> = emptyList(),
    val fechaSeleccionada: String = "",
    val pacienteSeleccionado: Paciente? = null,
    val historialCitas: List<Cita> = emptyList()
)

class TerapeutaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TerapeutaUiState())
    val uiState: StateFlow<TerapeutaUiState> = _uiState.asStateFlow()

    fun estaEnRiesgo(paciente: Paciente): Boolean =
        paciente.id in _uiState.value.pacientesEnRiesgo

    // No se permite alterar citas hasta disponer de una consulta aislada por especialistaId.
    fun actualizarEstadoCita(idCita: Int, nuevoEstado: EstadoCita) {
        _uiState.update { state ->
            if (state.citas.none { it.id == idCita }) return@update state
            state.copy(
                citas = state.citas.map { cita ->
                    if (cita.id == idCita) cita.copy(estado = nuevoEstado) else cita
                }
            )
        }
    }

    // No se acepta una selección externa mientras no haya pacientes autorizados cargados.
    fun seleccionarPaciente(paciente: Paciente) {
        if (_uiState.value.pacientes.none { it.id == paciente.id }) return
        _uiState.update { it.copy(pacienteSeleccionado = paciente) }
    }
}
