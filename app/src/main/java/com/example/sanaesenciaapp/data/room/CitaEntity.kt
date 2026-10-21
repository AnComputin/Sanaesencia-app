package com.example.sanaesenciaapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citas")
data class CitaEntity(
    @PrimaryKey val id: Int,
    val especialidad: String,
    val profesional: String,
    val modalidad: String,
    val inicioMillis: Long,
    val hora: String,
    val paciente: String,
    val estado: String,
    val pacienteEmail: String
)