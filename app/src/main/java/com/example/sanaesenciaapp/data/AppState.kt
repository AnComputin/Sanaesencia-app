package com.example.sanaesenciaapp.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sanaesenciaapp.data.room.AppDatabase
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
    private val db: AppDatabase
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

            // Gestión del Catálogo de Especialidades en Room
            val especialidadDao = db.especialidadDao()
            var listaEspDb = especialidadDao.obtenerTodas()

            // Si está vacío, insertamos los datos iniciales por defecto (Seed data)
            if (listaEspDb.isEmpty()) {
                val iniciales = listOf(
                    EspecialidadEntity(
                        id = 1,
                        nombre = "Psicología Infanto-Juvenil",
                        descripcion = "Acompañamiento psicológico integral para niños y jóvenes.",
                        icono = "🧠",
                        profesional = "Mg. Andrea Soto",
                        modalidad = "Ambas",
                        agendaPausada = false
                    ),
                    EspecialidadEntity(
                        id = 2,
                        nombre = "Fonoaudiología",
                        descripcion = "Evaluación y tratamiento de trastornos del habla y lenguaje.",
                        icono = "🗣️",
                        profesional = "Klga. Camila Rojas",
                        modalidad = "Presencial",
                        agendaPausada = false
                    ),
                    EspecialidadEntity(
                        id = 3,
                        nombre = "Terapia Ocupacional",
                        descripcion = "Estimulación sensorial y desarrollo de autonomía.",
                        icono = "🧩",
                        profesional = "TO. Javiera Paz",
                        modalidad = "Presencial",
                        agendaPausada = true
                    ),
                    EspecialidadEntity(
                        id = 4,
                        nombre = "Nutrición Integrativa",
                        descripcion = "Planes nutricionales enfocados en el bienestar holístico.",
                        icono = "🥗",
                        profesional = "Nut. Felipe Morales",
                        modalidad = "Ambas",
                        agendaPausada = false
                    )
                )
                especialidadDao.insertarTodas(iniciales)
                listaEspDb = especialidadDao.obtenerTodas()
            }

            // Actualizamos la lista observable en el hilo principal
            withContext(Dispatchers.Main) {
                especialidades.clear()
                especialidades.addAll(listaEspDb)
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

        val usuario = usuarios.find {
            it.email.equals(
                email.trim(),
                ignoreCase = true
            ) && Seguridad.verificar(password, it.password)
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
