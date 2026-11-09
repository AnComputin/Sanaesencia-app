package com.example.sanaesenciaapp.utils

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Utilidad de seguridad para el almacenamiento de contraseñas mediante PBKDF2-HMAC-SHA256 (RFC 8018).
 *
 * Basada en javax.crypto.Mac ("HmacSHA256") para garantizar compatibilidad nativa con Android API 24+.
 *
 * Formato del hash almacenado: "pbkdf2:sha256:<iteraciones>:<salHex>:<hashHex>"
 *
 * Ofrece soporte para la verificación y migración transparente de formatos históricos
 * (SHA-256 con sal y texto plano).
 */
object Seguridad {

    private const val LARGO_SAL_BYTES = 16
    private const val LARGO_CLAVE_BYTES = 32 // 256 bits
    private const val SEPARADOR = ":"
    private const val PREFIX_PBKDF2 = "pbkdf2:sha256:"

    // Configuración objetivo OWASP para PBKDF2-HMAC-SHA256 (600.000 iteraciones)
    const val DEFAULT_ITERATIONS = 600000

    // Límites de seguridad para evitar consumo excesivo de CPU en hashes manipulados
    private const val MIN_ITERATIONS = 1000
    private const val MAX_ITERATIONS = 1000000

    private const val ALGORITMO_HMAC = "HmacSHA256"
    private const val ALGORITMO_HASH_LEGACY = "SHA-256"

    /**
     * Genera un nuevo hash derivado mediante PBKDF2-HMAC-SHA256 con sal aleatoria de 16 bytes.
     * Retorna el resultado en formato versionado "pbkdf2:sha256:<iteraciones>:salHex:hashHex".
     */
    fun hashear(password: String, iteraciones: Int = DEFAULT_ITERATIONS): String {
        val random = SecureRandom()
        val salBytes = ByteArray(LARGO_SAL_BYTES)
        random.nextBytes(salBytes)

        val hashBytes = pbkdf2HmacSha256(
            password = password,
            salt = salBytes,
            iterations = iteraciones,
            dkLen = LARGO_CLAVE_BYTES
        )

        val salHex = bytesAHex(salBytes)
        val hashHex = bytesAHex(hashBytes)

        return "$PREFIX_PBKDF2$iteraciones$SEPARADOR$salHex$SEPARADOR$hashHex"
    }

    /**
     * Verifica la contraseña contra el hash almacenado.
     * Soporta el nuevo formato PBKDF2 y los formatos históricos (SHA-256 con sal y texto plano).
     */
    fun verificar(password: String, almacenado: String): Boolean {
        if (password.isEmpty() || almacenado.isEmpty()) return false

        return try {
            if (almacenado.startsWith(PREFIX_PBKDF2)) {
                verificarPbkdf2(password, almacenado)
            } else if (esFormatoSha256Legacy(almacenado)) {
                verificarSha256Legacy(password, almacenado)
            } else {
                // Formato histórico en texto plano (compatibilidad transicional)
                almacenado == password
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Indica si el formato almacenado corresponde al nuevo KDF PBKDF2.
     */
    fun esFormatoKdfNuevo(almacenado: String): Boolean {
        return almacenado.startsWith(PREFIX_PBKDF2)
    }

    /**
     * Implementación directa de PBKDF2-HMAC-SHA256 según RFC 8018 / PKCS #5 v2.1.
     * Utiliza javax.crypto.Mac ("HmacSHA256"), garantizada en Android API 24+.
     */
    fun pbkdf2HmacSha256(
        password: String,
        salt: ByteArray,
        iterations: Int,
        dkLen: Int
    ): ByteArray {
        val mac = Mac.getInstance(ALGORITMO_HMAC)
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val keySpec = SecretKeySpec(passwordBytes, ALGORITMO_HMAC)
        mac.init(keySpec)

        val hLen = mac.macLength // 32 bytes para SHA-256
        val l = (dkLen + hLen - 1) / hLen
        val r = dkLen - (l - 1) * hLen
        val dk = ByteArray(dkLen)

        val uXor = ByteArray(hLen)
        val block = ByteArray(salt.size + 4)
        System.arraycopy(salt, 0, block, 0, salt.size)

        for (i in 1..l) {
            block[salt.size] = (i ushr 24).toByte()
            block[salt.size + 1] = (i ushr 16).toByte()
            block[salt.size + 2] = (i ushr 8).toByte()
            block[salt.size + 3] = i.toByte()

            mac.update(block)
            var currentDigest = mac.doFinal()
            System.arraycopy(currentDigest, 0, uXor, 0, hLen)

            for (iterIndex in 2..iterations) {
                mac.update(currentDigest)
                currentDigest = mac.doFinal()
                for (k in 0 until hLen) {
                    uXor[k] = (uXor[k].toInt() xor currentDigest[k].toInt()).toByte()
                }
            }

            val offset = (i - 1) * hLen
            val copyLen = if (i == l) r else hLen
            System.arraycopy(uXor, 0, dk, offset, copyLen)
        }

        return dk
    }

    private fun verificarPbkdf2(password: String, almacenado: String): Boolean {
        // Formato esperado: "pbkdf2:sha256:<iteraciones>:<salHex>:<hashHex>"
        val partes = almacenado.split(SEPARADOR)
        if (partes.size != 5) return false

        val iteraciones = partes[2].toIntOrNull() ?: return false

        // Validación de límites de seguridad para iteraciones
        if (iteraciones < MIN_ITERATIONS || iteraciones > MAX_ITERATIONS) {
            return false
        }

        val salHex = partes[3]
        val hashOriginalHex = partes[4]

        if (salHex.length != LARGO_SAL_BYTES * 2 || hashOriginalHex.length != LARGO_CLAVE_BYTES * 2) {
            return false
        }

        val salBytes = hexABytes(salHex) ?: return false
        val hashOriginalBytes = hexABytes(hashOriginalHex) ?: return false

        val hashCalculadoBytes = pbkdf2HmacSha256(
            password = password,
            salt = salBytes,
            iterations = iteraciones,
            dkLen = hashOriginalBytes.size
        )

        return MessageDigest.isEqual(hashCalculadoBytes, hashOriginalBytes)
    }

    private fun verificarSha256Legacy(password: String, almacenado: String): Boolean {
        val partes = almacenado.split(SEPARADOR)
        if (partes.size != 2) return false

        val salHex = partes[0]
        val hashOriginalHex = partes[1]

        val salBytes = hexABytes(salHex) ?: return false
        val hashOriginalBytes = hexABytes(hashOriginalHex) ?: return false

        val digest = MessageDigest.getInstance(ALGORITMO_HASH_LEGACY)
        digest.update(salBytes)
        val hashCalculadoBytes = digest.digest(password.toByteArray(Charsets.UTF_8))

        return MessageDigest.isEqual(hashCalculadoBytes, hashOriginalBytes)
    }

    private fun esFormatoSha256Legacy(almacenado: String): Boolean {
        val partes = almacenado.split(SEPARADOR)
        if (partes.size != 2) return false
        val (salHex, hashHex) = partes
        return salHex.length == 32 && salHex.all { it.isHex() } &&
                hashHex.length == 64 && hashHex.all { it.isHex() }
    }

    private fun Char.isHex(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

    private fun bytesAHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

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
