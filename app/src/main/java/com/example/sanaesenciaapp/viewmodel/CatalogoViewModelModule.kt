package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sanaesenciaapp.data.repository.CatalogoRepository
import com.example.sanaesenciaapp.di.AppContainer

class CatalogoViewModelModule : AppViewModelModule {

    override fun supports(modelClass: Class<out ViewModel>): Boolean =
        modelClass == CatalogoViewModel::class.java

    override fun create(
        modelClass: Class<out ViewModel>,
        appContainer: AppContainer
    ): ViewModel {
        require(supports(modelClass)) {
            "CatalogoViewModelModule does not support ${modelClass.name}"
        }

        val repository = CatalogoRepository(
            especialidadDao = appContainer.especialidadDao,
            especialistaDao = appContainer.especialistaDao
        )
        return CatalogoViewModel(
            catalogoRepository = repository,
            savedStateHandle = SavedStateHandle()
        )
    }
}
