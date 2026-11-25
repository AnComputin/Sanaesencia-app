package com.example.sanaesenciaapp.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeguridadTest {

    private fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    @Test
    fun `1 pbkdf2HmacSha256 coincide exactamente con los vectores de prueba independientes para PBKDF2-HMAC-SHA256`() {
        val password = "password"
        val salt = "salt".toByteArray(Charsets.UTF_8)

        // Vector PBKDF2-HMAC-SHA256 Iteraciones = 1
        val v1 = Seguridad.pbkdf2HmacSha256(password, salt, 1, 32)
        assertEquals("120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b", toHex(v1))

        // Vector PBKDF2-HMAC-SHA256 Iteraciones = 2
        val v2 = Seguridad.pbkdf2HmacSha256(password, salt, 2, 32)
        assertEquals("ae4d0c95af6b46d32d0adff928f06dd02a303f8ef3c251dfd6e2d85a95474c43", toHex(v2))

        // Vector PBKDF2-HMAC-SHA256 Iteraciones = 4096
        val v4096 = Seguridad.pbkdf2HmacSha256(password, salt, 4096, 32)
        assertEquals("c5e478d59288c841aa530db6845c4c8d962893a001ce4e11a4963873aa98134a", toHex(v4096))
    }

    @Test
    fun `2 hashear produce un formato con prefijo pbkdf2 y 600000 iteraciones OWASP`() {
        val password = "Password123!"
        val hash1 = Seguridad.hashear(password)
        val hash2 = Seguridad.hashear(password)

        assertTrue(hash1.startsWith("pbkdf2:sha256:600000:"))
        assertTrue(hash2.startsWith("pbkdf2:sha256:600000:"))
        assertNotEquals(hash1, hash2)
        assertTrue(Seguridad.esFormatoKdfNuevo(hash1))
    }

    @Test
    fun `3 verificar devuelve true con la contrasena correcta sobre KDF PBKDF2`() {
        val password = "Password123!"
        val hash = Seguridad.hashear(password)

        assertTrue(Seguridad.verificar(password, hash))
    }

    @Test
    fun `4 verificar devuelve false con una contrasena incorrecta sobre KDF PBKDF2`() {
        val passwordCorrecta = "Password123!"
        val passwordIncorrecta = "PasswordErronea456!"
        val hash = Seguridad.hashear(passwordCorrecta)

        assertFalse(Seguridad.verificar(passwordIncorrecta, hash))
    }

    @Test
    fun `5 verificar rechaza parametros de iteraciones y longitudes fuera de limites de seguridad`() {
        val password = "Password123!"
        val salHex = "0123456789abcdef0123456789abcdef"

        // Iteraciones inferiores a 1000 deben ser rechazadas
        val hashPocasIter = "pbkdf2:sha256:100:$salHex:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        assertFalse(Seguridad.verificar(password, hashPocasIter))

        // Iteraciones superiores a 1.000.000 deben ser rechazadas
        val hashExcesivasIter = "pbkdf2:sha256:5000000:$salHex:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        assertFalse(Seguridad.verificar(password, hashExcesivasIter))

        // Sal con longitud incorrecta debe ser rechazada
        val hashSalInvalida = "pbkdf2:sha256:600000:salCorta:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        assertFalse(Seguridad.verificar(password, hashSalInvalida))
    }

    @Test
    fun `6 verificar soporta formato historico SHA-256 con sal`() {
        val password = "Password123!"
        val salHex = "0123456789abcdef0123456789abcdef"
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        digest.update(salHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray())
        val hashHex = digest.digest(password.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

        val hashHistorico = "$salHex:$hashHex"

        assertFalse("El formato histórico no debe ser clasificado como KDF nuevo", Seguridad.esFormatoKdfNuevo(hashHistorico))
        assertTrue("La contraseña correcta debe ser verificada en el formato histórico", Seguridad.verificar(password, hashHistorico))
        assertFalse("Una contraseña incorrecta debe ser rechazada", Seguridad.verificar("Erronea123", hashHistorico))
    }

    @Test
    fun `7 verificar soporta formato historico en texto plano`() {
        val passwordPlano = "clave_texto_plano_123"

        assertFalse(Seguridad.esFormatoKdfNuevo(passwordPlano))
        assertTrue(Seguridad.verificar(passwordPlano, passwordPlano))
        assertFalse(Seguridad.verificar("otraClave", passwordPlano))
    }

    @Test
    fun `8 verificar devuelve false con un texto almacenado de formato invalido`() {
        val password = "Password123!"

        assertFalse(Seguridad.verificar(password, "formatoInvalido"))
        assertFalse(Seguridad.verificar(password, "pbkdf2:sha256:invalid"))
        assertFalse(Seguridad.verificar(password, ""))
        assertFalse(Seguridad.verificar(password, "::"))
    }

    @Test
    fun `9 medicion de rendimiento por separado en entorno JVM Unit Test`() {
        val passwordCorrecta = "PasswordBenchmark123!"
        val passwordIncorrecta = "PasswordErronea456!"

        // 1. Medición de generación de hash a 600.000 iteraciones
        val tGeneracionInicio = System.nanoTime()
        val hashGenerado = Seguridad.hashear(passwordCorrecta, iteraciones = 600000)
        val tiempoGeneracionMs = (System.nanoTime() - tGeneracionInicio) / 1_000_000.0

        // 2. Medición de verificación con contraseña correcta
        val tVerifCorrectaInicio = System.nanoTime()
        val esValida = Seguridad.verificar(passwordCorrecta, hashGenerado)
        val tiempoVerifCorrectaMs = (System.nanoTime() - tVerifCorrectaInicio) / 1_000_000.0

        // 3. Medición de verificación con contraseña incorrecta
        val tVerifIncorrectaInicio = System.nanoTime()
        val esInvalida = Seguridad.verificar(passwordIncorrecta, hashGenerado)
        val tiempoVerifIncorrectaMs = (System.nanoTime() - tVerifIncorrectaInicio) / 1_000_000.0

        assertTrue(esValida)
        assertFalse(esInvalida)

        println("=== MEDICIÓN DE RENDIMIENTO DE PBKDF2-HMAC-SHA256 (600.000 iteraciones) ===")
        println("Entorno: JVM Unit Test (Host CPU)")
        println("1. Tiempo de generación del hash: %.2f ms".format(tiempoGeneracionMs))
        println("2. Tiempo de verificación exitosa: %.2f ms".format(tiempoVerifCorrectaMs))
        println("3. Tiempo de verificación fallida: %.2f ms".format(tiempoVerifIncorrectaMs))
    }
}
