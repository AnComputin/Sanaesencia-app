package com.example.sanaesenciaapp.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeguridadTest {

    @Test
    fun `hashear dos veces la misma contrasena produce textos distintos por la sal aleatoria`() {
        val password = "Password123!"
        val hash1 = Seguridad.hashear(password)
        val hash2 = Seguridad.hashear(password)

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `verificar devuelve true con la contrasena correcta`() {
        val password = "Password123!"
        val hash = Seguridad.hashear(password)

        assertTrue(Seguridad.verificar(password, hash))
    }

    @Test
    fun `verificar devuelve false con una contrasena incorrecta`() {
        val passwordCorrecta = "Password123!"
        val passwordIncorrecta = "PasswordErronea456!"
        val hash = Seguridad.hashear(passwordCorrecta)

        assertFalse(Seguridad.verificar(passwordIncorrecta, hash))
    }

    @Test
    fun `verificar devuelve false con un texto almacenado de formato invalido`() {
        val password = "Password123!"

        assertFalse(Seguridad.verificar(password, "formatoInvalido"))
        assertFalse(Seguridad.verificar(password, "salHexSinSeparador"))
        assertFalse(Seguridad.verificar(password, "sal:hash:extra"))
        assertFalse(Seguridad.verificar(password, ""))
        assertFalse(Seguridad.verificar(password, "::"))
    }
}
