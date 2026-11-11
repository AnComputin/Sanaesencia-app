package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.CitaEntity
import com.example.sanaesenciaapp.data.room.EspecialidadDao
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

private const val TOTAL_BOXES_ADMIN = 8
private const val DURACION_CITA_ADMIN_MINUTOS = 60

data class BoxEstadoAdmin(
    val numero: Int,
    val citas: List<CitaEntity> = emptyList()
) {
    val ocupado: Boolean
        get() = citas.isNotEmpty()

    val tieneConflicto: Boolean
        get() = citas.size > 1
}

data class BoxesAdminUiState(
    val fechaMillis: Long,
    val hora: String,
    val inicioMillis: Long,
    val finMillis: Long,
    val boxes: List<BoxEstadoAdmin>
) {
    val ocupados: Int
        get() = boxes.count { it.ocupado }

    val disponibles: Int
        get() = TOTAL_BOXES_ADMIN - ocupados

    val capacidadCompleta: Boolean
        get() = disponibles == 0
}

class AdminViewModel(
    private val especialidadDao: EspecialidadDao,
    private val citaDao: CitaDao
) : ViewModel() {

    val especialidades: StateFlow<List<EspecialidadEntity>> =
        especialidadDao.obtenerTodasFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val citas: StateFlow<List<CitaEntity>> =
        citaDao.obtenerTodasFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    val fechasBoxes: List<Long> = generarFechasDisponibles()
    val horasBoxes: List<String> =
        listOf("09:00", "10:30", "12:00", "14:00", "15:30", "17:00")

    private val _fechaBoxes = MutableStateFlow(
        fechasBoxes.firstOrNull() ?: inicioDelDia(System.currentTimeMillis())
    )
    val fechaBoxes: StateFlow<Long> = _fechaBoxes

    private val _horaBoxes = MutableStateFlow(horasBoxes.first())
    val horaBoxes: StateFlow<String> = _horaBoxes

    val boxesUiState: StateFlow<BoxesAdminUiState> =
        combine(citas, _fechaBoxes, _horaBoxes) { listaCitas, fecha, hora ->
            crearEstadoBoxes(
                listaCitas = listaCitas,
                fechaMillis = fecha,
                hora = hora
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = crearEstadoBoxes(
                listaCitas = emptyList(),
                fechaMillis = _fechaBoxes.value,
                hora = _horaBoxes.value
            )
        )

    fun actualizarModalidad(id: Int, nuevaModalidad: String) {
        viewModelScope.launch {
            especialidadDao.actualizarModalidad(id, nuevaModalidad)
        }
    }

    fun actualizarEstadoAgenda(id: Int, agendaDisponible: Boolean) {
        viewModelScope.launch {
            // agendaDisponible = true -> agendaPausada = false
            especialidadDao.actualizarAgendaPausada(id, !agendaDisponible)
        }
    }

    fun seleccionarFechaBoxes(fechaMillis: Long) {
        _fechaBoxes.value = inicioDelDia(fechaMillis)
    }

    fun seleccionarHoraBoxes(hora: String) {
        if (hora in horasBoxes) {
            _horaBoxes.value = hora
        }
    }

    private fun crearEstadoBoxes(
        listaCitas: List<CitaEntity>,
        fechaMillis: Long,
        hora: String
    ): BoxesAdminUiState {
        val inicio = construirInicio(fechaMillis, hora)
        val fin = inicio + DURACION_CITA_ADMIN_MINUTOS * 60_000L

        val citasPresenciales = listaCitas.filter { cita ->
            cita.estado.equals("CONFIRMADA", ignoreCase = true) &&
                cita.modalidad.equals("PRESENCIAL", ignoreCase = true) &&
                cita.boxAsignado in 1..TOTAL_BOXES_ADMIN &&
                cita.inicioMillis < fin &&
                cita.finMillis > inicio
        }

        val citasPorBox = citasPresenciales.groupBy { it.boxAsignado!! }

        val boxes = (1..TOTAL_BOXES_ADMIN).map { numero ->
            BoxEstadoAdmin(
                numero = numero,
                citas = citasPorBox[numero].orEmpty()
            )
        }

        return BoxesAdminUiState(
            fechaMillis = fechaMillis,
            hora = hora,
            inicioMillis = inicio,
            finMillis = fin,
            boxes = boxes
        )
    }

    private fun generarFechasDisponibles(): List<Long> {
        val fechas = mutableListOf<Long>()
        val calendario = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (fechas.size < 7) {
            if (calendario.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                fechas.add(calendario.timeInMillis)
            }
            calendario.add(Calendar.DAY_OF_MONTH, 1)
        }

        return fechas
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

    private fun inicioDelDia(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

class AdminViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(
                especialidadDao = db.especialidadDao(),
                citaDao = db.citaDao()
            ) as T
        }
        throw IllegalArgumentException("ViewModel de clase no reconocida")
    }
}
