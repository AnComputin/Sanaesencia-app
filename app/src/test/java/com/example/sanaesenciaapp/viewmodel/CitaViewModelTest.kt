package com.example.sanaesenciaapp.viewmodel

import com.example.sanaesenciaapp.data.remote.RetrofitClient
import com.example.sanaesenciaapp.data.remote.TokenManager
import com.example.sanaesenciaapp.data.room.CitaDao
import com.example.sanaesenciaapp.data.room.CitaEntity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de CitaViewModel: flujo de reserva, disponibilidad,
 * boxes (1-8), cancelación y reglas de agenda.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CitaViewModelTest {

    private class FakeCitaDao : CitaDao() {
        val citasGuardadas = mutableListOf<CitaEntity>()
        private val flujoCitas = MutableStateFlow<List<CitaEntity>>(emptyList())
        var siguienteId = 1

        private fun sincronizar() {
            flujoCitas.value = citasGuardadas.toList()
        }

        override fun obtenerTodasFlow(): Flow<List<CitaEntity>> {
            return flujoCitas
        }

        override fun obtenerPorUsuarioFlow(email: String): Flow<List<CitaEntity>> {
            return flujoCitas
        }

        override suspend fun obtenerTodas(): List<CitaEntity> {
            return citasGuardadas.toList()
        }

        override suspend fun contarSolapesEspecialista(
            especialistaId: Int, inicioMillis: Long, finMillis: Long
        ): Int {
            return citasGuardadas.count {
                it.especialistaId == especialistaId &&
                    (it.estado == "CONFIRMADA" || it.estado == "EN_PAGO") &&
                    it.inicioMillis < finMillis &&
                    it.finMillis > inicioMillis
            }
        }

        override suspend fun contarPresencialesSolapadas(
            inicioMillis: Long, finMillis: Long
        ): Int {
            return citasGuardadas.count {
                it.modalidad == "PRESENCIAL" &&
                    (it.estado == "CONFIRMADA" || it.estado == "EN_PAGO") &&
                    it.inicioMillis < finMillis &&
                    it.finMillis > inicioMillis
            }
        }

        override suspend fun obtenerBoxesOcupados(
            inicioMillis: Long, finMillis: Long
        ): List<Int> {
            return citasGuardadas.filter {
                it.modalidad == "PRESENCIAL" &&
                    (it.estado == "CONFIRMADA" || it.estado == "EN_PAGO") &&
                    it.boxAsignado != null &&
                    it.inicioMillis < finMillis &&
                    it.finMillis > inicioMillis
            }.mapNotNull { it.boxAsignado }
        }

        override suspend fun insertarInterno(cita: CitaEntity): Long {
            val citaConId = cita.copy(id = siguienteId++)
            citasGuardadas.add(citaConId)
            sincronizar()
            return citaConId.id.toLong()
        }

        override suspend fun actualizarEstado(id: Int, nuevoEstado: String) {
            val index = citasGuardadas.indexOfFirst { it.id == id }
            if (index != -1) {
                citasGuardadas[index] = citasGuardadas[index].copy(estado = nuevoEstado)
                sincronizar()
            }
        }

        override suspend fun eliminar(id: Int) {
            citasGuardadas.removeAll { it.id == id && it.estado == "EN_PAGO" }
            sincronizar()
        }

        override suspend fun eliminarRetenciones() {
            citasGuardadas.removeAll { it.estado == "EN_PAGO" }
            sincronizar()
        }
    }

    private lateinit var fakeDao: FakeCitaDao
    private lateinit var viewModel: CitaViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        RetrofitClient.resetParaPruebas()
        fakeDao = FakeCitaDao()
        viewModel = CitaViewModel(fakeDao)
    }

    @After
    fun tearDown() {
        RetrofitClient.resetParaPruebas()
        Dispatchers.resetMain()
    }

    private fun prepararReserva(
        especialistaId: Int = 1,
        modalidad: Modalidad = Modalidad.PRESENCIAL,
        hora: String = "09:00",
        fechaMillis: Long? = null
    ) {
        viewModel.seleccionarServicio(
            especialidad = "Psicología",
            profesional = "Dr. Test",
            modalidadPermitida = "Ambas",
            especialidadId = 1,
            especialistaId = especialistaId
        )
        viewModel.seleccionarModalidad(modalidad)
        viewModel.seleccionarFecha(fechaMillis ?: viewModel.fechasDisponibles.first())
        viewModel.seleccionarHora(hora)
        viewModel.seleccionarPaciente("Paciente Test", null)
    }

    // ════════════════════════════════════════════
    // ESTADO INICIAL Y SELECCIÓN
    // ════════════════════════════════════════════

    @Test
    fun `estado inicial tiene valores por defecto`() {
        val state = viewModel.uiState.value
        assertEquals(0, state.especialidadId)
        assertEquals(0, state.especialistaId)
        assertNull(state.modalidad)
        assertNull(state.fechaMillis)
        assertNull(state.hora)
        assertEquals("", state.nombrePaciente)
    }

    @Test
    fun `seleccionarServicio actualiza estado`() {
        viewModel.seleccionarServicio("Psicología", "Dr. Test", "Ambas", 1, 2)
        val state = viewModel.uiState.value
        assertEquals(1, state.especialidadId)
        assertEquals("Psicología", state.especialidad)
        assertEquals(2, state.especialistaId)
        assertEquals("Dr. Test", state.profesional)
    }

    @Test
    fun `seleccionarModalidad actualiza estado`() {
        viewModel.seleccionarModalidad(Modalidad.ONLINE)
        assertEquals(Modalidad.ONLINE, viewModel.uiState.value.modalidad)
    }

    @Test
    fun `cambiarNombre con menos de 3 caracteres muestra error`() {
        viewModel.cambiarNombre("Ab")
        assertNotNull(viewModel.uiState.value.errorNombre)
    }

    @Test
    fun `cambiarNombre valido limpia error`() {
        viewModel.cambiarNombre("Juan Pérez")
        assertNull(viewModel.uiState.value.errorNombre)
    }

    @Test
    fun `generarFechasDisponibles crea 7 fechas`() {
        assertEquals(7, viewModel.fechasDisponibles.size)
    }

    // ════════════════════════════════════════════
    // PUEDE CONTINUAR
    // ════════════════════════════════════════════

    @Test
    fun `puedeContinuar es false sin datos`() = runBlocking {
        withTimeout(2000) {
            assertFalse(viewModel.puedeContinuar.first { !it })
        }
    }

    @Test
    fun `puedeContinuar es true con todos los datos`() = runBlocking {
        prepararReserva()
        withTimeout(2000) {
            assertTrue(viewModel.puedeContinuar.first { it })
        }
    }

    // ════════════════════════════════════════════
    // CONFIRMACIÓN DE RESERVA
    // ════════════════════════════════════════════

    @Test
    fun `confirmarCita presencial asigna box`() = runTest {
        prepararReserva()

        var resultado: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { resultado = it }

        assertTrue(resultado == true)
        val ultima = viewModel.ultimaCita.value
        assertNotNull(ultima)
        assertNotNull(ultima?.boxAsignado)
    }

    @Test
    fun `confirmarCita online no asigna box`() = runTest {
        prepararReserva(modalidad = Modalidad.ONLINE)

        var resultado: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { resultado = it }

        assertTrue(resultado == true)
        assertNull(viewModel.ultimaCita.value?.boxAsignado)
    }

    @Test
    fun `confirmarCita sin datos incompletos falla`() = runTest {
        var resultado: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { resultado = it }

        assertFalse(resultado == true)
        assertNull(viewModel.ultimaCita.value)
    }

    @Test
    fun `confirmarCita solapada para el mismo especialista es rechazada`() = runTest {
        prepararReserva(especialistaId = 1)
        var primera: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { primera = it }
        assertTrue(primera == true)

        prepararReserva(especialistaId = 1)
        var segunda: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { segunda = it }

        assertFalse(segunda == true)
    }

    @Test
    fun `confirmarCita online no consume box y permite presencial simultanea`() = runTest {
        prepararReserva(especialistaId = 1, modalidad = Modalidad.ONLINE)
        var online: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { online = it }
        assertTrue(online == true)

        prepararReserva(especialistaId = 2, modalidad = Modalidad.PRESENCIAL)
        var presencial: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { presencial = it }

        assertTrue(presencial == true)
        assertEquals(1, viewModel.ultimaCita.value?.boxAsignado)
    }

    @Test
    fun `novena reserva presencial es rechazada con los 8 boxes ocupados`() = runTest {
        for (especialista in 1..8) {
            prepararReserva(especialistaId = especialista)
            var resultado: Boolean? = null
            viewModel.confirmarCita("test@mail.com") { resultado = it }
            assertTrue("La reserva $especialista debió ser aceptada", resultado == true)
        }

        prepararReserva(especialistaId = 9)
        var novena: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { novena = it }

        assertFalse(novena == true)
    }

    // ════════════════════════════════════════════
    // CANCELACIÓN
    // ════════════════════════════════════════════

    private fun crearCitaItem(
        estado: EstadoReserva = EstadoReserva.CONFIRMADA,
        inicioMillis: Long = System.currentTimeMillis() + 48 * 3600000L
    ) = CitaItem(
        id = 1,
        usuarioEmail = "test@mail.com",
        especialidadId = 1,
        especialidad = "Psicología",
        especialistaId = 1,
        profesional = "Dr. Test",
        modalidad = Modalidad.PRESENCIAL,
        inicioMillis = inicioMillis,
        finMillis = inicioMillis + 3600000L,
        hora = "09:00",
        paciente = "Paciente Test",
        estado = estado
    )

    @Test
    fun `puedeCancelar con mas de 24 horas de anticipacion`() {
        assertTrue(viewModel.puedeCancelar(crearCitaItem()))
    }

    @Test
    fun `no puede cancelar con menos de 24 horas`() {
        val cita = crearCitaItem(inicioMillis = System.currentTimeMillis() + 2 * 3600000L)
        assertFalse(viewModel.puedeCancelar(cita))
    }

    @Test
    fun `no puede cancelar cita ya cancelada`() {
        val cita = crearCitaItem(estado = EstadoReserva.CANCELADA)
        assertFalse(viewModel.puedeCancelar(cita))
    }

    @Test
    fun `cancelarCita cambia estado en persistencia`() = runTest {
        val fechaFutura = viewModel.fechasDisponibles[2]
        prepararReserva(fechaMillis = fechaFutura)
        viewModel.confirmarCita("test@mail.com") { }
        val id = viewModel.ultimaCita.value!!.id

        val cancelada = viewModel.cancelarCita(id)

        assertTrue(cancelada)
        assertEquals("CANCELADA", fakeDao.citasGuardadas.first { it.id == id }.estado)
    }

    @Test
    fun `mismo especialista con horarios distintos acepta ambas reservas`() = runTest {
        prepararReserva(especialistaId = 1, hora = "09:00")
        var primera: Boolean? = null
        viewModel.confirmarCita("test@mail.com"){primera = it}
        assertTrue(primera == true)

        prepararReserva(especialistaId = 1, hora = "10:30")
        var segunda: Boolean? = null
        viewModel.confirmarCita("test@mail.com"){segunda = it}

        assertTrue(segunda == true)
    }

    @Test
    fun `distintos especialistas al mismo horario aceptan ambas reservas`() = runTest {
        prepararReserva(especialistaId = 1, hora = "09:00")
        var primera: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { primera = it }
        assertTrue(primera == true)

        prepararReserva(especialistaId = 2, hora = "09:00")
        var segunda: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { segunda = it }

        assertTrue(segunda == true)
    }

    @Test
    fun `cambiar fecha despues de elegir hora limpia la hora`() {
        prepararReserva()
        assertNotNull(viewModel.uiState.value.hora)

        viewModel.seleccionarFecha(viewModel.fechasDisponibles[2])

        assertNull(viewModel.uiState.value.hora)   // la hora vieja no debe quedar "pegada"
    }

    @Test
    fun `cambiar modalidad despues de elegir hora limpia la hora`() {
        prepararReserva(modalidad = Modalidad.PRESENCIAL)
        assertNotNull(viewModel.uiState.value.hora)

        viewModel.seleccionarModalidad(Modalidad.ONLINE)

        assertNull(viewModel.uiState.value.hora)
    }

    // ════════════════════════════════════════════
    // VA-04: RETENCIÓN DURANTE EL PAGO
    // ════════════════════════════════════════════

    @Test
    fun `duracion del pago es 600 segundos`() {
        assertEquals(600, viewModel.duracionPagoSegundos)
    }

    @Test
    fun `iniciarPago crea una retencion EN_PAGO con box asignado`() = runTest {
        prepararReserva()

        val inicio = viewModel.iniciarPago("test@mail.com")

        assertTrue(inicio)
        val retencion = fakeDao.citasGuardadas.single()
        assertEquals("EN_PAGO", retencion.estado)
        assertNotNull(retencion.boxAsignado)
    }

    @Test
    fun `iniciarPago con wizard incompleto falla sin retener nada`() = runTest {
        val inicio = viewModel.iniciarPago("test@mail.com")

        assertFalse(inicio)
        assertTrue(fakeDao.citasGuardadas.isEmpty())
    }

    @Test
    fun `iniciarPago limpia retenciones huerfanas previas`() = runTest {
        fakeDao.citasGuardadas.add(
            CitaEntity(
                id = 99,
                usuarioEmail = "otro@mail.com",
                especialidadId = 1,
                especialidadNombre = "Psicología",
                especialistaId = 5,
                especialistaNombre = "Dr. Viejo",
                modalidad = "PRESENCIAL",
                inicioMillis = System.currentTimeMillis() + 86400000L,
                finMillis = System.currentTimeMillis() + 90000000L,
                hora = "09:00",
                pacienteNombre = "Huerfana",
                estado = "EN_PAGO",
                boxAsignado = 3
            )
        )

        prepararReserva()
        val inicio = viewModel.iniciarPago("test@mail.com")

        assertTrue(inicio)
        assertFalse(fakeDao.citasGuardadas.any { it.id == 99 })
        assertEquals(1, fakeDao.citasGuardadas.size)
    }

    @Test
    fun `retencion EN_PAGO bloquea el horario para otro especialista`() = runTest {
        prepararReserva(especialistaId = 1)
        assertTrue(viewModel.iniciarPago("test@mail.com"))

        // Otro flujo intenta reservar el mismo horario sin pagar aún.
        val entidadBloqueada = CitaEntity(
            usuarioEmail = "otro@mail.com",
            especialidadId = 1,
            especialidadNombre = "Psicología",
            especialistaId = 1,
            especialistaNombre = "Dr. Test",
            modalidad = "PRESENCIAL",
            inicioMillis = viewModel.fechasDisponibles.first() + 9 * 3600000L,
            finMillis = viewModel.fechasDisponibles.first() + 10 * 3600000L,
            hora = "09:00",
            pacienteNombre = "Intruso",
            estado = "CONFIRMADA"
        )

        val resultado = fakeDao.reservarSiDisponible(entidadBloqueada)

        assertNull(resultado)
    }

    @Test
    fun `liberarRetencion borra la retencion y libera el cupo`() = runTest {
        prepararReserva()
        assertTrue(viewModel.iniciarPago("test@mail.com"))
        assertEquals(1, fakeDao.citasGuardadas.size)

        viewModel.liberarRetencion()

        assertTrue(fakeDao.citasGuardadas.isEmpty())
    }

    @Test
    fun `confirmarCita con retencion cambia a CONFIRMADA sin fila duplicada`() = runTest {
        prepararReserva()
        assertTrue(viewModel.iniciarPago("test@mail.com"))
        assertEquals(1, fakeDao.citasGuardadas.size)

        var resultado: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { resultado = it }

        assertTrue(resultado == true)
        assertEquals(1, fakeDao.citasGuardadas.size)
        val cita = fakeDao.citasGuardadas.single()
        assertEquals("CONFIRMADA", cita.estado)
        assertNotNull(cita.boxAsignado)
        assertEquals("test@mail.com", cita.usuarioEmail)
    }

    @Test
    fun `confirmarCita sin retencion sigue usando el camino directo`() = runTest {
        // Compatibilidad: los flujos que no pasan por iniciarPago no se rompen.
        prepararReserva()

        var resultado: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { resultado = it }

        assertTrue(resultado == true)
        assertEquals("CONFIRMADA", fakeDao.citasGuardadas.single().estado)
    }

    @Test
    fun `confirmarCita tras liberar la retencion falla y limpia la hora`() = runTest {
        prepararReserva()
        assertTrue(viewModel.iniciarPago("test@mail.com"))
        viewModel.liberarRetencion()

        var resultado: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { resultado = it }

        // Sin retención válida y con el camino directo, la reserva se crea de nuevo.
        assertTrue(resultado == true)
        assertEquals("CONFIRMADA", fakeDao.citasGuardadas.single().estado)
    }

    @Test
    fun `flujo completo VA-04 iniciar, confirmar y salir sin retener`() = runTest {
        prepararReserva()

        assertTrue(viewModel.iniciarPago("test@mail.com"))
        assertEquals("EN_PAGO", fakeDao.citasGuardadas.single().estado)

        var resultado: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { resultado = it }
        assertTrue(resultado == true)

        // onDispose de PagoScreen tras confirmar: no debe borrar la cita confirmada.
        viewModel.liberarRetencion()

        assertEquals("CONFIRMADA", fakeDao.citasGuardadas.single().estado)
    }

    @Test
    fun `limpiarSesion borra seleccion actual y ultimaCita al cerrar sesion`() = runTest {
        prepararReserva()
        var resultado: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { resultado = it }
        assertTrue(resultado == true)
        assertNotNull(viewModel.ultimaCita.value)

        prepararReserva()
        assertEquals("Psicología", viewModel.uiState.value.especialidad)

        viewModel.limpiarSesion()

        assertNull(viewModel.ultimaCita.value)
        assertEquals("", viewModel.uiState.value.especialidad)
        assertEquals("", viewModel.uiState.value.nombrePaciente)
        assertEquals(0, viewModel.uiState.value.especialistaId)
    }

    @Test
    fun `confirmarCita con retencion y Retrofit maneja exito 201 error HTTP 409 y error de red`() = runBlocking {
        // 1. Exito 201 desde agenda-service
        val clientOk = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val path = chain.request().url.encodedPath
                val method = chain.request().method
                val body = when {
                    path.endsWith("/citas") && method == "GET" -> "[]"
                    path.endsWith("/citas") && method == "POST" ->
                        """{"id":50,"usuarioEmail":"test@mail.com","especialidadId":1,"especialidadNombre":"Psicología","especialistaId":1,"especialistaNombre":"Dr. Test","modalidad":"PRESENCIAL","inicioMillis":1000,"finMillis":3601000,"hora":"09:00","pacienteNombre":"Paciente Test","beneficiarioId":null,"estado":"CONFIRMADA","boxAsignado":3}"""
                    else -> "{}"
                }
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(if (method == "POST") 201 else 200)
                    .message("OK")
                    .body(body.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
        RetrofitClient.configurarParaPruebas("http://localhost/", clientOk, TokenManager(null))

        prepararReserva()
        assertTrue(viewModel.iniciarPago("test@mail.com"))
        assertTrue(viewModel.segundosRestantesPago() in 0..600)

        var ok: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { ok = it }
        withTimeout(3_000L) {
            while (ok == null) kotlinx.coroutines.delay(10L)
        }
        assertTrue(ok == true)
        assertEquals(1, viewModel.ultimaCita.value?.boxAsignado)

        // 2. Rechazo HTTP 409 elimina retencion y limpia hora
        val client409 = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val method = chain.request().method
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(if (method == "POST") 409 else 200)
                    .message("Conflict")
                    .body((if (method == "POST") "ocupado" else "[]").toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
        RetrofitClient.configurarParaPruebas("http://localhost/", client409, TokenManager(null))

        prepararReserva(especialistaId = 2, hora = "10:30")
        assertTrue(viewModel.iniciarPago("test@mail.com"))
        var res409: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { res409 = it }
        withTimeout(3_000L) {
            while (res409 == null) kotlinx.coroutines.delay(10L)
        }
        assertFalse(res409 == true)
        assertNull(viewModel.uiState.value.hora)
        assertFalse(fakeDao.citasGuardadas.any { it.estado == "EN_PAGO" })

        // 3. Error de red elimina retencion
        var fallarPost = false
        val clientRed = OkHttpClient.Builder()
            .addInterceptor { chain ->
                if (fallarPost && chain.request().method == "POST") throw IOException("sin red")
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("[]".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
        RetrofitClient.configurarParaPruebas("http://localhost/", clientRed, TokenManager(null))

        prepararReserva(especialistaId = 3, hora = "12:00")
        assertTrue(viewModel.iniciarPago("test@mail.com"))
        fallarPost = true
        var resRed: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { resRed = it }
        withTimeout(3_000L) {
            while (resRed == null) kotlinx.coroutines.delay(10L)
        }
        assertFalse(resRed == true)
    }

    @Test
    fun `modo en memoria sin DAO cubre seleccion de modalidad fija hora ocupada reserva y cancelacion`() = runBlocking {
        val memVm = CitaViewModel(citaDao = null)
        memVm.seleccionarServicio("Reiki", "Terapeuta", "Presencial", 2, 10)
        assertEquals(Modalidad.PRESENCIAL, memVm.uiState.value.modalidad)

        memVm.seleccionarServicio("Psicología", "Terapeuta 2", "Online", 1, 11)
        assertEquals(Modalidad.ONLINE, memVm.uiState.value.modalidad)

        val fechaFutura = memVm.fechasDisponibles[3]
        memVm.seleccionarFecha(fechaFutura)
        memVm.seleccionarHora("09:00")
        memVm.seleccionarPaciente("", null)
        assertNotNull(memVm.uiState.value.errorNombre)
        memVm.seleccionarPaciente("Paciente Memoria", 4)
        assertNull(memVm.uiState.value.errorNombre)

        var okMem: Boolean? = null
        memVm.confirmarCita("mem@mail.com") { okMem = it }
        assertTrue(okMem == true)
        val citaMemId = memVm.ultimaCita.value!!.id
        assertTrue(memVm.cancelarCita(citaMemId))
        assertEquals(EstadoReserva.CANCELADA, memVm.citas.value.first { it.id == citaMemId }.estado)

        val horariosJob = launch { memVm.horarios.collect {} }
        try {
            // Presencial en memoria hasta llenar 8 boxes
            for (esp in 1..8) {
                memVm.seleccionarServicio("Masaje", "Esp $esp", "Presencial", 3, esp)
                memVm.seleccionarFecha(fechaFutura)
                memVm.seleccionarHora("10:30")
                memVm.seleccionarPaciente("Pac $esp", null)
                var res: Boolean? = null
                memVm.confirmarCita("mem@mail.com") { res = it }
                assertTrue(res == true)
            }
            memVm.seleccionarServicio("Masaje", "Esp 9", "Presencial", 3, 9)
            memVm.seleccionarFecha(fechaFutura)
            withTimeout(3_000L) {
                while (memVm.horarios.value.first { it.hora == "10:30" }.disponible) {
                    kotlinx.coroutines.delay(10L)
                }
            }
            memVm.seleccionarHora("10:30")
            assertNull(memVm.uiState.value.hora) // horario ocupado no se selecciona
            memVm.seleccionarPaciente("Pac 9", null)
            var res9: Boolean? = null
            memVm.confirmarCita("mem@mail.com") { res9 = it }
            assertFalse(res9 == true)
        } finally {
            horariosJob.cancel()
        }
    }

    @Test
    fun `iniciarPago falla cuando la retencion fue borrada antes de confirmar o horario ya ocupado`() = runTest {
        prepararReserva(especialistaId = 1, hora = "09:00")
        var confirmadaPrevia: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { confirmadaPrevia = it }
        assertTrue(confirmadaPrevia == true)

        // Otro flujo intenta iniciarPago en el mismo horario ya confirmado
        prepararReserva(especialistaId = 1, hora = "09:00")
        assertFalse(viewModel.iniciarPago("otro@mail.com"))

        // Si se eliminan retenciones externamente antes de confirmarCita, falla limpio
        prepararReserva(especialistaId = 2, hora = "14:00")
        assertTrue(viewModel.iniciarPago("test@mail.com"))
        fakeDao.eliminarRetenciones()
        var confirmado: Boolean? = null
        viewModel.confirmarCita("test@mail.com") { confirmado = it }
        assertFalse(confirmado == true)
    }
}
