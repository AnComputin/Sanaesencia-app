package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.data.remote.RetrofitClient
import com.example.sanaesenciaapp.data.remote.TokenManager
import com.example.sanaesenciaapp.data.room.BeneficiarioDao
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.CitaEntity
import com.example.sanaesenciaapp.data.room.EspecialidadDao
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.data.room.EspecialistaDao
import com.example.sanaesenciaapp.data.room.EspecialistaEntity
import com.example.sanaesenciaapp.data.room.UsuarioDao
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
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
import java.io.IOException

class RemoteRepositoriesTest {

    private class FakeUsuarioDao : UsuarioDao {
        val usuarios = linkedMapOf<String, UsuarioEntity>()
        override suspend fun insertar(usuario: UsuarioEntity): Long {
            val k = usuario.email.lowercase()
            if (usuarios.containsKey(k)) return -1L
            usuarios[k] = usuario
            return 1L
        }
        override suspend fun obtenerTodos(): List<UsuarioEntity> = usuarios.values.toList()
        override suspend fun obtenerPorEmail(email: String): UsuarioEntity? =
            usuarios[email.trim().lowercase()]
        override suspend fun actualizarPassword(email: String, nuevaPassword: String): Int {
            val k = email.trim().lowercase()
            val cur = usuarios[k] ?: return 0
            usuarios[k] = cur.copy(password = nuevaPassword)
            return 1
        }
        override suspend fun contarCuentasSinFormatoPbkdf2(): Int = 0
    }

    private class FakeBeneficiarioDao : BeneficiarioDao() {
        val registros = linkedMapOf<Int, BeneficiarioEntity>()
        private var seq = 1
        override fun obtenerPorPropietarioFlow(email: String): Flow<List<BeneficiarioEntity>> =
            flow { emit(registros.values.filter { it.activo && it.propietarioEmail.equals(email, true) }) }
        override suspend fun obtenerPorPropietario(email: String): List<BeneficiarioEntity> =
            registros.values.filter { it.activo && it.propietarioEmail.equals(email, true) }
        override suspend fun obtenerPorIdYPropietario(id: Int, email: String): BeneficiarioEntity? =
            registros[id]?.takeIf { it.activo && it.propietarioEmail.equals(email, true) }
        override suspend fun obtenerTodosPorPropietario(email: String): List<BeneficiarioEntity> =
            registros.values.filter { it.propietarioEmail.equals(email, true) }
        override suspend fun insertarInterno(beneficiario: BeneficiarioEntity): Long {
            val rut = beneficiario.rut.trim().replace(".", "").uppercase()
            if (registros.values.any { it.propietarioEmail.equals(beneficiario.propietarioEmail, true) && it.rut == rut }) {
                return -1L
            }
            val id = if (beneficiario.id > 0) beneficiario.id else seq++
            registros[id] = beneficiario.copy(id = id, rut = rut)
            return id.toLong()
        }
        override suspend fun actualizarInterno(id: Int, email: String, nombre: String, rut: String, fechaNacimiento: String, parentesco: String): Int {
            val cur = registros[id]?.takeIf { it.propietarioEmail.equals(email, true) } ?: return 0
            registros[id] = cur.copy(nombre = nombre, rut = rut, fechaNacimiento = fechaNacimiento, parentesco = parentesco)
            return 1
        }
        override suspend fun desactivar(id: Int, email: String): Int {
            val cur = registros[id]?.takeIf { it.activo && it.propietarioEmail.equals(email, true) } ?: return 0
            registros[id] = cur.copy(activo = false)
            return 1
        }
        override suspend fun reactivar(id: Int, email: String): Int {
            val cur = registros[id]?.takeIf { !it.activo && it.propietarioEmail.equals(email, true) } ?: return 0
            registros[id] = cur.copy(activo = true)
            return 1
        }
    }

