package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.data.remote.RetrofitClient
import com.example.sanaesenciaapp.utils.Seguridad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class SesionUsuario(
    val nombre: String,
    val email: String,
    val rut: String,
    val telefono: String,
    val fechaNacimiento: String,
    val comuna: String,
    val rol: String
)

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

        // 1. Intentar autenticación remota en usuarios-service (:8081) para obtener el token JWT
        val usuarioRemoto = try {
            usuarioRepository.login(emailLimpio, password)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }

        if (usuarioRemoto != null) {
            return@withContext SesionUsuario(
                nombre = usuarioRemoto.nombre,
                email = usuarioRemoto.email,
                rut = usuarioRemoto.rut,
                telefono = usuarioRemoto.telefono,
                fechaNacimiento = usuarioRemoto.fechaNacimiento,
                comuna = usuarioRemoto.comuna,
                rol = usuarioRemoto.rol
            ).also { _sesionActual.value = it }
        }

        // 2. Respaldo local en Room (útil para cuentas semilla locales como admin@sanaesencia.cl o modo sin conexión)
        val usuario = try {
            usuarioRepository.obtenerPorEmail(emailLimpio)
        } catch (e: CancellationException) {
            throw e
        } ?: return@withContext null

        if (Seguridad.esFormatoKdfNuevo(usuario.password) && password == usuario.password) {
            return@withContext null
        }

        val passwordValida = Seguridad.verificar(password, usuario.password)
        if (!passwordValida) {
            return@withContext null
        }

        if (!Seguridad.esFormatoKdfNuevo(usuario.password)) {
            try {
                val nuevoHash = Seguridad.hashear(password)
                usuarioRepository.actualizarPassword(usuario.email, nuevoHash)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
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
        try {
            RetrofitClient.getTokenManager().limpiarToken()
        } catch (_: Exception) {
        }
        _sesionActual.value = null
    }
}