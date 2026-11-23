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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
                    assertFalse(viewModel.uiState.value.cargandoEspecialidades)
                    assertFalse(viewModel.uiState.value.cargandoEspecialistas)
                    assertFalse(viewModel.uiState.value.errorEspecialidades)
                    assertFalse(viewModel.uiState.value.errorEspecialistas)
                    assertEquals(4, viewModel.especialidadSeleccionada()?.id)
                } finally {
                    viewModelStore.clear()
                }
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun viewModelManejaErrorYReintentoDeEspecialidades() {
        val scheduler = kotlinx.coroutines.test.TestCoroutineScheduler()
        val dispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(dispatcher)
        try {
            runTest(dispatcher) {
                var debeFallar = true
                val repository = CatalogoRepository(
                    especialidadDao = object : EspecialidadDao {
                        override suspend fun obtenerTodas(): List<EspecialidadEntity> = emptyList()
                        override fun obtenerTodasFlow(): Flow<List<EspecialidadEntity>> = flow {
                            if (debeFallar) throw RuntimeException("Error simulado de base de datos")
                            emit(
                                listOf(
                                    EspecialidadEntity(
                                        id = 1,
                                        nombre = "Psicología",
                                        descripcion = "",
                                        icono = "",
                                        profesional = "",
                                        modalidad = "Ambas"
                                    )
                                )
                            )
                        }
                        override suspend fun insertarTodas(lista: List<EspecialidadEntity>) = Unit
                        override suspend fun actualizarTodas(lista: List<EspecialidadEntity>) = Unit
                        override suspend fun actualizarModalidad(id: Int, modalidad: String) = Unit
                        override suspend fun actualizarAgendaPausada(id: Int, agendaPausada: Boolean) = Unit
                    },
                    especialistaDao = FakeEspecialistaDao(emptyList())
                )
                val viewModel = CatalogoViewModel(
                    catalogoRepository = repository,
                    savedStateHandle = SavedStateHandle()
                )

                backgroundScope.launch(dispatcher) {
                    viewModel.especialidades.collect()
                }
                advanceUntilIdle()

                assertTrue(viewModel.uiState.value.errorEspecialidades)
                assertFalse(viewModel.uiState.value.cargandoEspecialidades)
                assertTrue(viewModel.especialidades.value.isEmpty())

                // Simular reintento exitoso
                debeFallar = false
                viewModel.reintentarEspecialidades()
                advanceUntilIdle()

                assertFalse(viewModel.uiState.value.errorEspecialidades)
                assertFalse(viewModel.uiState.value.cargandoEspecialidades)
                assertEquals(1, viewModel.especialidades.value.size)
                assertEquals(1, viewModel.especialidades.value.first().id)
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun viewModelManejaErrorYReintentoDeEspecialistas() {
        val scheduler = kotlinx.coroutines.test.TestCoroutineScheduler()
        val dispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(dispatcher)
        try {
            runTest(dispatcher) {
                var debeFallar = true
                val repository = CatalogoRepository(
                    especialidadDao = FakeEspecialidadDao(),
                    especialistaDao = object : EspecialistaDao {
                        override fun obtenerPorEspecialidadFlow(especialidadId: Int): Flow<List<EspecialistaEntity>> = flow {
                            if (debeFallar) throw RuntimeException("Error de red o DB")
                            emit(
                                listOf(
                                    EspecialistaEntity(
                                        id = 1,
                                        nombre = "Dra. Test",
                                        especialidadId = especialidadId,
                                        modalidad = "Ambas"
                                    )
                                )
                            )
                        }
                        override suspend fun obtenerPorEspecialidad(especialidadId: Int): List<EspecialistaEntity> = emptyList()
                        override suspend fun obtenerPorId(id: Int): EspecialistaEntity? = null
                        override suspend fun obtenerTodos(): List<EspecialistaEntity> = emptyList()
                        override suspend fun insertarTodos(lista: List<EspecialistaEntity>) = Unit
                    }
                )
                val viewModel = CatalogoViewModel(
                    catalogoRepository = repository,
                    savedStateHandle = SavedStateHandle(mapOf("especialidadId" to 4))
                )

                backgroundScope.launch(dispatcher) {
                    viewModel.especialistas.collect()
                }
                advanceUntilIdle()

                assertTrue(viewModel.uiState.value.errorEspecialistas)
                assertFalse(viewModel.uiState.value.cargandoEspecialistas)
                assertTrue(viewModel.especialistas.value.isEmpty())

                // Reintentar especialistas
                debeFallar = false
                viewModel.reintentarEspecialistas()
                advanceUntilIdle()

                assertFalse(viewModel.uiState.value.errorEspecialistas)
                assertFalse(viewModel.uiState.value.cargandoEspecialistas)
                assertEquals(1, viewModel.especialistas.value.size)
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun viewModelSinEspecialidadIdRetornaListaVaciaYNoCarga() {
        val repository = CatalogoRepository(
            especialidadDao = FakeEspecialidadDao(),
            especialistaDao = FakeEspecialistaDao(emptyList())
        )
        val viewModel = CatalogoViewModel(
            catalogoRepository = repository,
            savedStateHandle = SavedStateHandle()
        )

        assertNull(viewModel.especialidadId.value)
        assertNull(viewModel.especialidadSeleccionada())
    }

    @Test
    fun viewModelConEspecialidadSinEspecialistasRetornaListaVaciaSinError() {
        val scheduler = kotlinx.coroutines.test.TestCoroutineScheduler()
        val dispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(dispatcher)
        try {
            runTest(dispatcher) {
                val repository = CatalogoRepository(
                    especialidadDao = FakeEspecialidadDao(),
                    especialistaDao = FakeEspecialistaDao(emptyList())
                )
                val viewModel = CatalogoViewModel(
                    catalogoRepository = repository,
                    savedStateHandle = SavedStateHandle(mapOf("especialidadId" to 99))
                )

                backgroundScope.launch(dispatcher) {
                    viewModel.especialistas.collect()
                }
                advanceUntilIdle()

                assertFalse(viewModel.uiState.value.cargandoEspecialistas)
                assertFalse(viewModel.uiState.value.errorEspecialistas)
                assertTrue(viewModel.especialistas.value.isEmpty())
                assertEquals(99, viewModel.especialidadId.value)
                assertNull(viewModel.especialidadSeleccionada())
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
