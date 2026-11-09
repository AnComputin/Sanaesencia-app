package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.utils.Seguridad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Datos de identidad necesarios durante una sesión. No contiene credenciales.
 */
data class SesionUsuario(
    val nombre: String,
    val email: String,
    val rut: String,
    val telefono: String,
    val fechaNacimiento: String,
    val comuna: String,
    val rol: String
)

/**
 * Centraliza la autenticación y el estado en memoria de la sesión.
 *
 * Consulta las credenciales a través de [UsuarioRepository] en un contexto de coroutine [ioDispatcher]
 * sin exponer entidades Room completas ni hashes al resto de la aplicación.
 */
class SesionRepository(
    private val usuarioRepository: UsuarioRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _sesionActual = MutableStateFlow<SesionUsuario?>(null)
    val sesionActual: StateFlow<SesionUsuario?> = _sesionActual.asStateFlow()

    suspend fun autenticar(
        email: String,
        password: String
    ): SesionUsuario? = withContext(ioDispatcher) {
        val emailLimpio = email.trim()

        if (emailLimpio.isBlank() || password.isEmpty()) {
            return@withContext null
        }

        val usuario = try {
            usuarioRepository.obtenerPorEmail(emailLimpio)
        } catch (e: CancellationException) {
            throw e
        } ?: return@withContext null

        // Rechaza el intento de ingresar el hash KDF codificado almacenado como si fuera la contraseña
        if (Seguridad.esFormatoKdfNuevo(usuario.password) && password == usuario.password) {
            return@withContext null
        }

        val passwordValida = Seguridad.verificar(password, usuario.password)

        if (!passwordValida) {
            return@withContext null
        }

        // Migración transparente de credenciales históricas hacia KDF PBKDF2 al iniciar sesión exitosamente
        if (!Seguridad.esFormatoKdfNuevo(usuario.password)) {
            try {
                val nuevoHash = Seguridad.hashear(password)
                usuarioRepository.actualizarPassword(usuario.email, nuevoHash)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // Si falla la persistencia de la migración, la autenticación no se interrumpe
            }
        }

        SesionUsuario(
            nombre = usuario.nombre,
            email = usuario.email,
            rut = usuario.rut,
            telefono = usuario.telefono,
            fechaNacimiento = usuario.fechaNacimiento,
            comuna = usuario.comuna,
            rol = usuario.rol
        ).also { _sesionActual.value = it }
    }

    fun cerrarSesion() {
        _sesionActual.value = null
    }
}
