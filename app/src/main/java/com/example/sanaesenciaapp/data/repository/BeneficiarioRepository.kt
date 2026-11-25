package com.example.sanaesenciaapp.data.repository

import android.database.sqlite.SQLiteException
import com.example.sanaesenciaapp.data.remote.RetrofitClient
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
import kotlinx.coroutines.flow.onStart
import java.util.Locale

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
                    .onStart { sincronizarDesdeServidor(email) }
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

    suspend fun sincronizarDesdeServidor(email: String) {
        if (!RetrofitClient.estaInicializado()) return
        try {
            val response = RetrofitClient.usuariosApi.listarBeneficiarios(email)
            if (response.isSuccessful) {
                response.body()?.forEach { remoto ->
                    val resultado = beneficiarioDao.registrarOReactivar(remoto)
                    if (resultado == ResultadoRegistroBeneficiario.YA_EXISTE_ACTIVO) {
                        val existente = beneficiarioDao
                            .buscarPorRutYPropietario(remoto.propietarioEmail, remoto.rut)
                            .firstOrNull()
                        if (existente != null) {
                            beneficiarioDao.actualizar(
                                id = existente.id,
                                email = existente.propietarioEmail,
                                nombre = remoto.nombre,
                                rut = remoto.rut,
                                fechaNacimiento = remoto.fechaNacimiento,
                                parentesco = remoto.parentesco
                            )
                            if (!remoto.activo) {
                                beneficiarioDao.desactivar(existente.id, existente.propietarioEmail)
                            }
                        }
                    }
                }
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Exception) {
            // Si no hay conexión, Room sigue sirviendo los datos locales
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

        val entidad = BeneficiarioEntity(
            propietarioEmail = email,
            nombre = nombre.trim(),
            rut = rutNormalizado,
            fechaNacimiento = normalizarFechaIso(fechaNacimiento),
            parentesco = parentesco.trim()
        )

        if (!RetrofitClient.estaInicializado()) {
            return conManejoPersistencia {
                when (beneficiarioDao.registrarOReactivar(entidad)) {
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

        return try {
            val response = RetrofitClient.usuariosApi.crearBeneficiario(entidad)
            if (!response.isSuccessful) {
                val errorMsg = response.errorBody()?.string().orEmpty()
                return when {
                    errorMsg.contains("activo", ignoreCase = true) -> ResultadoBeneficiario.YaExisteActivo
                    errorMsg.contains("RUT", ignoreCase = true) -> ResultadoBeneficiario.DatosInvalidos
                    else -> ResultadoBeneficiario.ErrorOperacion
                }
            }

            val guardadoRemoto = response.body() ?: entidad
            conManejoPersistencia {
                when (beneficiarioDao.registrarOReactivar(guardadoRemoto)) {
                    ResultadoRegistroBeneficiario.REACTIVADO -> ResultadoBeneficiario.Reactivado
                    else -> ResultadoBeneficiario.Insertado
                }
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Exception) {
            ResultadoBeneficiario.ErrorOperacion
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

        val fechaIso = normalizarFechaIso(fechaNacimiento)
        val entidad = BeneficiarioEntity(
            id = id,
            propietarioEmail = email,
            nombre = nombre.trim(),
            rut = rutNormalizado,
            fechaNacimiento = fechaIso,
            parentesco = parentesco.trim()
        )

        if (!RetrofitClient.estaInicializado()) {
            return conManejoPersistencia {
                val filas = beneficiarioDao.actualizar(
                    id = id,
                    email = email,
                    nombre = nombre.trim(),
                    rut = rutNormalizado,
                    fechaNacimiento = fechaIso,
                    parentesco = parentesco.trim()
                )
                if (filas == 1) {
                    ResultadoBeneficiario.Actualizado
                } else {
                    val existe = beneficiarioDao.obtenerPorIdYPropietario(id, email)
                    if (existe == null) ResultadoBeneficiario.NoEncontrado
                    else ResultadoBeneficiario.ConflictoRut
                }
            }
        }

        return try {
            val response = RetrofitClient.usuariosApi.actualizarBeneficiario(id, entidad)
            if (!response.isSuccessful) {
                val errorMsg = response.errorBody()?.string().orEmpty()
                return if (errorMsg.contains("RUT", ignoreCase = true)) {
                    ResultadoBeneficiario.ConflictoRut
                } else {
                    ResultadoBeneficiario.NoEncontrado
                }
            }

            conManejoPersistencia {
                val filas = beneficiarioDao.actualizar(
                    id = id,
                    email = email,
                    nombre = nombre.trim(),
                    rut = rutNormalizado,
                    fechaNacimiento = fechaIso,
                    parentesco = parentesco.trim()
                )
                if (filas == 1) ResultadoBeneficiario.Actualizado
                else ResultadoBeneficiario.NoEncontrado
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Exception) {
            ResultadoBeneficiario.ErrorOperacion
        }
    }

    suspend fun desactivar(id: Int): ResultadoBeneficiario<Nothing> {
        val email = emailDeSesion() ?: return ResultadoBeneficiario.SesionNoAutenticada
        if (id <= 0) return ResultadoBeneficiario.DatosInvalidos

        if (!RetrofitClient.estaInicializado()) {
            return conManejoPersistencia {
                if (beneficiarioDao.desactivar(id, email) == 1) {
                    ResultadoBeneficiario.Desactivado
                } else {
                    ResultadoBeneficiario.NoEncontrado
                }
            }
        }

        return try {
            val response = RetrofitClient.usuariosApi.desactivarBeneficiario(id)
            if (!response.isSuccessful) return ResultadoBeneficiario.NoEncontrado

            conManejoPersistencia {
                if (beneficiarioDao.desactivar(id, email) == 1) {
                    ResultadoBeneficiario.Desactivado
                } else {
                    ResultadoBeneficiario.NoEncontrado
                }
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Exception) {
            ResultadoBeneficiario.ErrorOperacion
        }
    }

    suspend fun reactivar(id: Int): ResultadoBeneficiario<Nothing> {
        val email = emailDeSesion() ?: return ResultadoBeneficiario.SesionNoAutenticada
        if (id <= 0) return ResultadoBeneficiario.DatosInvalidos

        if (!RetrofitClient.estaInicializado()) {
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

        return try {
            val response = RetrofitClient.usuariosApi.reactivarBeneficiario(id)
            if (!response.isSuccessful) return ResultadoBeneficiario.NoEncontrado

            conManejoPersistencia {
                if (beneficiarioDao.reactivar(id, email) == 1) {
                    ResultadoBeneficiario.Reactivado
                } else if (beneficiarioDao.obtenerPorIdYPropietario(id, email) != null) {
                    ResultadoBeneficiario.YaExisteActivo
                } else {
                    ResultadoBeneficiario.NoEncontrado
                }
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Exception) {
            ResultadoBeneficiario.ErrorOperacion
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

    private fun normalizarFechaIso(fecha: String): String {
        val limpia = fecha.trim()
        val match = Regex("""^(\d{2})[/-](\d{2})[/-](\d{4})$""").matchEntire(limpia)
        return if (match != null) {
            val (dia, mes, anio) = match.destructured
            "$anio-$mes-$dia"
        } else {
            limpia
        }
    }
}