package com.example.sanaesenciaapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EspecialistaDao {
    @Query("SELECT * FROM especialistas WHERE especialidadId = :especialidadId AND activo = 1 ORDER BY nombre")
    fun obtenerPorEspecialidadFlow(especialidadId: Int): Flow<List<EspecialistaEntity>>

    @Query("SELECT * FROM especialistas WHERE especialidadId = :especialidadId AND activo = 1 ORDER BY nombre")
    suspend fun obtenerPorEspecialidad(especialidadId: Int): List<EspecialistaEntity>

    @Query("SELECT * FROM especialistas WHERE id = :id")
    suspend fun obtenerPorId(id: Int): EspecialistaEntity?

    @Query("SELECT * FROM especialistas WHERE activo = 1 ORDER BY nombre")
    suspend fun obtenerTodos(): List<EspecialistaEntity>

    // Los datos iniciales solo completan registros faltantes. No sobrescriben cambios futuros.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(lista: List<EspecialistaEntity>)
}
