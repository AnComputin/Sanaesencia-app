package com.example.sanaesenciaapp.data.repository

import android.database.sqlite.SQLiteException
import com.example.sanaesenciaapp.data.room.BeneficiarioDao
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import com.example.sanaesenciaapp.data.room.ResultadoRegistroBeneficiario
import com.example.sanaesenciaapp.utils.validarRut
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Locale

/**
 * Resultados públicos de operaciones de beneficiarios.
 *
 * Los errores de persistencia conservan la excepción SQLite original para diagnóstico
 * en una capa superior, sin incluir datos personales en mensajes.
 */
sealed interface ResultadoBeneficiario<out T> {
    data class Exito<T>(val valor: T) : ResultadoBeneficiario<T>
    data object SesionNoAutenticada : ResultadoBeneficiario<Nothing>
    data object DatosInvalidos : ResultadoBeneficiario<Nothing>
    data object NoEncontrado : ResultadoBeneficiario<Nothing>
    data object YaExisteActivo : ResultadoBeneficiario<Nothing>
    data object InconsistenciaDatos : ResultadoBeneficiario<Nothing>
    data object ConflictoInsercion : ResultadoBeneficiario<Nothing>
    data object ConflictoRut : ResultadoBeneficiario<Nothing>
    data object Insertado : ResultadoBeneficiario<Nothing>
    data object Reactivado : ResultadoBeneficiario<Nothing>
    data object Actualizado : ResultadoBeneficiario<Nothing>
    data object Desactivado : ResultadoBeneficiario<Nothing>
    data object ErrorOperacion : ResultadoBeneficiario<Nothing>
    data class ErrorPersistencia(val causa: SQLiteException) : ResultadoBeneficiario<Nothing>
}

/**
 * Acceso a beneficiarios limitado a la identidad presente en [SesionRepository].
 *
 * Ninguna operación recibe un propietario desde la UI. Cada llamada de escritura/lectura
 * puntual obtiene el correo de la sesión vigente; el Flow cambia de propietario o emite
 * SesionNoAutenticada cuando la sesión cambia o se cierra.
 */
