package com.example.sanaesenciaapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios")
    suspend fun obtenerTodos(): List<UsuarioEntity>
}