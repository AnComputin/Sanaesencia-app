package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.CitaEntity
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
    val usuarioEmail: String = "",
    val especialidadId: Int,
    val especialidad: String,
    val especialistaId: Int,
    val profesional: String,
    val modalidad: Modalidad,
    val inicioMillis: Long,
    val finMillis: Long,
    val hora: String,
    val paciente: String,
    val estado: EstadoReserva,
    val boxAsignado: Int? = null
)

data class CitaUiState(
    val especialidadId: Int = 0,
    val especialidad: String = "",
    val especialistaId: Int = 0,
    val profesional: String = "",
    val modalidadPermitida: String = "Ambas",
    val modalidad: Modalidad? = null,
    val fechaMillis: Long? = null,
    val hora: String? = null,
    val nombrePaciente: String = "",
    val errorNombre: String? = null
)

private const val CANCELACION_HORAS = 24
private const val DURACION_CITA_MINUTOS = 60
private const val TOTAL_BOXES = 8

/**
 * ViewModel de reservas.
 *
 * Con un CitaDao real, Room es la fuente de verdad y las citas sobreviven al cierre de la app.
 * Sin DAO (solo previews), usa una lista en memoria para que las vistas puedan renderizarse.
 */
class CitaViewModel(
    private val citaDao: CitaDao? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitaUiState())
    val uiState: StateFlow<CitaUiState> = _uiState.asStateFlow()

    val fechasDisponibles: List<Long> = generarFechasDisponibles()

    private val _citasMemoria = MutableStateFlow<List<CitaItem>>(emptyList())

    val citas: StateFlow<List<CitaItem>> =
        citaDao
            ?.obtenerTodasFlow()
            ?.map { entidades -> entidades.map { it.toItem() } }
            ?.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )
            ?: _citasMemoria.asStateFlow()

    private val _ultimaCita = MutableStateFlow<CitaItem?>(null)
    val ultimaCita: StateFlow<CitaItem?> = _ultimaCita.asStateFlow()

    val horarios: StateFlow<List<Horario>> = combine(_uiState, citas) { estado, listaCitas ->
        calcularHorarios(estado, listaCitas)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = horasBase().map { Horario(it, true) }
    )

    val duracionPagoSegundos: Int = 600
    private var pagoInicioMillis: Long? = null

    val puedeContinuar: StateFlow<Boolean> = _uiState.map { estado ->
        estado.especialidadId > 0 &&
            estado.especialistaId > 0 &&
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

    fun seleccionarServicio(
        especialidad: String,
        profesional: String,
        modalidadPermitida: String,
        especialidadId: Int,
        especialistaId: Int
    ) {
        val modalidadInicial = when {
            modalidadPermitida.equals("Presencial", ignoreCase = true) -> Modalidad.PRESENCIAL
            modalidadPermitida.equals("Online", ignoreCase = true) -> Modalidad.ONLINE
            else -> null
        }

        _uiState.value = CitaUiState(
            especialidadId = especialidadId,
            especialidad = especialidad,
            especialistaId = especialistaId,
            profesional = profesional,
            modalidadPermitida = modalidadPermitida,
            modalidad = modalidadInicial
        )
        reiniciarPago()
    }

    fun seleccionarModalidad(m: Modalidad) {
        _uiState.update { estado ->
            // La disponibilidad puede cambiar entre Online y Presencial por los boxes.
            estado.copy(modalidad = m, hora = null)
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
        if (horarioObj == null || !horarioObj.disponible) return

        _uiState.update { estado -> estado.copy(hora = hora) }
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
        val formato = SimpleDateFormat("EEE d MMM", Locale("es", "CL"))
        return formato.format(Date(millis))
    }

    fun puedeCancelar(cita: CitaItem): Boolean {
        if (cita.estado != EstadoReserva.CONFIRMADA) return false
        val ahora = System.currentTimeMillis()
        val horasFaltantes = (cita.inicioMillis - ahora) / (1000 * 60 * 60)
        return horasFaltantes >= CANCELACION_HORAS
    }

    fun cancelarCita(id: Int): Boolean {
        val citaActual = citas.value.find { it.id == id } ?: return false
        if (!puedeCancelar(citaActual)) return false

        if (citaDao != null) {
            viewModelScope.launch {
                citaDao.actualizarEstado(id, EstadoReserva.CANCELADA.name)
            }
        } else {
            _citasMemoria.update { lista ->
                lista.map { cita ->
                    if (cita.id == id) cita.copy(estado = EstadoReserva.CANCELADA) else cita
                }
            }
        }

        return true
    }

    /**
     * Confirma la reserva solo si el especialista y, cuando corresponde, un box siguen libres.
     * El callback se ejecuta en Main y permite que PagoScreen navegue únicamente si Room confirmó.
     */
    fun confirmarCita(
        usuarioEmail: String,
        onResultado: (Boolean) -> Unit = {}
    ) {
        val estado = _uiState.value
        val modalidad = estado.modalidad
        val fechaMillis = estado.fechaMillis
        val horaStr = estado.hora
        val paciente = estado.nombrePaciente.trim()

        if (
            usuarioEmail.isBlank() ||
            estado.especialidadId <= 0 ||
            estado.especialistaId <= 0 ||
            estado.especialidad.isBlank() ||
            estado.profesional.isBlank() ||
            modalidad == null ||
            fechaMillis == null ||
            horaStr.isNullOrBlank() ||
            paciente.length < 3
        ) {
            onResultado(false)
            return
        }

        val inicio = construirInicio(fechaMillis, horaStr)
        val fin = inicio + DURACION_CITA_MINUTOS * 60_000L

        val entidad = CitaEntity(
            usuarioEmail = usuarioEmail.trim(),
            especialidadId = estado.especialidadId,
            especialidadNombre = estado.especialidad,
            especialistaId = estado.especialistaId,
            especialistaNombre = estado.profesional,
            modalidad = modalidad.name,
            inicioMillis = inicio,
            finMillis = fin,
            hora = horaStr,
            pacienteNombre = paciente,
            estado = EstadoReserva.CONFIRMADA.name,
            boxAsignado = null
        )

        if (citaDao != null) {
            viewModelScope.launch {
                val reservada = runCatching {
                    citaDao.reservarSiDisponible(entidad)
                }.getOrNull()

                if (reservada == null) {
                    // El horario pudo ocuparse mientras el usuario estaba en el pago.
                    _uiState.update { it.copy(hora = null) }
                    onResultado(false)
                    return@launch
                }

                _ultimaCita.value = reservada.toItem()
                limpiarFormularioTrasReserva()
                onResultado(true)
            }
        } else {
            val citaMemoria = reservarEnMemoria(entidad)
            if (citaMemoria == null) {
                _uiState.update { it.copy(hora = null) }
                onResultado(false)
                return
            }

            _citasMemoria.update { it + citaMemoria }
            _ultimaCita.value = citaMemoria
            limpiarFormularioTrasReserva()
            onResultado(true)
        }
    }

    private fun reservarEnMemoria(entidad: CitaEntity): CitaItem? {
        val existentes = _citasMemoria.value

        val especialistaOcupado = existentes.any { cita ->
            cita.estado == EstadoReserva.CONFIRMADA &&
                cita.especialistaId == entidad.especialistaId &&
                seSolapan(cita.inicioMillis, cita.finMillis, entidad.inicioMillis, entidad.finMillis)
        }
        if (especialistaOcupado) return null

        val box = if (entidad.modalidad == Modalidad.PRESENCIAL.name) {
            val ocupadas = existentes.filter { cita ->
                cita.estado == EstadoReserva.CONFIRMADA &&
                    cita.modalidad == Modalidad.PRESENCIAL &&
                    seSolapan(cita.inicioMillis, cita.finMillis, entidad.inicioMillis, entidad.finMillis)
            }

            if (ocupadas.size >= TOTAL_BOXES) return null

            val boxesOcupados = ocupadas.mapNotNull { it.boxAsignado }.toSet()
            (1..TOTAL_BOXES).firstOrNull { it !in boxesOcupados } ?: return null
        } else {
            null
        }

        val nuevoId = (existentes.maxOfOrNull { it.id } ?: 0) + 1
        return entidad.copy(id = nuevoId, boxAsignado = box).toItem()
    }

    private fun limpiarFormularioTrasReserva() {
        _uiState.update { actual ->
            actual.copy(
                modalidad = if (actual.modalidadPermitida.equals("Ambas", ignoreCase = true)) null else actual.modalidad,
                fechaMillis = null,
                hora = null,
                nombrePaciente = "",
                errorNombre = null
            )
        }
        reiniciarPago()
    }

    private fun calcularHorarios(
        estado: CitaUiState,
        listaCitas: List<CitaItem>
    ): List<Horario> {
        val fechaMillis = estado.fechaMillis
        if (fechaMillis == null || estado.especialistaId <= 0) {
            return horasBase().map { Horario(it, true) }
        }

        return horasBase().map { hora ->
            val inicio = construirInicio(fechaMillis, hora)
            val fin = inicio + DURACION_CITA_MINUTOS * 60_000L

            val especialistaOcupado = listaCitas.any { cita ->
                cita.estado == EstadoReserva.CONFIRMADA &&
                    cita.especialistaId == estado.especialistaId &&
                    seSolapan(cita.inicioMillis, cita.finMillis, inicio, fin)
            }

            val hayBoxDisponible = if (estado.modalidad == Modalidad.PRESENCIAL) {
                val presencialesSolapadas = listaCitas.count { cita ->
                    cita.estado == EstadoReserva.CONFIRMADA &&
                        cita.modalidad == Modalidad.PRESENCIAL &&
                        seSolapan(cita.inicioMillis, cita.finMillis, inicio, fin)
                }
                presencialesSolapadas < TOTAL_BOXES
            } else {
                true
            }

            Horario(
                hora = hora,
                disponible = !especialistaOcupado && hayBoxDisponible
            )
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

    private fun construirInicio(fechaMillis: Long, hora: String): Long {
        return Calendar.getInstance().apply {
            timeInMillis = fechaMillis
            val partes = hora.split(":")
            set(Calendar.HOUR_OF_DAY, partes.getOrNull(0)?.toIntOrNull() ?: 0)
            set(Calendar.MINUTE, partes.getOrNull(1)?.toIntOrNull() ?: 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun horasBase(): List<String> =
        listOf("09:00", "10:30", "12:00", "14:00", "15:30", "17:00")

    private fun seSolapan(
        inicioExistente: Long,
        finExistente: Long,
        inicioNuevo: Long,
        finNuevo: Long
    ): Boolean {
        return inicioExistente < finNuevo && finExistente > inicioNuevo
    }
}

class CitaViewModelFactory(
    private val citaDao: CitaDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CitaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CitaViewModel(citaDao) as T
        }
        throw IllegalArgumentException("ViewModel de clase no reconocida")
    }
}

private fun CitaEntity.toItem(): CitaItem {
    val modalidadEnum = runCatching { Modalidad.valueOf(modalidad.uppercase()) }
        .getOrDefault(Modalidad.PRESENCIAL)

    val estadoEnum = runCatching { EstadoReserva.valueOf(estado.uppercase()) }
        .getOrDefault(EstadoReserva.CONFIRMADA)

    return CitaItem(
        id = id,
        usuarioEmail = usuarioEmail,
        especialidadId = especialidadId,
        especialidad = especialidadNombre,
        especialistaId = especialistaId,
        profesional = especialistaNombre,
        modalidad = modalidadEnum,
        inicioMillis = inicioMillis,
        finMillis = finMillis,
        hora = hora,
        paciente = pacienteNombre,
        estado = estadoEnum,
        boxAsignado = boxAsignado
    )
}
