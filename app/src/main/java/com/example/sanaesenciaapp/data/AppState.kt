package com.example.sanaesenciaapp.data

import androidx.compose.runtime.mutableStateListOf
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class Usuario(
    val nombre: String,
    val email: String,
    val password: String
)

class AppState(
    private val db: AppDatabase
) {

    val usuarios = mutableStateListOf<Usuario>()

    var usuarioActual: Usuario? = null
        private set

    private val scope =
        CoroutineScope(Dispatchers.IO)


    fun cargarDatos() {

        scope.launch {

            val usuariosDb =
                db.usuarioDao().obtenerTodos()

            usuarios.clear()

            usuarios.addAll(

                usuariosDb.map {

                    Usuario(
                        nombre = it.nombre,
                        email = it.email,
                        password = it.password
                    )
                }
            )
        }
    }


    fun registrarUsuario(
        nombre: String,
        email: String,
        password: String
    ): Boolean {

        if (
            usuarios.any {
                it.email == email
            }
        ) {
            return false
        }

        val nuevoUsuario = Usuario(
            nombre = nombre,
            email = email,
            password = password
        )

        usuarios.add(nuevoUsuario)

        scope.launch {

            db.usuarioDao().insertar(

                UsuarioEntity(
                    nombre = nombre,
                    email = email,
                    password = password
                )
            )
        }

        return true
    }


    fun login(
        email: String,
        password: String
    ): Boolean {

        val usuario = usuarios.find {

            it.email == email &&
                    it.password == password
        }

        if (usuario == null) {

            return false
        }

        usuarioActual = usuario

        return true
    }


    fun logout() {

        usuarioActual = null
    }
}