package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModelStore
import com.example.sanaesenciaapp.data.remote.RetrofitClient
import com.example.sanaesenciaapp.data.remote.TokenManager
import com.example.sanaesenciaapp.data.repository.CitaRepository
import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.CitaEntity
import com.example.sanaesenciaapp.data.room.EspecialidadDao
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private class FakeEspecialidadDao(
        inicial: List<EspecialidadEntity> = emptyList()
    ) : EspecialidadDao {
        val flujo = MutableStateFlow(inicial)

        override suspend fun obtenerTodas(): List<EspecialidadEntity> = flujo.value
        override fun obtenerTodasFlow(): Flow<List<EspecialidadEntity>> = flujo
        override suspend fun insertarTodas(lista: List<EspecialidadEntity>) {
            val actuales = flujo.value.toMutableList()
            lista.forEach { nueva ->
                if (actuales.none { it.id == nueva.id }) actuales.add(nueva)
            }
            flujo.value = actuales
        }
        override suspend fun actualizarTodas(lista: List<EspecialidadEntity>) {
            flujo.value = lista
        }
        override suspend fun actualizarModalidad(id: Int, modalidad: String) {
            flujo.value = flujo.value.map {
                if (it.id == id) it.copy(modalidad = modalidad) else it
            }
        }
        override suspend fun actualizarAgendaPausada(id: Int, agendaPausada: Boolean) {
            flujo.value = flujo.value.map {
                if (it.id == id) it.copy(agendaPausada = agendaPausada) else it
            }
        }
    }

    private class FakeCitaDao : CitaDao() {
        val citas = MutableStateFlow<List<CitaEntity>>(emptyList())
        private var nextId = 1

        override fun obtenerTodasFlow(): Flow<List<CitaEntity>> = citas
        override fun obtenerPorUsuarioFlow(email: String): Flow<List<CitaEntity>> =
            MutableStateFlow(citas.value.filter { it.usuarioEmail == email })
        override suspend fun obtenerTodas(): List<CitaEntity> = citas.value
        override suspend fun contarSolapesEspecialista(especialistaId: Int, inicioMillis: Long, finMillis: Long): Int =
            citas.value.count {
                it.especialistaId == especialistaId &&
                    (it.estado == "CONFIRMADA" || it.estado == "EN_PAGO") &&
                    it.inicioMillis < finMillis && it.finMillis > inicioMillis
            }
        override suspend fun contarPresencialesSolapadas(inicioMillis: Long, finMillis: Long): Int =
            citas.value.count {
                it.modalidad == "PRESENCIAL" &&
                    (it.estado == "CONFIRMADA" || it.estado == "EN_PAGO") &&
                    it.inicioMillis < finMillis && it.finMillis > inicioMillis
            }
        override suspend fun obtenerBoxesOcupados(inicioMillis: Long, finMillis: Long): List<Int> =
            citas.value.filter {
                it.modalidad == "PRESENCIAL" &&
                    (it.estado == "CONFIRMADA" || it.estado == "EN_PAGO") &&
                    it.boxAsignado != null &&
                    it.inicioMillis < finMillis && it.finMillis > inicioMillis
            }.mapNotNull { it.boxAsignado }
        override suspend fun insertarInterno(cita: CitaEntity): Long {
            val id = if (cita.id > 0) cita.id else nextId++
            citas.value = citas.value + cita.copy(id = id)
            return id.toLong()
        }
        override suspend fun actualizarEstado(id: Int, nuevoEstado: String) {
            citas.value = citas.value.map { if (it.id == id) it.copy(estado = nuevoEstado) else it }
        }
        override suspend fun eliminar(id: Int) {
            citas.value = citas.value.filterNot { it.id == id && it.estado == "EN_PAGO" }
        }
        override suspend fun eliminarRetenciones() {
            citas.value = citas.value.filterNot { it.estado == "EN_PAGO" }
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        RetrofitClient.resetParaPruebas()
    }

    @After
    fun tearDown() {
        RetrofitClient.resetParaPruebas()
        Dispatchers.resetMain()
    }

    private fun especialidad(id: Int = 1) = EspecialidadEntity(
        id = id,
        nombre = "Psicología",
        descripcion = "Desc",
        icono = "🌱",
        profesional = "5",
        modalidad = "Ambas",
        agendaPausada = false
    )

    private suspend fun esperarHasta(condicion: () -> Boolean) {
        withTimeout(3_000L) {
            while (!condicion()) {
                delay(15L)
            }
        }
    }

    @Test
    fun `boxesUiState refleja ocupacion de 8 boxes conflictos y citas online excluidas`() = runBlocking {
        val espDao = FakeEspecialidadDao(listOf(especialidad()))
        val citaDao = FakeCitaDao()
        val store = ViewModelStore()
        val vm = AdminViewModel(espDao, citaDao, CitaRepository(citaDao)).also { store.put("admin", it) }
        val collectJob = launch { vm.boxesUiState.collect() }
        try {
            val fecha = vm.fechasBoxes.first()
            val hora = "10:30"
            vm.seleccionarFechaBoxes(fecha)
            vm.seleccionarHoraBoxes(hora)
            vm.seleccionarHoraBoxes("23:59")
            assertEquals("10:30", vm.horaBoxes.value)

            val inicio = Calendar.getInstance().apply {
                timeInMillis = fecha
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val fin = inicio + 60 * 60_000L

            val lista = (1..8).map { box ->
                CitaEntity(
                    id = box,
                    usuarioEmail = "u$box@cl.cl",
                    especialidadId = 1,
                    especialidadNombre = "Psicología",
                    especialistaId = box,
                    especialistaNombre = "Esp $box",
                    modalidad = "PRESENCIAL",
                    inicioMillis = inicio,
                    finMillis = fin,
                    hora = hora,
                    pacienteNombre = "Pac $box",
                    estado = "CONFIRMADA",
                    boxAsignado = box
                )
            } + listOf(
                CitaEntity(
                    id = 9,
                    usuarioEmail = "extra@cl.cl",
                    especialidadId = 1,
                    especialidadNombre = "Psicología",
                    especialistaId = 9,
                    especialistaNombre = "Esp 9",
                    modalidad = "PRESENCIAL",
                    inicioMillis = inicio,
                    finMillis = fin,
                    hora = hora,
                    pacienteNombre = "Conflicto",
                    estado = "CONFIRMADA",
                    boxAsignado = 1
                ),
                CitaEntity(
                    id = 10,
                    usuarioEmail = "online@cl.cl",
                    especialidadId = 1,
                    especialidadNombre = "Psicología",
                    especialistaId = 10,
                    especialistaNombre = "Esp 10",
                    modalidad = "ONLINE",
                    inicioMillis = inicio,
                    finMillis = fin,
                    hora = hora,
                    pacienteNombre = "Online",
                    estado = "CONFIRMADA",
                    boxAsignado = null
                )
            )
            citaDao.citas.value = lista
            esperarHasta { vm.boxesUiState.value.ocupados == 8 }

            val state = vm.boxesUiState.value
            assertEquals(8, state.ocupados)
            assertEquals(0, state.disponibles)
            assertTrue(state.capacidadCompleta)
            assertTrue(state.boxes.first { it.numero == 1 }.tieneConflicto)
            assertFalse(state.boxes.first { it.numero == 2 }.tieneConflicto)
        } finally {
            collectJob.cancel()
            store.clear()
        }
    }

    @Test
    fun `actualizarModalidad y actualizarEstadoAgenda funcionan en modo local`() = runBlocking {
        val espDao = FakeEspecialidadDao(listOf(especialidad(1)))
        val citaDao = FakeCitaDao()
        val store = ViewModelStore()
        val vm = AdminViewModel(espDao, citaDao).also { store.put("admin", it) }
        val collectJob = launch { vm.especialidades.collect() }
        try {
            vm.actualizarModalidad(1, "Online")
            vm.actualizarEstadoAgenda(1, false)
            esperarHasta { espDao.flujo.value.single().agendaPausada }

            val actualizada = espDao.flujo.value.single()
            assertEquals("Online", actualizada.modalidad)
            assertTrue(actualizada.agendaPausada)
        } finally {
            collectJob.cancel()
            store.clear()
        }
    }

    @Test
    fun `actualizarModalidad y actualizarEstadoAgenda validan respuesta HTTP y reportan errores con Retrofit`() = runBlocking {
        var responderExito = true
        var lanzarExcepcionRed = false
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                if (lanzarExcepcionRed) throw IOException("fallo de red")
                val path = chain.request().url.encodedPath
                val body = when {
                    path.contains("citas") -> "[]"
                    responderExito -> """{"id":1,"nombre":"Psicología","descripcion":"Desc","icono":"🌱","profesional":"5","modalidad":"Presencial","agendaPausada":true}"""
                    else -> """{"error":"denegado"}"""
                }
                val code = if (path.contains("citas") || responderExito) 200 else 403
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message("Resp")
                    .body(body.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
        RetrofitClient.configurarParaPruebas("http://localhost/", client, TokenManager(null))

        val espDao = FakeEspecialidadDao(listOf(especialidad(1)))
        val citaDao = FakeCitaDao()
        val store = ViewModelStore()
        val vm = AdminViewModel(espDao, citaDao).also { store.put("admin", it) }
        val collectJob = launch { vm.especialidades.collect() }
        try {
            esperarHasta { vm.especialidades.value.isNotEmpty() }

            vm.actualizarModalidad(1, "Presencial")
            esperarHasta { espDao.flujo.value.single().modalidad == "Presencial" }

            vm.actualizarEstadoAgenda(1, false)
            esperarHasta { espDao.flujo.value.single().agendaPausada }
            assertNull(vm.mensajeError.value)

            responderExito = false
            vm.actualizarModalidad(1, "Online")
            esperarHasta { vm.mensajeError.value != null }
            assertEquals("Presencial", espDao.flujo.value.single().modalidad)

            vm.limpiarError()
            assertNull(vm.mensajeError.value)

            vm.actualizarEstadoAgenda(1, true)
            esperarHasta { vm.mensajeError.value != null }

            vm.limpiarError()
            lanzarExcepcionRed = true
            vm.actualizarModalidad(1, "Ambas")
            esperarHasta { vm.mensajeError.value != null }
            assertTrue(vm.mensajeError.value!!.contains("conexión", ignoreCase = true))

            vm.limpiarError()
            vm.actualizarEstadoAgenda(1, true)
            esperarHasta { vm.mensajeError.value != null }
            assertTrue(vm.mensajeError.value!!.contains("conexión", ignoreCase = true))

            vm.limpiarError()
            vm.actualizarModalidad(999, "Online")
            esperarHasta { vm.mensajeError.value != null }

            vm.refrescarBoxes()
        } finally {
            collectJob.cancel()
            store.clear()
        }
    }
}
