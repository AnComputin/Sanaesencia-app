package com.example.sanaesenciaapp.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "especialistas",
    foreignKeys = [
        ForeignKey(
            entity = EspecialidadEntity::class,
            parentColumns = ["id"],
            childColumns = ["especialidadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["especialidadId"])]
)
data class EspecialistaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val especialidadId: Int,
    val modalidad: String, // "Presencial", "Online", "Ambas"
    val activo: Boolean = true
)
