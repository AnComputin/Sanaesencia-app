package com.example.sanaesenciaapp.data.repository

import android.database.sqlite.SQLiteException
import com.example.sanaesenciaapp.data.remote.LoginRequest
import com.example.sanaesenciaapp.data.remote.RegistroRequest
import com.example.sanaesenciaapp.data.remote.RetrofitClient
import com.example.sanaesenciaapp.data.remote.UsuarioDto
import com.example.sanaesenciaapp.data.room.UsuarioDao
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import kotlinx.coroutines.CancellationException

sealed interface ResultadoRegistroUsuario {
    data object Registrado : ResultadoRegistroUsuario
    data object CorreoDuplicado : ResultadoRegistroUsuario
    data object ErrorPersistencia : ResultadoRegistroUsuario
}

class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {
    suspend fun obtenerTodos(): List<UsuarioEntity> = usuarioDao.obtenerTodos()

    suspend fun obtenerPorEmail(email: String): UsuarioEntity? {
        val correoLimpio = email.trim()
        val local = usuarioDao.obtenerPorEmail(correoLimpio)
        if (local != null) return local

        return try {
            val response = RetrofitClient.usuariosApi.obtenerPorEmail(correoLimpio.lowercase())
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    val entidad = dto.toEntity(passwordGuardar = "")
                    usuarioDao.insertar(entidad)
                    entidad
                }
            } else {
                null
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Autentica contra usuarios-service (:8081), guarda el token JWT en TokenManager
     * y sincroniza el perfil del usuario en Room.
     */
    suspend fun login(email: String, passwordPlano: String): UsuarioEntity? {
        return try {
            val response = RetrofitClient.usuariosApi.login(
                LoginRequest(email = email.trim(), password = passwordPlano)
            )
            if (!response.isSuccessful) return null

            val auth = response.body() ?: return null
            RetrofitClient.getTokenManager().guardarToken(auth.token)

            val entidad = auth.usuario.toEntity(passwordGuardar = "")
            usuarioDao.insertar(entidad)
            entidad
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Registra el usuario en MySQL (usuarios-service) y guarda el JWT + caché en Room.
     * Si se pasa [passwordPlano], se envía sin doble hash para que Spring Boot lo encripte
     * con Pbkdf2PasswordEncoderCompat.
     */
    suspend fun registrar(
        usuario: UsuarioEntity,
        passwordPlano: String? = null
    ): ResultadoRegistroUsuario {
        return try {
            val correoLimpio = usuario.email.trim()
            val passwordEnviar = passwordPlano ?: usuario.password

            val response = RetrofitClient.usuariosApi.registro(
                RegistroRequest(
                    email = correoLimpio,
                    nombre = usuario.nombre.trim(),
                    password = passwordEnviar,
                    rut = usuario.rut.trim(),
                    telefono = usuario.telefono.trim(),
                    fechaNacimiento = normalizarFechaIso(usuario.fechaNacimiento),
                    comuna = usuario.comuna.trim(),
                    rol = usuario.rol.trim()
                )
            )

            when {
                response.isSuccessful -> {
                    val auth = response.body()
                    if (auth != null) {
                        RetrofitClient.getTokenManager().guardarToken(auth.token)
                    }
                    val rowId = usuarioDao.insertar(usuario.copy(email = correoLimpio))
                    if (rowId == -1L) {
                        ResultadoRegistroUsuario.CorreoDuplicado
                    } else {
                        ResultadoRegistroUsuario.Registrado
                    }
                }
                response.code() == 400 || response.code() == 409 -> {
                    ResultadoRegistroUsuario.CorreoDuplicado
                }
                else -> ResultadoRegistroUsuario.ErrorPersistencia
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: SQLiteException) {
            ResultadoRegistroUsuario.ErrorPersistencia
        } catch (_: Exception) {
            ResultadoRegistroUsuario.ErrorPersistencia
        }
    }

    suspend fun actualizarPassword(email: String, nuevaPassword: String): Boolean {
        return try {
            val filasAfectadas = usuarioDao.actualizarPassword(email.trim(), nuevaPassword)
            filasAfectadas > 0
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            false
        }
    }

    suspend fun insertar(usuario: UsuarioEntity): Long = usuarioDao.insertar(usuario)

    suspend fun contarCuentasSinFormatoPbkdf2(): Int =
        usuarioDao.contarCuentasSinFormatoPbkdf2()

    private fun UsuarioDto.toEntity(passwordGuardar: String): UsuarioEntity =
        UsuarioEntity(
            email = email,
            nombre = nombre,
            password = passwordGuardar,
            rut = rut,
            telefono = telefono,
            fechaNacimiento = fechaNacimiento,
            comuna = comuna,
            rol = rol
        )

    private fun normalizarFechaIso(fecha: String): String {
        val limpia = fecha.trim()
        val match = Regex("""^(\d{2})[/-](\d{2})[/-](\d{4})$""").matchEntire(limpia)
        return if (match != null) {
            val (dia, mes, anio) = match.destructured
            "$anio-$mes-$dia"
        } else {
            limpia
        }
    }
}