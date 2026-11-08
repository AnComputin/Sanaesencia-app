package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sanaesenciaapp.data.repository.CitaRepository
import com.example.sanaesenciaapp.di.AppContainer

class AgendaViewModelModule : AppViewModelModule {
    override fun supports(modelClass: Class<out ViewModel>): Boolean{
        return modelClass == AgendarviewModel::class.java ||
                modelClass == PagoViewModel::class.java ||
                modelClass == MisCitasViewModel::class.java
    }

    override fun create(
        modelClass: Class<out ViewModel>,
        appContainer: AppContainer
    ): ViewModel{
        val citaRepository = CitaRepository(appContainer.citaDao)

        return when (modelClass){
            AgendarviewModel::class.java -> AgendarviewModel(
                citaRepository = citaRepository,
                sesionRepository = appContainer.sesionRepository
            )

            PagoViewModel::class.java -> PagoViewModel(
                citaRepository = citaRepository
            )

            MisCitasViewModel::class.java -> MisCitasViewModel(
                citaRepository = citaRepository,
                sesionRepository = appContainer.sesionRepository
            )
            else -> throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
        }
    }
}