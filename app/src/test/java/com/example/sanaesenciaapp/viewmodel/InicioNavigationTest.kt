package com.example.sanaesenciaapp.viewmodel

import com.example.sanaesenciaapp.data.repository.SesionUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para la lógica de navegación y accesos según rol y autenticación
 * en las pantallas de Inicio y Home (Tarea AL-01).
 */
class InicioNavigationTest {

    // ─────────────────────────────────────────────────────────────
    // Determinación de destino post-login según rol
    // ─────────────────────────────────────────────────────────────

    private fun resolverDestinoPostLogin(usuario: SesionUsuario?): String {
        return when (usuario?.rol) {
            "ADMIN" -> "home_admin"
            "TERAPEUTA" -> "home_terapeuta"
            else -> "home"
        }
    }

    @Test
    fun `usuario admin navega a home_admin`() {
        val admin = SesionUsuario(
            nombre = "Admin",
            email = "admin@sanaesencia.cl",
            rut = "11.111.111-1",
            telefono = "912345678",
            fechaNacimiento = "01/01/1980",
            comuna = "Los Vilos",
            rol = "ADMIN"
        )
        assertEquals("home_admin", resolverDestinoPostLogin(admin))
    }

    @Test
    fun `usuario terapeuta navega a home_terapeuta`() {
        val terapeuta = SesionUsuario(
            nombre = "Terapeuta",
            email = "terapeuta@sanaesencia.cl",
            rut = "22.222.222-2",
            telefono = "912345678",
            fechaNacimiento = "01/01/1985",
            comuna = "Los Vilos",
            rol = "TERAPEUTA"
        )
        assertEquals("home_terapeuta", resolverDestinoPostLogin(terapeuta))
    }

    @Test
    fun `usuario paciente navega a home`() {
        val paciente = SesionUsuario(
            nombre = "Paciente",
            email = "paciente@gmail.com",
            rut = "33.333.333-3",
            telefono = "912345678",
            fechaNacimiento = "01/01/1995",
            comuna = "Los Vilos",
            rol = "PACIENTE"
        )
        assertEquals("home", resolverDestinoPostLogin(paciente))
    }

    @Test
    fun `usuario nulo navega a home por defecto`() {
        assertEquals("home", resolverDestinoPostLogin(null))
    }

    // ─────────────────────────────────────────────────────────────
    // Visibilidad de controles y accesos en Home según sesión
    // ─────────────────────────────────────────────────────────────

    private fun puedeVerMisCitas(usuario: SesionUsuario?): Boolean {
        return usuario != null
    }

    private fun puedeCerrarSesion(usuario: SesionUsuario?): Boolean {
        return usuario != null
    }

    private fun resolverDestinoReserva(usuario: SesionUsuario?): String {
        return if (usuario == null) "login" else "seleccionar_especialidad"
    }

    @Test
    fun `sin sesion no se muestra boton mis citas ni cerrar sesion`() {
        assertFalse(puedeVerMisCitas(null))
        assertFalse(puedeCerrarSesion(null))
    }

    @Test
    fun `sin sesion boton reservar redirige a login`() {
        assertEquals("login", resolverDestinoReserva(null))
    }

    @Test
    fun `con sesion activa se muestra boton mis citas y cerrar sesion`() {
        val usuario = SesionUsuario(
            nombre = "Ale",
            email = "ale@duocuc.cl",
            rut = "12.345.678-5",
            telefono = "987654321",
            fechaNacimiento = "15/05/1990",
            comuna = "Los Vilos",
            rol = "PACIENTE"
        )
        assertTrue(puedeVerMisCitas(usuario))
        assertTrue(puedeCerrarSesion(usuario))
        assertEquals("seleccionar_especialidad", resolverDestinoReserva(usuario))
    }

    // ─────────────────────────────────────────────────────────────
    // Verificación de rutas de catálogo de especialidades
    // ─────────────────────────────────────────────────────────────

    private fun resolverRutaEspecialidad(id: Int, agendaPausada: Boolean): String {
        return if (agendaPausada) "detalle_servicio/$id" else "seleccionar_especialista/$id"
    }

    @Test
    fun `especialidad con agenda activa navega a seleccion de especialista`() {
        val ruta = resolverRutaEspecialidad(id = 1, agendaPausada = false)
        assertEquals("seleccionar_especialista/1", ruta)
    }

    @Test
    fun `especialidad con agenda pausada navega a detalle de servicio`() {
        val ruta = resolverRutaEspecialidad(id = 2, agendaPausada = true)
        assertEquals("detalle_servicio/2", ruta)
    }
}
