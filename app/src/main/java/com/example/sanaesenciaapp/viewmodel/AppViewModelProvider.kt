package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.sanaesenciaapp.di.AppContainer

/**
 * Contrato de módulo ViewModel por característica o integrante (Mark, Valentina, Alejandra, Anaís).
 *
 * Cada integrante debe crear su propia implementación de [AppViewModelModule] dentro de su paquete
 * de módulo (ej. `AuthViewModelModule` para Mark, `AgendaViewModelModule` para Valentina, `CatalogoViewModelModule` para Alejandra).
 *
 * Guía de integración para el equipo:
 * 1. Implementar [supports] para responder `true` únicamente con las clases ViewModel asignadas a su módulo.
 * 2. Implementar [create] para instanciar sus ViewModels obteniendo dependencias compartidas de [AppContainer].
 * 3. Para ViewModels que requieran [androidx.lifecycle.SavedStateHandle], implementar la sobrecarga de [create] que recibe [CreationExtras].
 * 4. Pasar su módulo a la lista de [AppViewModelProvider].
 * 5. NO modificar los archivos ni módulos pertenecientes a otros integrantes.
 */
interface AppViewModelModule {
    fun supports(modelClass: Class<out ViewModel>): Boolean

    fun create(
        modelClass: Class<out ViewModel>,
        appContainer: AppContainer
    ): ViewModel

    fun create(
        modelClass: Class<out ViewModel>,
        appContainer: AppContainer,
        extras: CreationExtras
    ): ViewModel = create(modelClass, appContainer)
}

/**
 * Punto de despacho compartido para los módulos de ViewModel por integrante.
 * Recibe la lista de módulos explícitamente sin acoplarlos en un único archivo.
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
        return create(modelClass, CreationExtras.Empty)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val matchingModules = modules.filter { module ->
            module.supports(modelClass)
        }
        check(matchingModules.size == 1) {
            "Expected exactly one ViewModel module for ${modelClass.name}, found ${matchingModules.size}"
        }

        val viewModel = matchingModules.single().create(modelClass, appContainer, extras)
        require(modelClass.isInstance(viewModel)) {
            "The ViewModel module returned ${viewModel::class.java.name} for ${modelClass.name}"
        }
        return modelClass.cast(viewModel)
            ?: throw IllegalStateException("The ViewModel module returned null for ${modelClass.name}")
    }
}
