package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.repository.CitaRepository
import com.example.sanaesenciaapp.data.repository.SesionRepository
import com.example.sanaesenciaapp.data.room.CitaEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*Estado de la pantalla Mis Citas.*/

data class MisCitasUiState(
    val citas: List<CitaEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorGeneral: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MisCitasViewModel(
    private val citaRepository: CitaRepository,
    sesionRepository: SesionRepository
) : ViewModel() {

    private val _errorGeneral = MutableStateFlow<String?>(null)
    val errorGeneral: StateFlow<String?> = _errorGeneral.asStateFlow()

    /* Flujo reactivo de citas del usuario autenticado.*/
    val citas: StateFlow<List<CitaEntity>> = sesionRepository.sesionActual
        .flatMapLatest { usuario ->
            if (usuario != null) {
                citaRepository.obtenerCitasPorUsuario(usuario.email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /*Cancela una cita por su id.*/
    fun cancelarCita(id: Int) {
        viewModelScope.launch {
            try {
                citaRepository.cancelarCita(id)
            } catch (e: Exception) {
                _errorGeneral.value = "Error al cancelar la cita"
            }
        }
    }

    /*Limpia el error general.*/
    fun limpiarError() {
        _errorGeneral.value = null
    }
}