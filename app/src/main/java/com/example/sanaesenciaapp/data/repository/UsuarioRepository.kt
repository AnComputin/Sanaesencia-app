package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.data.room.UsuarioDao
import com.example.sanaesenciaapp.data.room.UsuarioEntity

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

    suspend fun insertar(usuario: UsuarioEntity) {
        usuarioDao.insertar(usuario)
    }
}
