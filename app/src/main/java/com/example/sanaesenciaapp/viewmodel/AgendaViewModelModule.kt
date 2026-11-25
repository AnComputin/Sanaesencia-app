package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sanaesenciaapp.di.AppContainer

/**
 * Módulo de inyección ViewModel para las características de Agenda y Citas.
 */
class AgendaViewModelModule : AppViewModelModule {
    override fun supports(modelClass: Class<out ViewModel>): Boolean {
        return modelClass == CitaViewModel::class.java
    }

    override fun create(
        modelClass: Class<out ViewModel>,
        appContainer: AppContainer,
    ): ViewModel {
        if (modelClass == CitaViewModel::class.java) {
            return CitaViewModel(appContainer.citaDao)
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
