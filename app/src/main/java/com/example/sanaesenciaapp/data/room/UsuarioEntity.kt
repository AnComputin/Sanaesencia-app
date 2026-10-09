package com.example.sanaesenciaapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(

    @PrimaryKey
    val email: String,

    val nombre: String,

    val password: String
)