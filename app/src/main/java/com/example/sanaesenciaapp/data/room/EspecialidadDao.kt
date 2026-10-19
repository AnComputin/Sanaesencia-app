package com.example.sanaesenciaapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EspecialidadDao {
    @Query("SELECT * FROM especialidades")
    suspend fun obtenerTodas(): List<EspecialidadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(lista: List<EspecialidadEntity>)
}