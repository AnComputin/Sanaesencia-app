package com.example.sanaesenciaapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.room.AppDatabase
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
                val iniciales = listOf(
                    EspecialidadEntity(
                        id = 1,
                        nombre = "Psicología Infanto-Juvenil",
                        descripcion = "Acompañamiento psicológico integral para niños y jóvenes.",
                        icono = "🧠",
                        profesional = "Mg. Andrea Soto",
                        modalidad = "Ambas",
                        agendaPausada = false
                    ),
                    EspecialidadEntity(
                        id = 2,
                        nombre = "Fonoaudiología",
                        descripcion = "Evaluación y tratamiento de trastornos del habla y lenguaje.",
                        icono = "🗣️",
                        profesional = "Klga. Camila Rojas",
                        modalidad = "Presencial",
                        agendaPausada = false
                    ),
                    EspecialidadEntity(
                        id = 3,
                        nombre = "Terapia Ocupacional",
                        descripcion = "Estimulación sensorial y desarrollo de autonomía.",
                        icono = "🧩",
                        profesional = "TO. Javiera Paz",
                        modalidad = "Presencial",
                        agendaPausada = true // Cumple con la HU-02 (agenda pausada)
                    ),
                    EspecialidadEntity(
                        id = 4,
                        nombre = "Nutrición Integrativa",
                        descripcion = "Planes nutricionales enfocados en el bienestar holístico.",
                        icono = "🥗",
                        profesional = "Nut. Felipe Morales",
                        modalidad = "Ambas",
                        agendaPausada = false
                    )
                )
                dao.insertarTodas(iniciales)
                lista = dao.obtenerTodas()
            }
            especialidades = lista
        }
    }

    fun seleccionarEspecialidad(esp: EspecialidadEntity) {
        especialidadSeleccionada = esp
    }
}