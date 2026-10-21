package com.example.sanaesenciaapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citas")
data class CitaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioEmail: String,
    val especialidadId: Int,
    val especialidadNombre: String,
    val especialistaId: Int,
    val especialistaNombre: String,
    val modalidad: String, // "PRESENCIAL", "ONLINE"
    val inicioMillis: Long,
    val finMillis: Long,
    val hora: String,
    val pacienteNombre: String,
    val estado: String, // "CONFIRMADA", "COMPLETADA", "CANCELADA"
    val boxAsignado: Int? = null
)
