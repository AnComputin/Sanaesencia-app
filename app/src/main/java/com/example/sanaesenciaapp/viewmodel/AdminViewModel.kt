package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.EspecialidadDao
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(private val especialidadDao: EspecialidadDao) : ViewModel() {

    val especialidades: StateFlow<List<EspecialidadEntity>> =
        especialidadDao.obtenerTodasFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
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
}

class AdminViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(db.especialidadDao()) as T
        }
        throw IllegalArgumentException("ViewModel de clase no reconocida")
    }
}
