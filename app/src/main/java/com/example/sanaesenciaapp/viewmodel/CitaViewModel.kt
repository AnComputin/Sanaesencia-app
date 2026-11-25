package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.repository.CitaApiException
import com.example.sanaesenciaapp.data.repository.CitaFuente
import com.example.sanaesenciaapp.data.repository.ReglasAgenda
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
    CANCELADA,
    EN_PAGO
}

/**
 * Estados de integración con la fuente remota de citas (VA-05).
 * El mapeo de errores sigue el contrato actual de agenda-service:
 * conflicto = 400 (409 aceptado por si se incorpora), 401/403 = autorización.
 */
sealed interface EstadoIntegracion {
    object Inactivo : EstadoIntegracion
    object Cargando : EstadoIntegracion
    object Exito : EstadoIntegracion
    data class Conflicto(val mensaje: String) : EstadoIntegracion
    data class SinConexion(val mensaje: String) : EstadoIntegracion
    data class NoAutorizado(val mensaje: String) : EstadoIntegracion
    data class Error(val mensaje: String) : EstadoIntegracion
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

/**
 * ViewModel de reservas.
 *
 * Con un CitaDao real, Room es la fuente de verdad y las citas sobreviven al cierre de la app.
 * Sin DAO (solo previews), usa una lista en memoria para que las vistas puedan renderizarse.
 */
class CitaViewModel(
    private val citaDao: CitaDao? = null,
    private val citaFuente: CitaFuente? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitaUiState())
    val uiState: StateFlow<CitaUiState> = _uiState.asStateFlow()

    /*VA-05: estado de la integración con la fuente remota (Inactivo sin fuente registrada)*/
    private val _estadoIntegracion =
        MutableStateFlow<EstadoIntegracion>(EstadoIntegracion.Inactivo)
    val estadoIntegracion: StateFlow<EstadoIntegracion> = _estadoIntegracion.asStateFlow()

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

    val duracionPagoSegundos: Int = ReglasAgenda.DURACION_PAGO_SEGUNDOS
    private var pagoInicioMillis: Long? = null

    /*retencion activa del pago simulado (VA-04): cupo y box conservados como EN_PAGO*/
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

    /**
     * Inicia el pago simulado reteniendo el cupo (VA-04, opción a).
     *
     * 1. Limpia retenciones huérfanas de pagos abandonados.
     * 2. Valida el wizard y crea una cita EN_PAGO (con box asignado si es presencial)
     *    para que nadie más tome el horario durante los [ReglasAgenda.DURACION_PAGO_SEGUNDOS].
     *
     * Retorna false si el wizard está incompleto u el horario ya no está disponible;
     * en ese caso no se retiene nada.
     */
    suspend fun iniciarPago(usuarioEmail: String): Boolean {
        val dao = citaDao

        // Sin DAO (solo previews): el temporizador funciona pero no hay cupo que retener.
        if (dao == null) {
            if (pagoInicioMillis == null) {
                pagoInicioMillis = System.currentTimeMillis()
            }
            return true
        }

        // Huérfanas de pagos abandonados o cerrados con la app en medio del pago.
        runCatching { dao.eliminarRetenciones() }

        val entidad = construirEntidad(usuarioEmail, ReglasAgenda.ESTADO_EN_PAGO)
            ?: return false

        val retenida = runCatching { dao.reservarSiDisponible(entidad) }.getOrNull()
        if (retenida == null) {
            // El horario se ocupó mientras el usuario navegaba hasta el pago.
            _uiState.update { it.copy(hora = null) }
            return false
        }

        retencionId = retenida.id
        retencion = retenida
        if (pagoInicioMillis == null) {
            pagoInicioMillis = System.currentTimeMillis()
        }
        return true
    }

    fun segundosRestantesPago(): Int {
        val inicio = pagoInicioMillis ?: return duracionPagoSegundos
        val transcurridos = ((System.currentTimeMillis() - inicio) / 1000).toInt()
        return (duracionPagoSegundos - transcurridos).coerceAtLeast(0)
    }

    fun reiniciarPago() {
        pagoInicioMillis = null
    }

    /**
     * Libera la retención EN_PAGO si existe (salida del pago, expiración o back).
     * Es no-op si la cita ya fue confirmada: solo borra mientras siga EN_PAGO.
     */
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

    /**
     * VA-05 Fase 1: carga las citas del usuario autenticado desde la fuente remota.
     *
     * Sin fuente registrada (infraestructura de Anaís pendiente) el estado vuelve
     * a [EstadoIntegracion.Inactivo] y Room sigue siendo la única fuente visible.
     * La identidad no viaja en los argumentos: la decide el backend vía JWT.
     */
    fun cargarMisCitas() {
        val fuente = citaFuente
        if (fuente == null) {
            _estadoIntegracion.value = EstadoIntegracion.Inactivo
            return
        }

        _estadoIntegracion.value = EstadoIntegracion.Cargando
        viewModelScope.launch {
            fuente.listarPropias().fold(
                onSuccess = {
                    _estadoIntegracion.value = EstadoIntegracion.Exito
                },
                onFailure = { error ->
                    _estadoIntegracion.value = mapearErrorIntegracion(error)
                }
            )
        }
    }

