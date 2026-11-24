package com.example.sanaesenciaapp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sanaesenciaapp.data.remote.CrearEspecialidadRequest
import com.example.sanaesenciaapp.data.remote.CrearEspecialistaRequest
import com.example.sanaesenciaapp.data.remote.RetrofitClient
import com.example.sanaesenciaapp.data.repository.SesionRepository
import com.example.sanaesenciaapp.data.repository.SesionUsuario
import com.example.sanaesenciaapp.data.repository.TiendaRepository
import com.example.sanaesenciaapp.data.repository.UsuarioRepository
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import com.example.sanaesenciaapp.data.room.ESPECIALIDADES_INICIALES
import com.example.sanaesenciaapp.data.room.ESPECIALISTAS_INICIALES
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.data.room.EspecialistaEntity
import com.example.sanaesenciaapp.data.room.UsuarioEntity
import com.example.sanaesenciaapp.utils.Seguridad
import kotlinx.coroutines.CancellationException
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
    val sesionRepository: SesionRepository,
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

    init {
        scope.launch {
            sesionRepository.sesionActual.collect { sesion ->
                withContext(Dispatchers.Main) {
                    sincronizarSesion(sesion)
                }
            }
        }
    }

    fun sincronizarSesion(sesion: SesionUsuario?) {
        usuarioActual = sesion
        beneficiarioSeleccionado = null
        if (sesion != null) {
            observarBeneficiarios(sesion.email)
        } else {
            beneficiariosJob?.cancel()
            beneficiariosJob = null
            beneficiarios.clear()
        }
    }

    fun cargarDatos() {
        scope.launch {
            // 1. Usuario administrador de demostración
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

            // 2. Sincronizar Especialidades desde catalogo-service (:8082)
            var especialidadesRemotas = try {
                val resp = RetrofitClient.catalogoApi.listarEspecialidades()
                if (resp.isSuccessful) resp.body().orEmpty() else emptyList()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emptyList()
            }

            if (especialidadesRemotas.isEmpty()) {
                val creadas = ESPECIALIDADES_INICIALES.mapNotNull { esp ->
                    runCatching {
                        RetrofitClient.catalogoApi.crearEspecialidad(
                            CrearEspecialidadRequest(
                                nombre = esp.nombre,
                                descripcion = esp.descripcion,
                                icono = esp.icono,
                                profesional = esp.profesional,
                                modalidad = esp.modalidad,
                                agendaPausada = esp.agendaPausada
                            )
                        ).body()
                    }.getOrNull()
                }
                if (creadas.isNotEmpty()) especialidadesRemotas = creadas
            }

            if (especialidadesRemotas.isNotEmpty()) {
                db.especialidadDao().insertarTodas(especialidadesRemotas)
            } else {
                db.especialidadDao().insertarTodas(ESPECIALIDADES_INICIALES)
            }

            // 3. Sincronizar Especialistas desde terapeutas-service (:8083)
            var especialistasRemotos = try {
                val resp = RetrofitClient.terapeutasApi.listarEspecialistas()
                if (resp.isSuccessful) resp.body().orEmpty() else emptyList()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emptyList()
            }

            if (especialistasRemotos.isEmpty()) {
                val creados = ESPECIALISTAS_INICIALES.mapNotNull { doc ->
                    runCatching {
                        RetrofitClient.terapeutasApi.crearEspecialista(
                            CrearEspecialistaRequest(
                                nombre = doc.nombre,
                                especialidadId = doc.especialidadId,
                                modalidad = doc.modalidad
                            )
                        ).body()
                    }.getOrNull()
                }
                if (creados.isNotEmpty()) especialistasRemotos = creados
            }

            if (especialistasRemotos.isNotEmpty()) {
                db.especialistaDao().insertarTodos(especialistasRemotos)
            } else {
                db.especialistaDao().insertarTodos(ESPECIALISTAS_INICIALES)
            }

            // 4. Sincronizar Productos y precios desde tienda-service (:8085)
            TiendaRepository(db).sincronizarProductosDesdeServidor()

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

            // Room notifica en vivo los cambios de especialidades.
            db.especialidadDao().obtenerTodasFlow().collect { listaActualizada ->
                withContext(Dispatchers.Main) {
                    especialidades.clear()
                    especialidades.addAll(listaActualizada.sortedBy { it.id })

                    especialidadSeleccionada?.let { seleccionada ->
                        especialidadSeleccionada = listaActualizada.find { it.id == seleccionada.id }
                    }
                }
            }
        }
    }

    fun cargarEspecialistasPorEspecialidad(especialidadId: Int) {
        scope.launch {
            try {
                val resp = RetrofitClient.terapeutasApi.listarEspecialistas(especialidadId)
                if (resp.isSuccessful) {
                    resp.body()?.takeIf { it.isNotEmpty() }?.let { remotos ->
                        db.especialistaDao().insertarTodos(remotos)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }

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
            try {
                val resp = RetrofitClient.usuariosApi.listarBeneficiarios(email.trim().lowercase())
                if (resp.isSuccessful) {
                    resp.body()?.forEach { remoto ->
                        db.beneficiarioDao().registrarOReactivar(remoto)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }

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

        val fechaIso = normalizarFechaIso(fechaNacimiento)
        val nuevo = BeneficiarioEntity(
            propietarioEmail = email.trim().lowercase(),
            nombre = nombre.trim(),
            rut = rut.trim().replace(".", "").uppercase(),
            fechaNacimiento = fechaIso,
            parentesco = parentesco.trim()
        )

        scope.launch {
            try {
                val resp = RetrofitClient.usuariosApi.crearBeneficiario(nuevo)
                if (!resp.isSuccessful) {
                    val errorMsg = resp.errorBody()?.string()?.takeIf { it.isNotBlank() }
                        ?: "No se pudo registrar el beneficiario"
                    withContext(Dispatchers.Main) {
                        onResultado(false, errorMsg)
                    }
                    return@launch
                }

                val guardadoRemoto = resp.body() ?: nuevo
                val idLocal = db.beneficiarioDao().insertar(guardadoRemoto)

                withContext(Dispatchers.Main) {
                    beneficiarioSeleccionado = if (guardadoRemoto.id > 0) {
                        guardadoRemoto
                    } else if (idLocal > 0) {
                        guardadoRemoto.copy(id = idLocal.toInt())
                    } else {
                        guardadoRemoto
                    }
                    onResultado(true, null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    onResultado(false, "Error de conexión con el servidor")
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
            usuarioRepository.registrar(
                usuario = UsuarioEntity(
                    nombre = nuevoUsuario.nombre,
                    email = nuevoUsuario.email,
                    password = nuevoUsuario.password,
                    rut = nuevoUsuario.rut,
                    telefono = nuevoUsuario.telefono,
                    fechaNacimiento = nuevoUsuario.fechaNacimiento,
                    comuna = nuevoUsuario.comuna,
                    rol = nuevoUsuario.rol
                ),
                passwordPlano = password
            )
        }

        return true
    }

    fun existeCorreo(email: String): Boolean {
        return usuarios.any {
            it.email.equals(email.trim(), ignoreCase = true)
        }
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