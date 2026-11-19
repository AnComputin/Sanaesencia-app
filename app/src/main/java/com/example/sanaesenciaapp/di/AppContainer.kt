package com.example.sanaesenciaapp.di

import android.content.Context
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.BeneficiarioDao
import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.EspecialidadDao
import com.example.sanaesenciaapp.data.room.EspecialistaDao
import com.example.sanaesenciaapp.data.room.UsuarioDao
import com.example.sanaesenciaapp.data.repository.BeneficiarioRepository
import com.example.sanaesenciaapp.data.repository.SesionRepository
import com.example.sanaesenciaapp.data.repository.UsuarioRepository

open class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    open val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }

    open val usuarioDao: UsuarioDao by lazy { database.usuarioDao() }
    open val especialidadDao: EspecialidadDao by lazy { database.especialidadDao() }
    open val especialistaDao: EspecialistaDao by lazy { database.especialistaDao() }
    open val citaDao: CitaDao by lazy { database.citaDao() }
    open val beneficiarioDao: BeneficiarioDao by lazy { database.beneficiarioDao() }

    open val usuarioRepository: UsuarioRepository by lazy { UsuarioRepository(usuarioDao) }
    open val sesionRepository: SesionRepository by lazy { SesionRepository(usuarioRepository) }
    open val beneficiarioRepository: BeneficiarioRepository by lazy {
        BeneficiarioRepository(beneficiarioDao, sesionRepository)
    }
}
