package com.example.sanaesenciaapp.utils

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Utilidad de seguridad para el almacenamiento de contraseñas mediante hashing con sal.
 *
 * NOTA: Esta implementación utiliza SHA-256 con sal con fines educativos.
 * En un entorno de producción profesional, se deben utilizar algoritmos
 * de derivación de claves diseñados para contraseñas como PBKDF2, bcrypt o Argon2.
 */
object Seguridad {

    private const val LARGO_SAL_BYTES = 16
    private const val SEPARADOR = ":"
    private const val ALGORITMO_HASH = "SHA-256"

    /**
     * Genera una sal aleatoria de 16 bytes y calcula el hash SHA-256 combinando
     * la sal y la contraseña en UTF-8.
     * Retorna el resultado en formato "salHex:hashHex".
     */
    fun hashear(password: String): String {
        val random = SecureRandom()
        val salBytes = ByteArray(LARGO_SAL_BYTES)
        random.nextBytes(salBytes)

        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance(ALGORITMO_HASH)
        digest.update(salBytes)
        val hashBytes = digest.digest(passwordBytes)

        val salHex = bytesAHex(salBytes)
        val hashHex = bytesAHex(hashBytes)

        return "$salHex$SEPARADOR$hashHex"
    }

    /**
     * Verifica si la contraseña ingresada coincide con el hash almacenado en formato "salHex:hashHex".
     * Devuelve false si el formato es inválido o si las contraseñas no coinciden.
     */
    fun verificar(password: String, almacenado: String): Boolean {
        return try {
            val partes = almacenado.split(SEPARADOR)
            if (partes.size != 2) return false

            val salHex = partes[0]
            val hashHexOriginal = partes[1]

            if (salHex.isEmpty() || hashHexOriginal.isEmpty()) return false

            val salBytes = hexABytes(salHex) ?: return false
            val hashOriginalBytes = hexABytes(hashHexOriginal) ?: return false

            val passwordBytes = password.toByteArray(Charsets.UTF_8)
            val digest = MessageDigest.getInstance(ALGORITMO_HASH)
            digest.update(salBytes)
            val hashCalculadoBytes = digest.digest(passwordBytes)

            MessageDigest.isEqual(hashCalculadoBytes, hashOriginalBytes)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Convierte un arreglo de bytes a su representación en formato hexadecimal minúscula.
     */
    private fun bytesAHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    /**
     * Convierte una cadena hexadecimal a un arreglo de bytes.
     * Devuelve null si la cadena es inválida o tiene longitud impar.
     */
    private fun hexABytes(hex: String): ByteArray? {
        if (hex.length % 2 != 0) return null
        val bytes = ByteArray(hex.length / 2)
        for (i in bytes.indices) {
            val indice = i * 2
            val byteValue = hex.substring(indice, indice + 2).toIntOrNull(16) ?: return null
            bytes[i] = byteValue.toByte()
        }
        return bytes
    }
}
