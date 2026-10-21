package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.CitaEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Modalidad {
    PRESENCIAL,
    ONLINE
}

enum class EstadoReserva {
    CONFIRMADA,
    COMPLETADA,
    CANCELADA
}

data class Horario(
    val hora: String,
    val disponible: Boolean
)

data class CitaItem(
    val id: Int,
    val especialidad: String,
    val profesional: String,
    val modalidad: Modalidad,
    val inicioMillis: Long,
    val hora: String,
    val paciente: String,
    val estado: EstadoReserva,
    val pacienteEmail: String = ""
)

data class CitaUiState(
    val especialidad: String = "",
    val profesional: String = "",
    val modalidadPermitida: String = "Ambas",
    val modalidad: Modalidad? = null,
    val fechaMillis: Long? = null,
    val hora: String? = null,
    val nombrePaciente: String = "",
    val errorNombre: String? = null,
)

private const val CANCELACION_HORAS = 24

class CitaViewModel(private val citaDao: CitaDao? = null) : ViewModel() {

    private val _uiState = MutableStateFlow(CitaUiState())
    val uiState: StateFlow<CitaUiState> = _uiState.asStateFlow()

    val fechasDisponibles: List<Long> = generarFechasDisponibles()

    private val _citas = MutableStateFlow<List<CitaItem>>(emptyList())

    // Carga las citas guardadas en Room al crear el ViewModel
    private val cargaInicial: Job = viewModelScope.launch {
        val guardadas = citaDao?.obtenerTodas().orEmpty()
        if (guardadas.isNotEmpty()) _citas.value = guardadas.map { it.aItem() }
    }


    private val _usuarioEmail = MutableStateFlow("")

    // Cada usuario ve solo sus propias citas
    val citas: StateFlow<List<CitaItem>> = combine(_citas, _usuarioEmail) { lista, email ->
        lista.filter { it.pacienteEmail == email }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val _ultimaCita = MutableStateFlow<CitaItem?>(null)
    val ultimaCita: StateFlow<CitaItem?> = _ultimaCita.asStateFlow()

    val horarios: StateFlow<List<Horario>> = combine(_uiState, _citas) { estado, listaCitas ->
        calcularHorarios(estado.fechaMillis, listaCitas)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = generarHorariosFalsos(null)
    )

    val duracionPagoSegundos: Int = 600
    private var pagoInicioMillis: Long? = null

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

    fun iniciarPago() {
        if (pagoInicioMillis == null) {
            pagoInicioMillis = System.currentTimeMillis()
        }
    }

    fun segundosRestantesPago(): Int {
        val inicio = pagoInicioMillis ?: return duracionPagoSegundos
        val transcurridos = ((System.currentTimeMillis() - inicio) / 1000).toInt()
        return (duracionPagoSegundos - transcurridos).coerceAtLeast(0)
    }

    fun reiniciarPago() {
        pagoInicioMillis = null
    }

    fun seleccionarServicio(especialidad: String, profesional: String, modalidadPermitida: String
    ) {
        _uiState.update {
            it.copy(especialidad = especialidad, profesional = profesional, modalidadPermitida = modalidadPermitida
            )
        }
    }

    fun seleccionarModalidad(m: Modalidad) {
        _uiState.update { estado ->
            estado.copy(modalidad = m)
        }
        reiniciarPago()
    }

    fun seleccionarFecha(millis: Long) {
        _uiState.update { estado ->
            estado.copy(fechaMillis = millis, hora = null)
        }
        reiniciarPago()
    }

    fun seleccionarHora(hora: String) {
        val horarioObj = horarios.value.find { it.hora == hora }
        if (horarioObj == null || !horarioObj.disponible) {
            return
        }
        _uiState.update { estado ->
            estado.copy(hora = hora)
        }
        reiniciarPago()
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
        val formato = SimpleDateFormat("EEE d MMM", Locale.forLanguageTag("es-CL"))
        return formato.format(Date(millis))
    }

    fun puedeCancelar(cita: CitaItem): Boolean {
        if (cita.estado != EstadoReserva.CONFIRMADA) return false
        val ahora = System.currentTimeMillis()
        val horasFaltantes = (cita.inicioMillis - ahora) / (1000 * 60 * 60)
        return horasFaltantes >= CANCELACION_HORAS
    }

    fun cancelarCita(id: Int): Boolean {
        val citaActual = _citas.value.find { it.id == id } ?: return false
        if (!puedeCancelar(citaActual)) return false

        _citas.update { lista ->
            lista.map { cita ->
                if (cita.id == id) cita.copy(estado = EstadoReserva.CANCELADA) else cita
            }
        }
        viewModelScope.launch {
            citaDao?.insertar(citaActual.copy(estado = EstadoReserva.CANCELADA).aEntity())
        }
        return true
    }
    // Se llama cada vez que cambia la sesión (login o logout)
    fun establecerUsuario(email: String) {
        _usuarioEmail.value = email
        _ultimaCita.value = null
        _uiState.value = CitaUiState()

        if (email.isBlank()) return
        viewModelScope.launch {
            cargaInicial.join()
            // Datos de ejemplo: se asignan y guardan una sola vez por usuario
            if (_citas.value.none { it.pacienteEmail == email }) {
                val base = _citas.value.maxOfOrNull { it.id } ?: 0
                val ejemplo = generarCitasFalsas().map {
                    it.copy(id = it.id + base, pacienteEmail = email)
                }
                _citas.update { it + ejemplo }
                citaDao?.insertarTodas(ejemplo.map { it.aEntity() })
            }
        }
    }
    fun confirmarCita() {
        val estado = _uiState.value
        val especialidad = if (estado.especialidad.isNotBlank()) estado.especialidad else "Psicología"
        val profesional = if (estado.profesional.isNotBlank()) estado.profesional else "Por asignar"
        val modalidad = estado.modalidad
        val fechaMillis = estado.fechaMillis
        val horaStr = estado.hora
        val paciente = estado.nombrePaciente

        if (modalidad == null || fechaMillis == null || horaStr.isNullOrBlank() || paciente.isBlank()) {
            return
        }

        val cal = Calendar.getInstance().apply {
            timeInMillis = fechaMillis
            val partes = horaStr.split(":")
            val hora = partes.getOrNull(0)?.toIntOrNull() ?: 0
            val minuto = partes.getOrNull(1)?.toIntOrNull() ?: 0
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nuevoId = (_citas.value.maxOfOrNull { it.id } ?: 0) + 1
        val nuevaCita = CitaItem(
            id = nuevoId,
            especialidad = especialidad,
            profesional = profesional,
            modalidad = modalidad,
            inicioMillis = cal.timeInMillis,
            hora = horaStr,
            paciente = paciente,
            estado = EstadoReserva.CONFIRMADA,
            pacienteEmail = _usuarioEmail.value
        )


        _citas.update { it + nuevaCita }
        viewModelScope.launch { citaDao?.insertar(nuevaCita.aEntity()) }
        _ultimaCita.value = nuevaCita

        _uiState.update { actual ->
            actual.copy(
                modalidad = null,
                fechaMillis = null,
                hora = null,
                nombrePaciente = "",
                errorNombre = null
            )
        }
        reiniciarPago()
    }

    private fun calcularHorarios(fechaMillis: Long?, listaCitas: List<CitaItem>): List<Horario> {
        val horariosBase = generarHorariosFalsos(fechaMillis)
        if (fechaMillis == null) {
            return horariosBase
        }

        val calSeleccionada = Calendar.getInstance().apply { timeInMillis = fechaMillis }
        val calCita = Calendar.getInstance()

        val horasOcupadasPorCitas = listaCitas.filter { cita ->
            if (cita.estado != EstadoReserva.CONFIRMADA) return@filter false
            calCita.timeInMillis = cita.inicioMillis
            calCita.get(Calendar.YEAR) == calSeleccionada.get(Calendar.YEAR) &&
                    calCita.get(Calendar.DAY_OF_YEAR) == calSeleccionada.get(Calendar.DAY_OF_YEAR)
        }.map { it.hora }.toSet()

        return horariosBase.map { horario ->
            val libreEnBase = horario.disponible
            val ocupadaPorCita = horasOcupadasPorCitas.contains(horario.hora)
            horario.copy(disponible = libreEnBase && !ocupadaPorCita)
        }
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

    private fun generarCitasFalsas(): List<CitaItem> {
        val cal1 = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 3)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal2 = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal3 = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -5)
            set(Calendar.HOUR_OF_DAY, 11)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal4 = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -10)
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal5 = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -12)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

