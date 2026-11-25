package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.remote.RetrofitClient
import com.example.sanaesenciaapp.data.repository.CitaRepository
import com.example.sanaesenciaapp.data.repository.ReglasAgenda
import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.CitaEntity
import kotlinx.coroutines.CancellationException
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
import com.example.sanaesenciaapp.data.remote.CrearCitaRequest
import com.example.sanaesenciaapp.data.remote.OcupacionCitaDto

enum class Modalidad {
    PRESENCIAL,
    ONLINE
}

enum class EstadoReserva {
    CONFIRMADA,
    COMPLETADA,
    CANCELADA,
    EN_PAGO
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
    val beneficiarioId: Int? = null,
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
    val beneficiarioId: Int? = null,
    val errorNombre: String? = null
)

class CitaViewModel(
    private val citaDao: CitaDao? = null
) : ViewModel() {

    private val citaRepository: CitaRepository? = citaDao?.let { CitaRepository(it) }

    private val _uiState = MutableStateFlow(CitaUiState())
    val uiState: StateFlow<CitaUiState> = _uiState.asStateFlow()

    val fechasDisponibles: List<Long> = generarFechasDisponibles()

    private val _citasMemoria = MutableStateFlow<List<CitaItem>>(emptyList())

    val citas: StateFlow<List<CitaItem>> =
        citaRepository
            ?.obtenerTodasLasCitas()
            ?.map { entidades -> entidades.map { it.toItem() } }
            ?.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )
            ?: _citasMemoria.asStateFlow()

    private val _ultimaCita = MutableStateFlow<CitaItem?>(null)
    val ultimaCita: StateFlow<CitaItem?> = _ultimaCita.asStateFlow()

    private val _mensajeErrorReserva = MutableStateFlow<String?>(null)
    val mensajeErrorReserva: StateFlow<String?> = _mensajeErrorReserva.asStateFlow()

    private val _ocupacionRemota = MutableStateFlow<List<OcupacionCitaDto>>(emptyList())

    val horarios: StateFlow<List<Horario>> = combine(_uiState, citas, _ocupacionRemota) { estado, listaCitas, ocupacionRemota ->
        calcularHorarios(estado, listaCitas, ocupacionRemota)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = horasBase().map { Horario(it, true) }
    )

    val duracionPagoSegundos: Int = ReglasAgenda.DURACION_PAGO_SEGUNDOS
    private var pagoInicioMillis: Long? = null

    private var retencionId: Int? = null
    private var retencion: CitaEntity? = null

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

    fun refrescarOcupacion() {
        val repo = citaRepository ?: return
        viewModelScope.launch {
            repo.obtenerOcupacionRemota()?.let { lista ->
                _ocupacionRemota.value = lista
            }
        }
    }

    private suspend fun refrescarOcupacionInternal() {
        val repo = citaRepository ?: return
        repo.obtenerOcupacionRemota()?.let { lista ->
            _ocupacionRemota.value = lista
        }
    }

    suspend fun iniciarPago(usuarioEmail: String): Boolean {
        _mensajeErrorReserva.value = null
        if (pagoInicioMillis == null) {
            pagoInicioMillis = System.currentTimeMillis()
        }

        val dao = citaDao ?: return true

        citaRepository?.sincronizarCitasDesdeServidor(null)
        refrescarOcupacionInternal()
        runCatching { dao.eliminarRetenciones() }

        val entidad = construirEntidad(usuarioEmail, ReglasAgenda.ESTADO_EN_PAGO)
            ?: return false

        val ocupadaEnServidor = slotOcupadoEnRemoto(
            especialistaId = entidad.especialistaId,
            modalidadPresencial = ReglasAgenda.esPresencial(entidad.modalidad),
            inicio = entidad.inicioMillis,
            fin = entidad.finMillis,
            listaCitas = citas.value,
            ocupacionRemota = _ocupacionRemota.value
        )
        if (ocupadaEnServidor) {
            _mensajeErrorReserva.value = "La hora dejó de estar disponible. Vuelve a seleccionar un horario."
            _uiState.update { it.copy(hora = null) }
            return false
        }

        val retenida = runCatching { dao.reservarSiDisponible(entidad) }.getOrNull()
        if (retenida == null) {
            _mensajeErrorReserva.value = "La hora dejó de estar disponible. Vuelve a seleccionar un horario."
            _uiState.update { it.copy(hora = null) }
            return false
        }

        retencionId = retenida.id
        retencion = retenida
        return true
    }

    fun segundosRestantesPago(): Int {
        val inicio = pagoInicioMillis ?: System.currentTimeMillis().also {
            pagoInicioMillis = it
        }
        val transcurridos = ((System.currentTimeMillis() - inicio) / 1000).toInt()
        return (duracionPagoSegundos - transcurridos).coerceAtLeast(0)
    }

    fun reiniciarPago() {
        pagoInicioMillis = null
    }

    fun limpiarSesion() {
        liberarRetencion()
        _mensajeErrorReserva.value = null
        _ultimaCita.value = null
        _ocupacionRemota.value = emptyList()
        _uiState.value = CitaUiState()
    }

    fun liberarRetencion() {
        val id = retencionId
        retencionId = null
        retencion = null

        val dao = citaDao
        if (id != null && dao != null) {
            viewModelScope.launch {
                runCatching { dao.eliminar(id) }
            }
        }
        reiniciarPago()
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
        refrescarOcupacion()
    }

    fun seleccionarModalidad(m: Modalidad) {
        _uiState.update { estado ->
            estado.copy(modalidad = m, hora = null)
        }
        reiniciarPago()
    }

    fun seleccionarFecha(millis: Long) {
        _uiState.update { estado ->
            estado.copy(fechaMillis = millis, hora = null)
        }
        reiniciarPago()
        refrescarOcupacion()
    }

    fun seleccionarHora(hora: String) {
        val horarioObj = horarios.value.find { it.hora == hora }
        if (horarioObj == null || !horarioObj.disponible) return

        _uiState.update { estado -> estado.copy(hora = hora) }
        reiniciarPago()
    }

    fun seleccionarPaciente(nombre: String, beneficiarioId: Int?) {
        val error = when {
            nombre.isBlank() -> "El nombre no puede estar vacío"
            nombre.trim().length < 3 -> "El nombre debe tener al menos 3 caracteres"
            else -> null
        }

        _uiState.update { estado ->
            estado.copy(
                nombrePaciente = nombre,
                beneficiarioId = beneficiarioId,
                errorNombre = error
            )
        }
    }

    fun cambiarNombre(valor: String) {
        seleccionarPaciente(valor, null)
    }

    fun formatearFecha(millis: Long): String {
        val formato = SimpleDateFormat("EEE d MMM", Locale("es", "CL"))
        return formato.format(Date(millis))
    }

    fun puedeCancelar(cita: CitaItem): Boolean {
        if (cita.estado != EstadoReserva.CONFIRMADA) return false
        val ahora = System.currentTimeMillis()
        val horasFaltantes = (cita.inicioMillis - ahora) / (1000 * 60 * 60)
        return horasFaltantes >= ReglasAgenda.CANCELACION_HORAS
    }

    fun cancelarCita(id: Int): Boolean {
        val citaActual = citas.value.find { it.id == id } ?: return false
        if (!puedeCancelar(citaActual)) return false

        val repo = citaRepository
        if (repo != null) {
            viewModelScope.launch {
                repo.cancelarCita(id)
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

    fun confirmarCita(
        usuarioEmail: String,
        onResultado: (Boolean) -> Unit = {}
    ) {
        _mensajeErrorReserva.value = null
        val dao = citaDao
        val repo = citaRepository
        val idRetencion = retencionId

        // Ruta VA-04: confirma la retención EN_PAGO y registra la cita en agenda-service (:8084)
        if (dao != null && repo != null && idRetencion != null) {
            val retenida = retencion
            viewModelScope.launch {
                val existe = runCatching {
                    dao.obtenerTodas().find { it.id == idRetencion && it.estado == ReglasAgenda.ESTADO_EN_PAGO }
                }.getOrNull()

                if (retenida == null || existe == null) {
                    retencionId = null
                    retencion = null
                    _uiState.update { it.copy(hora = null) }
                    _mensajeErrorReserva.value = "La reserva temporal expiró. Vuelve a seleccionar un horario."
                    onResultado(false)
                    return@launch
                }

                // Se envía boxAsignado = null para que agenda-service asigne automáticamente
                // un box disponible (1..8) según la ocupación real del servidor.
                val requestRemoto = CrearCitaRequest(
                    usuarioEmail = existe.usuarioEmail.trim().lowercase(),
                    especialidadId = existe.especialidadId,
                    especialidadNombre = existe.especialidadNombre,
                    especialistaId = existe.especialistaId,
                    especialistaNombre = existe.especialistaNombre,
                    modalidad = existe.modalidad,
                    inicioMillis = existe.inicioMillis,
                    finMillis = existe.finMillis,
                    hora = existe.hora,
                    pacienteNombre = existe.pacienteNombre,
                    beneficiarioId = existe.beneficiarioId,
                    estado = ReglasAgenda.ESTADO_CONFIRMADA,
                    boxAsignado = null
                )

                var guardadaFinal: CitaEntity? = null
                var rechazadaPorCupo = false
                var errorServidor: String? = null

                if (!RetrofitClient.estaInicializado()) {
                    guardadaFinal = existe.copy(estado = ReglasAgenda.ESTADO_CONFIRMADA)
                } else {
                    try {
                        val response = RetrofitClient.agendaApi.reservarCita(requestRemoto)
                        when {
                            response.isSuccessful -> {
                                guardadaFinal = response.body() ?: existe.copy(estado = ReglasAgenda.ESTADO_CONFIRMADA)
                            }
                            response.code() == 400 || response.code() == 409 -> {
                                rechazadaPorCupo = true
                                errorServidor = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                                    ?: "La hora dejó de estar disponible. Vuelve a seleccionar un horario."
                                _ocupacionRemota.update { actual ->
                                    val nuevoSlot = OcupacionCitaDto(
                                        especialistaId = existe.especialistaId,
                                        modalidad = existe.modalidad,
                                        inicioMillis = existe.inicioMillis,
                                        finMillis = existe.finMillis
                                    )
                                    if (nuevoSlot in actual) actual else actual + nuevoSlot
                                }
                                refrescarOcupacion()
                            }
                            response.code() == 401 -> {
                                errorServidor = "Tu sesión expiró en el servidor. Vuelve a iniciar sesión para confirmar tu cita."
                            }
                            response.code() == 403 -> {
                                errorServidor = "Solo las cuentas con rol Paciente pueden reservar citas."
                            }
                            else -> {
                                errorServidor = "No se pudo confirmar la reserva en el servidor (HTTP ${response.code()}). Intenta nuevamente."
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        errorServidor = "Sin conexión con el servidor de agenda. Intenta nuevamente."
                    }
                }

                if (errorServidor != null || guardadaFinal == null) {
                    _mensajeErrorReserva.value = errorServidor
                        ?: "La hora dejó de estar disponible. Vuelve a seleccionar un horario."
                    if (rechazadaPorCupo || guardadaFinal == null && errorServidor == null) {
                        runCatching { dao.eliminar(idRetencion) }
                        retencionId = null
                        retencion = null
                        _uiState.update { it.copy(hora = null) }
                    }
                    onResultado(false)
                    return@launch
                }

                // Reemplaza la retención temporal por la cita confirmada (evitando colisión de PK local)
                runCatching { dao.eliminar(idRetencion) }
                val guardadaLocal: CitaEntity? =
                    runCatching { dao.reservarSiDisponible(guardadaFinal) }.getOrNull()
                        ?: runCatching { dao.reservarSiDisponible(guardadaFinal.copy(id = 0)) }.getOrNull()

                retencionId = null
                retencion = null
                _ultimaCita.value = (guardadaLocal ?: guardadaFinal).toItem()
                limpiarFormularioTrasReserva()
                onResultado(true)
            }
            return
        }

        val entidad = construirEntidad(usuarioEmail, EstadoReserva.CONFIRMADA.name)
        if (entidad == null) {
            onResultado(false)
            return
        }

        if (repo != null) {
            viewModelScope.launch {
                val reservada = repo.reservarCita(entidad)

                if (reservada == null) {
                    _mensajeErrorReserva.value = "La hora dejó de estar disponible. Vuelve a seleccionar un horario."
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

    private fun construirEntidad(usuarioEmail: String, estado: String): CitaEntity? {
        val estadoUi = _uiState.value
        val modalidad = estadoUi.modalidad
        val fechaMillis = estadoUi.fechaMillis
        val horaStr = estadoUi.hora
        val paciente = estadoUi.nombrePaciente.trim()

        if (
            usuarioEmail.isBlank() ||
            estadoUi.especialidadId <= 0 ||
            estadoUi.especialistaId <= 0 ||
            estadoUi.especialidad.isBlank() ||
            estadoUi.profesional.isBlank() ||
            modalidad == null ||
            fechaMillis == null ||
            horaStr.isNullOrBlank() ||
            paciente.length < 3
        ) {
            return null
        }

        val inicio = construirInicio(fechaMillis, horaStr)
        val fin = ReglasAgenda.calcularFin(inicio)

        return CitaEntity(
            usuarioEmail = usuarioEmail.trim(),
            especialidadId = estadoUi.especialidadId,
            especialidadNombre = estadoUi.especialidad,
            especialistaId = estadoUi.especialistaId,
            especialistaNombre = estadoUi.profesional,
            modalidad = modalidad.name,
            inicioMillis = inicio,
            finMillis = fin,
            hora = horaStr,
            pacienteNombre = paciente,
            beneficiarioId = estadoUi.beneficiarioId,
            estado = estado,
            boxAsignado = null
        )
    }

    private fun reservarEnMemoria(entidad: CitaEntity): CitaItem? {
        val existentes = _citasMemoria.value

        val especialistaOcupado = existentes.any { cita ->
            cita.estado == EstadoReserva.CONFIRMADA &&
                    cita.especialistaId == entidad.especialistaId &&
                    ReglasAgenda.haySolapamiento(cita.inicioMillis, cita.finMillis, entidad.inicioMillis, entidad.finMillis)
        }
        if (especialistaOcupado) return null

        val box = if (entidad.modalidad == Modalidad.PRESENCIAL.name) {
            val ocupadas = existentes.filter { cita ->
                cita.estado == EstadoReserva.CONFIRMADA &&
                        cita.modalidad == Modalidad.PRESENCIAL &&
                        ReglasAgenda.haySolapamiento(cita.inicioMillis, cita.finMillis, entidad.inicioMillis, entidad.finMillis)
            }

            if (ocupadas.size >= ReglasAgenda.TOTAL_BOXES) return null

            val boxesOcupados = ocupadas.mapNotNull { it.boxAsignado }.toSet()
            ReglasAgenda.obtenerBoxLibre(boxesOcupados) ?: return null
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
                beneficiarioId = null,
                errorNombre = null
            )
        }
        reiniciarPago()
    }

    private fun calcularHorarios(
        estado: CitaUiState,
        listaCitas: List<CitaItem>,
        ocupacionRemota: List<OcupacionCitaDto> = emptyList()
    ): List<Horario> {
        val fechaMillis = estado.fechaMillis
        if (fechaMillis == null || estado.especialistaId <= 0) {
            return horasBase().map { Horario(it, true) }
        }

        return horasBase().map { hora ->
            val inicio = construirInicio(fechaMillis, hora)
            val fin = ReglasAgenda.calcularFin(inicio)

            val ocupado = slotOcupadoEnRemoto(
                especialistaId = estado.especialistaId,
                modalidadPresencial = estado.modalidad == Modalidad.PRESENCIAL,
                inicio = inicio,
                fin = fin,
                listaCitas = listaCitas,
                ocupacionRemota = ocupacionRemota
            )

            Horario(
                hora = hora,
                disponible = !ocupado
            )
        }
    }

    private fun slotOcupadoEnRemoto(
        especialistaId: Int,
        modalidadPresencial: Boolean,
        inicio: Long,
        fin: Long,
        listaCitas: List<CitaItem>,
        ocupacionRemota: List<OcupacionCitaDto>
    ): Boolean {
        val activasLocales = listaCitas.filter { cita ->
            cita.estado == EstadoReserva.CONFIRMADA || cita.estado == EstadoReserva.EN_PAGO
        }

        val especialistaOcupado = activasLocales.any { cita ->
            cita.especialistaId == especialistaId &&
                ReglasAgenda.haySolapamiento(cita.inicioMillis, cita.finMillis, inicio, fin)
        } || ocupacionRemota.any { remota ->
            remota.especialistaId == especialistaId &&
                ReglasAgenda.haySolapamiento(remota.inicioMillis, remota.finMillis, inicio, fin)
        }
        if (especialistaOcupado) return true

        if (!modalidadPresencial) return false

        val localesPresenciales = activasLocales.filter { cita ->
            cita.modalidad == Modalidad.PRESENCIAL &&
                ReglasAgenda.haySolapamiento(cita.inicioMillis, cita.finMillis, inicio, fin)
        }
        val clavesLocales = localesPresenciales.map { Triple(it.especialistaId, it.inicioMillis, it.finMillis) }.toSet()

        val remotasAdicionales = ocupacionRemota.count { remota ->
            remota.modalidad.equals(Modalidad.PRESENCIAL.name, ignoreCase = true) &&
                ReglasAgenda.haySolapamiento(remota.inicioMillis, remota.finMillis, inicio, fin) &&
                Triple(remota.especialistaId, remota.inicioMillis, remota.finMillis) !in clavesLocales
        }

        return (localesPresenciales.size + remotasAdicionales) >= ReglasAgenda.TOTAL_BOXES
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
        beneficiarioId = beneficiarioId,
        estado = estadoEnum,
        boxAsignado = boxAsignado
    )
}