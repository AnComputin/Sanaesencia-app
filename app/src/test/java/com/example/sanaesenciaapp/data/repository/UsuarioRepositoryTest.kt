package com.example.sanaesenciaapp.data.repository

import android.database.sqlite.SQLiteException
import com.example.sanaesenciaapp.data.room.UsuarioDao
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class UsuarioRepositoryTest {

    private class FakeUsuarioDao : UsuarioDao {
        val usuarios = mutableMapOf<String, UsuarioEntity>()
        var throwOnInsert: SQLiteException? = null
        var throwCancellationOnInsert: Boolean = false
        var throwCancellationOnGet: Boolean = false

        override suspend fun insertar(usuario: UsuarioEntity): Long {
            if (throwCancellationOnInsert) throw CancellationException("cancelled")
            throwOnInsert?.let { throw it }

            val key = usuario.email.lowercase()
            return if (usuarios.containsKey(key)) {
                -1L
            } else {
                usuarios[key] = usuario
                1L
            }
        }

        override suspend fun obtenerTodos(): List<UsuarioEntity> = usuarios.values.toList()

        override suspend fun obtenerPorEmail(email: String): UsuarioEntity? {
            if (throwCancellationOnGet) throw CancellationException("cancelled")
            return usuarios[email.lowercase()]
        }
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

    @Test
    fun `obtenerPorEmail devuelve el usuario si existe y null si no existe`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        val repository = UsuarioRepository(fakeDao)

        val user = usuario("Persona@dominio.cl")
        repository.registrar(user)

        val encontrado = repository.obtenerPorEmail("Persona@dominio.cl")
        assertEquals(user, encontrado)

        val inexistente = repository.obtenerPorEmail("noexiste@dominio.cl")
        assertNull(inexistente)
    }

    @Test
    fun `obtenerPorEmail es insensible a mayusculas y aplica trim`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        val repository = UsuarioRepository(fakeDao)

        val user = usuario("Persona@dominio.cl")
        repository.registrar(user)

        val enMinusculas = repository.obtenerPorEmail("persona@dominio.cl")
        assertEquals(user, enMinusculas)

        val enMayusculas = repository.obtenerPorEmail("PERSONA@DOMINIO.CL")
        assertEquals(user, enMayusculas)

        val conEspacios = repository.obtenerPorEmail("  Persona@dominio.cl  ")
        assertEquals(user, conEspacios)

        val conEspaciosYMinusculas = repository.obtenerPorEmail("  persona@dominio.cl  ")
        assertEquals(user, conEspaciosYMinusculas)
    }

    @Test
    fun `registrar usuario con correo duplicado no modifica el original`() = runBlocking {
        val fakeDao = FakeUsuarioDao()
        val repository = UsuarioRepository(fakeDao)

        val original = usuario("Persona@dominio.cl", nombre = "Original")
        val resultadoPrimero = repository.registrar(original)
        assertEquals(ResultadoRegistroUsuario.Registrado, resultadoPrimero)

        val duplicado = usuario("persona@dominio.cl", nombre = "Duplicado")
        val resultadoDuplicado = repository.registrar(duplicado)
        assertEquals(ResultadoRegistroUsuario.CorreoDuplicado, resultadoDuplicado)

        val almacenado = repository.obtenerPorEmail("persona@dominio.cl")
        assertEquals("Original", almacenado?.nombre)
    }

    @Test
    fun `registrar cuando ocurre SQLiteException retorna ErrorPersistencia`() = runBlocking {
        val fakeDao = FakeUsuarioDao().apply {
            throwOnInsert = SQLiteException("DB error")
        }
        val repository = UsuarioRepository(fakeDao)

        val resultado = repository.registrar(usuario("test@dominio.cl"))
        assertEquals(ResultadoRegistroUsuario.ErrorPersistencia, resultado)
    }

    @Test
    fun `registrar propaga CancellationException`() {
        val fakeDao = FakeUsuarioDao().apply {
            throwCancellationOnInsert = true
        }
        val repository = UsuarioRepository(fakeDao)

        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.registrar(usuario("test@dominio.cl"))
            }
        }
    }

    @Test
    fun `obtenerPorEmail propaga CancellationException`() {
        val fakeDao = FakeUsuarioDao().apply {
            throwCancellationOnGet = true
        }
        val repository = UsuarioRepository(fakeDao)

        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.obtenerPorEmail("test@dominio.cl")
            }
        }
    }
}
