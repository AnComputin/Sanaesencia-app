package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.data.room.UsuarioDao
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import com.example.sanaesenciaapp.utils.Seguridad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class SesionRepositoryTest {

    private class FakeUsuarioDao : UsuarioDao {
        val usuarios = mutableMapOf<String, UsuarioEntity>()
        var obtenerTodosLlamado = false
        var obtenerPorEmailLlamadas = 0
        var throwCancellationOnGet = false

        override suspend fun insertar(usuario: UsuarioEntity): Long {
            val key = usuario.email.lowercase()
            return if (usuarios.containsKey(key)) {
                -1L
            } else {
                usuarios[key] = usuario
                1L
            }
        }

        override suspend fun obtenerTodos(): List<UsuarioEntity> {
            obtenerTodosLlamado = true
            return usuarios.values.toList()
        }

        override suspend fun obtenerPorEmail(email: String): UsuarioEntity? {
            if (throwCancellationOnGet) throw CancellationException("cancelled")
            obtenerPorEmailLlamadas++
            return usuarios[email.lowercase()]
        }
    }

    private fun crearUsuario(
        email: String = "paciente@sanaesencia.cl",
        nombre: String = "Anaís Valenzuela",
        password: String = Seguridad.hashear("password123"),
        rol: String = "PACIENTE"
    ) = UsuarioEntity(
        email = email,
        nombre = nombre,
        password = password,
        rut = "12.345.678-9",
        telefono = "+56912345678",
        fechaNacimiento = "15/05/1992",
        comuna = "Los Vilos",
        rol = rol
    )

    @Test
    fun `1 autenticar con usuario existente y contrasena correcta retorna SesionUsuario`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        val user = crearUsuario(password = Seguridad.hashear("password123"))
        fakeDao.insertar(user)

        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        val resultado = sesionRepo.autenticar("paciente@sanaesencia.cl", "password123")

        assertNotNull(resultado)
        assertEquals("paciente@sanaesencia.cl", resultado?.email)
        assertEquals("Anaís Valenzuela", resultado?.nombre)
        assertEquals("PACIENTE", resultado?.rol)
    }

    @Test
    fun `2 autenticar con usuario inexistente retorna null`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        val resultado = sesionRepo.autenticar("inexistente@sanaesencia.cl", "password123")

        assertNull(resultado)
    }

    @Test
    fun `3 autenticar con contrasena incorrecta retorna null`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        fakeDao.insertar(crearUsuario(password = Seguridad.hashear("correcta123")))

        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        val resultado = sesionRepo.autenticar("paciente@sanaesencia.cl", "erronea123")

        assertNull(resultado)
    }

    @Test
    fun `4 autenticar con correo con mayusculas y minusculas es insensible a capitalizacion`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        fakeDao.insertar(crearUsuario(email = "Paciente@SanaEsencia.cl", password = Seguridad.hashear("password123")))

        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        val resultadoMin = sesionRepo.autenticar("paciente@sanaesencia.cl", "password123")
        assertNotNull(resultadoMin)

        val resultadoMay = sesionRepo.autenticar("PACIENTE@SANAESENCIA.CL", "password123")
        assertNotNull(resultadoMay)
    }

    @Test
    fun `5 autenticar con correo con espacios exteriores aplica trim y autentica`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        fakeDao.insertar(crearUsuario(email = "paciente@sanaesencia.cl", password = Seguridad.hashear("password123")))

        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        val resultado = sesionRepo.autenticar("   paciente@sanaesencia.cl   ", "password123")

        assertNotNull(resultado)
        assertEquals("paciente@sanaesencia.cl", resultado?.email)
    }

    @Test
    fun `6 autenticar con hash almacenado introducido literalmente como contrasena es rechazado`() = runBlocking {
        val hash = Seguridad.hashear("claveSecreta123")
        val fakeDao = FakeUsuarioDao()
        fakeDao.insertar(crearUsuario(password = hash))

        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        val resultado = sesionRepo.autenticar("paciente@sanaesencia.cl", hash)

        assertNull("El hash literal nunca debe ser aceptado como contraseña", resultado)
    }

    @Test
    fun `7 autenticar con contrasena historica en texto plano autentica correctamente`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        fakeDao.insertar(crearUsuario(password = "clave_plano_123"))

        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        val resultado = sesionRepo.autenticar("paciente@sanaesencia.cl", "clave_plano_123")

        assertNotNull(resultado)
        assertEquals("paciente@sanaesencia.cl", resultado?.email)
    }

    @Test
    fun `7b autenticar conserva espacios intencionales en la contrasena`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        val passwordConEspacios = "  clave con espacios  "
        fakeDao.insertar(crearUsuario(password = Seguridad.hashear(passwordConEspacios)))

        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        val resultadoExitoso = sesionRepo.autenticar("paciente@sanaesencia.cl", passwordConEspacios)
        assertNotNull("La contraseña con espacios intencionales debe ser aceptada si coincide exactamente", resultadoExitoso)

        val resultadoFallido = sesionRepo.autenticar("paciente@sanaesencia.cl", "clave con espacios")
        assertNull("Si se omiten los espacios intencionales la autenticación debe fallar", resultadoFallido)
    }

    @Test
    fun `8 autenticar propaga CancellationException correctamente`() {
        val fakeDao = FakeUsuarioDao().apply {
            throwCancellationOnGet = true
        }
        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        assertThrows(CancellationException::class.java) {
            runBlocking {
                sesionRepo.autenticar("paciente@sanaesencia.cl", "password123")
            }
        }
    }

    @Test
    fun `9 autenticar consulta exclusivamente por email y no invoca obtenerTodos`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        fakeDao.insertar(crearUsuario(password = Seguridad.hashear("password123")))

        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        sesionRepo.autenticar("paciente@sanaesencia.cl", "password123")

        assertFalse("obtenerTodos no debe ser invocado durante la autenticación", fakeDao.obtenerTodosLlamado)
        assertEquals(1, fakeDao.obtenerPorEmailLlamadas)
    }

    @Test
    fun `10 SesionUsuario no expone contrasenas ni hashes`() {
        val campos = SesionUsuario::class.java.declaredFields
            .filter { !it.isSynthetic && !it.name.startsWith("$") }
            .map { it.name.lowercase() }

        assertFalse(campos.any { it.contains("password") })
        assertFalse(campos.any { it.contains("hash") })
        assertFalse(campos.any { it.contains("credencial") })
        assertFalse(campos.any { it.contains("sal") })

        val camposEsperados = setOf("nombre", "email", "rut", "telefono", "fechanacimiento", "comuna", "rol")
        assertEquals(camposEsperados, campos.toSet())
    }

    @Test
    fun `11 sesionActual cambia despues de autenticar exitosamente`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        fakeDao.insertar(crearUsuario(password = Seguridad.hashear("password123")))

        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        assertNull(sesionRepo.sesionActual.value)

        val sesion = sesionRepo.autenticar("paciente@sanaesencia.cl", "password123")
        assertNotNull(sesion)
        assertEquals(sesion, sesionRepo.sesionActual.value)
    }

    @Test
    fun `12 cerrarSesion limpia sesionActual a null`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        fakeDao.insertar(crearUsuario(password = Seguridad.hashear("password123")))

        val usuarioRepo = UsuarioRepository(fakeDao)
        val sesionRepo = SesionRepository(usuarioRepo, Dispatchers.Unconfined)

        sesionRepo.autenticar("paciente@sanaesencia.cl", "password123")
        assertNotNull(sesionRepo.sesionActual.value)

        sesionRepo.cerrarSesion()
        assertNull(sesionRepo.sesionActual.value)
    }
}
