package com.example.sanaesenciaapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.ESPECIALIDADES_INICIALES
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import kotlinx.coroutines.launch

class CatalogoViewModel(private val db: AppDatabase) : ViewModel() {

    // Lista observable para la interfaz gráfica
    var especialidades by mutableStateOf<List<EspecialidadEntity>>(emptyList())
        private set

    var especialidadSeleccionada by mutableStateOf<EspecialidadEntity?>(null)
        private set

    init {
        cargarEspecialidades()
    }

    fun cargarEspecialidades() {
        viewModelScope.launch {
            val dao = db.especialidadDao()
            var lista = dao.obtenerTodas()

            // Si la base de datos local está vacía, insertamos los datos por defecto (Seed data)
            if (lista.isEmpty()) {
                dao.insertarTodas(ESPECIALIDADES_INICIALES)
                lista = dao.obtenerTodas()
            }
            especialidades = lista
        }
    }

    fun seleccionarEspecialidad(esp: EspecialidadEntity) {
        especialidadSeleccionada = esp
    }
}
