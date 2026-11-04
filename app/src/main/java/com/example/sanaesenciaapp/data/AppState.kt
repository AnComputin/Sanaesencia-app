package com.example.sanaesenciaapp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import com.example.sanaesenciaapp.data.room.ESPECIALIDADES_INICIALES
import com.example.sanaesenciaapp.data.room.ESPECIALISTAS_INICIALES
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.data.room.EspecialistaEntity
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import com.example.sanaesenciaapp.data.repository.SesionRepository
import com.example.sanaesenciaapp.data.repository.UsuarioRepository
import com.example.sanaesenciaapp.utils.Seguridad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    val db: AppDatabase,
    private val sesionRepository: SesionRepository,
    private val usuarioRepository: UsuarioRepository
) {

    val usuarios = mutableStateListOf<Usuario>()

    var usuarioActual by mutableStateOf(sesionRepository.sesionActual.value)
        private set

    // Catálogo observable para Compose.
    val especialidades = mutableStateListOf<EspecialidadEntity>()
    var especialidadSeleccionada by mutableStateOf<EspecialidadEntity?>(null)

    val especialistas = mutableStateListOf<EspecialistaEntity>()
    var especialistaSeleccionado by mutableStateOf<EspecialistaEntity?>(null)

    val beneficiarios = mutableStateListOf<BeneficiarioEntity>()
    var beneficiarioSeleccionado by mutableStateOf<BeneficiarioEntity?>(null)

    private val scope = CoroutineScope(Dispatchers.IO)
    private var beneficiariosJob: Job? = null

    fun cargarDatos() {
        scope.launch {
            // Usuario administrador ficticio para la demostración del prototipo.
            val usuariosAntes = usuarioRepository.obtenerTodos()
            val adminExistente = usuariosAntes.find {
                it.email.equals("admin@sanaesencia.cl", ignoreCase = true)
            }

            val adminEntity = UsuarioEntity(
                nombre = adminExistente?.nombre ?: "Administrador SanaEsencia",
                email = "admin@sanaesencia.cl",
                password = Seguridad.hashear("admin123"),
                rut = adminExistente?.rut?.ifBlank { "11.111.111-1" } ?: "11.111.111-1",
                telefono = adminExistente?.telefono?.ifBlank { "+56912345678" } ?: "+56912345678",
                fechaNacimiento = adminExistente?.fechaNacimiento?.ifBlank { "01/01/1985" } ?: "01/01/1985",
                comuna = adminExistente?.comuna?.ifBlank { "Santiago" } ?: "Santiago",
                rol = "ADMIN"
            )
            usuarioRepository.insertar(adminEntity)

            // Los seeds SOLO completan registros faltantes. Así los cambios del panel Admin
            // (modalidad y agendaPausada) se conservan al cerrar y volver a abrir la app.
            db.especialidadDao().insertarTodas(ESPECIALIDADES_INICIALES)
            db.especialistaDao().insertarTodos(ESPECIALISTAS_INICIALES)

            val usuariosDb = usuarioRepository.obtenerTodos()
            val usuariosMapeados = usuariosDb.map {
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

            withContext(Dispatchers.Main) {
                usuarios.clear()
                usuarios.addAll(usuariosMapeados)
            }

            // Room notifica en vivo los cambios hechos desde el panel administrador.
            db.especialidadDao().obtenerTodasFlow().collect { listaActualizada ->
                withContext(Dispatchers.Main) {
                    especialidades.clear()
                    especialidades.addAll(listaActualizada.sortedBy { it.id })

                    // Mantener sincronizada la especialidad actualmente seleccionada.
                    especialidadSeleccionada?.let { seleccionada ->
                        especialidadSeleccionada = listaActualizada.find { it.id == seleccionada.id }
                    }
                }
            }
        }
    }

    fun cargarEspecialistasPorEspecialidad(especialidadId: Int) {
        scope.launch {
            val lista = db.especialistaDao().obtenerPorEspecialidad(especialidadId)
            withContext(Dispatchers.Main) {
                especialistas.clear()
                especialistas.addAll(lista)
            }
        }
    }

    private fun observarBeneficiarios(email: String) {
        beneficiariosJob?.cancel()
        beneficiariosJob = scope.launch {
            db.beneficiarioDao().obtenerPorPropietarioFlow(email).collect { lista ->
                withContext(Dispatchers.Main) {
                    beneficiarios.clear()
                    beneficiarios.addAll(lista)

                    beneficiarioSeleccionado?.let { seleccionado ->
                        beneficiarioSeleccionado = lista.find { it.id == seleccionado.id }
                    }
                }
            }
        }
    }

    fun seleccionarBeneficiario(beneficiario: BeneficiarioEntity?) {
        beneficiarioSeleccionado = beneficiario
    }

    fun registrarBeneficiario(
        nombre: String,
        rut: String,
        fechaNacimiento: String,
        parentesco: String,
        onResultado: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val email = usuarioActual?.email
        if (email.isNullOrBlank()) {
            onResultado(false, "Debes iniciar sesión para registrar un beneficiario")
            return
        }

        val nuevo = BeneficiarioEntity(
            propietarioEmail = email,
            nombre = nombre.trim(),
            rut = rut.trim().replace(".", "").uppercase(),
            fechaNacimiento = fechaNacimiento.trim(),
            parentesco = parentesco.trim()
        )

        scope.launch {
            val id = db.beneficiarioDao().insertar(nuevo)
            withContext(Dispatchers.Main) {
                if (id > 0) {
                    beneficiarioSeleccionado = nuevo.copy(id = id.toInt())
                    onResultado(true, null)
                } else {
                    onResultado(false, "Este beneficiario ya está registrado en tu cuenta")
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
        val emailLimpio = email.trim()

        if (usuarios.any { it.email.equals(emailLimpio, ignoreCase = true) }) {
            return false
        }

        // El registro público nunca debe crear administradores.
        val rolSeguro = if (rol == "TERAPEUTA") "TERAPEUTA" else "PACIENTE"
        val passwordHasheada = Seguridad.hashear(password)

        val nuevoUsuario = Usuario(
            nombre = nombre.trim(),
            email = emailLimpio,
            password = passwordHasheada,
            rut = rut.trim(),
            telefono = telefono.trim(),
            fechaNacimiento = fechaNacimiento.trim(),
            comuna = comuna.trim(),
            rol = rolSeguro
        )

        usuarios.add(nuevoUsuario)

        scope.launch {
            usuarioRepository.insertar(
                UsuarioEntity(
                    nombre = nuevoUsuario.nombre,
                    email = nuevoUsuario.email,
                    password = nuevoUsuario.password,
                    rut = nuevoUsuario.rut,
                    telefono = nuevoUsuario.telefono,
                    fechaNacimiento = nuevoUsuario.fechaNacimiento,
                    comuna = nuevoUsuario.comuna,
                    rol = nuevoUsuario.rol
                )
            )
        }

        return true
    }

    fun existeCorreo(email: String): Boolean {
        return usuarios.any {
            it.email.equals(email.trim(), ignoreCase = true)
        }
    }

    fun login(
        email: String,
        password: String
    ): Boolean {
        val sesion = sesionRepository.autenticar(email, password, usuarios) ?: return false

        usuarioActual = sesion
        beneficiarioSeleccionado = null
        observarBeneficiarios(sesion.email)
        return true
    }

    fun logout() {
        beneficiariosJob?.cancel()
        beneficiariosJob = null
        beneficiarios.clear()
        beneficiarioSeleccionado = null
        sesionRepository.cerrarSesion()
        usuarioActual = null
        especialidadSeleccionada = null
        especialistaSeleccionado = null
        especialistas.clear()
    }
}
