package com.example.sanaesenciaapp.viewmodel

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import com.example.sanaesenciaapp.utils.Seguridad
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import com.example.sanaesenciaapp.data.repository.BeneficiarioRepository
import com.example.sanaesenciaapp.data.repository.SesionRepository
import com.example.sanaesenciaapp.data.repository.UsuarioRepository
import com.example.sanaesenciaapp.data.room.BeneficiarioDao
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import com.example.sanaesenciaapp.data.room.UsuarioDao
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import com.example.sanaesenciaapp.di.AppContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BeneficiarioViewModelModuleTest {
    @Test
    fun supportsSoloBeneficiarioViewModel() {
        val module = BeneficiarioViewModelModule()

        assertTrue(module.supports(BeneficiarioViewModel::class.java))
        assertFalse(module.supports(CatalogoViewModel::class.java))
    }

    @Test
    fun creaViewModelUsandoRepositorioDelContenedor() {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        try {
            runTest(dispatcher) {
                val sesion = SesionRepository(UsuarioRepository(EmptyUsuarioDao()))
                val repository = BeneficiarioRepository(
                    beneficiarioDao = EmptyBeneficiarioDao(),
                    sesionRepository = sesion
                )
                val container = TestAppContainer(repository, sesion)
                val viewModelStore = ViewModelStore()
                try {
                    val viewModel = BeneficiarioViewModelModule().create(
                        BeneficiarioViewModel::class.java,
                        container
                    )
                    viewModelStore.put("beneficiario", viewModel)
                    assertTrue(viewModel is BeneficiarioViewModel)
                    advanceUntilIdle()
                } finally {
                    viewModelStore.clear()
                }
            }
        } finally {
            Dispatchers.resetMain()
        }
    }
    @Test
    fun cambiarDeCuentaLimpiaLaListaAntesDeQueRoomResponda() {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        try {
            runTest(dispatcher) {
                val password = "PruebaTemporal1!"
                val hash = Seguridad.hashear(password)
                val cuentaA = "a@example.com"
                val cuentaB = "b@example.com"
                val usuarios = object : EmptyUsuarioDao() {
                    override suspend fun obtenerPorEmail(email: String): UsuarioEntity =
                        UsuarioEntity(email, "Cuenta de prueba", hash, "", "", "", "", "PACIENTE")
                }
                val sesion = SesionRepository(UsuarioRepository(usuarios), dispatcher)
                val respuestaB = CompletableDeferred<List<BeneficiarioEntity>>()
                val escrituraA = CompletableDeferred<Int>()
                val datoA = BeneficiarioEntity(id = 1, propietarioEmail = cuentaA, nombre = "Registro A",
                    rut = "12345678-5", fechaNacimiento = "01/01/2015", parentesco = "Hijo")
                val datoB = datoA.copy(id = 2, propietarioEmail = cuentaB, nombre = "Registro B")
                val dao = object : EmptyBeneficiarioDao() {
                    override fun obtenerPorPropietarioFlow(email: String): Flow<List<BeneficiarioEntity>> =
                        if (email == cuentaA) flowOf(listOf(datoA)) else flow { emit(respuestaB.await()) }

                    override suspend fun desactivar(id: Int, email: String): Int = escrituraA.await()
                }
                val repository = BeneficiarioRepository(dao, sesion)
                val store = ViewModelStore()
                try {
                    val viewModel = BeneficiarioViewModel(repository, sesion.sesionActual)
                    store.put("beneficiario", viewModel)
                    sesion.autenticar(cuentaA, password)
                    runCurrent()
                    assertEquals(listOf(datoA), viewModel.uiState.value.beneficiarios)
                    val versionA = viewModel.uiState.value.versionSesion
                    var resultadoAnteriorVisible = false
                    viewModel.desactivar(datoA.id) { resultadoAnteriorVisible = true }
                    runCurrent()
                    assertTrue(viewModel.uiState.value.guardando)

                    sesion.autenticar(cuentaB, password)
                    runCurrent()
                    assertTrue(viewModel.uiState.value.beneficiarios.isEmpty())
                    assertTrue(viewModel.uiState.value.cargando)
                    assertFalse(viewModel.uiState.value.guardando)
                    assertTrue(viewModel.uiState.value.versionSesion > versionA)

                    escrituraA.complete(1)
                    runCurrent()
                    assertFalse(resultadoAnteriorVisible)
                    assertTrue(viewModel.uiState.value.beneficiarios.isEmpty())

                    respuestaB.complete(listOf(datoB))
                    runCurrent()
                    assertEquals(listOf(datoB), viewModel.uiState.value.beneficiarios)

                    sesion.cerrarSesion()
                    runCurrent()
                    assertTrue(viewModel.uiState.value.sinSesion)
                    assertTrue(viewModel.uiState.value.beneficiarios.isEmpty())
                } finally {
                    store.clear()
                }
            }
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class TestAppContainer(
    repository: BeneficiarioRepository,
    override val sesionRepository: SesionRepository
) : AppContainer(TestContext()) {
    override val beneficiarioRepository: BeneficiarioRepository = repository
}

private class TestContext : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
}

private open class EmptyBeneficiarioDao : BeneficiarioDao() {
    override fun obtenerPorPropietarioFlow(email: String): Flow<List<BeneficiarioEntity>> =
        flowOf(emptyList())

    override suspend fun obtenerPorPropietario(email: String): List<BeneficiarioEntity> =
        emptyList()

    override suspend fun obtenerPorIdYPropietario(id: Int, email: String): BeneficiarioEntity? =
        null

    override suspend fun obtenerTodosPorPropietario(email: String): List<BeneficiarioEntity> =
        emptyList()

    override suspend fun insertarInterno(beneficiario: BeneficiarioEntity): Long = 0L

    override suspend fun actualizarInterno(
        id: Int,
        email: String,
        nombre: String,
        rut: String,
        fechaNacimiento: String,
        parentesco: String
    ): Int = 0

    override suspend fun desactivar(id: Int, email: String): Int = 0

    override suspend fun reactivar(id: Int, email: String): Int = 0
}

private open class EmptyUsuarioDao : UsuarioDao {
    override suspend fun insertar(usuario: UsuarioEntity): Long = 0L
    override suspend fun obtenerTodos(): List<UsuarioEntity> = emptyList()
    override suspend fun obtenerPorEmail(email: String): UsuarioEntity? = null
    override suspend fun actualizarPassword(email: String, nuevaPassword: String): Int = 0
    override suspend fun contarCuentasSinFormatoPbkdf2(): Int = 0
}
