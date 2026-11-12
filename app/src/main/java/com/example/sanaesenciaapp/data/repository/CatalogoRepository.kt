package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.data.room.EspecialidadDao
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.data.room.EspecialistaDao
import com.example.sanaesenciaapp.data.room.EspecialistaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Provides the catalog data as observable Room flows.
 *
 * This repository is read-only so catalog observation does not seed, alter, or delete
 * persisted catalog records.
 */
class CatalogoRepository(
    private val especialidadDao: EspecialidadDao,
    private val especialistaDao: EspecialistaDao
) {
    fun observarEspecialidades(): Flow<List<EspecialidadEntity>> =
        especialidadDao.obtenerTodasFlow()

    fun observarEspecialistas(especialidadId: Int): Flow<List<EspecialistaEntity>> =
        especialistaDao.obtenerPorEspecialidadFlow(especialidadId)
}
