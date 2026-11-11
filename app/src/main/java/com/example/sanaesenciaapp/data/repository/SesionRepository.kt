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

        val passwordValida = if (esFormatoHashValido(usuario.password)) {
            // Verificación criptográfica con sal y SHA-256 (prototipo).
            // Se rechaza explícitamente cualquier coincidencia literal con el hash almacenado
            // para evitar que la cadena codificada de la base de datos se utilice como contraseña.
            if (password == usuario.password) {
                false
            } else {
                Seguridad.verificar(password, usuario.password)
            }
        } else {
            // Compatibilidad transicional con cuentas históricas almacenadas en texto plano.
            // Se preservan espacios intencionales de la contraseña.
            usuario.password == password
        }

        if (!passwordValida) {
            return@withContext null
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

    private fun esFormatoHashValido(almacenado: String): Boolean {
        val partes = almacenado.split(":")
        if (partes.size != 2) return false
        val (salHex, hashHex) = partes
        return salHex.length == 32 && salHex.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' } &&
            hashHex.length == 64 && hashHex.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
    }
}
