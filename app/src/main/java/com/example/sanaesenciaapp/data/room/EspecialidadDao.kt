package com.example.sanaesenciaapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EspecialidadDao {
    @Query("SELECT * FROM especialidades")
    suspend fun obtenerTodas(): List<EspecialidadEntity>

    @Query("SELECT * FROM especialidades")
    fun obtenerTodasFlow(): Flow<List<EspecialidadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(lista: List<EspecialidadEntity>)

    @Query("UPDATE especialidades SET modalidad = :modalidad WHERE id = :id")
    suspend fun actualizarModalidad(id: Int, modalidad: String)

    @Query("UPDATE especialidades SET agendaPausada = :agendaPausada WHERE id = :id")
    suspend fun actualizarAgendaPausada(id: Int, agendaPausada: Boolean)
}