    private class FakeCitaDao : CitaDao() {
        val citas = mutableListOf<CitaEntity>()
        private var nextId = 1
        override fun obtenerTodasFlow(): Flow<List<CitaEntity>> = flow { emit(citas.toList()) }
        override fun obtenerPorUsuarioFlow(email: String): Flow<List<CitaEntity>> =
            flow { emit(citas.filter { it.usuarioEmail.equals(email, true) }) }
        override suspend fun obtenerTodas(): List<CitaEntity> = citas.toList()
        override suspend fun contarSolapesEspecialista(especialistaId: Int, inicioMillis: Long, finMillis: Long): Int =
            citas.count { it.especialistaId == especialistaId && it.estado != "CANCELADA" && it.inicioMillis < finMillis && it.finMillis > inicioMillis }
        override suspend fun contarPresencialesSolapadas(inicioMillis: Long, finMillis: Long): Int =
            citas.count { it.modalidad == "PRESENCIAL" && it.estado != "CANCELADA" && it.inicioMillis < finMillis && it.finMillis > inicioMillis }
        override suspend fun obtenerBoxesOcupados(inicioMillis: Long, finMillis: Long): List<Int> =
            citas.filter { it.modalidad == "PRESENCIAL" && it.estado != "CANCELADA" && it.boxAsignado != null && it.inicioMillis < finMillis && it.finMillis > inicioMillis }
                .mapNotNull { it.boxAsignado }
        override suspend fun insertarInterno(cita: CitaEntity): Long {
            val id = if (cita.id > 0) cita.id else nextId++
            if (citas.any { it.id == id }) {
                throw IllegalStateException("PK collision $id")
            }
            citas.add(cita.copy(id = id))
            return id.toLong()
        }
        override suspend fun actualizarEstado(id: Int, nuevoEstado: String) {
            val idx = citas.indexOfFirst { it.id == id }
            if (idx >= 0) citas[idx] = citas[idx].copy(estado = nuevoEstado)
        }
        override suspend fun eliminar(id: Int) {
            citas.removeAll { it.id == id && it.estado == "EN_PAGO" }
        }
        override suspend fun eliminarRetenciones() {
            citas.removeAll { it.estado == "EN_PAGO" }
        }
    }

    private fun configurarRetrofit(interceptor: Interceptor): TokenManager {
        val tm = TokenManager(null)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        RetrofitClient.configurarParaPruebas("http://localhost/", client, tm)
        return tm
    }

    @Before
    fun setUp() {
        RetrofitClient.resetParaPruebas()
    }

    @After
    fun tearDown() {
        RetrofitClient.resetParaPruebas()
    }