        return listOf(
            CitaItem(
                id = 1,
                especialidad = "Psicología",
                profesional = "Dra. Ana López",
                modalidad = Modalidad.PRESENCIAL,
                inicioMillis = cal1.timeInMillis,
                hora = "10:00",
                paciente = "Juan Pérez",
                estado = EstadoReserva.CONFIRMADA
            ),
            CitaItem(
                id = 2,
                especialidad = "Nutrición",
                profesional = "Lic. Carlos Ruiz",
                modalidad = Modalidad.ONLINE,
                inicioMillis = cal2.timeInMillis,
                hora = formatoHora.format(cal2.time),
                paciente = "Juan Pérez",
                estado = EstadoReserva.CONFIRMADA
            ),
            CitaItem(
                id = 3,
                especialidad = "Fonoaudiología",
                profesional = "Lic. María Gómez",
                modalidad = Modalidad.PRESENCIAL,
                inicioMillis = cal3.timeInMillis,
                hora = "11:30",
                paciente = "Juan Pérez",
                estado = EstadoReserva.COMPLETADA
            ),
            CitaItem(
                id = 4,
                especialidad = "Terapia Ocupacional",
                profesional = "Dra. Sofía Torres",
                modalidad = Modalidad.ONLINE,
                inicioMillis = cal4.timeInMillis,
                hora = "16:00",
                paciente = "Juan Pérez",
                estado = EstadoReserva.COMPLETADA
            ),
            CitaItem(
                id = 5,
                especialidad = "Psicología",
                profesional = "Dra. Ana López",
                modalidad = Modalidad.PRESENCIAL,
                inicioMillis = cal5.timeInMillis,
                hora = "09:00",
                paciente = "Juan Pérez",
                estado = EstadoReserva.CANCELADA
            )
        )
    }
}

private fun CitaEntity.aItem() = CitaItem(
    id, especialidad, profesional, Modalidad.valueOf(modalidad),
    inicioMillis, hora, paciente, EstadoReserva.valueOf(estado), pacienteEmail
)

private fun CitaItem.aEntity() = CitaEntity(
    id, especialidad, profesional, modalidad.name,
    inicioMillis, hora, paciente, estado.name, pacienteEmail
)