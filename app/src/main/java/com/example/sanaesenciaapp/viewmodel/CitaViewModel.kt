package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Modalidad {
    PRESENCIAL,
    ONLINE
}

data class Horario(
    val hora: String,
    val disponible: Boolean
)

data class CitaUiState(
    val especialidad: String = "",
    val profesional: String = "",
    val modalidad: Modalidad? = null,
    val fechaMillis: Long? = null,
    val hora: String? = null,
    val nombrePaciente: String = "",
    val errorNombre: String? = null
)

class CitaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CitaUiState())
    val uiState: StateFlow<CitaUiState> = _uiState.asStateFlow()

    val fechasDisponibles: List<Long> = generarFechasDisponibles()

    // TODO: Los horarios falsos se reemplazarán por CitaRepository cuando el compañero lo publique.
    private val _horarios = MutableStateFlow(generarHorariosFalsos(null))
    val horarios: StateFlow<List<Horario>> = _horarios.asStateFlow()

    val puedeContinuar: StateFlow<Boolean> = _uiState.map { estado ->
        estado.modalidad != null &&
                estado.fechaMillis != null &&
                estado.hora != null &&
                estado.nombrePaciente.trim().length >= 3 &&
                estado.errorNombre == null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    fun seleccionarServicio(especialidad: String, profesional: String) {
        _uiState.update { estado ->
            estado.copy(especialidad = especialidad, profesional = profesional)
        }
    }

    fun seleccionarModalidad(m: Modalidad) {
        _uiState.update { estado ->
            estado.copy(modalidad = m)
        }
    }

    fun seleccionarFecha(millis: Long) {
        _uiState.update { estado ->
            estado.copy(fechaMillis = millis, hora = null)
        }
        _horarios.value = generarHorariosFalsos(millis)
    }

    fun seleccionarHora(hora: String) {
        _uiState.update { estado ->
            estado.copy(hora = hora)
        }
    }

    fun cambiarNombre(valor: String) {
        val error = when {
            valor.isBlank() -> "El nombre no puede estar vacío"
            valor.trim().length < 3 -> "El nombre debe tener al menos 3 caracteres"
            else -> null
        }
        _uiState.update { estado ->
            estado.copy(
                nombrePaciente = valor,
                errorNombre = error
            )
        }
    }

    fun formatearFecha(millis: Long): String {
        val formato = SimpleDateFormat("EEE d MMM", Locale("es", "CL"))
        return formato.format(Date(millis))
    }

    private fun generarFechasDisponibles(): List<Long> {
        val listaFechas = mutableListOf<Long>()
        val calendario = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (listaFechas.size < 7) {
            if (calendario.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                listaFechas.add(calendario.timeInMillis)
            }
            calendario.add(Calendar.DAY_OF_MONTH, 1)
        }
        return listaFechas
    }

    // TODO: Los horarios falsos se reemplazarán por CitaRepository cuando el compañero lo publique.
    private fun generarHorariosFalsos(fechaMillis: Long?): List<Horario> {
        val horasBase = listOf("09:00", "10:30", "12:00", "14:00", "15:30", "17:00")

        if (fechaMillis == null) {
            // Por defecto 2 horas marcadas como no disponibles (10:30 y 15:30)
            return horasBase.mapIndexed { indice, hora ->
                Horario(hora = hora, disponible = indice != 1 && indice != 4)
            }
        }

        // Variar las 2 horas no disponibles según la fecha seleccionada
        val calendario = Calendar.getInstance().apply { timeInMillis = fechaMillis }
        val diaDelAno = calendario.get(Calendar.DAY_OF_YEAR)
        val indiceNoDisponible1 = diaDelAno % horasBase.size
        val indiceNoDisponible2 = (indiceNoDisponible1 + 2) % horasBase.size

        return horasBase.mapIndexed { indice, hora ->
            Horario(
                hora = hora,
                disponible = indice != indiceNoDisponible1 && indice != indiceNoDisponible2
            )
        }
    }
}
