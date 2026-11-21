package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.sanaesenciaapp.data.repository.CatalogoRepository
import com.example.sanaesenciaapp.di.AppContainer

class CatalogoViewModelModule : AppViewModelModule {
    override fun supports(modelClass: Class<out ViewModel>): Boolean =
        modelClass == CatalogoViewModel::class.java

    override fun create(
        modelClass: Class<out ViewModel>,
        appContainer: AppContainer
    ): ViewModel = error("CatalogoViewModel requires destination CreationExtras")

    override fun create(
        modelClass: Class<out ViewModel>,
        appContainer: AppContainer,
        extras: CreationExtras
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
            savedStateHandle = extras.createSavedStateHandle()
        )
    }
}
