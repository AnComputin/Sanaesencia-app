package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sanaesenciaapp.di.AppContainer

/**
 * Factory module owned by a feature. Each module handles only the ViewModels
 * belonging to that feature and obtains shared dependencies from the container.
 */
interface AppViewModelModule {
    fun supports(modelClass: Class<out ViewModel>): Boolean

    fun create(
        modelClass: Class<out ViewModel>,
        appContainer: AppContainer
    ): ViewModel
}

/**
 * Shared dispatch point for feature-owned ViewModel modules.
 * Feature modules are supplied explicitly; this class does not own their factories.
 */
class AppViewModelProvider(
    private val appContainer: AppContainer,
    modules: List<AppViewModelModule>
) : ViewModelProvider.Factory {

    private val modules = modules.toList()

    init {
        require(this.modules.isNotEmpty()) {
            "AppViewModelProvider requires at least one feature module"
        }
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val matchingModules = modules.filter { module ->
            module.supports(modelClass)
        }
        check(matchingModules.size == 1) {
            "Expected exactly one ViewModel module for ${modelClass.name}, found ${matchingModules.size}"
        }

        val viewModel = matchingModules.single().create(modelClass, appContainer)
        require(modelClass.isInstance(viewModel)) {
            "The ViewModel module returned ${viewModel::class.java.name} for ${modelClass.name}"
        }
        return modelClass.cast(viewModel)
            ?: throw IllegalStateException("The ViewModel module returned null for ${modelClass.name}")
    }
}
