package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModelStore
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.data.repository.LineaCompra
import com.example.sanaesenciaapp.data.repository.ResultadoCompra
import com.example.sanaesenciaapp.data.repository.SesionRepository
import com.example.sanaesenciaapp.data.repository.TiendaRepositoryContract
import com.example.sanaesenciaapp.data.repository.UsuarioRepository
import com.example.sanaesenciaapp.data.room.BeneficiarioDao
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import com.example.sanaesenciaapp.data.room.EspecialidadDao
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.data.room.EspecialistaDao
import com.example.sanaesenciaapp.data.room.EspecialistaEntity
import com.example.sanaesenciaapp.data.room.ProductoEntity
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `validacion reactiva de email muestra error al escribir formato invalido y desaparece al corregir`() {
        val viewModel = AuthViewModel()

        viewModel.onEmailChange("correo_invalido")
        assertNotNull(viewModel.uiState.value.errorEmail)

        viewModel.onEmailChange("usuario@sanaesencia.cl")
        assertNull(viewModel.uiState.value.errorEmail)
    }

    @Test
    fun `validacion reactiva de password y confirmacion refleja errores mientras se escribe`() {
        val viewModel = AuthViewModel()

        viewModel.onPasswordChange("abc")
        assertNotNull(viewModel.uiState.value.errorPassword)

        viewModel.onPasswordChange("ClaveSegura1!")
        assertNull(viewModel.uiState.value.errorPassword)

        viewModel.onConfirmPasswordChange("OtraClave1!")
        assertNotNull(viewModel.uiState.value.errorConfirmPassword)

        viewModel.onConfirmPasswordChange("ClaveSegura1!")
        assertNull(viewModel.uiState.value.errorConfirmPassword)
    }

    @Test
    fun `onLoginPasswordChange permite contrasenas cortas existentes sin rechazar por reglas de registro`() {
        val viewModel = AuthViewModel()

        viewModel.onLoginPasswordChange("admin123")
        assertNull(viewModel.uiState.value.errorPassword)

        viewModel.onLoginPasswordChange("")
        assertNotNull(viewModel.uiState.value.errorPassword)
    }

    @Test
    fun `validarRegistro rechaza formulario vacio o incompleto y marca todos los campos obligatorios`() {
        val viewModel = AuthViewModel()

        val valido = viewModel.validarRegistro()

        assertFalse(valido)
        val state = viewModel.uiState.value
        assertNotNull(state.errorNombre)
        assertNotNull(state.errorEmail)
        assertNotNull(state.errorRut)
        assertNotNull(state.errorTelefono)
        assertNotNull(state.errorPassword)
        assertNotNull(state.errorFechaNacimiento)
        assertNotNull(state.errorTerminos)
    }

    @Test
    fun `onRolChange bloquea autoasignacion de rol ADMIN`() {
        val viewModel = AuthViewModel()

        viewModel.onRolChange("ADMIN")
        assertEquals("PACIENTE", viewModel.uiState.value.rol)

        viewModel.onRolChange("TERAPEUTA")
        assertEquals("TERAPEUTA", viewModel.uiState.value.rol)
    }

    @Test
    fun `validacion reactiva de nombre y rut limpia el error al corregir`() {
        val viewModel = AuthViewModel()

        viewModel.onNombreChange("A1")
        assertNotNull(viewModel.uiState.value.errorNombre)
        viewModel.onNombreChange("Anaís Riquelme")
        assertNull(viewModel.uiState.value.errorNombre)

        viewModel.onRutChange("123456780")
        assertNotNull(viewModel.uiState.value.errorRut)
        viewModel.onRutChange("123456785")
        assertNull(viewModel.uiState.value.errorRut)
    }

    @Test
    fun `validadores individuales toggles telefono fecha y comuna actualizan estado`() {
        val vm = AuthViewModel()
        vm.onTelefonoChange("912345678")
        vm.onFechaNacimientoChange("01/01/1995")
        vm.onComunaChange("Los Vilos")
        vm.onAceptarTerminosChange(true)
        vm.togglePasswordVisibility()
        vm.toggleConfirmPasswordVisibility()
        vm.validarCampoNombre()
        vm.validarCampoEmail()
        vm.validarCampoRut()
        vm.validarCampoTelefono()
        vm.validarCampoPassword()
        vm.validarCampoConfirmPassword()
        vm.validarCampoFechaNacimiento()
        vm.validarCampoComuna()
        vm.setErrorGeneral("msg")

        assertTrue(vm.uiState.value.isPasswordVisible)
        assertTrue(vm.uiState.value.isConfirmPasswordVisible)
        assertEquals("msg", vm.uiState.value.errorGeneral)
    }

    @Test
    fun `registrarCuenta e iniciarSesion manejan exito duplicado y errores`() = runTest(dispatcher) {
        val usuarios = mutableMapOf<String, UsuarioEntity>()
        val usuarioDao = object : UsuarioDao {
            override suspend fun insertar(usuario: UsuarioEntity): Long {
                val k = usuario.email.lowercase()
                if (usuarios.containsKey(k)) return -1L
                usuarios[k] = usuario
                return 1L
            }
            override suspend fun obtenerTodos() = usuarios.values.toList()
            override suspend fun obtenerPorEmail(email: String) = usuarios[email.trim().lowercase()]
            override suspend fun actualizarPassword(email: String, nuevaPassword: String) = 1
            override suspend fun contarCuentasSinFormatoPbkdf2() = 0
        }
        val usuarioRepo = UsuarioRepository(usuarioDao)
        val sesionRepo = SesionRepository(usuarioRepo, dispatcher)
        val appState = AppState(
            _db = null,
            especialidadDao = object : EspecialidadDao {
                override suspend fun obtenerTodas() = emptyList<EspecialidadEntity>()
                override fun obtenerTodasFlow() = flowOf(emptyList<EspecialidadEntity>())
                override suspend fun insertarTodas(lista: List<EspecialidadEntity>) {}
                override suspend fun actualizarTodas(lista: List<EspecialidadEntity>) {}
                override suspend fun actualizarModalidad(id: Int, modalidad: String) {}
                override suspend fun actualizarAgendaPausada(id: Int, agendaPausada: Boolean) {}
            },
            especialistaDao = object : EspecialistaDao {
                override fun obtenerPorEspecialidadFlow(especialidadId: Int) = flowOf(emptyList<EspecialistaEntity>())
                override suspend fun obtenerPorEspecialidad(especialidadId: Int) = emptyList<EspecialistaEntity>()
                override suspend fun obtenerPorId(id: Int): EspecialistaEntity? = null
                override suspend fun obtenerTodos() = emptyList<EspecialistaEntity>()
                override suspend fun insertarTodos(lista: List<EspecialistaEntity>) {}
            },
            beneficiarioDao = object : BeneficiarioDao() {
                override fun obtenerPorPropietarioFlow(email: String): Flow<List<BeneficiarioEntity>> = flowOf(emptyList())
                override suspend fun obtenerPorPropietario(email: String) = emptyList<BeneficiarioEntity>()
                override suspend fun obtenerPorIdYPropietario(id: Int, email: String): BeneficiarioEntity? = null
                override suspend fun obtenerTodosPorPropietario(email: String) = emptyList<BeneficiarioEntity>()
                override suspend fun insertarInterno(beneficiario: BeneficiarioEntity) = 1L
                override suspend fun actualizarInterno(id: Int, email: String, nombre: String, rut: String, fechaNacimiento: String, parentesco: String) = 1
                override suspend fun desactivar(id: Int, email: String) = 1
                override suspend fun reactivar(id: Int, email: String) = 1
            },
            tiendaRepository = object : TiendaRepositoryContract {
                override val productos = flowOf(emptyList<ProductoEntity>())
                override val todosLosProductos = flowOf(emptyList<ProductoEntity>())
                override suspend fun guardarProducto(producto: ProductoEntity) {}
                override suspend fun eliminarProducto(id: Int) {}
                override suspend fun actualizarStock(id: Int, nuevoStock: Int) {}
                override suspend fun registrarCompra(email: String, nombre: String, metodoPago: String, lineas: List<LineaCompra>) =
                    ResultadoCompra(true, "OK")
            },
            sesionRepository = sesionRepo,
            usuarioRepository = usuarioRepo,
            ioDispatcher = dispatcher,
            mainDispatcher = dispatcher
        )

        val store = ViewModelStore()
        val vm = AuthViewModel(sesionRepo).also { store.put("auth", it) }
        try {
            vm.onNombreChange("Anaís Riquelme")
            vm.onEmailChange("anais@sanaesencia.cl")
            vm.onRutChange("123456785")
            vm.onTelefonoChange("912345678")
            vm.onFechaNacimientoChange("01/01/1995")
            vm.onComunaChange("Los Vilos")
            vm.onPasswordChange("ClaveSegura1!")
            vm.onConfirmPasswordChange("ClaveSegura1!")
            vm.onAceptarTerminosChange(true)

            var exitoRegistro = false
            vm.registrarCuenta(appState, "Ya existe", "Error BD") { exitoRegistro = true }
            advanceUntilIdle()
            assertTrue(exitoRegistro)

            // Correo duplicado
            vm.registrarCuenta(appState, "Ya existe", "Error BD") {}
            advanceUntilIdle()
            assertEquals("Ya existe", vm.uiState.value.errorGeneral)

            // Login exitoso y credenciales inválidas
            vm.iniciarSesion(appState, sesionRepo)
            advanceUntilIdle()
            assertTrue(vm.uiState.value.loginExitoso)

            vm.onLoginNavegado()
            assertFalse(vm.uiState.value.loginExitoso)

            vm.onLoginPasswordChange("Incorrecta")
            vm.iniciarSesion(appState, sesionRepo)
            advanceUntilIdle()
            assertFalse(vm.uiState.value.loginExitoso)
            assertEquals("Credenciales inválidas", vm.uiState.value.errorGeneral)

            // Validación de campos vacíos en login y sin repositorio
            val vmSinRepo = AuthViewModel(null).also { store.put("authSinRepo", it) }
            vmSinRepo.onEmailChange("invalido")
            vmSinRepo.onLoginPasswordChange("")
            vmSinRepo.iniciarSesion(null, null)
            assertNotNull(vmSinRepo.uiState.value.errorGeneral)

            vmSinRepo.onEmailChange("anais@sanaesencia.cl")
            vmSinRepo.onLoginPasswordChange("ClaveSegura1!")
            vmSinRepo.iniciarSesion(null, null)
            assertEquals("No se pudo acceder a la sesión", vmSinRepo.uiState.value.errorGeneral)

            // Registro sin appState muestra error de BD
            vm.onNombreChange("Anaís Riquelme")
            vm.onRutChange("123456785")
            vm.onTelefonoChange("912345678")
            vm.onFechaNacimientoChange("01/01/1995")
            vm.onComunaChange("Los Vilos")
            vm.onPasswordChange("ClaveSegura1!")
            vm.onConfirmPasswordChange("ClaveSegura1!")
            vm.onAceptarTerminosChange(true)
            vm.registrarCuenta(null, "Dup", "Error BD") {}
            assertEquals("Error BD", vm.uiState.value.errorGeneral)
        } finally {
            store.clear()
        }
    }
}
