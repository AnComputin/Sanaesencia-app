package com.example.sanaesenciaapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Locale

enum class ResultadoRegistroBeneficiario {
    INSERTADO,
    REACTIVADO,
    YA_EXISTE_ACTIVO,
    INCONSISTENCIA_DATOS,
    CONFLICTO_INSERCION,
    DATOS_INVALIDOS,
    ERROR
}

@Dao
abstract class BeneficiarioDao {

    @Query(
        """
        SELECT * FROM beneficiarios
        WHERE TRIM(:email) != ''
          AND LOWER(TRIM(propietarioEmail)) = LOWER(TRIM(:email))
          AND activo = 1
        ORDER BY nombre COLLATE NOCASE ASC
        """
    )
    abstract fun obtenerPorPropietarioFlow(email: String): Flow<List<BeneficiarioEntity>>

    @Query(
        """
        SELECT * FROM beneficiarios
        WHERE TRIM(:email) != ''
          AND LOWER(TRIM(propietarioEmail)) = LOWER(TRIM(:email))
          AND activo = 1
        ORDER BY nombre COLLATE NOCASE ASC
        """
    )
    abstract suspend fun obtenerPorPropietario(email: String): List<BeneficiarioEntity>

    @Query(
        """
        SELECT * FROM beneficiarios
        WHERE TRIM(:email) != ''
          AND id = :id
          AND LOWER(TRIM(propietarioEmail)) = LOWER(TRIM(:email))
          AND activo = 1
        LIMIT 1
        """
    )
    abstract suspend fun obtenerPorIdYPropietario(id: Int, email: String): BeneficiarioEntity?

    @Query(
        """
        SELECT * FROM beneficiarios
        WHERE TRIM(:email) != ''
          AND LOWER(TRIM(propietarioEmail)) = LOWER(TRIM(:email))
        """
    )
    protected abstract suspend fun obtenerTodosPorPropietario(email: String): List<BeneficiarioEntity>

    suspend fun buscarPorRutYPropietario(
        email: String,
        rut: String
    ): List<BeneficiarioEntity> {
        val emailNormalizado = normalizarEmailBeneficiario(email)
        val rutNormalizado = normalizarRutBeneficiario(rut)
        if (emailNormalizado.isBlank() || !rutNormalizado.contieneIdentificadorRut()) {
            return emptyList()
        }
        return obtenerTodosPorPropietario(emailNormalizado)
            .filter { normalizarRutBeneficiario(it.rut) == rutNormalizado }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertarInterno(beneficiario: BeneficiarioEntity): Long

    suspend fun insertar(beneficiario: BeneficiarioEntity): Long {
        val emailNormalizado = normalizarEmailBeneficiario(beneficiario.propietarioEmail)
        val rutNormalizado = normalizarRutBeneficiario(beneficiario.rut)
        require(emailNormalizado.isNotBlank()) { "El correo del propietario es obligatorio" }
        require(rutNormalizado.contieneIdentificadorRut()) { "El RUT del beneficiario es obligatorio" }

        return insertarInterno(
            beneficiario.copy(
                propietarioEmail = emailNormalizado,
                rut = rutNormalizado
            )
        )
    }

    @Query(
        """
        UPDATE beneficiarios
        SET nombre = :nombre,
            rut = :rut,
            fechaNacimiento = :fechaNacimiento,
            parentesco = :parentesco
        WHERE TRIM(:email) != ''
          AND id = :id
          AND LOWER(TRIM(propietarioEmail)) = LOWER(TRIM(:email))
        """
    )
    protected abstract suspend fun actualizarInterno(
        id: Int,
        email: String,
        nombre: String,
        rut: String,
        fechaNacimiento: String,
        parentesco: String
    ): Int

    @Transaction
    open suspend fun actualizar(
        id: Int,
        email: String,
        nombre: String,
        rut: String,
        fechaNacimiento: String,
        parentesco: String
    ): Int {
        val emailNormalizado = normalizarEmailBeneficiario(email)
        val rutNormalizado = normalizarRutBeneficiario(rut)
        if (emailNormalizado.isBlank() || !rutNormalizado.contieneIdentificadorRut()) return 0

        val colisiones = buscarPorRutYPropietario(emailNormalizado, rutNormalizado)
            .any { it.id != id }
        if (colisiones) return 0

        return actualizarInterno(
            id = id,
            email = emailNormalizado,
            nombre = nombre,
            rut = rutNormalizado,
            fechaNacimiento = fechaNacimiento,
            parentesco = parentesco
        )
    }

    @Query(
        """
        UPDATE beneficiarios SET activo = 0
        WHERE TRIM(:email) != ''
          AND id = :id
          AND LOWER(TRIM(propietarioEmail)) = LOWER(TRIM(:email))
          AND activo = 1
        """
    )
    abstract suspend fun desactivar(id: Int, email: String): Int

    @Query(
        """
        UPDATE beneficiarios SET activo = 1
        WHERE TRIM(:email) != ''
          AND id = :id
          AND LOWER(TRIM(propietarioEmail)) = LOWER(TRIM(:email))
          AND activo = 0
        """
    )
    abstract suspend fun reactivar(id: Int, email: String): Int

    @Transaction
    open suspend fun registrarOReactivar(beneficiario: BeneficiarioEntity): ResultadoRegistroBeneficiario {
        val emailNormalizado = normalizarEmailBeneficiario(beneficiario.propietarioEmail)
        val rutNormalizado = normalizarRutBeneficiario(beneficiario.rut)
        if (emailNormalizado.isBlank() || !rutNormalizado.contieneIdentificadorRut()) {
            return ResultadoRegistroBeneficiario.DATOS_INVALIDOS
        }

        val coincidencias = buscarPorRutYPropietario(emailNormalizado, rutNormalizado)
        return when {
            coincidencias.size > 1 -> ResultadoRegistroBeneficiario.INCONSISTENCIA_DATOS
            coincidencias.singleOrNull()?.activo == true ->
                ResultadoRegistroBeneficiario.YA_EXISTE_ACTIVO
            coincidencias.singleOrNull() != null -> {
                val id = coincidencias.single().id
                if (reactivar(id, emailNormalizado) == 1) {
                    ResultadoRegistroBeneficiario.REACTIVADO
                } else {
                    ResultadoRegistroBeneficiario.ERROR
                }
            }
            else -> {
                val id = insertar(
                    beneficiario.copy(
                        propietarioEmail = emailNormalizado,
                        rut = rutNormalizado
                    )
                )
                if (id == -1L) {
                    ResultadoRegistroBeneficiario.CONFLICTO_INSERCION
                } else if (id > 0L) {
                    ResultadoRegistroBeneficiario.INSERTADO
                } else {
                    ResultadoRegistroBeneficiario.ERROR
                }
            }
        }
    }
}

private fun normalizarEmailBeneficiario(email: String): String =
    email.trim().lowercase(Locale.ROOT)

private fun normalizarRutBeneficiario(rut: String): String =
    rut.trim().replace(".", "").uppercase(Locale.ROOT)

private fun String.contieneIdentificadorRut(): Boolean =
    any(Char::isLetterOrDigit)