class BeneficiarioRepository(
    private val beneficiarioDao: BeneficiarioDao,
    private val sesionRepository: SesionRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarBeneficiarios(): Flow<ResultadoBeneficiario<List<BeneficiarioEntity>>> =
        sesionRepository.sesionActual.flatMapLatest { sesion ->
            val email = sesion?.email?.normalizarEmail()?.takeIf(String::isNotBlank)
            if (email == null) {
                flowOf(ResultadoBeneficiario.SesionNoAutenticada)
            } else {
                beneficiarioDao.obtenerPorPropietarioFlow(email)
                    .map<List<BeneficiarioEntity>, ResultadoBeneficiario<List<BeneficiarioEntity>>> {
                        ResultadoBeneficiario.Exito(it)
                    }
                    .catch { error ->
                        when (error) {
                            is CancellationException -> throw error
                            is SQLiteException -> emit(ResultadoBeneficiario.ErrorPersistencia(error))
                            else -> throw error
                        }
                    }
            }
        }

    suspend fun obtenerPorId(id: Int): ResultadoBeneficiario<BeneficiarioEntity> {
        val email = emailDeSesion() ?: return ResultadoBeneficiario.SesionNoAutenticada
        if (id <= 0) return ResultadoBeneficiario.DatosInvalidos

        return conManejoPersistencia {
            val beneficiario = beneficiarioDao.obtenerPorIdYPropietario(id, email)
                ?: return@conManejoPersistencia ResultadoBeneficiario.NoEncontrado
            ResultadoBeneficiario.Exito(beneficiario)
        }
    }

    suspend fun buscarPorRut(rut: String): ResultadoBeneficiario<List<BeneficiarioEntity>> {
        val email = emailDeSesion() ?: return ResultadoBeneficiario.SesionNoAutenticada
        val rutNormalizado = rut.normalizarRut()
        if (validarRut(rutNormalizado) != null) return ResultadoBeneficiario.DatosInvalidos

        return conManejoPersistencia {
            val encontrados = beneficiarioDao.buscarPorRutYPropietario(email, rutNormalizado)
            if (encontrados.isEmpty()) ResultadoBeneficiario.NoEncontrado
            else ResultadoBeneficiario.Exito(encontrados)
        }
    }

    /**
     * Registra usando siempre el correo autenticado. La búsqueda, inserción o reactivación
     * se delega íntegramente a la transacción de BeneficiarioDao.
     */
    suspend fun registrar(
        nombre: String,
        rut: String,
        fechaNacimiento: String,
        parentesco: String
    ): ResultadoBeneficiario<Nothing> {
        val email = emailDeSesion() ?: return ResultadoBeneficiario.SesionNoAutenticada
        val rutNormalizado = rut.normalizarRut()
        if (nombre.isBlank() || validarRut(rutNormalizado) != null ||
            fechaNacimiento.isBlank() || parentesco.isBlank()
        ) {
            return ResultadoBeneficiario.DatosInvalidos
        }

        return conManejoPersistencia {
            when (
                beneficiarioDao.registrarOReactivar(
                    BeneficiarioEntity(
                        propietarioEmail = email,
                        nombre = nombre.trim(),
                        rut = rutNormalizado,
                        fechaNacimiento = fechaNacimiento.trim(),
                        parentesco = parentesco.trim()
                    )
                )
            ) {
                ResultadoRegistroBeneficiario.INSERTADO -> ResultadoBeneficiario.Insertado
                ResultadoRegistroBeneficiario.REACTIVADO -> ResultadoBeneficiario.Reactivado
                ResultadoRegistroBeneficiario.YA_EXISTE_ACTIVO -> ResultadoBeneficiario.YaExisteActivo
                ResultadoRegistroBeneficiario.INCONSISTENCIA_DATOS -> ResultadoBeneficiario.InconsistenciaDatos
                ResultadoRegistroBeneficiario.CONFLICTO_INSERCION -> ResultadoBeneficiario.ConflictoInsercion
                ResultadoRegistroBeneficiario.DATOS_INVALIDOS -> ResultadoBeneficiario.DatosInvalidos
                ResultadoRegistroBeneficiario.ERROR -> ResultadoBeneficiario.ErrorOperacion
            }
        }
    }

    suspend fun actualizar(
        id: Int,
        nombre: String,
        rut: String,
        fechaNacimiento: String,
        parentesco: String
    ): ResultadoBeneficiario<Nothing> {
        val email = emailDeSesion() ?: return ResultadoBeneficiario.SesionNoAutenticada
        val rutNormalizado = rut.normalizarRut()
        if (id <= 0 || nombre.isBlank() || validarRut(rutNormalizado) != null ||
            fechaNacimiento.isBlank() || parentesco.isBlank()
        ) {
            return ResultadoBeneficiario.DatosInvalidos
        }

        return conManejoPersistencia {
            val filas = beneficiarioDao.actualizar(
                id = id,
                email = email,
                nombre = nombre.trim(),
                rut = rutNormalizado,
                fechaNacimiento = fechaNacimiento.trim(),
                parentesco = parentesco.trim()
            )
            if (filas == 1) {
                ResultadoBeneficiario.Actualizado
            } else {
                val rutEnUso = beneficiarioDao.buscarPorRutYPropietario(email, rutNormalizado)
                    .any { it.id != id }
                if (rutEnUso) ResultadoBeneficiario.ConflictoRut
                else ResultadoBeneficiario.NoEncontrado
            }
        }
    }

    suspend fun desactivar(id: Int): ResultadoBeneficiario<Nothing> {
        val email = emailDeSesion() ?: return ResultadoBeneficiario.SesionNoAutenticada
        if (id <= 0) return ResultadoBeneficiario.DatosInvalidos

        return conManejoPersistencia {
            if (beneficiarioDao.desactivar(id, email) == 1) {
                ResultadoBeneficiario.Desactivado
            } else {
                ResultadoBeneficiario.NoEncontrado
            }
        }
    }

    suspend fun reactivar(id: Int): ResultadoBeneficiario<Nothing> {
        val email = emailDeSesion() ?: return ResultadoBeneficiario.SesionNoAutenticada
        if (id <= 0) return ResultadoBeneficiario.DatosInvalidos

        return conManejoPersistencia {
            if (beneficiarioDao.reactivar(id, email) == 1) {
                ResultadoBeneficiario.Reactivado
            } else if (beneficiarioDao.obtenerPorIdYPropietario(id, email) != null) {
                ResultadoBeneficiario.YaExisteActivo
            } else {
                ResultadoBeneficiario.NoEncontrado
            }
        }
    }

    private fun emailDeSesion(): String? =
        sesionRepository.sesionActual.value?.email
            ?.normalizarEmail()
            ?.takeIf(String::isNotBlank)

    private suspend fun <T> conManejoPersistencia(
        operacion: suspend () -> ResultadoBeneficiario<T>
    ): ResultadoBeneficiario<T> =
        try {
            operacion()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: SQLiteException) {
            ResultadoBeneficiario.ErrorPersistencia(error)
        }

    private fun String.normalizarEmail(): String = trim().lowercase(Locale.ROOT)

    private fun String.normalizarRut(): String = trim().replace(".", "").uppercase(Locale.ROOT)
}
