package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.example.sanaesenciaapp.data.repository.CatalogoRepository
import com.example.sanaesenciaapp.data.room.EspecialidadDao
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.data.room.EspecialistaDao
import com.example.sanaesenciaapp.data.room.EspecialistaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogoViewModelModuleTest {

    @Test
    fun supportsSoloCatalogoViewModel() {
        val module = CatalogoViewModelModule()

        assertTrue(module.supports(CatalogoViewModel::class.java))
        assertFalse(module.supports(BeneficiarioViewModel::class.java))
    }

    @Test
    fun viewModelLeeEspecialidadIdYObservaEspecialistasFiltrados() {
        val scheduler = kotlinx.coroutines.test.TestCoroutineScheduler()
        val dispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(dispatcher)
        try {
            runTest(dispatcher) {
                val viewModelStore = ViewModelStore()
                try {
            val especialista = EspecialistaEntity(
                id = 10,
                nombre = "Profesional de prueba",
                especialidadId = 4,
                modalidad = "Online"
            )
            val repository = CatalogoRepository(
                especialidadDao = FakeEspecialidadDao(),
                especialistaDao = FakeEspecialistaDao(listOf(especialista))
            )
            val viewModel = CatalogoViewModel(
                catalogoRepository = repository,
                savedStateHandle = SavedStateHandle(mapOf("especialidadId" to 4))
            )
            viewModelStore.put("catalogo", viewModel)
            backgroundScope.launch(dispatcher) {
                viewModel.especialistas.collect()
            }
            backgroundScope.launch(dispatcher) {
                viewModel.especialidades.collect()
            }

            advanceUntilIdle()

            assertEquals(4, viewModel.especialidadId.value)
            assertEquals(listOf(especialista), viewModel.especialistas.value)
            assertEquals(listOf(4, 9), viewModel.especialidades.value.map { it.id })
                } finally {
                    viewModelStore.clear()
                }
            }
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class FakeEspecialidadDao : EspecialidadDao {
    private val data = MutableStateFlow(
        listOf(
            EspecialidadEntity(
                id = 9,
                nombre = "Especialidad posterior",
                descripcion = "",
                icono = "",
                profesional = "",
                modalidad = "Ambas"
            ),
            EspecialidadEntity(
                id = 4,
                nombre = "Especialidad de prueba",
                descripcion = "",
                icono = "",
                profesional = "",
                modalidad = "Ambas"
            )
        )
    )

    override suspend fun obtenerTodas(): List<EspecialidadEntity> = data.value
    override fun obtenerTodasFlow(): Flow<List<EspecialidadEntity>> = data
    override suspend fun insertarTodas(lista: List<EspecialidadEntity>) = Unit
    override suspend fun actualizarTodas(lista: List<EspecialidadEntity>) = Unit
    override suspend fun actualizarModalidad(id: Int, modalidad: String) = Unit
    override suspend fun actualizarAgendaPausada(id: Int, agendaPausada: Boolean) = Unit
}

private class FakeEspecialistaDao(
    especialistas: List<EspecialistaEntity>
) : EspecialistaDao {
    private val data = especialistas

    override fun obtenerPorEspecialidadFlow(especialidadId: Int): Flow<List<EspecialistaEntity>> =
        MutableStateFlow(data.filter { it.especialidadId == especialidadId })

    override suspend fun obtenerPorEspecialidad(especialidadId: Int): List<EspecialistaEntity> =
        data.filter { it.especialidadId == especialidadId }

    override suspend fun obtenerPorId(id: Int): EspecialistaEntity? =
        data.firstOrNull { it.id == id }

    override suspend fun obtenerTodos(): List<EspecialistaEntity> = data
    override suspend fun insertarTodos(lista: List<EspecialistaEntity>) = Unit
}