    private fun mapearErrorIntegracion(error: Throwable): EstadoIntegracion = when {
        error is CitaApiException && error.codigoHttp == 401 ->
            EstadoIntegracion.NoAutorizado("Tu sesión expiró. Vuelve a iniciar sesión.")

        error is CitaApiException && error.codigoHttp == 403 ->
            EstadoIntegracion.NoAutorizado("No tienes permiso para ver estas citas.")

        error is CitaApiException && (error.codigoHttp == 400 || error.codigoHttp == 409) ->
            EstadoIntegracion.Conflicto(
                error.message ?: "La solicitud fue rechazada por el servidor."
            )

        error is java.io.IOException ->
            EstadoIntegracion.SinConexion("Sin conexión con el servidor. Revisa tu internet.")

        else ->
            EstadoIntegracion.Error(
                error.message ?: "Error inesperado al cargar tus citas."
            )
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
        // Si el usuario edita manualmente el nombre, deja de estar vinculado a una ficha.
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

        val dao = citaDao
        if (dao != null) {
            viewModelScope.launch {
                dao.actualizarEstado(id, EstadoReserva.CANCELADA.name)
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
     * Si hay una retención EN_PAGO activa (VA-04), la confirma en su lugar de insertar
     * una fila nueva: el cupo y el box ya están conservados.
     * El callback se ejecuta en Main y permite que PagoScreen navegue únicamente si Room confirmó.
     */
    fun confirmarCita(
        usuarioEmail: String,
        onResultado: (Boolean) -> Unit = {}
    ) {
        val dao = citaDao
        val idRetencion = retencionId

        // Ruta VA-04: la retención existe; solo cambia de EN_PAGO a CONFIRMADA.
        if (dao != null && idRetencion != null) {
            val retenida = retencion
            viewModelScope.launch {
                val existe = runCatching {
                    dao.obtenerTodas().find { it.id == idRetencion && it.estado == ReglasAgenda.ESTADO_EN_PAGO }
                }.getOrNull()

                if (retenida == null || existe == null) {
                    // La retención se perdió (limpieza concurrente): no se puede confirmar.
                    retencionId = null
                    retencion = null
                    _uiState.update { it.copy(hora = null) }
                    onResultado(false)
                    return@launch
                }

                val confirmada = existe.copy(estado = ReglasAgenda.ESTADO_CONFIRMADA)
                val actualizada = runCatching {
                    dao.actualizarEstado(idRetencion, ReglasAgenda.ESTADO_CONFIRMADA)
                }.isSuccess

                if (!actualizada) {
                    retencionId = null
                    retencion = null
                    _uiState.update { it.copy(hora = null) }
                    onResultado(false)
                    return@launch
                }

                retencionId = null
                retencion = null
                _ultimaCita.value = confirmada.toItem()
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

        if (dao != null) {
            viewModelScope.launch {
                val reservada = runCatching {
                    dao.reservarSiDisponible(entidad)
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

    /**
     * Construye la entidad desde el wizard actual. Retorna null si falta cualquier dato
     * obligatorio (incluye un usuario logueado).
     */
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
        listaCitas: List<CitaItem>
    ): List<Horario> {
        val fechaMillis = estado.fechaMillis
        if (fechaMillis == null || estado.especialistaId <= 0) {
            return horasBase().map { Horario(it, true) }
        }

        return horasBase().map { hora ->
            val inicio = construirInicio(fechaMillis, hora)
            val fin = ReglasAgenda.calcularFin(inicio)

            val especialistaOcupado = listaCitas.any { cita ->
                (cita.estado == EstadoReserva.CONFIRMADA || cita.estado == EstadoReserva.EN_PAGO) &&
                    cita.especialistaId == estado.especialistaId &&
                    ReglasAgenda.haySolapamiento(cita.inicioMillis, cita.finMillis, inicio, fin)
            }

            val hayBoxDisponible = if (estado.modalidad == Modalidad.PRESENCIAL) {
                val presencialesSolapadas = listaCitas.count { cita ->
                    (cita.estado == EstadoReserva.CONFIRMADA || cita.estado == EstadoReserva.EN_PAGO) &&
                        cita.modalidad == Modalidad.PRESENCIAL &&
                        ReglasAgenda.haySolapamiento(cita.inicioMillis, cita.finMillis, inicio, fin)
                }
                presencialesSolapadas < ReglasAgenda.TOTAL_BOXES
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
