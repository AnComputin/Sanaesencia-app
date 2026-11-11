package com.example.sanaesenciaapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "especialidades")
data class EspecialidadEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val descripcion: String,
    val icono: String,
    val profesional: String,
    val modalidad: String, // "Presencial", "Online", "Ambas"
    val agendaPausada: Boolean = false
)