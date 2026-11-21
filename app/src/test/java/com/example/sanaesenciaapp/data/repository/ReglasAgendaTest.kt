package com.example.sanaesenciaapp.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasAgendaTest {

    //duracion cita
    @Test
    fun `cita dura exactamente 60 minutos`(){
        val inicio = 1000000L
        val fin = ReglasAgenda.calcularFin(inicio)
        val diferenciaMinutos = (fin - inicio)/(60 * 1000L)
        assertEquals(60L, diferenciaMinutos)
    }

    @Test
    fun `modalidad presencial es valida`(){
        assertTrue(ReglasAgenda.esModalidadValida("PRESENCIAL"))
    }

    @Test
    fun `modalidad online es valida`(){
        assertTrue(ReglasAgenda.esModalidadValida("ONLINE"))
    }

    @Test
    fun `modalidad invalida es rechazada`(){
        assertFalse(ReglasAgenda.esModalidadValida("TELEFONO"))
    }

    @Test
    fun `modalidad presencial detecta correctamente`(){
        assertTrue(ReglasAgenda.esPresencial("PRESENCIAL"))
        assertFalse(ReglasAgenda.esPresencial("ONLINE"))
    }

    @Test
    fun `citas solapadas son detectadas`(){
        val solapan = ReglasAgenda.haySolapamiento(
            inicio1 = 0, fin1 = 60,
            inicio2 = 30, fin2 = 90
        )
        assertTrue(solapan)
    }

    @Test
    fun `citas sin solapamiento no se detectan`(){
        val solapan = ReglasAgenda.haySolapamiento(
            inicio1 = 0, fin1 = 60,
            inicio2 = 60, fin2 = 120
        )
        assertFalse(solapan)
    }

    @Test
    fun `cita contenida en otra se detecta como solapada`(){
        val solapan = ReglasAgenda.haySolapamiento(
            inicio1 = 0, fin1 = 120,
            inicio2 = 30, fin2 = 60
        )
        assertTrue(solapan)
    }

    //horario valido
    @Test
    fun `horario valido retorna true`(){
        assertTrue(ReglasAgenda.esHorarioValido(0,60))
    }

    @Test
    fun `horario invalido inicio igual a fin retorna false`(){
        assertFalse(ReglasAgenda.esHorarioValido(120,60))
    }

    //boxes

    @Test
    fun `box 1 es valido`(){
        assertTrue(ReglasAgenda.esBoxValido(1))
    }

    @Test
    fun `box 8 es valido`(){
        assertTrue(ReglasAgenda.esBoxValido(8))
    }

    @Test
    fun `box 0 es invalido`(){
        assertFalse(ReglasAgenda.esBoxValido(0))
    }

    @Test
    fun `box 9 es invalido`(){
        assertFalse(ReglasAgenda.esBoxValido(9))
    }

    @Test
    fun `obtener box libre retorna primer box disponible`(){
        val ocupados = setOf(1,2,3)
        assertEquals(4, ReglasAgenda.obtenerBoxLibre(ocupados))
    }

    @Test
    fun `obtener box libre con todos ocupados retorna null`(){
        val ocupados = setOf(1,2,3,4,5,6,7,8)
        assertNull(ReglasAgenda.obtenerBoxLibre(ocupados))
    }

    @Test
    fun `obtener box libre sin ocupados retorna box 1`(){
        val ocupados = emptySet<Int>()
        assertEquals(1, ReglasAgenda.obtenerBoxLibre(ocupados))
    }

    //validacion reserva completa

    @Test
    fun `reserva presencial valida retorna null`(){
        val resultado = ReglasAgenda.validarReserva(
            modalidad = "PRESENCIAL",
            inicioMillis = 0,
            finMillis = 60,
            boxAsignado = 3
        )
        assertNull(resultado)
    }

    @Test
    fun `reserva presencial sin box retorna error`(){
        val resultado = ReglasAgenda.validarReserva(
            modalidad = "PRESENCIAL",
            inicioMillis = 0,
            finMillis = 60,
            boxAsignado = null
        )
        assertNotNull(resultado)
    }

    @Test
    fun `reserva con modalidad invalida retorna error`(){
        val resultado = ReglasAgenda.validarReserva(
            modalidad = "TELEFONO",
            inicioMillis = 0,
            finMillis = 60,
            boxAsignado = 1
        )
        assertNotNull(resultado)
    }

    @Test
    fun `reserva con horario invalido retorna error`() {
        val resultado = ReglasAgenda.validarReserva(
            modalidad = "PRESENCIAL",
            inicioMillis = 120,
            finMillis = 60,
            boxAsignado = 1
        )
        assertNotNull(resultado)
    }

    @Test
    fun `reserva presencial con box invalido retorna error`() {
        val resultado = ReglasAgenda.validarReserva(
            modalidad = "PRESENCIAL",
            inicioMillis = 0,
            finMillis = 60,
            boxAsignado = 9
        )
        assertNotNull(resultado)
    }

    //regla 8 boxes
    @Test
    fun `octavo box asignado correctamente`(){
        val ocupados = setOf(1,2,3,4,5,6,7)
        assertEquals(8, ReglasAgenda.obtenerBoxLibre(ocupados))
    }

    @Test
    fun `novena reserva presencial es rechazada cuando todos los boxes estan ocupados`(){
        val ocupados = setOf(1,2,3,4,5,6,7,8)
        val boxLibre = ReglasAgenda.obtenerBoxLibre(ocupados)
        assertNull(boxLibre)
    }

}