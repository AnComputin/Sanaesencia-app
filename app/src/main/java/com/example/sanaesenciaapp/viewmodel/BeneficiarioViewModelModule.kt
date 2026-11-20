package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sanaesenciaapp.di.AppContainer

class BeneficiarioViewModelModule : AppViewModelModule {
    override fun supports(modelClass: Class<out ViewModel>): Boolean =
        modelClass == BeneficiarioViewModel::class.java

    override fun create(
        modelClass: Class<out ViewModel>,
        appContainer: AppContainer
    ): ViewModel {
        require(supports(modelClass)) {
            "BeneficiarioViewModelModule does not support ${modelClass.name}"
        }
        return BeneficiarioViewModel(appContainer.beneficiarioRepository)
    }
}
