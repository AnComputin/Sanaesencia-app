package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.repository.CitaRepository
import com.example.sanaesenciaapp.data.repository.ReglasAgenda
import com.example.sanaesenciaapp.data.repository.SesionRepository
import com.example.sanaesenciaapp.data.room.CitaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AgendarUiState(
    val especialidadId: Int = 0,
    val especialidadNombre: String = "",
    val especialidad: String = "",
    val especialistaId: Int = 0,
    val especialistaNombre: String = "",
    val profesional: String = "",
    val modalidad: String = "",
    val modalidadPermitida: String = "Ambas",
    val fechaMillis: Long? = null,
    val hora: String = "",
    val inicioMillis: Long = 0,
    val nombrePaciente: String = "",
    val beneficiarioId: Int? = null,
    val errorNombre: String? = null,
    val errorModalidad: String? = null,
    val errorFecha: String? = null,
    val errorHora: String? = null,
    val isLoading: Boolean = false,
    val reservaExitosa: Boolean = false,
    val errorGeneral: String? = null,
    val horarios: List<Horario> = emptyList(),
    val fechasDisponibles: List<Int> = emptyList(),
    val puedeContinuar: Boolean = false
)

class AgendarViewModel(
    private val citaRepository: CitaRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendarUiState())
    val uiState: StateFlow<AgendarUiState> = _uiState.asStateFlow()

    init {
        generarFechasDisponibles()
    }

    // ── Selección de servicio ──────────────────────

    fun seleccionarServicio(especialidadId: Int, especialidadNombre: String) {
        _uiState.update {
            it.copy(
                especialidadId = especialidadId,
                especialidadNombre = especialidadNombre,
                especialidad = especialidadNombre
            )
        }
    }

    fun seleccionarEspecialista(especialistaId: Int, especialistaNombre: String) {
        _uiState.update {
            it.copy(
                especialistaId = especialistaId,
                especialistaNombre = especialistaNombre,
                profesional = especialistaNombre
            )
        }
    }

    // ── Modalidad ─────────────────────────────────

    fun seleccionarModalidad(modalidad: Modalidad) {
        val modalidadStr = modalidad.name
        val error = if (!ReglasAgenda.esModalidadValida(modalidadStr)) {
            "Modalidad inválida"
        } else null
        _uiState.update {
            it.copy(modalidad = modalidadStr, errorModalidad = error)
        }
        actualizarPuedeContinuar()
    }

    // ── Fecha y hora ──────────────────────────────

    fun seleccionarFecha(fechaMillis: Int) {
        _uiState.update { it.copy(fechaMillis = fechaMillis.toLong(), errorFecha = null) }
        actualizarPuedeContinuar()
    }

    fun seleccionarHora(hora: String, inicioMillis: Long = 0) {
        _uiState.update {
            it.copy(hora = hora, inicioMillis = inicioMillis, errorHora = null)
        }
        actualizarPuedeContinuar()
    }

    // ── Nombre del paciente ───────────────────────

    fun cambiarNombre(nombre: String) {
        val error = if (nombre.trim().length < 3) {
            "El nombre debe tener al menos 3 caracteres"
        } else null
        _uiState.update {
            it.copy(nombrePaciente = nombre, errorNombre = error)
        }
        actualizarPuedeContinuar()
    }

    fun seleccionarPaciente(nombre: String, beneficiarioId: Int? = null) {
        _uiState.update {
            it.copy(nombrePaciente = nombre, beneficiarioId = beneficiarioId)
        }
        actualizarPuedeContinuar()
    }

    // ── Fechas disponibles ────────────────────────

    fun generarFechasDisponibles() {
        val fechas = mutableListOf<Int>()
        var contador = 0
        while (fechas.size < 7) {
            val fecha = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, contador)
            }
            if (fecha.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                fechas.add(fecha.timeInMillis.toInt())
            }
            contador++
        }
        _uiState.update { it.copy(fechasDisponibles = fechas) }
    }

    // ── Horarios disponibles ──────────────────────

    fun generarHorarios() {
        val base = listOf("09:00", "10:00", "11:00", "14:00", "15:00", "16:00")
        val horarios = base.map { Horario(hora = it, disponible = true) }
        _uiState.update { it.copy(horarios = horarios) }
    }

    // ── Formatear fecha ───────────────────────────

    fun formatearFecha(millis: Long): String {
        val formato = SimpleDateFormat("EEE d MMM", Locale("es", "CL"))
        return formato.format(Date(millis))
    }

    // ── Puede continuar ──────────────────────────

    private fun actualizarPuedeContinuar() {
        val state = _uiState.value
        val puede = state.modalidad.isNotBlank()
                && state.fechaMillis != null
                && state.hora.isNotBlank()
                && state.nombrePaciente.trim().length >= 3
        _uiState.update { it.copy(puedeContinuar = puede) }
    }

    // ── Confirmar reserva ─────────────────────────

    fun confirmarReserva() {
        val state = _uiState.value

        if (state.isLoading) return

        val errorNombre = if (state.nombrePaciente.trim().length < 3) {
            "El nombre debe tener al menos 3 caracteres"
        } else null

        val errorModalidad = if (!ReglasAgenda.esModalidadValida(state.modalidad)) {
            "Debe seleccionar una modalidad"
        } else null

        val errorFecha = if (state.fechaMillis == null) {
            "Debe seleccionar una fecha"
        } else null

        val errorHora = if (state.hora.isBlank()) {
            "Debe seleccionar una hora"
        } else null

        if (errorNombre != null || errorModalidad != null || errorFecha != null || errorHora != null) {
            _uiState.update {
                it.copy(
                    errorNombre = errorNombre,
                    errorModalidad = errorModalidad,
                    errorFecha = errorFecha,
                    errorHora = errorHora
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorGeneral = null) }

        viewModelScope.launch {
            val usuario = sesionRepository.sesionActual.value
            if (usuario == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorGeneral = "Debe iniciar sesión")
                }
                return@launch
            }

            val finMillis = ReglasAgenda.calcularFin(state.inicioMillis)

            val cita = CitaEntity(
                usuarioEmail = usuario.email,
                especialidadId = state.especialidadId,
                especialidadNombre = state.especialidadNombre,
                especialistaId = state.especialistaId,
                especialistaNombre = state.especialistaNombre,
                modalidad = state.modalidad,
                inicioMillis = state.inicioMillis,
                finMillis = finMillis,
                hora = state.hora,
                pacienteNombre = state.nombrePaciente.trim(),
                estado = "CONFIRMADA"
            )

            val resultado = citaRepository.reservarCita(cita)

            if (resultado != null) {
                _uiState.update {
                    it.copy(isLoading = false, reservaExitosa = true)
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorGeneral = "El horario ya no está disponible")
                }
            }
        }
    }

    fun limpiarEstado() {
        _uiState.value = AgendarUiState()
    }
}
