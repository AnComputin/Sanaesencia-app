package com.example.sanaesenciaapp.utils

import androidx.compose.ui.text.AnnotatedString
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import com.example.sanaesenciaapp.data.room.DetalleVentaEntity
import com.example.sanaesenciaapp.data.room.ESPECIALIDADES_INICIALES
import com.example.sanaesenciaapp.data.room.ESPECIALISTAS_INICIALES
import com.example.sanaesenciaapp.data.room.VentaEntity
import com.example.sanaesenciaapp.ui.components.RutVisualTransformation
import com.example.sanaesenciaapp.viewmodel.CitaItem
import com.example.sanaesenciaapp.viewmodel.CitaViewModel
import com.example.sanaesenciaapp.viewmodel.EstadoCita
import com.example.sanaesenciaapp.viewmodel.EstadoReserva
import com.example.sanaesenciaapp.viewmodel.Modalidad
import com.example.sanaesenciaapp.viewmodel.Paciente
import com.example.sanaesenciaapp.viewmodel.PausaConscienteViewModel
import com.example.sanaesenciaapp.viewmodel.RespiracionViewModel
import com.example.sanaesenciaapp.viewmodel.TerapeutaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModelsAndUtilsTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `RutVisualTransformation formatea cuerpo y digito verificador y mapea offsets`() {
        val transform = RutVisualTransformation()

        val parcial = transform.filter(AnnotatedString("1234567"))
        assertEquals("1.234.567", parcial.text.text)
        assertEquals(0, parcial.offsetMapping.originalToTransformed(0))
        assertEquals(9, parcial.offsetMapping.originalToTransformed(7))
        assertEquals(7, parcial.offsetMapping.transformedToOriginal(9))

        val completo = transform.filter(AnnotatedString("12345678k"))
        assertEquals("12.345.678-K", completo.text.text)
        assertEquals(12, completo.offsetMapping.originalToTransformed(9))
        assertEquals(11, completo.offsetMapping.originalToTransformed(8))
        assertEquals(9, completo.offsetMapping.transformedToOriginal(12))
    }

    @Test
    fun `datos iniciales y entidades Room mantienen integridad esperada`() {
        assertEquals(7, ESPECIALIDADES_INICIALES.size)
        assertEquals(30, ESPECIALISTAS_INICIALES.size)

        val venta = VentaEntity(1, "a@b.cl", "Ana", 100L, 9000, "PAGADA", "Tarjeta")
        val detalle = DetalleVentaEntity(1, 1, 2, "Aceite", 4500, 2, 9000)
        val ben = BeneficiarioEntity(1, "a@b.cl", "Hijo", "12345678-5", "2015-01-01", "Hijo")
        val ubic = ResultadoUbicacion(-31.91, -71.51, 1.2f, "Los Vilos")

        assertEquals(9000, venta.total)
        assertEquals(2, detalle.cantidad)
        assertEquals("Hijo", ben.nombre)
        assertEquals(1.2f, ubic.distanciaKm, 0.01f)
    }

    @Test
    fun `PausaConscienteViewModel TerapeutaViewModel y CitaViewModel cubren ramas auxiliares`() {
        val pausaVm = PausaConscienteViewModel()
        pausaVm.iniciar()
        pausaVm.siguientePaso()
        assertEquals(1, pausaVm.uiState.value.pasoIndex)
        pausaVm.pasoAnterior()
        assertEquals(0, pausaVm.uiState.value.pasoIndex)
        pausaVm.pausarOReanudar()
        assertTrue(pausaVm.uiState.value.estaPausado)

        val terapeutaVm = TerapeutaViewModel()
        assertFalse(terapeutaVm.estaEnRiesgo(Paciente(1, "N", "1-9", "01/01/2026", "Box 1")))
        assertEquals(EstadoCita.PENDIENTE, EstadoCita.values().first())

        val citaVm = CitaViewModel()
        citaVm.seleccionarModalidad(Modalidad.ONLINE)
        assertEquals(Modalidad.ONLINE, citaVm.uiState.value.modalidad)
        citaVm.cambiarNombre("Carla")
        assertEquals("Carla", citaVm.uiState.value.nombrePaciente)
        assertTrue(citaVm.formatearFecha(System.currentTimeMillis()).isNotBlank())

        val inicioFutura = System.currentTimeMillis() + 48 * 3600_000L
        val futura = CitaItem(
            id = 1,
            usuarioEmail = "ana@sana.cl",
            especialidadId = 1,
            especialidad = "Psicología",
            especialistaId = 1,
            profesional = "Dr",
            modalidad = Modalidad.PRESENCIAL,
            inicioMillis = inicioFutura,
            finMillis = inicioFutura + 3600_000L,
            hora = "09:00",
            paciente = "Ana",
            estado = EstadoReserva.CONFIRMADA
        )
        assertTrue(citaVm.puedeCancelar(futura))
        assertFalse(citaVm.puedeCancelar(futura.copy(estado = EstadoReserva.CANCELADA)))
        assertFalse(
            citaVm.puedeCancelar(
                futura.copy(inicioMillis = System.currentTimeMillis() + 2 * 3600_000L)
            )
        )

        val respVm = RespiracionViewModel()
        respVm.iniciarOSincronizar()
        assertTrue(respVm.uiState.value.estaActivo)
        respVm.pausar()
        respVm.iniciarOSincronizar()
        assertFalse(respVm.uiState.value.estaPausado)
        respVm.reiniciar()
        assertNotNull(respVm.uiState.value)
    }
}
