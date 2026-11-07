package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.CitaEntity
import kotlinx.coroutines.flow.Flow

class CitaRepository(private val citaDao: CitaDao
){
    fun obtenerCitasPorUsuario(email: String): Flow<List<CitaEntity>>{
        return citaDao.obtenerPorUsuarioFlow(email)
    }

    /*obtine toda la cita(para administrativo y terapeuta)*/
    fun obtenerTodasLasCitas(): Flow<List<CitaEntity>>{
        return citaDao.obtenerTodasFlow()
    }
    /*reserva cita. retorna cita guardadad si no null*/
    suspend fun reservarCita(cita: CitaEntity): CitaEntity?{
        return citaDao.reservarSiDisponible(cita)
    }
    /*actualiza estado de cita*/
    suspend fun actualizarEstadoCita(id: Int,nuevoEstado: String){
        citaDao.actualizarEstado(id,nuevoEstado)
    }

    /*cancela una cita*/
    suspend fun cancelarCita(id: Int){
        citaDao.actualizarEstado(id,"CANCELADA")
    }

    /*MARCA CITA COMO COMPLETA*/
    suspend fun completarCita(id: Int){
        citaDao.actualizarEstado(id,"COMPLETADA")
    }
}