    @Test
    fun `UsuarioRepository y SesionRepository cubren login registro y obtenerPorEmail con Retrofit`() = runBlocking {
        val tm = configurarRetrofit { chain ->
            val path = chain.request().url.encodedPath
            val (code, body) = when {
                path.endsWith("/login") -> 200 to """
                    {"token":"jwt-login","tipo":"Bearer","expiraEnSegundos":3600,
                    "usuario":{"email":"ana@sana.cl","nombre":"Ana","rut":"12345678-5","telefono":"912345678","fechaNacimiento":"1990-01-01","comuna":"Los Vilos","rol":"PACIENTE","activo":true}}
                """.trimIndent()
                path.endsWith("/registro") -> 201 to """
                    {"token":"jwt-reg","tipo":"Bearer","expiraEnSegundos":3600,
                    "usuario":{"email":"nuevo@sana.cl","nombre":"Nuevo","rut":"12345678-5","telefono":"912345678","fechaNacimiento":"1992-05-10","comuna":"Los Vilos","rol":"PACIENTE","activo":true}}
                """.trimIndent()
                path.contains("/api/usuarios/") -> 200 to """
                    {"email":"remoto@sana.cl","nombre":"Remoto","rut":"12345678-5","telefono":"912345678","fechaNacimiento":"1991-01-01","comuna":"Los Vilos","rol":"PACIENTE","activo":true}
                """.trimIndent()
                else -> 404 to ""
            }
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("OK")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val dao = FakeUsuarioDao()
        val repo = UsuarioRepository(dao)
        val sesionRepo = SesionRepository(repo, Dispatchers.Unconfined)

        // Registro remoto
        val regRes = repo.registrar(
            UsuarioEntity("nuevo@sana.cl", "Nuevo", "hash", "12345678-5", "912345678", "10/05/1992", "Los Vilos", "PACIENTE"),
            passwordPlano = "Clave123!"
        )
        assertEquals(ResultadoRegistroUsuario.Registrado, regRes)
        assertEquals("jwt-reg", tm.obtenerToken())

        // Obtener remoto cuando no está en Room
        val remoto = repo.obtenerPorEmail("remoto@sana.cl")
        assertEquals("Remoto", remoto?.nombre)

        // Login remoto vía SesionRepository
        val sesion = sesionRepo.autenticar("ana@sana.cl", "Clave123!")
        assertEquals("ana@sana.cl", sesion?.email)
        assertEquals("jwt-login", tm.obtenerToken())

        sesionRepo.cerrarSesion()
        assertNull(sesionRepo.sesionActual.value)
        assertNull(tm.obtenerToken())
    }

    @Test
    fun `UsuarioRepository maneja errores 400 409 500 y red`() = runBlocking {
        val dao = FakeUsuarioDao()
        val repo = UsuarioRepository(dao)
        val entidad = UsuarioEntity("u@sana.cl", "U", "h", "12345678-5", "912345678", "01/01/1990", "Los Vilos", "PACIENTE")

        configurarRetrofit { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(400).message("Bad").body("El RUT no es válido".toResponseBody("text/plain".toMediaType())).build()
        }
        assertTrue(repo.registrar(entidad, "P1!") is ResultadoRegistroUsuario.ErrorServidor)
        assertNull(repo.login("u@sana.cl", "P1!"))
        assertNull(repo.obtenerPorEmail("x@sana.cl"))

        configurarRetrofit { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(409).message("Conflict").body("Ya existe correo".toResponseBody("text/plain".toMediaType())).build()
        }
        assertEquals(ResultadoRegistroUsuario.CorreoDuplicado, repo.registrar(entidad, "P1!"))

        configurarRetrofit { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(500).message("Err").body("".toResponseBody("text/plain".toMediaType())).build()
        }
        assertTrue(repo.registrar(entidad, "P1!") is ResultadoRegistroUsuario.ErrorServidor)

        configurarRetrofit { throw IOException("sin red") }
        assertTrue(repo.registrar(entidad, "P1!") is ResultadoRegistroUsuario.ErrorServidor)
        assertNull(repo.login("u@sana.cl", "P1!"))
        assertNull(repo.obtenerPorEmail("x@sana.cl"))
    }

    @Test
    fun `BeneficiarioRepository sincroniza registra actualiza desactiva y reactiva con Retrofit`() = runBlocking {
        val usuarioDao = FakeUsuarioDao()
        val usuarioRepo = UsuarioRepository(usuarioDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        configurarRetrofit { chain ->
            val path = chain.request().url.encodedPath
            val method = chain.request().method
            val body = when {
                path.endsWith("/login") -> """{"token":"jwt","tipo":"Bearer","expiraEnSegundos":3600,"usuario":{"email":"titular@sana.cl","nombre":"Titular","rut":"12345678-5","telefono":"91","fechaNacimiento":"1990-01-01","comuna":"Los Vilos","rol":"PACIENTE","activo":true}}"""
                path.endsWith("/beneficiarios") && method == "GET" ->
                    """[{"id":1,"propietarioEmail":"titular@sana.cl","nombre":"Hijo Sync","rut":"12345678-5","fechaNacimiento":"2015-01-01","parentesco":"Hijo","activo":true}]"""
                path.endsWith("/beneficiarios") && method == "POST" ->
                    """{"id":2,"propietarioEmail":"titular@sana.cl","nombre":"Hija Nueva","rut":"11111111-1","fechaNacimiento":"2017-02-02","parentesco":"Hija","activo":true}"""
                path.endsWith("/desactivar") ->
                    """{"id":1,"propietarioEmail":"titular@sana.cl","nombre":"Hijo Sync","rut":"12345678-5","fechaNacimiento":"2015-01-01","parentesco":"Hijo","activo":false}"""
                path.endsWith("/reactivar") ->
                    """{"id":1,"propietarioEmail":"titular@sana.cl","nombre":"Hijo Sync","rut":"12345678-5","fechaNacimiento":"2015-01-01","parentesco":"Hijo","activo":true}"""
                method == "PUT" ->
                    """{"id":1,"propietarioEmail":"titular@sana.cl","nombre":"Hijo Editado","rut":"12345678-5","fechaNacimiento":"2015-01-01","parentesco":"Hijo","activo":true}"""
                else -> "{}"
            }
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK").body(body.toResponseBody("application/json".toMediaType())).build()
        }

        assertNotNull(sesionRepo.autenticar("titular@sana.cl", "123"))
        val benDao = FakeBeneficiarioDao()
        val repo = BeneficiarioRepository(benDao, sesionRepo)

        val observados = repo.observarBeneficiarios().first() as ResultadoBeneficiario.Exito
        assertEquals(1, observados.valor.size)

        // Segunda sincronización actualiza datos existentes
        repo.sincronizarDesdeServidor("titular@sana.cl")

        assertEquals(
            ResultadoBeneficiario.Insertado,
            repo.registrar("Hija Nueva", "11.111.111-1", "02/02/2017", "Hija")
        )
        assertEquals(
            ResultadoBeneficiario.Actualizado,
            repo.actualizar(1, "Hijo Editado", "12.345.678-5", "01/01/2015", "Hijo")
        )
        assertEquals(ResultadoBeneficiario.Desactivado, repo.desactivar(1))
        assertEquals(ResultadoBeneficiario.Reactivado, repo.reactivar(1))

        // Error HTTP y red
        configurarRetrofit { throw IOException("sin red") }
        assertEquals(ResultadoBeneficiario.ErrorOperacion, repo.registrar("X", "12.345.678-5", "01/01/2015", "Hijo"))
        assertEquals(ResultadoBeneficiario.ErrorOperacion, repo.actualizar(1, "X", "12.345.678-5", "01/01/2015", "Hijo"))
        assertEquals(ResultadoBeneficiario.ErrorOperacion, repo.desactivar(1))
        assertEquals(ResultadoBeneficiario.ErrorOperacion, repo.reactivar(1))
    }

    @Test
    fun `CitaRepository sincroniza reserva y rechaza errores remotos`() = runBlocking {
        val dao = FakeCitaDao()
        val repo = CitaRepository(dao)
        val cita = CitaEntity(
            usuarioEmail = "pac@sana.cl",
            especialidadId = 1,
            especialidadNombre = "Psicología",
            especialistaId = 1,
            especialistaNombre = "Dr. Test",
            modalidad = "PRESENCIAL",
            inicioMillis = 1000L,
            finMillis = 3601000L,
            hora = "09:00",
            pacienteNombre = "Pac",
            estado = "CONFIRMADA"
        )

        configurarRetrofit { chain ->
            val path = chain.request().url.encodedPath
            val method = chain.request().method
            val body = when {
                path.endsWith("/citas") && method == "GET" ->
                    """[{"id":10,"usuarioEmail":"pac@sana.cl","especialidadId":1,"especialidadNombre":"Psicología","especialistaId":2,"especialistaNombre":"Dr. Otro","modalidad":"PRESENCIAL","inicioMillis":5000000,"finMillis":8600000,"hora":"10:30","pacienteNombre":"Pac","beneficiarioId":null,"estado":"CONFIRMADA","boxAsignado":2}]"""
                path.endsWith("/citas") && method == "POST" ->
                    """{"id":11,"usuarioEmail":"pac@sana.cl","especialidadId":1,"especialidadNombre":"Psicología","especialistaId":1,"especialistaNombre":"Dr. Test","modalidad":"PRESENCIAL","inicioMillis":1000,"finMillis":3601000,"hora":"09:00","pacienteNombre":"Pac","beneficiarioId":null,"estado":"CONFIRMADA","boxAsignado":1}"""
                path.endsWith("/estado") ->
                    """{"id":11,"usuarioEmail":"pac@sana.cl","especialidadId":1,"especialidadNombre":"Psicología","especialistaId":1,"especialistaNombre":"Dr. Test","modalidad":"PRESENCIAL","inicioMillis":1000,"finMillis":3601000,"hora":"09:00","pacienteNombre":"Pac","beneficiarioId":null,"estado":"CANCELADA","boxAsignado":1}"""
                else -> "{}"
            }
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK").body(body.toResponseBody("application/json".toMediaType())).build()
        }

        val todas = repo.obtenerTodasLasCitas().first()
        assertEquals(1, todas.size)
        assertEquals(10, todas.single().id)

        val reservada = repo.reservarCita(cita)
        assertNotNull(reservada)
        assertEquals(11, reservada?.id)

        repo.cancelarCita(11)
        assertEquals("CANCELADA", dao.citas.first { it.id == 11 }.estado)

        // Si el backend responde 500 o error de red, no confirma localmente
        configurarRetrofit { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(500).message("Error").body("fail".toResponseBody("text/plain".toMediaType())).build()
        }
        assertNull(repo.reservarCita(cita.copy(inicioMillis = 90000L, finMillis = 99000L)))

        configurarRetrofit { throw IOException("sin red") }
        assertNull(repo.reservarCita(cita.copy(inicioMillis = 190000L, finMillis = 199000L)))
        assertNull(repo.obtenerOcupacionRemota())

        // Sincronización maneja colisión de PK local sin abortar el resto del lote
        configurarRetrofit { chain ->
            val path = chain.request().url.encodedPath
            val body = when {
                path.endsWith("/citas/ocupacion") ->
                    """[{"especialistaId":3,"modalidad":"ONLINE","inicioMillis":400000,"finMillis":4000000}]"""
                path.endsWith("/citas") ->
                    """[
                        {"id":10,"usuarioEmail":"otro@sana.cl","especialidadId":1,"especialidadNombre":"Psicología","especialistaId":4,"especialistaNombre":"Dr. X","modalidad":"ONLINE","inicioMillis":500000,"finMillis":4100000,"hora":"11:00","pacienteNombre":"Otro","beneficiarioId":null,"estado":"CONFIRMADA","boxAsignado":null},
                        {"id":12,"usuarioEmail":"pac@sana.cl","especialidadId":1,"especialidadNombre":"Psicología","especialistaId":5,"especialistaNombre":"Dr. Y","modalidad":"ONLINE","inicioMillis":600000,"finMillis":4200000,"hora":"12:00","pacienteNombre":"Pac","beneficiarioId":null,"estado":"CONFIRMADA","boxAsignado":null}
                    ]"""
                else -> "[]"
            }
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK").body(body.toResponseBody("application/json".toMediaType())).build()
        }
        repo.sincronizarCitasDesdeServidor(null)
        assertTrue(dao.citas.any { it.especialistaId == 5 })
        assertEquals(1, repo.obtenerOcupacionRemota()?.size)
    }

    @Test
    fun `CatalogoRepository ordena especialidades por id y lista especialistas`() = runBlocking {
        val espDao = object : EspecialidadDao {
            override suspend fun obtenerTodas() = emptyList<EspecialidadEntity>()
            override fun obtenerTodasFlow() = flowOf(
                listOf(
                    EspecialidadEntity(3, "C", "D", "🌿", "1", "Ambas", false),
                    EspecialidadEntity(1, "A", "D", "🌿", "1", "Ambas", false)
                )
            )
            override suspend fun insertarTodas(lista: List<EspecialidadEntity>) {}
            override suspend fun actualizarTodas(lista: List<EspecialidadEntity>) {}
            override suspend fun actualizarModalidad(id: Int, modalidad: String) {}
            override suspend fun actualizarAgendaPausada(id: Int, agendaPausada: Boolean) {}
        }
        val docDao = object : EspecialistaDao {
            override fun obtenerPorEspecialidadFlow(especialidadId: Int) =
                flowOf(listOf(EspecialistaEntity(1, "Doc", especialidadId, "Ambas")))
            override suspend fun obtenerPorEspecialidad(especialidadId: Int) = emptyList<EspecialistaEntity>()
            override suspend fun obtenerPorId(id: Int): EspecialistaEntity? = null
            override suspend fun obtenerTodos() = emptyList<EspecialistaEntity>()
            override suspend fun insertarTodos(lista: List<EspecialistaEntity>) {}
        }

        val repo = CatalogoRepository(espDao, docDao)
        assertEquals(listOf(1, 3), repo.observarEspecialidades().first().map { it.id })
        assertEquals(1, repo.observarEspecialistas(1).first().single().especialidadId)
    }

    @Test
    fun `BeneficiarioRepository cubre modo local validaciones y errores HTTP`() = runBlocking {
        val usuarioDao = FakeUsuarioDao()
        val usuarioRepo = UsuarioRepository(usuarioDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)
        val benDao = FakeBeneficiarioDao()
        val repo = BeneficiarioRepository(benDao, sesionRepo)

        // Sin sesion
        assertEquals(ResultadoBeneficiario.SesionNoAutenticada, repo.obtenerPorId(1))
        assertEquals(ResultadoBeneficiario.SesionNoAutenticada, repo.buscarPorRut("12345678-5"))
        assertEquals(ResultadoBeneficiario.SesionNoAutenticada, repo.registrar("A", "12345678-5", "01/01/2015", "Hijo"))
        assertEquals(ResultadoBeneficiario.SesionNoAutenticada, repo.actualizar(1, "A", "12345678-5", "01/01/2015", "Hijo"))
        assertEquals(ResultadoBeneficiario.SesionNoAutenticada, repo.desactivar(1))
        assertEquals(ResultadoBeneficiario.SesionNoAutenticada, repo.reactivar(1))

        // Autenticar localmente (sin Retrofit)
        val hash = com.example.sanaesenciaapp.utils.Seguridad.hashear("Clave123!")
        usuarioDao.insertar(UsuarioEntity("titular@sana.cl", "Titular", hash, "12345678-5", "91", "1990-01-01", "Los Vilos", "PACIENTE"))
        assertNotNull(sesionRepo.autenticar("titular@sana.cl", "Clave123!"))

        assertEquals(ResultadoBeneficiario.DatosInvalidos, repo.obtenerPorId(0))
        assertEquals(ResultadoBeneficiario.NoEncontrado, repo.obtenerPorId(99))
        assertEquals(ResultadoBeneficiario.DatosInvalidos, repo.buscarPorRut("invalido"))
        assertEquals(ResultadoBeneficiario.NoEncontrado, repo.buscarPorRut("12345678-5"))

        // Registrar, buscar, actualizar, desactivar y reactivar en modo local
        assertEquals(ResultadoBeneficiario.DatosInvalidos, repo.registrar("", "12345678-5", "01/01/2015", "Hijo"))
        assertEquals(ResultadoBeneficiario.Insertado, repo.registrar("Hijo 1", "12.345.678-5", "01/01/2015", "Hijo"))
        assertEquals(ResultadoBeneficiario.YaExisteActivo, repo.registrar("Hijo 1", "12.345.678-5", "01/01/2015", "Hijo"))
        assertTrue(repo.obtenerPorId(1) is ResultadoBeneficiario.Exito)
        assertTrue(repo.buscarPorRut("12.345.678-5") is ResultadoBeneficiario.Exito)

        assertEquals(ResultadoBeneficiario.Actualizado, repo.actualizar(1, "Hijo Edit", "12.345.678-5", "02/02/2015", "Hijo"))
        assertEquals(ResultadoBeneficiario.NoEncontrado, repo.actualizar(999, "X", "12.345.678-5", "02/02/2015", "Hijo"))
        assertEquals(ResultadoBeneficiario.Desactivado, repo.desactivar(1))
        assertEquals(ResultadoBeneficiario.NoEncontrado, repo.desactivar(999))
        assertEquals(ResultadoBeneficiario.Reactivado, repo.reactivar(1))
        assertEquals(ResultadoBeneficiario.YaExisteActivo, repo.reactivar(1))
        assertEquals(ResultadoBeneficiario.NoEncontrado, repo.reactivar(999))

        // Errores HTTP especificos con Retrofit activo
        configurarRetrofit { chain ->
            val path = chain.request().url.encodedPath
            val method = chain.request().method
            val msg = when {
                method == "POST" -> "Ya existe activo"
                method == "PUT" -> "Conflicto de RUT"
                path.endsWith("/desactivar") || path.endsWith("/reactivar") -> "No encontrado"
                else -> "Error"
            }
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(400).message("Bad").body(msg.toResponseBody("text/plain".toMediaType())).build()
        }
        assertEquals(ResultadoBeneficiario.YaExisteActivo, repo.registrar("H", "11111111-1", "01/01/2018", "Hija"))
        assertEquals(ResultadoBeneficiario.ConflictoRut, repo.actualizar(1, "H", "11111111-1", "01/01/2018", "Hija"))
        assertEquals(ResultadoBeneficiario.NoEncontrado, repo.desactivar(1))
        assertEquals(ResultadoBeneficiario.NoEncontrado, repo.reactivar(1))
    }

    @Test
    fun `UsuarioRepository y SesionRepository cubren actualizacion password migracion y bloqueo admin local`() = runBlocking {
        val dao = FakeUsuarioDao()
        val repo = UsuarioRepository(dao)
        val sesionRepo = SesionRepository(repo, Dispatchers.Unconfined)

        // Registro local y actualizarPassword
        val u = UsuarioEntity("pac@sana.cl", "Pac", "clavePlana", "12345678-5", "91", "01/01/1990", "Los Vilos", "PACIENTE")
        assertEquals(ResultadoRegistroUsuario.Registrado, repo.registrar(u))
        assertEquals(ResultadoRegistroUsuario.CorreoDuplicado, repo.registrar(u))
        assertEquals(0, repo.contarCuentasSinFormatoPbkdf2())
        assertTrue(repo.actualizarPassword("pac@sana.cl", com.example.sanaesenciaapp.utils.Seguridad.hashear("clavePlana")))
        assertFalse(repo.actualizarPassword("noexiste@sana.cl", "x"))

        // Autenticacion local exitosa y con credenciales invalidas
        assertNull(sesionRepo.autenticar("", "x"))
        assertNull(sesionRepo.autenticar("pac@sana.cl", "incorrecta"))
        val ok = sesionRepo.autenticar("pac@sana.cl", "clavePlana")
        assertEquals("pac@sana.cl", ok?.email)

        // ADMIN no puede iniciar sesion via respaldo local sin servidor
        dao.insertar(UsuarioEntity("admin@sana.cl", "Admin", com.example.sanaesenciaapp.utils.Seguridad.hashear("admin123"), "11111111-1", "91", "1985-01-01", "Santiago", "ADMIN"))
        assertNull(sesionRepo.autenticar("admin@sana.cl", "admin123"))

        // Migracion de contraseña legacy en texto plano a PBKDF2
        dao.insertar(UsuarioEntity("legacy@sana.cl", "Legacy", "antigua123", "12345678-5", "91", "1990-01-01", "Los Vilos", "PACIENTE"))
        assertNotNull(sesionRepo.autenticar("legacy@sana.cl", "antigua123"))
        assertTrue(com.example.sanaesenciaapp.utils.Seguridad.esFormatoKdfNuevo(dao.obtenerPorEmail("legacy@sana.cl")!!.password))
    }
}
