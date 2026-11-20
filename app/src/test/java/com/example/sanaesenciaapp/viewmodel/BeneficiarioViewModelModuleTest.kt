package com.example.sanaesenciaapp.viewmodel

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
                val repository = BeneficiarioRepository(
                    beneficiarioDao = EmptyBeneficiarioDao(),
                    sesionRepository = SesionRepository(UsuarioRepository(EmptyUsuarioDao()))
                )
                val container = TestAppContainer(repository)
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
}

private class TestAppContainer(
    repository: BeneficiarioRepository
) : AppContainer(TestContext()) {
    override val beneficiarioRepository: BeneficiarioRepository = repository
}

private class TestContext : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
}

private class EmptyBeneficiarioDao : BeneficiarioDao() {
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

private class EmptyUsuarioDao : UsuarioDao {
    override suspend fun insertar(usuario: UsuarioEntity): Long = 0L
    override suspend fun obtenerTodos(): List<UsuarioEntity> = emptyList()
    override suspend fun obtenerPorEmail(email: String): UsuarioEntity? = null
    override suspend fun actualizarPassword(email: String, nuevaPassword: String): Int = 0
    override suspend fun contarCuentasSinFormatoPbkdf2(): Int = 0
}
