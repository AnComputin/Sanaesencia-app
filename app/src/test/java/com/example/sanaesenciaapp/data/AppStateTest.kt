package com.example.sanaesenciaapp.data

import com.example.sanaesenciaapp.data.remote.RetrofitClient
import com.example.sanaesenciaapp.data.remote.TokenManager
import com.example.sanaesenciaapp.data.repository.LineaCompra
import com.example.sanaesenciaapp.data.repository.ResultadoCompra
import com.example.sanaesenciaapp.data.repository.ResultadoRegistroUsuario
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeUsuarioDao : UsuarioDao {
        val usuarios = mutableListOf<UsuarioEntity>()
        override suspend fun insertar(usuario: UsuarioEntity): Long {
            if (usuarios.any { it.email.equals(usuario.email, ignoreCase = true) }) return -1L
            usuarios.add(usuario)
            return 1L
        }
        override suspend fun obtenerTodos(): List<UsuarioEntity> = usuarios.toList()
        override suspend fun obtenerPorEmail(email: String): UsuarioEntity? =
            usuarios.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
        override suspend fun actualizarPassword(email: String, nuevaPassword: String): Int = 1
        override suspend fun contarCuentasSinFormatoPbkdf2(): Int = 0
    }

    private class FakeEspecialidadDao : EspecialidadDao {
        val flow = MutableStateFlow<List<EspecialidadEntity>>(emptyList())
        override suspend fun obtenerTodas(): List<EspecialidadEntity> = flow.value
        override fun obtenerTodasFlow(): Flow<List<EspecialidadEntity>> = flowOf(flow.value)
        override suspend fun insertarTodas(lista: List<EspecialidadEntity>) {
            flow.value = lista
        }
        override suspend fun actualizarTodas(lista: List<EspecialidadEntity>) {
            flow.value = lista
        }
        override suspend fun actualizarModalidad(id: Int, modalidad: String) {}
        override suspend fun actualizarAgendaPausada(id: Int, agendaPausada: Boolean) {}
    }

    private class FakeEspecialistaDao : EspecialistaDao {
        var lista = listOf<EspecialistaEntity>()
        override fun obtenerPorEspecialidadFlow(especialidadId: Int): Flow<List<EspecialistaEntity>> =
            flowOf(lista.filter { it.especialidadId == especialidadId })
        override suspend fun obtenerPorEspecialidad(especialidadId: Int): List<EspecialistaEntity> =
            lista.filter { it.especialidadId == especialidadId }
        override suspend fun obtenerPorId(id: Int): EspecialistaEntity? = lista.firstOrNull { it.id == id }
        override suspend fun obtenerTodos(): List<EspecialistaEntity> = lista
        override suspend fun insertarTodos(lista: List<EspecialistaEntity>) {
            this.lista = lista
        }
    }

    private class FakeBeneficiarioDao : BeneficiarioDao() {
        val lista = mutableListOf<BeneficiarioEntity>()
        private var nextId = 1
        override fun obtenerPorPropietarioFlow(email: String): Flow<List<BeneficiarioEntity>> =
            flowOf(lista.filter { it.propietarioEmail.equals(email, true) && it.activo })
        override suspend fun obtenerPorPropietario(email: String): List<BeneficiarioEntity> =
            lista.filter { it.propietarioEmail.equals(email, true) && it.activo }
        override suspend fun obtenerPorIdYPropietario(id: Int, email: String): BeneficiarioEntity? =
            lista.firstOrNull { it.id == id && it.propietarioEmail.equals(email, true) }
        override suspend fun obtenerTodosPorPropietario(email: String): List<BeneficiarioEntity> =
            lista.filter { it.propietarioEmail.equals(email, true) }
        override suspend fun insertarInterno(beneficiario: BeneficiarioEntity): Long {
            val id = if (beneficiario.id > 0) beneficiario.id else nextId++
            lista.add(beneficiario.copy(id = id))
            return id.toLong()
        }
        override suspend fun actualizarInterno(id: Int, email: String, nombre: String, rut: String, fechaNacimiento: String, parentesco: String): Int = 1
        override suspend fun desactivar(id: Int, email: String): Int = 1
        override suspend fun reactivar(id: Int, email: String): Int = 1
    }

    private class FakeTiendaRepo : TiendaRepositoryContract {
        var sincronizado = false
        override val productos: Flow<List<ProductoEntity>> = flowOf(emptyList())
        override val todosLosProductos: Flow<List<ProductoEntity>> = flowOf(emptyList())
        override suspend fun sincronizarProductosDesdeServidor() { sincronizado = true }
        override suspend fun guardarProducto(producto: ProductoEntity) {}
        override suspend fun eliminarProducto(id: Int) {}
        override suspend fun actualizarStock(id: Int, nuevoStock: Int) {}
        override suspend fun registrarCompra(email: String, nombre: String, metodoPago: String, lineas: List<LineaCompra>): ResultadoCompra =
            ResultadoCompra(true, "OK")
    }

    @Before
    fun setUp() {
        RetrofitClient.resetParaPruebas()
    }

    @After
    fun tearDown() {
        RetrofitClient.resetParaPruebas()
    }

    private fun crearAppState(
        io: kotlinx.coroutines.CoroutineDispatcher = dispatcher,
        main: kotlinx.coroutines.CoroutineDispatcher = dispatcher
    ): AppState {
        val usuarioDao = FakeUsuarioDao()
        val usuarioRepo = UsuarioRepository(usuarioDao)
        val sesionRepo = SesionRepository(usuarioRepo, io)
        return AppState(
            _db = null,
            especialidadDao = FakeEspecialidadDao(),
            especialistaDao = FakeEspecialistaDao(),
            beneficiarioDao = FakeBeneficiarioDao(),
            tiendaRepository = FakeTiendaRepo(),
            sesionRepository = sesionRepo,
            usuarioRepository = usuarioRepo,
            ioDispatcher = io,
            mainDispatcher = main
        )
    }

    @Test
    fun `cargarDatos y cargarEspecialistasPorEspecialidad cargan catalogo local y sincronizan remoto`() = runTest(dispatcher) {
        val appState = crearAppState()
        appState.cargarDatos()
        advanceUntilIdle()

        assertEquals(7, appState.especialidades.size)
        assertTrue(appState.usuarios.any { it.email == "admin@sanaesencia.cl" })

        appState.cargarEspecialistasPorEspecialidad(1)
        advanceUntilIdle()
        assertEquals(5, appState.especialistas.size)
    }

    @Test
    fun `registrarUsuarioAsync y registrarUsuario validan duplicados y normalizan rol`() = runTest(dispatcher) {
        val appState = crearAppState()

        val res = appState.registrarUsuarioAsync(
            nombre = "Ana López",
            email = "ana@sanaesencia.cl",
            password = "ClaveSegura1!",
            rut = "12345678-5",
            telefono = "912345678",
            fechaNacimiento = "01/01/1995",
            comuna = "Los Vilos",
            rol = "ADMIN" // debe degradarse a PACIENTE
        )
        assertEquals(ResultadoRegistroUsuario.Registrado, res)
        assertTrue(appState.existeCorreo("ANA@sanaesencia.cl"))
        assertEquals("PACIENTE", appState.usuarios.single().rol)

        val dup = appState.registrarUsuarioAsync(
            nombre = "Ana 2",
            email = "ana@sanaesencia.cl",
            password = "ClaveSegura1!",
            rut = "12345678-5",
            telefono = "912345678",
            fechaNacimiento = "01/01/1995",
            comuna = "Los Vilos",
            rol = "TERAPEUTA"
        )
        assertEquals(ResultadoRegistroUsuario.CorreoDuplicado, dup)

        assertTrue(
            appState.registrarUsuario(
                nombre = "Pedro",
                email = "pedro@sanaesencia.cl",
                password = "ClaveSegura1!",
                rut = "12345678-5",
                telefono = "912345678",
                fechaNacimiento = "02/02/1990",
                comuna = "Los Vilos",
                rol = "TERAPEUTA"
            )
        )
        advanceUntilIdle()
        assertFalse(
            appState.registrarUsuario(
                nombre = "Pedro 2",
                email = "pedro@sanaesencia.cl",
                password = "ClaveSegura1!",
                rut = "12345678-5",
                telefono = "912345678",
                fechaNacimiento = "02/02/1990",
                comuna = "Los Vilos",
                rol = "PACIENTE"
            )
        )
    }

    @Test
    fun `registrarBeneficiario y logout gestionan sesion local y remota`() = runBlocking {
        val appState = crearAppState(
            io = kotlinx.coroutines.Dispatchers.Unconfined,
            main = kotlinx.coroutines.Dispatchers.Unconfined
        )
        var sinSesionOk = true
        var sinSesionMsg: String? = null
        appState.registrarBeneficiario("Hijo", "12345678-5", "01/01/2015", "Hijo") { ok, msg ->
            sinSesionOk = ok
            sinSesionMsg = msg
        }
        assertFalse(sinSesionOk)
        assertNotNull(sinSesionMsg)

        // Registrar y autenticar
        appState.registrarUsuarioAsync(
            "Titular", "titular@sana.cl", "Clave123!", "12345678-5", "912345678", "01/01/1990", "Los Vilos", "PACIENTE"
        )
        val sesion = appState.sesionRepository.autenticar("titular@sana.cl", "Clave123!")
        appState.sincronizarSesion(sesion)
        assertNotNull(appState.usuarioActual)

        var regOk = false
        appState.registrarBeneficiario("Menor Uno", "12.345.678-5", "15/03/2016", "Hijo") { ok, _ ->
            regOk = ok
        }
        assertTrue(regOk)
        assertEquals("2016-03-15", appState.beneficiarioSeleccionado?.fechaNacimiento)

        // Probar registrarBeneficiario con Retrofit activo (error y exito)
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val respBody = """{"id":55,"propietarioEmail":"titular@sana.cl","nombre":"Menor Dos","rut":"12345678-5","fechaNacimiento":"2018-05-01","parentesco":"Hija","activo":true}"""
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(201)
                    .message("Created")
                    .body(respBody.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
        RetrofitClient.configurarParaPruebas("http://localhost/", client, TokenManager(null))

        val remotoDeferred = CompletableDeferred<Boolean>()
        appState.registrarBeneficiario("Menor Dos", "12.345.678-5", "2018-05-01", "Hija") { ok, _ ->
            remotoDeferred.complete(ok)
        }
        val remotoOk = withTimeout(3_000L) { remotoDeferred.await() }
        assertTrue(remotoOk)
        assertEquals(55, appState.beneficiarioSeleccionado?.id)

        appState.logout()
        assertNull(appState.usuarioActual)
        assertNull(appState.beneficiarioSeleccionado)
        assertTrue(appState.beneficiarios.isEmpty())
    }

    @Test
    fun `cargarDatos y cargarEspecialistasPorEspecialidad sincronizan con Retrofit como admin y manejan errores`() = runBlocking {
        var responderVacio = true
        var fallarRed = false
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                if (fallarRed) throw java.io.IOException("sin red")
                val path = chain.request().url.encodedPath
                val method = chain.request().method
                val body = when {
                    path.endsWith("/especialidades") && method == "GET" ->
                        if (responderVacio) "[]" else """[{"id":1,"nombre":"Psicología","descripcion":"D","icono":"🧠","profesional":"5","modalidad":"Ambas","agendaPausada":false}]"""
                    path.endsWith("/especialidades") && method == "POST" ->
                        """{"id":1,"nombre":"Psicología","descripcion":"D","icono":"🧠","profesional":"5","modalidad":"Ambas","agendaPausada":false}"""
                    path.endsWith("/especialistas") && method == "GET" ->
                        if (responderVacio) "[]" else """[{"id":1,"nombre":"Dra. Remota","especialidadId":1,"modalidad":"Ambas","activo":true}]"""
                    path.endsWith("/especialistas") && method == "POST" ->
                        """{"id":1,"nombre":"Dra. Remota","especialidadId":1,"modalidad":"Ambas","activo":true}"""
                    path.endsWith("/productos") -> "[]"
                    path.endsWith("/login") ->
                        """{"token":"jwt-admin","tipo":"Bearer","expiraEnSegundos":3600,"usuario":{"email":"admin@sanaesencia.cl","nombre":"Admin","rut":"11.111.111-1","telefono":"+569","fechaNacimiento":"1985-01-01","comuna":"Santiago","rol":"ADMIN","activo":true}}"""
                    else -> "[]"
                }
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(body.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        RetrofitClient.configurarParaPruebas("http://localhost/", client, TokenManager(null))
        val appState = crearAppState(
            io = kotlinx.coroutines.Dispatchers.Unconfined,
            main = kotlinx.coroutines.Dispatchers.Unconfined
        )

        // Autenticar como admin remoto para cubrir rama de seed remoto cuando viene vacio
        val adminSesion = appState.sesionRepository.autenticar("admin@sanaesencia.cl", "admin123")
        assertNotNull(adminSesion)
        appState.sincronizarSesion(adminSesion)

        appState.cargarDatos()
        withTimeout(3_000L) {
            while (appState.especialidades.isEmpty()) kotlinx.coroutines.delay(10L)
        }
        assertTrue(appState.especialidades.isNotEmpty())

        // Segundo ciclo con listas no vacias y filtro por especialidad
        responderVacio = false
        appState.cargarDatos()
        appState.cargarEspecialistasPorEspecialidad(1)
        withTimeout(3_000L) {
            while (appState.especialistas.isEmpty()) kotlinx.coroutines.delay(10L)
        }
        assertTrue(appState.especialistas.isNotEmpty())

        // Tercer ciclo con error de red (fallback a datos locales)
        fallarRed = true
        appState.cargarDatos()
        appState.cargarEspecialistasPorEspecialidad(1)
        kotlinx.coroutines.delay(50L)
        assertTrue(appState.especialidades.isNotEmpty())
    }

    @Test
    fun `registrarBeneficiario maneja error HTTP 400 y fallo de red con Retrofit`() = runBlocking {
        val appState = crearAppState(
            io = kotlinx.coroutines.Dispatchers.Unconfined,
            main = kotlinx.coroutines.Dispatchers.Unconfined
        )
        appState.registrarUsuarioAsync(
            "Titular", "t@sana.cl", "Clave123!", "12345678-5", "912345678", "01/01/1990", "Los Vilos", "PACIENTE"
        )
        val sesion = appState.sesionRepository.autenticar("t@sana.cl", "Clave123!")
        appState.sincronizarSesion(sesion)

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val path = chain.request().url.encodedPath
                if (path.endsWith("/beneficiarios") && chain.request().method == "POST") {
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(400)
                        .message("Bad")
                        .body("RUT inválido".toResponseBody("text/plain".toMediaType()))
                        .build()
                } else {
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body("[]".toResponseBody("application/json".toMediaType()))
                        .build()
                }
            }
            .build()
        RetrofitClient.configurarParaPruebas("http://localhost/", client, TokenManager(null))

        val err400 = CompletableDeferred<String?>()
        appState.registrarBeneficiario("Menor", "12345678-5", "01/01/2016", "Hijo") { ok, msg ->
            assertFalse(ok)
            err400.complete(msg)
        }
        assertEquals("RUT inválido", withTimeout(3_000L) { err400.await() })

        val clientIo = OkHttpClient.Builder()
            .addInterceptor { throw java.io.IOException("sin red") }
            .build()
        RetrofitClient.configurarParaPruebas("http://localhost/", clientIo, TokenManager(null))
        val errRed = CompletableDeferred<String?>()
        appState.registrarBeneficiario("Menor", "12345678-5", "01/01/2016", "Hijo") { ok, msg ->
            assertFalse(ok)
            errRed.complete(msg)
        }
        assertTrue(withTimeout(3_000L) { errRed.await() }!!.contains("conexión", ignoreCase = true))
    }
}
