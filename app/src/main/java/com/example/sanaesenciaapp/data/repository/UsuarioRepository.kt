package com.example.sanaesenciaapp.data.repository

import android.database.sqlite.SQLiteException
import com.example.sanaesenciaapp.data.room.UsuarioDao
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import kotlinx.coroutines.CancellationException

sealed interface ResultadoRegistroUsuario {
    data object Registrado : ResultadoRegistroUsuario
    data object CorreoDuplicado : ResultadoRegistroUsuario
    data object ErrorPersistencia : ResultadoRegistroUsuario
}

/**
 * Acceso a persistencia de usuarios mediante la instancia compartida de Room.
 *
 * La consulta completa se usa durante la carga interna de usuarios y la autenticación
 * local existente. No debe utilizarse para mostrar perfiles porque UsuarioEntity
 * incluye el dato de credencial almacenado.
 */
class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {
    suspend fun obtenerTodos(): List<UsuarioEntity> = usuarioDao.obtenerTodos()

    /**
     * Inserta el correo recortado conservando su capitalización original.
     * La clave primaria NOCASE de SQLite resuelve atómicamente los duplicados.
     */
    suspend fun registrar(usuario: UsuarioEntity): ResultadoRegistroUsuario {
        return try {
            val rowId = usuarioDao.insertar(usuario.copy(email = usuario.email.trim()))
            if (rowId == -1L) {
                ResultadoRegistroUsuario.CorreoDuplicado
            } else {
                ResultadoRegistroUsuario.Registrado
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: SQLiteException) {
            ResultadoRegistroUsuario.ErrorPersistencia
        }
    }

    /**
     * Inserción de bajo nivel usada por la carga inicial existente.
     * IGNORE garantiza que una fila previa no sea reemplazada.
     */
    suspend fun insertar(usuario: UsuarioEntity): Long = usuarioDao.insertar(usuario)
}
