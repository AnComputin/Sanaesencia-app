package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    val estado: EstadoCita = EstadoCita.PENDIENTE,
    val notas: String = ""
)

enum class EstadoCita {
    PENDIENTE,
    ASISTIDA,
    INASISTENTE,
    CANCELADA_TERAPEUTA
}

//Estado de las 3 pantallas

data class TerapeutaUiState(
    // Pacientes asignados
    val pacientes: List<Paciente> = emptyList(),
    val pacientesEnRiesgo: Set<Int> = emptySet(),

    // Agenda del día
    val citas: List<Cita> = emptyList(),
    val fechaSeleccionada: String = "",

    // Detalle de continuidad
    val pacienteSeleccionado: Paciente? = null,
    val historialCitas: List<Cita> = emptyList(),
    val notasEvolucion: String = ""
)

//ViewModel

class TerapeutaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TerapeutaUiState())
    val uiState: StateFlow<TerapeutaUiState> = _uiState.asStateFlow()

    init {
        cargarDatosEjemplo()
        calcularRiesgoDesercion()
    }

    //Pacientes asignados

    fun calcularRiesgoDesercion() {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val hoy = Calendar.getInstance()

        val enRiesgo = _uiState.value.pacientes.filter { paciente ->
            try {
                val fechaUltima = formato.parse(paciente.ultimaAtencion) ?: return@filter false
                val diasDiferencia = (hoy.timeInMillis - fechaUltima.time) / (1000 * 60 * 60 * 24)
                diasDiferencia > 30
            } catch (e: Exception) {
                false
            }
        }.map { it.id }.toSet()

        _uiState.update { it.copy(pacientesEnRiesgo = enRiesgo) }
    }

    fun estaEnRiesgo(paciente: Paciente): Boolean {
        return paciente.id in _uiState.value.pacientesEnRiesgo
    }

    //Agenda del día

    fun actualizarEstadoCita(idCita: Int, nuevoEstado: EstadoCita) {
        _uiState.update { state ->
            state.copy(
                citas = state.citas.map { cita ->
                    if (cita.id == idCita) cita.copy(estado = nuevoEstado) else cita
                }
            )
        }
    }

    //Detalle de continuidad

    fun seleccionarPaciente(paciente: Paciente) {
        val historial = _uiState.value.citas.filter { it.paciente == paciente.nombre }
        _uiState.update {
            it.copy(
                pacienteSeleccionado = paciente,
                historialCitas = historial
            )
        }
    }

    fun onNotasEvolucionChange(nuevasNotas: String) {
        _uiState.update { it.copy(notasEvolucion = nuevasNotas) }
    }

    //Datos de ejemplo (reemplazar con Room después)

    private fun cargarDatosEjemplo() {
        val pacientes = listOf(
            Paciente(1, "María González", "12.345.678-5", "01/10/2026", "Box 1"),
            Paciente(2, "Juan Pérez", "9.876.543-2", "15/08/2026", "Box 3"),
            Paciente(3, "Ana López", "11.222.333-4", "10/10/2026", "Box 5"),
            Paciente(4, "Carlos Ruiz", "7.777.777-7", "01/07/2026", "Box 2"),
            Paciente(5, "Laura Silva", "8.888.888-K", "05/10/2026", "Box 4")
        )

        val citas = listOf(
            Cita(1, "María González", "09:00", "Box 1"),
            Cita(2, "Juan Pérez", "10:00", "Box 3", EstadoCita.ASISTIDA),
            Cita(3, "Ana López", "11:30", "Box 5"),
            Cita(4, "Carlos Ruiz", "14:00", "Box 2"),
            Cita(5, "Laura Silva", "15:30", "Box 4")
        )

        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val hoy = formato.format(Calendar.getInstance().time)

        _uiState.update {
            it.copy(
                pacientes = pacientes,
                citas = citas,
                fechaSeleccionada = hoy
            )
        }
    }
}
