package com.example.sanaesenciaapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios")
    suspend fun obtenerTodos(): List<UsuarioEntity>

    @Query("SELECT * FROM usuarios WHERE email = :email COLLATE NOCASE")
    suspend fun obtenerPorEmail(email: String): UsuarioEntity?

    @Query("UPDATE usuarios SET password = :nuevaPassword WHERE email = :email COLLATE NOCASE")
    suspend fun actualizarPassword(email: String, nuevaPassword: String): Int
}