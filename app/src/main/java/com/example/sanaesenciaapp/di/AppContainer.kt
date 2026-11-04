package com.example.sanaesenciaapp.di

import android.content.Context
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.BeneficiarioDao
import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.EspecialidadDao
import com.example.sanaesenciaapp.data.room.EspecialistaDao
import com.example.sanaesenciaapp.data.room.UsuarioDao
import com.example.sanaesenciaapp.data.repository.SesionRepository

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: AppDatabase = AppDatabase.getInstance(appContext)

    val usuarioDao: UsuarioDao = database.usuarioDao()
    val especialidadDao: EspecialidadDao = database.especialidadDao()
    val especialistaDao: EspecialistaDao = database.especialistaDao()
    val citaDao: CitaDao = database.citaDao()
    val beneficiarioDao: BeneficiarioDao = database.beneficiarioDao()

    val sesionRepository = SesionRepository()
}
