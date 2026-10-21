package com.example.sanaesenciaapp.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.ESPECIALIDADES_INICIALES
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import com.example.sanaesenciaapp.utils.Seguridad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Usuario(
    val nombre: String,
    val email: String,
    val password: String,
    val rut: String,
    val telefono: String,
    val fechaNacimiento: String,
    val comuna: String,
    val rol: String
)

class AppState(
    val db: AppDatabase
) {

    val usuarios = mutableStateListOf<Usuario>()

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    // Lista observable de especialidades para la UI y Room
    val especialidades = mutableStateListOf<EspecialidadEntity>()
    var especialidadSeleccionada by mutableStateOf<EspecialidadEntity?>(null)

    private val scope = CoroutineScope(Dispatchers.IO)

    fun cargarDatos() {
        scope.launch {
            val usuariosDb = db.usuarioDao().obtenerTodos()
            usuarios.clear()
            usuarios.addAll(
                usuariosDb.map {
                    Usuario(
                        nombre = it.nombre,
                        email = it.email,
                        password = it.password,
                        rut = it.rut,
                        telefono = it.telefono,
                        fechaNacimiento = it.fechaNacimiento,
                        comuna = it.comuna,
                        rol = it.rol
                    )
                }
            )

            // Asegurar que existe y está actualizado el usuario Administrador ficticio para pruebas
            val adminExistente = usuariosDb.find {
                it.email.equals("admin@sanaesencia.cl", ignoreCase = true)
            }
            val passwordHasheadaAdmin = Seguridad.hashear("admin123")
            val adminEntity = UsuarioEntity(
                nombre = adminExistente?.nombre ?: "Administrador SanaEsencia",
                email = "admin@sanaesencia.cl",
                password = passwordHasheadaAdmin,
                rut = adminExistente?.rut?.ifBlank { "11.111.111-1" } ?: "11.111.111-1",
                telefono = adminExistente?.telefono?.ifBlank { "+56912345678" } ?: "+56912345678",
                fechaNacimiento = adminExistente?.fechaNacimiento?.ifBlank { "01/01/1985" } ?: "01/01/1985",
                comuna = adminExistente?.comuna?.ifBlank { "Santiago" } ?: "Santiago",
                rol = "ADMIN"
            )
            db.usuarioDao().insertar(adminEntity)

            val usuarioAdminObj = Usuario(
                nombre = adminEntity.nombre,
                email = adminEntity.email,
                password = adminEntity.password,
                rut = adminEntity.rut,
                telefono = adminEntity.telefono,
                fechaNacimiento = adminEntity.fechaNacimiento,
                comuna = adminEntity.comuna,
                rol = adminEntity.rol
            )

            withContext(Dispatchers.Main) {
                val indexAdmin = usuarios.indexOfFirst { it.email.equals("admin@sanaesencia.cl", ignoreCase = true) }
                if (indexAdmin >= 0) {
                    usuarios[indexAdmin] = usuarioAdminObj
                } else {
                    usuarios.add(usuarioAdminObj)
                }
            }

            // Gestión del Catálogo de Especialidades en Room
            val especialidadDao = db.especialidadDao()
            val listaEspDb = especialidadDao.obtenerTodas()

            // Si está vacío, insertamos los datos iniciales por defecto (Seed data)
            if (listaEspDb.isEmpty()) {
                especialidadDao.insertarTodas(ESPECIALIDADES_INICIALES)
            }

            // Observar cambios en tiempo real desde Room
            especialidadDao.obtenerTodasFlow().collect { listaActualizada ->
                withContext(Dispatchers.Main) {
                    especialidades.clear()
                    especialidades.addAll(listaActualizada)
                }
            }
        }
    }

    fun registrarUsuario(
        nombre: String,
        email: String,
        password: String,
        rut: String,
        telefono: String,
        fechaNacimiento: String,
        comuna: String,
        rol: String
    ): Boolean {

        if (
            usuarios.any {
                it.email.equals(
                    email.trim(),
                    ignoreCase = true
                )
            }
        ) {
            return false
        }

        val passwordHasheada = Seguridad.hashear(password)

        val nuevoUsuario = Usuario(
            nombre = nombre.trim(),
            email = email.trim(),
            password = passwordHasheada,
            rut = rut.trim(),
            telefono = telefono.trim(),
            fechaNacimiento = fechaNacimiento.trim(),
            comuna = comuna.trim(),
            rol = rol
        )

        usuarios.add(nuevoUsuario)

        scope.launch {

            db.usuarioDao().insertar(

                UsuarioEntity(
                    nombre = nombre.trim(),
                    email = email.trim(),
                    password = passwordHasheada,
                    rut = rut.trim(),
                    telefono = telefono.trim(),
                    fechaNacimiento = fechaNacimiento.trim(),
                    comuna = comuna.trim(),
                    rol = rol
                )
            )
        }

        return true
    }

    fun existeCorreo(email: String): Boolean {

        return usuarios.any {
            it.email.equals(
                email.trim(),
                ignoreCase = true
            )
        }
    }

    fun login(
        email: String,
        password: String
    ): Boolean {
        val emailLimpio = email.trim()
        val passLimpia = password.trim()

        val usuario = usuarios.find {
            it.email.equals(emailLimpio, ignoreCase = true) &&
                    (Seguridad.verificar(passLimpia, it.password) ||
                     Seguridad.verificar(password, it.password) ||
                     it.password == passLimpia ||
                     it.password == password)
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
