package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.repository.SesionRepository
import com.example.sanaesenciaapp.data.room.BeneficiarioDao
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BeneficiarioUiState(
    val beneficiarios: List<BeneficiarioEntity> = emptyList(),
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val error: ErrorBeneficiario? = null
)

enum class ErrorBeneficiario {
    SESION_REQUERIDA,
    PERSISTENCIA
}

enum class ResultadoBeneficiario {
    REGISTRADO,
    DUPLICADO,
    SESION_REQUERIDA,
    ERROR
}

/**
 * Operaciones de beneficiarios restringidas al correo de la sesión autenticada.
 * Nunca recibe un propietario desde la pantalla y evita las consultas por ID sin propietario.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BeneficiarioViewModel(
    private val beneficiarioDao: BeneficiarioDao,
    private val sesionRepository: SesionRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeneficiarioUiState())
    val uiState: StateFlow<BeneficiarioUiState> = _uiState.asStateFlow()

    init {
        observarBeneficiariosDeLaSesion()
    }

    fun registrarBeneficiario(
        nombre: String,
        rut: String,
        fechaNacimiento: String,
        parentesco: String,
        onResultado: (ResultadoBeneficiario) -> Unit = {}
    ) {
        val propietario = sesionRepository.sesionActual.value?.email
            ?.let(::normalizarCorreo)
            ?.takeIf(String::isNotBlank)

        if (propietario == null) {
            _uiState.value = _uiState.value.copy(
                guardando = false,
                error = ErrorBeneficiario.SESION_REQUERIDA
            )
            onResultado(ResultadoBeneficiario.SESION_REQUERIDA)
            return
        }

        if (_uiState.value.guardando) return
        _uiState.value = _uiState.value.copy(guardando = true, error = null)

        viewModelScope.launch {
            val rutNormalizado = normalizarRut(rut)
            val resultado = try {
                withContext(ioDispatcher) {
                    val yaRegistrado = beneficiarioDao
                        .obtenerPorPropietario(propietario)
                        .any { normalizarRut(it.rut) == rutNormalizado }

                    if (yaRegistrado) {
                        ResultadoBeneficiario.DUPLICADO
                    } else {
                        val id = beneficiarioDao.insertar(
                            BeneficiarioEntity(
                                propietarioEmail = propietario,
                                nombre = nombre.trim(),
                                rut = rutNormalizado,
                                fechaNacimiento = fechaNacimiento.trim(),
                                parentesco = parentesco.trim()
                            )
                        )
                        if (id > 0L) {
                            ResultadoBeneficiario.REGISTRADO
                        } else {
                            // La restricción única del DAO también cubre duplicados inactivos.
                            ResultadoBeneficiario.DUPLICADO
                        }
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                ResultadoBeneficiario.ERROR
            }

            _uiState.value = _uiState.value.copy(
                guardando = false,
                error = if (resultado == ResultadoBeneficiario.ERROR) {
                    ErrorBeneficiario.PERSISTENCIA
                } else {
                    null
                }
            )
            onResultado(resultado)
        }
    }

    private fun observarBeneficiariosDeLaSesion() {
        viewModelScope.launch {
            sesionRepository.sesionActual
                .map { sesion -> sesion?.email?.let(::normalizarCorreo)?.takeIf(String::isNotBlank) }
                .distinctUntilChanged()
                .flatMapLatest { propietario ->
                    _uiState.value = _uiState.value.copy(
                        beneficiarios = emptyList(),
                        cargando = propietario != null,
                        error = null
                    )
                    if (propietario == null) {
                        flowOf(emptyList())
                    } else {
                        beneficiarioDao.obtenerPorPropietarioFlow(propietario)
                    }
                }
                .catch {
                    _uiState.value = _uiState.value.copy(
                        beneficiarios = emptyList(),
                        cargando = false,
                        error = ErrorBeneficiario.PERSISTENCIA
                    )
                }
                .collect { beneficiarios ->
                    _uiState.value = _uiState.value.copy(
                        beneficiarios = beneficiarios,
                        cargando = false,
                        error = null
                    )
                }
        }
    }

    private fun normalizarCorreo(email: String): String =
        email.trim().lowercase(Locale.ROOT)

    private fun normalizarRut(rut: String): String =
        rut.trim().replace(".", "").uppercase(Locale.ROOT)
}
