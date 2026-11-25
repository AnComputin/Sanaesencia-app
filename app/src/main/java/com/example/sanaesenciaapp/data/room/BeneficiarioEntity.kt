package com.example.sanaesenciaapp.data.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persona a cargo del paciente titular que puede recibir una atención.
 * Se relaciona con el titular mediante su correo para mantener el modelo simple
 * y permitir que cada cuenta vea únicamente sus propios beneficiarios.
 */
@Entity(
    tableName = "beneficiarios",
    indices = [
        Index(value = ["propietarioEmail"]),
        Index(value = ["propietarioEmail", "rut"], unique = true)
    ]
)
data class BeneficiarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val propietarioEmail: String,
    val nombre: String,
    val rut: String,
    val fechaNacimiento: String,
    val parentesco: String,
    val activo: Boolean = true
)
