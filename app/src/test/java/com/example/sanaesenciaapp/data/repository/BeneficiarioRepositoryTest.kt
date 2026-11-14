package com.example.sanaesenciaapp.data.repository

import android.database.sqlite.SQLiteException
import com.example.sanaesenciaapp.data.room.BeneficiarioDao
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import com.example.sanaesenciaapp.data.room.UsuarioDao
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import com.example.sanaesenciaapp.utils.Seguridad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BeneficiarioRepositoryTest {

    @Test
    fun observarBeneficiariosFiltraPorSesion() = runBlocking {
        val dao = FakeBeneficiarioDao().apply {
            seed(beneficiario("propio@sanaesencia.cl", "Propio"))
            seed(beneficiario("ajeno@sanaesencia.cl", "Ajeno"))
        }
        val repo = BeneficiarioRepository(dao, sesionAutenticada("propio@sanaesencia.cl"))
        val resultado = repo.observarBeneficiarios().first()

        assertEquals(listOf("Propio"), (resultado as ResultadoBeneficiario.Exito).valor.map { it.nombre })
        assertEquals("propio@sanaesencia.cl", dao.ultimoPropietarioConsultado)
    }

    @Test
    fun sinSesionNoConsultaBeneficiarios() = runBlocking {
        val dao = FakeBeneficiarioDao().apply { seed(beneficiario("persona@sanaesencia.cl", "Privado")) }
        val repo = BeneficiarioRepository(dao, sesionVacia())

        assertEquals(ResultadoBeneficiario.SesionNoAutenticada, repo.observarBeneficiarios().first())
        assertEquals(ResultadoBeneficiario.SesionNoAutenticada, repo.obtenerPorId(1))
        assertEquals(null, dao.ultimoPropietarioConsultado)
    }

    @Test
    fun noObtienePorIdBeneficiarioDeOtroPropietario() = runBlocking {
        val dao = FakeBeneficiarioDao().apply { seed(beneficiario("ajeno@sanaesencia.cl", "Privado")) }
        val repo = BeneficiarioRepository(dao, sesionAutenticada("propio@sanaesencia.cl"))

        assertEquals(ResultadoBeneficiario.NoEncontrado, repo.obtenerPorId(1))
        assertEquals("propio@sanaesencia.cl", dao.ultimoPropietarioConsultado)
    }

    @Test
    fun noActualizaNiDesactivaBeneficiarioAjeno() = runBlocking {
        val dao = FakeBeneficiarioDao().apply {
            seed(beneficiario("ajeno@sanaesencia.cl", "Original"))
            seed(beneficiario("ajeno@sanaesencia.cl", "Inactivo", activo = false))
        }
        val repo = BeneficiarioRepository(dao, sesionAutenticada("propio@sanaesencia.cl"))

        assertEquals(
            ResultadoBeneficiario.NoEncontrado,
            repo.actualizar(1, "Modificado", "12.345.678-5", "01/01/2000", "Familiar")
        )
        assertEquals(ResultadoBeneficiario.NoEncontrado, repo.desactivar(1))
        assertEquals(ResultadoBeneficiario.NoEncontrado, repo.reactivar(2))
        assertEquals("Original", dao.registros.getValue(1).nombre)
        assertTrue(dao.registros.getValue(1).activo)
        assertEquals("Inactivo", dao.registros.getValue(2).nombre)
        assertTrue(!dao.registros.getValue(2).activo)
    }

    @Test
    fun registrarUsaLaIdentidadAutenticada() = runBlocking {
        val dao = FakeBeneficiarioDao()
        val repo = BeneficiarioRepository(dao, sesionAutenticada("  Persona@SanaEsencia.cl "))

        assertEquals(
            ResultadoBeneficiario.Insertado,
            repo.registrar(" Beneficiaria ", "12.345.678-5", "01/01/2000", " Familiar ")
        )
        val guardado = dao.registros.values.single()
        assertEquals("persona@sanaesencia.cl", guardado.propietarioEmail)
        assertEquals("Beneficiaria", guardado.nombre)
        assertEquals("12345678-5", guardado.rut)
    }

    @Test
    fun rutActivoDuplicadoNoCreaOtraFila() = runBlocking {
        val dao = FakeBeneficiarioDao()
        val repo = BeneficiarioRepository(dao, sesionAutenticada("persona@sanaesencia.cl"))

        assertEquals(
            ResultadoBeneficiario.Insertado,
            repo.registrar("Original", "12.345.678-5", "01/01/2000", "Familiar")
        )
        assertEquals(
            ResultadoBeneficiario.YaExisteActivo,
            repo.registrar("No reemplazar", "12345678-5", "02/02/2001", "Otro")
        )
        assertEquals(1, dao.registros.size)
        assertEquals("Original", dao.registros.values.single().nombre)
    }

    @Test
    fun rutInactivoSeReactivaConservandoIdYDatos() = runBlocking {
        val dao = FakeBeneficiarioDao().apply {
            seed(beneficiario("persona@sanaesencia.cl", "Original", activo = false))
        }
        val repo = BeneficiarioRepository(dao, sesionAutenticada("persona@sanaesencia.cl"))

        assertEquals(
            ResultadoBeneficiario.Reactivado,
            repo.registrar("No sobrescribir", "12345678-5", "09/09/2009", "Otro")
        )
        val restaurado = dao.registros.values.single()
        assertEquals(1, restaurado.id)
        assertEquals("Original", restaurado.nombre)
        assertEquals("12345678-5", restaurado.rut)
        assertEquals("01/01/2000", restaurado.fechaNacimiento)
        assertEquals("Familiar", restaurado.parentesco)
        assertTrue(restaurado.activo)
    }

    @Test
    fun propietariosDistintosPuedenUsarElMismoRut() = runBlocking {
        val dao = FakeBeneficiarioDao()
        val repoUno = BeneficiarioRepository(dao, sesionAutenticada("uno@sanaesencia.cl"))
        val repoDos = BeneficiarioRepository(dao, sesionAutenticada("dos@sanaesencia.cl"))

        assertEquals(ResultadoBeneficiario.Insertado, repoUno.registrar("Primero", "12.345.678-5", "01/01/2000", "Familiar"))
        assertEquals(ResultadoBeneficiario.Insertado, repoDos.registrar("Segundo", "12.345.678-5", "01/01/2000", "Familiar"))
        assertEquals(2, dao.registros.size)
        assertEquals(setOf("uno@sanaesencia.cl", "dos@sanaesencia.cl"), dao.registros.values.map { it.propietarioEmail }.toSet())
    }

    @Test
    fun entradasInvalidasNoInsertan() = runBlocking {
        val dao = FakeBeneficiarioDao()
        val repo = BeneficiarioRepository(dao, sesionAutenticada("persona@sanaesencia.cl"))

        assertEquals(ResultadoBeneficiario.DatosInvalidos, repo.registrar("Persona", "...---", "01/01/2000", "Familiar"))
        assertEquals(ResultadoBeneficiario.DatosInvalidos, repo.registrar(" ", "12.345.678-5", "01/01/2000", "Familiar"))
        assertTrue(dao.registros.isEmpty())
    }

    @Test
    fun errorSqliteConservaCausa() = runBlocking {
        val sqlite = SQLiteException("fallo ficticio de persistencia")
        val dao = FakeBeneficiarioDao().apply { errorInsercion = sqlite }
        val repo = BeneficiarioRepository(dao, sesionAutenticada("persona@sanaesencia.cl"))

        val resultado = repo.registrar("Persona", "12.345.678-5", "01/01/2000", "Familiar")
        assertTrue(resultado is ResultadoBeneficiario.ErrorPersistencia)
        assertSame(sqlite, (resultado as ResultadoBeneficiario.ErrorPersistencia).causa)
    }

    @Test
    fun cancellationExceptionSePropaga() {
        val dao = FakeBeneficiarioDao().apply { cancelarInsercion = true }
        val repo = BeneficiarioRepository(dao, runBlocking { sesionAutenticada("persona@sanaesencia.cl") })

        assertThrows(CancellationException::class.java) {
            runBlocking { repo.registrar("Persona", "12.345.678-5", "01/01/2000", "Familiar") }
        }
    }

    private suspend fun sesionAutenticada(email: String): SesionRepository {
        val usuarioDao = FakeUsuarioDao(
            UsuarioEntity(
                email = email.trim().lowercase(),
                nombre = "Usuario de prueba",
                password = Seguridad.hashear("clave-prueba", iteraciones = 1000),
                rut = "12.345.678-5",
                telefono = "900000000",
                fechaNacimiento = "01/01/1990",
                comuna = "Santiago",
                rol = "PACIENTE"
            )
        )
        return SesionRepository(UsuarioRepository(usuarioDao), Dispatchers.Unconfined).also {
            check(it.autenticar(email, "clave-prueba") != null)
        }
    }

    private fun sesionVacia() = SesionRepository(UsuarioRepository(FakeUsuarioDao(null)), Dispatchers.Unconfined)

    private fun beneficiario(email: String, nombre: String, activo: Boolean = true) =
        BeneficiarioEntity(
            id = 0,
            propietarioEmail = email,
            nombre = nombre,
            rut = "12.345.678-5",
            fechaNacimiento = "01/01/2000",
            parentesco = "Familiar",
            activo = activo
        )

    private class FakeUsuarioDao(private val usuario: UsuarioEntity?) : UsuarioDao {
        override suspend fun insertar(usuario: UsuarioEntity): Long = 1L
        override suspend fun obtenerTodos(): List<UsuarioEntity> = listOfNotNull(usuario)
        override suspend fun obtenerPorEmail(email: String): UsuarioEntity? =
            usuario?.takeIf { it.email.equals(email.trim(), ignoreCase = true) }
        override suspend fun actualizarPassword(email: String, nuevaPassword: String): Int = 0
        override suspend fun contarCuentasSinFormatoPbkdf2(): Int = 0
    }

    private class FakeBeneficiarioDao : BeneficiarioDao() {
        val registros = linkedMapOf<Int, BeneficiarioEntity>()
        var ultimoPropietarioConsultado: String? = null
        var errorInsercion: SQLiteException? = null
        var cancelarInsercion = false
        private var siguienteId = 1

        fun seed(beneficiario: BeneficiarioEntity) {
            val guardado = beneficiario.copy(
                id = siguienteId++,
                propietarioEmail = beneficiario.propietarioEmail.trim().lowercase(),
                rut = beneficiario.rut.trim().replace(".", "").uppercase()
            )
            registros[guardado.id] = guardado
        }

        override fun obtenerPorPropietarioFlow(email: String): Flow<List<BeneficiarioEntity>> {
            ultimoPropietarioConsultado = email
            return flowOf(registros.values.filter { it.activo && it.propietarioEmail.equals(email, true) })
        }

        override suspend fun obtenerPorPropietario(email: String): List<BeneficiarioEntity> {
            ultimoPropietarioConsultado = email
            return registros.values.filter { it.activo && it.propietarioEmail.equals(email, true) }
        }

        override suspend fun obtenerPorIdYPropietario(id: Int, email: String): BeneficiarioEntity? {
            ultimoPropietarioConsultado = email
            return registros[id]?.takeIf { it.activo && it.propietarioEmail.equals(email, true) }
        }

        override suspend fun obtenerTodosPorPropietario(email: String): List<BeneficiarioEntity> {
            ultimoPropietarioConsultado = email
            return registros.values.filter { it.propietarioEmail.equals(email, true) }
        }

        override suspend fun insertarInterno(beneficiario: BeneficiarioEntity): Long {
            if (cancelarInsercion) throw CancellationException("cancelación ficticia")
            errorInsercion?.let { throw it }
            val owner = beneficiario.propietarioEmail.trim().lowercase()
            val rut = beneficiario.rut.trim().replace(".", "").uppercase()
            if (registros.values.any { it.propietarioEmail.equals(owner, true) && it.rut == rut }) return -1L
            val id = siguienteId++
            registros[id] = beneficiario.copy(id = id, propietarioEmail = owner, rut = rut)
            return id.toLong()
        }

        override suspend fun actualizarInterno(
            id: Int,
            email: String,
            nombre: String,
            rut: String,
            fechaNacimiento: String,
            parentesco: String
        ): Int {
            val previo = registros[id]?.takeIf { it.propietarioEmail.equals(email, true) } ?: return 0
            registros[id] = previo.copy(nombre = nombre, rut = rut, fechaNacimiento = fechaNacimiento, parentesco = parentesco)
            return 1
        }

        override suspend fun desactivar(id: Int, email: String): Int {
            val previo = registros[id]?.takeIf { it.activo && it.propietarioEmail.equals(email, true) } ?: return 0
            registros[id] = previo.copy(activo = false)
            return 1
        }

        override suspend fun reactivar(id: Int, email: String): Int {
            val previo = registros[id]?.takeIf { !it.activo && it.propietarioEmail.equals(email, true) } ?: return 0
            registros[id] = previo.copy(activo = true)
            return 1
        }
    }
}
