package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModelStore
import com.example.sanaesenciaapp.data.repository.SesionRepository
import com.example.sanaesenciaapp.data.repository.UsuarioRepository
import com.example.sanaesenciaapp.data.room.BeneficiarioDao
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import com.example.sanaesenciaapp.data.room.UsuarioDao
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
class BeneficiarioViewModelModuleTest {

    @Test
    fun supportsSoloBeneficiarioViewModel() {
        val module = BeneficiarioViewModelModule()

        assertTrue(module.supports(BeneficiarioViewModel::class.java))
        assertFalse(module.supports(CatalogoViewModel::class.java))
    }

    @Test
    fun propietarioSeObtieneDeSesionYDuplicadosSeLimitanACadaCuenta() {
        val scheduler = kotlinx.coroutines.test.TestCoroutineScheduler()
        val dispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(dispatcher)
        try {
            runTest(dispatcher) {
                val viewModelStore = ViewModelStore()
                val beneficiarioOtraCuenta = BeneficiarioEntity(
            id = 20,
            propietarioEmail = "otra@example.com",
            nombre = "Beneficiario existente",
            rut = "12345678-5",
            fechaNacimiento = "01/01/2015",
            parentesco = "Hijo"
        )
        val beneficiarioDao = FakeBeneficiarioDao(listOf(beneficiarioOtraCuenta))
        val usuarioDao = FakeUsuarioDao(
            listOf(
                usuario("Titular", "Titular@Example.com"),
                usuario("Otra cuenta", "otra@example.com")
            )
        )
        val sesionRepository = SesionRepository(
            usuarioRepository = UsuarioRepository(usuarioDao),
            ioDispatcher = dispatcher
        )
        try {
            assertEquals("Titular@Example.com", sesionRepository.autenticar(" TITULAR@example.com ", "prueba")?.email)
            val viewModel = BeneficiarioViewModel(
                beneficiarioDao = beneficiarioDao,
                sesionRepository = sesionRepository,
                ioDispatcher = dispatcher
            )
            viewModelStore.put("beneficiarios", viewModel)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.beneficiarios.isEmpty())

            var result: ResultadoBeneficiario? = null
            viewModel.registrarBeneficiario(
                nombre = " Persona de prueba ",
                rut = "12.345.678-5",
                fechaNacimiento = "01/01/2015",
                parentesco = " Hijo "
            ) { result = it }
            advanceUntilIdle()

            assertEquals(ResultadoBeneficiario.REGISTRADO, result)
            val registrado = beneficiarioDao.registros.last()
            assertEquals("titular@example.com", registrado.propietarioEmail)
            assertEquals("12345678-5", registrado.rut)

            result = null
            viewModel.registrarBeneficiario(
                nombre = "Duplicado",
                rut = "12345678-5",
                fechaNacimiento = "01/01/2015",
                parentesco = "Hijo"
            ) { result = it }
            advanceUntilIdle()
            assertEquals(ResultadoBeneficiario.DUPLICADO, result)
            assertEquals(2, beneficiarioDao.registros.size)

            assertEquals(
                "Otra cuenta",
                sesionRepository.autenticar("otra@example.com", "prueba")?.nombre
            )
            advanceUntilIdle()
            assertEquals(listOf(beneficiarioOtraCuenta), viewModel.uiState.value.beneficiarios)
                } finally {
                    viewModelStore.clear()
                }
            }
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class FakeBeneficiarioDao(
    initial: List<BeneficiarioEntity>
) : BeneficiarioDao {
    val registros = initial.toMutableList()

    override fun obtenerPorPropietarioFlow(email: String): Flow<List<BeneficiarioEntity>> =
        flowOf(registros.filter { it.propietarioEmail == email && it.activo })

    override suspend fun obtenerPorPropietario(email: String): List<BeneficiarioEntity> =
        registros.filter { it.propietarioEmail == email && it.activo }

    override suspend fun obtenerPorId(id: Int): BeneficiarioEntity? =
        registros.firstOrNull { it.id == id }

    override suspend fun insertar(beneficiario: BeneficiarioEntity): Long {
        val duplicate = registros.any {
            it.propietarioEmail == beneficiario.propietarioEmail &&
                it.rut == beneficiario.rut
        }
        if (duplicate) return -1L
        val nextId = (registros.maxOfOrNull { it.id } ?: 0) + 1
        registros += beneficiario.copy(id = nextId)
        return nextId.toLong()
    }

    override suspend fun desactivar(id: Int, email: String) {
        val index = registros.indexOfFirst {
            it.id == id && it.propietarioEmail == email
        }
        if (index >= 0) registros[index] = registros[index].copy(activo = false)
    }
}

private class FakeUsuarioDao(
    private val usuarios: List<UsuarioEntity>
) : UsuarioDao {
    override suspend fun insertar(usuario: UsuarioEntity): Long = -1L
    override suspend fun obtenerTodos(): List<UsuarioEntity> = usuarios
    override suspend fun obtenerPorEmail(email: String): UsuarioEntity? =
        usuarios.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
}

private fun usuario(nombre: String, email: String) = UsuarioEntity(
    email = email,
    nombre = nombre,
    password = "prueba",
    rut = "1-9",
    telefono = "",
    fechaNacimiento = "",
    comuna = "",
    rol = "PACIENTE"
)
