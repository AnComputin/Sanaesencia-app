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

data class AgendarUiState(
    val especialidadId: Int = 0,
    val especialidadNombre: String = "",
    val especialistaId: Int = 0,
    val especialistaNombre: String = "",
    val modalidad: String = "",
    val fechaMillis: Long? = null,
    val hora: String = "",
    val inicioMillis: Long = 0,
    val nombrePaciente: String = "",
    val errorNombre: String? = null,
    val errorModalidad: String? = null,
    val errorFecha: String? = null,
    val errorHora: String? = null,
    val isLoading: Boolean = false,
    val reservaExitosa: Boolean = false,
    val errorGeneral: String? = null
)

class AgendarviewModel(
    private val citaRepository: CitaRepository,
    private val sesionRepository: SesionRepository
): ViewModel(){
    private val  _uiState = MutableStateFlow(AgendarUiState())
    val uiState: StateFlow<AgendarUiState> = _uiState.asStateFlow()

    //seleccionar servicio
    fun seleccionarServicio(especialidadId: Int, especialidadNombre: String){
        _uiState.update {
            it.copy(especialidadId = especialidadId, especialidadNombre = especialidadNombre)
        }
    }

    fun seleccionarEspecialista(especialistaId: Int, especialistaNombre: String){
        _uiState.update {
            it.copy(especialistaId = especialistaId, especialistaNombre = especialistaNombre)
        }
    }
    //modalidad
    fun seleccionarModalidad(modalidad: String){
        val error = if (!ReglasAgenda.esModalidadValida(modalidad)){
            "Modalidad inválida"
        }else null
        _uiState.update {
            it.copy(modalidad=modalidad, errorModalidad = error)
        }
    }
    //fecha y hora
    fun seleccionarFecha(fechaMillis: Long){
        _uiState.update { it.copy(fechaMillis=fechaMillis, errorFecha = null) }
    }

    fun seleccionarHora(hora: String, inicioMillis: Long){
        _uiState.update {
            it.copy(hora = hora, inicioMillis = inicioMillis, errorHora = null)
        }
    }

    //nombre paciente
    fun cambiarNombre(nombre: String){
        val error = if (nombre.trim().length <3){
            "El nombre debe tener al menos 3 caracteres"
        }else null
        _uiState.update {
            it.copy(nombrePaciente = nombre, errorNombre = error)
        }
    }

    //confirma reserva
    fun confirmarReserva(){
        val state = _uiState.value

        //valida todo antes de reserva
        val errorNombre = if (state.nombrePaciente.trim().length<3){
            "El nombre debe tener al menos 3 caracteres"
        }else null

        val errorModalidad = if (!ReglasAgenda.esModalidadValida(state.modalidad)){
            "Debe seleccionar una modalidad"
        }else null

        val errorFecha = if (state.fechaMillis == null){
            "Debe seleccionar una fecha"
        }else null

        val errorHora = if (state.hora.isBlank()){
            "Debe seleccionar una hora"
        }else null

        if (errorNombre != null || errorModalidad !=null || errorFecha != null || errorHora !=null){
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

        //reservar
        _uiState.update { it.copy(isLoading = true, errorGeneral = null) }

        viewModelScope.launch {
            val usuario = sesionRepository.sesionActual.value
            if (usuario == null){
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

            if (resultado != null){
                _uiState.update {
                    it.copy(isLoading = false, reservaExitosa = true)
                }
            }else{
                _uiState.update {
                    it.copy(isLoading = false, errorGeneral = "El horario ya no está disponible")
                }
            }
        }
    }
    //limpiar estado
    fun limpiarEstado(){
        _uiState.value = AgendarUiState()
    }
}