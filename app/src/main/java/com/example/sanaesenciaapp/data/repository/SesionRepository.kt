package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.data.Usuario
import com.example.sanaesenciaapp.utils.Seguridad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
 * Centraliza la autenticación local existente y el estado en memoria de la sesión.
 *
 * La verificación conserva las reglas actuales de AppState. La lista recibida procede
 * de sus usuarios cargados desde Room y solo se consulta durante la autenticación;
 * no se conserva en este repositorio.
 */
class SesionRepository {

    private val _sesionActual = MutableStateFlow<SesionUsuario?>(null)
    val sesionActual: StateFlow<SesionUsuario?> = _sesionActual.asStateFlow()

    internal fun autenticar(
        email: String,
        password: String,
        usuarios: List<Usuario>
    ): SesionUsuario? {
        val emailLimpio = email.trim()
        val passwordLimpia = password.trim()

        val usuario = usuarios.find {
            it.email.equals(emailLimpio, ignoreCase = true) &&
                (
                    Seguridad.verificar(passwordLimpia, it.password) ||
                        Seguridad.verificar(password, it.password) ||
                        // Compatibilidad con usuarios antiguos guardados sin hash.
                        it.password == passwordLimpia ||
                        it.password == password
                    )
        } ?: return null

        return SesionUsuario(
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
