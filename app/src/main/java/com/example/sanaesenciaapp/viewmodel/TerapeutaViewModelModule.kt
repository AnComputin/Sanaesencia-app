package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sanaesenciaapp.di.AppContainer

/**
 * Módulo de inyección ViewModel para las características del portal Terapeuta.
 */
class TerapeutaViewModelModule : AppViewModelModule {
    override fun supports(modelClass: Class<out ViewModel>): Boolean {
        return modelClass == TerapeutaViewModel::class.java
    }

    override fun create(
        modelClass: Class<out ViewModel>,
        appContainer: AppContainer,
    ): ViewModel {
        if (modelClass == TerapeutaViewModel::class.java) {
            return TerapeutaViewModel()
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
