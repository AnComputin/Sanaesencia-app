package com.example.sanaesenciaapp.data.room

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de CitaDao con Room real (VA-04/VA-05, §8.8).
 *
 * Validan lo que el FakeCitaDao no puede probar: la atomicidad real de
 * @Transaction en reservarSiDisponible, la retención EN_PAGO en SQLite
 * y el borrado condicionado de retenciones.
 */
@RunWith(AndroidJUnit4::class)
class CitaDaoRoomTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CitaDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.citaDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun cita(
        especialistaId: Int,
        inicioMillis: Long,
        modalidad: String = "PRESENCIAL",
        estado: String = "CONFIRMADA",
        usuarioEmail: String = "test@mail.com"
    ) = CitaEntity(
        usuarioEmail = usuarioEmail,
        especialidadId = 1,
        especialidadNombre = "Psicología",
        especialistaId = especialistaId,
        especialistaNombre = "Dr. $especialistaId",
        modalidad = modalidad,
        inicioMillis = inicioMillis,
        finMillis = inicioMillis + 60 * 60 * 1000L,
        hora = "09:00",
        pacienteNombre = "Paciente Test",
        estado = estado
    )

    // Fijo para no depender del reloj del dispositivo.
    private val slot0 = 1_900_000_000_000L
    private val slot1 = slot0 + 60 * 60 * 1000L

    // ════════════════════════════════════════════
    // BOXES Y TRANSACCIÓN ATÓMICA
    // ════════════════════════════════════════════

    @Test
    fun ochoReservasPresencialesRecibenBoxesDistintosYLaNovenaEsRechazada() = runBlocking {
        val boxes = mutableListOf<Int>()

        for (especialista in 1..8) {
            val reservada = dao.reservarSiDisponible(cita(especialista, slot0))
            assertNotNull("Reserva $especialista debió aceptarse", reservada)
            boxes.add(reservada!!.boxAsignado!!)
        }

        assertEquals(8, boxes.distinct().size)
        assertEquals((1..8).toSet(), boxes.toSet())

        val novena = dao.reservarSiDisponible(cita(9, slot0))
        assertNull("La novena presencial superpuesta debe rechazarse", novena)
    }

    @Test
    fun reservasConcurrentesNoCompartenBox() = runBlocking {
        val resultados = withContext(Dispatchers.IO) {
            (1..8).map { especialista ->
                async { dao.reservarSiDisponible(cita(especialista, slot0)) }
            }.awaitAll()
        }

        val boxes = resultados.mapNotNull { it?.boxAsignado }
        assertEquals(8, boxes.size)
        assertEquals(8, boxes.distinct().size)
        assertEquals((1..8).toSet(), boxes.toSet())
    }

    @Test
    fun boxSeLiberaAlCancelarYPuedeReasignarse() = runBlocking {
        val primera = dao.reservarSiDisponible(cita(1, slot0))!!
        assertEquals(1, primera.boxAsignado)

        dao.actualizarEstado(primera.id, "CANCELADA")

        val segunda = dao.reservarSiDisponible(cita(2, slot0))
        assertNotNull(segunda)
        assertEquals("El box liberado debe reasignarse", 1, segunda!!.boxAsignado)
    }

    @Test
    fun onlineNoConsumeBox() = runBlocking {
        val online = dao.reservarSiDisponible(cita(1, slot0, modalidad = "ONLINE"))!!
        assertNull(online.boxAsignado)

        val presencial = dao.reservarSiDisponible(cita(2, slot0))!!
        assertEquals("El primer box debe ir a la presencial", 1, presencial.boxAsignado)
    }

    // ════════════════════════════════════════════
    // VA-04: RETENCIÓN EN_PAGO EN SQLITE
    // ════════════════════════════════════════════

    @Test
    fun retencionEnPagoBloqueaElHorarioDelEspecialista() = runBlocking {
        val retenida = dao.reservarSiDisponible(cita(1, slot0, estado = "EN_PAGO"))
        assertNotNull(retenida)
        assertNotNull("La retención conserva el box", retenida!!.boxAsignado)

        val intento = dao.reservarSiDisponible(cita(1, slot0))
        assertNull("El horario retenido no puede tomarse", intento)

        val otroEspecialista = dao.reservarSiDisponible(cita(2, slot0))
        assertNotNull("Otro especialista sí puede usar el mismo slot", otroEspecialista)
    }

    @Test
    fun retencionEnPagoPresencialCuentaParaLaNovena() = runBlocking {
        for (especialista in 1..7) {
            assertNotNull(dao.reservarSiDisponible(cita(especialista, slot0)))
        }
        val retenida = dao.reservarSiDisponible(cita(8, slot0, estado = "EN_PAGO"))
        assertNotNull(retenida)

        val novena = dao.reservarSiDisponible(cita(9, slot0))
        assertNull("Con 7 confirmadas + 1 retenida, la novena se rechaza", novena)
    }

    @Test
    fun confirmarRetencionConservaElBoxYElHorario() = runBlocking {
        val retenida = dao.reservarSiDisponible(cita(1, slot0, estado = "EN_PAGO"))!!
        assertEquals(1, retenida.boxAsignado)

        dao.actualizarEstado(retenida.id, "CONFIRMADA")

        val intento = dao.reservarSiDisponible(cita(1, slot0))
        assertNull("Tras confirmar el horario sigue ocupado", intento)

        val fila = dao.obtenerTodas().single()
        assertEquals("CONFIRMADA", fila.estado)
        assertEquals(1, fila.boxAsignado)
    }

    @Test
    fun eliminarSoloBorraRetencionesEnPago() = runBlocking {
        val confirmada = dao.reservarSiDisponible(cita(1, slot0))!!
        val retenida = dao.reservarSiDisponible(cita(2, slot0, estado = "EN_PAGO"))!!

        dao.eliminar(confirmada.id)
        assertEquals(
            "No debe borrarse una cita CONFIRMADA",
            2,
            dao.obtenerTodas().size
        )

        dao.eliminar(retenida.id)
        val restantes = dao.obtenerTodas()
        assertEquals(1, restantes.size)
        assertEquals("CONFIRMADA", restantes.single().estado)
    }

    @Test
    fun eliminarRetencionesLimpiaHuerfanasConservandoConfirmadas() = runBlocking {
        dao.reservarSiDisponible(cita(1, slot0))!!
        dao.reservarSiDisponible(cita(2, slot1))!!
        dao.reservarSiDisponible(cita(3, slot0, estado = "EN_PAGO"))!!
        dao.reservarSiDisponible(cita(4, slot1, estado = "EN_PAGO"))!!

        dao.eliminarRetenciones()

        val restantes = dao.obtenerTodas()
        assertEquals(2, restantes.size)
        assertTrue(restantes.all { it.estado == "CONFIRMADA" })
        assertFalse(restantes.any { it.estado == "EN_PAGO" })
    }

    // ════════════════════════════════════════════
    // FLUJO Y FILTRADO
    // ════════════════════════════════════════════

    @Test
    fun obtenerPorUsuarioFlowFiltraPorEmail() = runBlocking {
        dao.reservarSiDisponible(cita(1, slot0, usuarioEmail = "ana@mail.com"))
        dao.reservarSiDisponible(cita(2, slot0, usuarioEmail = "otro@mail.com"))

        val deAna = dao.obtenerPorUsuarioFlow("ana@mail.com").first()

        assertEquals(1, deAna.size)
        assertEquals("ana@mail.com", deAna.single().usuarioEmail)
    }

    @Test
    fun intervalosContiguosNoSeSolapanEnRoom() = runBlocking {
        val primera = dao.reservarSiDisponible(cita(1, slot0))
        val contigua = dao.reservarSiDisponible(cita(1, slot1))

        assertNotNull(primera)
        assertNotNull("Intervalo contiguo debe permitirse", contigua)
        assertEquals(2, dao.obtenerTodas().size)
    }
}
