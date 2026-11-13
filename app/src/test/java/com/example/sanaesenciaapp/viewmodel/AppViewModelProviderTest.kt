package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.example.sanaesenciaapp.di.AppContainer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test

class DummySimpleViewModel : ViewModel()

class DummySavedStateViewModel(val savedStateHandle: SavedStateHandle) : ViewModel()

class DummyUnusedViewModel : ViewModel()

class AppViewModelProviderTest {

    private class FakeSimpleModule : AppViewModelModule {
        override fun supports(modelClass: Class<out ViewModel>): Boolean {
            return modelClass == DummySimpleViewModel::class.java
        }

        override fun create(
            modelClass: Class<out ViewModel>,
            appContainer: AppContainer
        ): ViewModel {
            return DummySimpleViewModel()
        }
    }

    private class FakeExtrasModule : AppViewModelModule {
        override fun supports(modelClass: Class<out ViewModel>): Boolean {
            return modelClass == DummySavedStateViewModel::class.java
        }

        override fun create(
            modelClass: Class<out ViewModel>,
            appContainer: AppContainer
        ): ViewModel {
            return DummySavedStateViewModel(SavedStateHandle())
        }

        override fun create(
            modelClass: Class<out ViewModel>,
            appContainer: AppContainer,
            extras: CreationExtras
        ): ViewModel {
            val handle = SavedStateHandle(mapOf("especialidadId" to 3))
            return DummySavedStateViewModel(handle)
        }
    }

    @Test
    fun `inicializar provider sin modulos arroja IllegalArgumentException`() {
        val fakeContainer = createDummyContainer()
        assertThrows(IllegalArgumentException::class.java) {
            AppViewModelProvider(fakeContainer, emptyList())
        }
    }

    @Test
    fun `crear ViewModel simple busca el modulo compatible y retorna la instancia`() {
        val fakeContainer = createDummyContainer()
        val provider = AppViewModelProvider(fakeContainer, listOf(FakeSimpleModule()))

        val viewModel = provider.create(DummySimpleViewModel::class.java)
        assertNotNull(viewModel)
    }

    @Test
    fun `crear ViewModel sin modulo compatible arroja IllegalStateException`() {
        val fakeContainer = createDummyContainer()
        val provider = AppViewModelProvider(fakeContainer, listOf(FakeSimpleModule()))

        assertThrows(IllegalStateException::class.java) {
            provider.create(DummyUnusedViewModel::class.java)
        }
    }

    @Test
    fun `crear ViewModel con multiples modulos compatibles arroja IllegalStateException`() {
        val fakeContainer = createDummyContainer()
        val provider = AppViewModelProvider(fakeContainer, listOf(FakeSimpleModule(), FakeSimpleModule()))

        assertThrows(IllegalStateException::class.java) {
            provider.create(DummySimpleViewModel::class.java)
        }
    }

    @Test
    fun `crear ViewModel con CreationExtras entrega extras al modulo`() {
        val fakeContainer = createDummyContainer()
        val provider = AppViewModelProvider(fakeContainer, listOf(FakeExtrasModule()))

        val extras = MutableCreationExtras()
        val viewModel = provider.create(DummySavedStateViewModel::class.java, extras)

        assertNotNull(viewModel)
        val id = viewModel.savedStateHandle.get<Int>("especialidadId")
        assertEquals(3, id)
    }

    private fun createDummyContainer(): AppContainer {
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val field = unsafeClass.getDeclaredField("theUnsafe")
        field.isAccessible = true
        val unsafe = field.get(null)
        val allocateMethod = unsafeClass.getMethod("allocateInstance", Class::class.java)
        return allocateMethod.invoke(unsafe, AppContainer::class.java) as AppContainer
    }
}
