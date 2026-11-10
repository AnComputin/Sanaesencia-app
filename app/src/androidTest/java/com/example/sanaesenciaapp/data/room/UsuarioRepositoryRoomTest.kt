package com.example.sanaesenciaapp.data.room

import android.database.sqlite.SQLiteException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sanaesenciaapp.data.repository.ResultadoRegistroUsuario
import com.example.sanaesenciaapp.data.repository.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsuarioRepositoryRoomTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: UsuarioRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = UsuarioRepository(database.usuarioDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertarDevuelveRowIdYRegistrarDevuelveRegistrado() = runBlocking {
        val rowId = database.usuarioDao().insertar(usuario("directo@dominio.cl"))
        assertTrue(rowId > 0L)

        val resultado = repository.registrar(usuario("Nuevo@dominio.cl"))
        assertEquals(ResultadoRegistroUsuario.Registrado, resultado)
    }

    @Test
    fun duplicadosExactosYConMayusculasNoSobrescribenElUsuarioOriginal() = runBlocking {
        val original = usuario("Persona@dominio.cl")
        assertEquals(ResultadoRegistroUsuario.Registrado, repository.registrar(original))

        assertEquals(
            ResultadoRegistroUsuario.CorreoDuplicado,
            repository.registrar(usuario("Persona@dominio.cl", nombre = "Otro"))
        )
        assertEquals(
            ResultadoRegistroUsuario.CorreoDuplicado,
            repository.registrar(usuario("persona@dominio.cl", nombre = "Otro"))
        )
        assertEquals(
            ResultadoRegistroUsuario.CorreoDuplicado,
            repository.registrar(usuario("PERSONA@DOMINIO.CL", nombre = "Otro"))
        )

        assertEquals(listOf(original), database.usuarioDao().obtenerTodos())
    }

    @Test
    fun trimSeAplicaSinCambiarLaCapitalizacionAlmacenada() = runBlocking {
        val original = usuario("  Persona@dominio.cl  ")
        assertEquals(ResultadoRegistroUsuario.Registrado, repository.registrar(original))

        assertEquals(
            ResultadoRegistroUsuario.CorreoDuplicado,
            repository.registrar(usuario("persona@dominio.cl"))
        )

        val stored = database.usuarioDao().obtenerTodos().single()
        assertEquals("Persona@dominio.cl", stored.email)
        assertEquals(original.password, stored.password)
        assertEquals(original.rol, stored.rol)
    }

    @Test
    fun concurrenciaSoloInsertaUnUsuarioYNoReemplazaElPrimero() = runBlocking {
        val first = usuario("Persona@dominio.cl", nombre = "Original")
        val second = usuario("persona@dominio.cl", nombre = "Concurrente")

        val results = withContext(Dispatchers.IO) {
            listOf(
                async { repository.registrar(first) },
                async { repository.registrar(second) }
            ).awaitAll()
        }

        assertEquals(1, results.count { it == ResultadoRegistroUsuario.Registrado })
        assertEquals(1, results.count { it == ResultadoRegistroUsuario.CorreoDuplicado })

        val stored = database.usuarioDao().obtenerTodos().single()
        assertTrue(stored == first || stored == second)
        assertEquals(
            if (stored == first) "Original" else "Concurrente",
            stored.nombre
        )
        assertEquals(1, database.usuarioDao().obtenerTodos().size)
    }

    @Test
    fun errorSqliteSeDevuelveComoErrorPersistencia() = runBlocking {
        val failingRepository = UsuarioRepository(
            FailingUsuarioDao { throw SQLiteException("simulated storage failure") }
        )

        assertEquals(
            ResultadoRegistroUsuario.ErrorPersistencia,
            failingRepository.registrar(usuario("error@dominio.cl"))
        )
    }

    @Test
    fun cancelacionSePropaga() {
        val cancellingRepository = UsuarioRepository(
            FailingUsuarioDao { throw CancellationException("cancelled") }
        )

        assertThrows(CancellationException::class.java) {
            runBlocking {
                cancellingRepository.registrar(usuario("cancel@dominio.cl"))
            }
        }
    }

    @Test
    fun obtenerPorEmailDevuelveUsuarioExistenteYNullSiNoExiste() = runBlocking {
        val original = usuario("Persona@dominio.cl")
        repository.registrar(original)

        val encontrado = repository.obtenerPorEmail("Persona@dominio.cl")
        assertEquals(original, encontrado)

        val inexistente = repository.obtenerPorEmail("noexiste@dominio.cl")
        assertNull(inexistente)
    }

    @Test
    fun obtenerPorEmailEsInsensibleAMayusculasYAplicaTrim() = runBlocking {
        val original = usuario("Persona@dominio.cl")
        repository.registrar(original)

        val enMinusculas = repository.obtenerPorEmail("persona@dominio.cl")
        assertEquals(original, enMinusculas)

        val enMayusculas = repository.obtenerPorEmail("PERSONA@DOMINIO.CL")
        assertEquals(original, enMayusculas)

        val conEspacios = repository.obtenerPorEmail("  Persona@dominio.cl  ")
        assertEquals(original, conEspacios)

        val conEspaciosYMinusculas = repository.obtenerPorEmail("  persona@dominio.cl  ")
        assertEquals(original, conEspaciosYMinusculas)
    }

    private fun usuario(
        email: String,
        nombre: String = "Persona",
        password: String = "credential-hash",
        rol: String = "PACIENTE"
    ) = UsuarioEntity(
        email = email,
        nombre = nombre,
        password = password,
        rut = "12.345.678-9",
        telefono = "+56912345678",
        fechaNacimiento = "01/01/1990",
        comuna = "Santiago",
        rol = rol
    )

    private class FailingUsuarioDao(
        private val insert: suspend (UsuarioEntity) -> Long = { -1L },
        private val getByEmail: suspend (String) -> UsuarioEntity? = { null }
    ) : UsuarioDao {
        override suspend fun insertar(usuario: UsuarioEntity): Long = insert(usuario)

        override suspend fun obtenerTodos(): List<UsuarioEntity> = emptyList()

        override suspend fun obtenerPorEmail(email: String): UsuarioEntity? = getByEmail(email)

        override suspend fun actualizarPassword(email: String, nuevaPassword: String): Int = 0
    }
}
