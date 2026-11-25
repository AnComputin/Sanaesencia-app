package com.example.sanaesenciaapp.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(

    @PrimaryKey
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val email: String,

    val nombre: String,

    val password: String,

    val rut: String,

    val telefono: String,

    val fechaNacimiento: String,

    val comuna: String,

    val rol: String
)