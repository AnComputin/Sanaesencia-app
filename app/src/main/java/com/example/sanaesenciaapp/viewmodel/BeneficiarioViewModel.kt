package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.repository.BeneficiarioRepository
import com.example.sanaesenciaapp.data.repository.SesionUsuario
import com.example.sanaesenciaapp.data.repository.ResultadoBeneficiario
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class BeneficiarioUiState(
    val versionSesion: Long = 0,
    val beneficiarios: List<BeneficiarioEntity> = emptyList(),
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val sinSesion: Boolean = false,
    val errorPersistencia: Boolean = false
)

class BeneficiarioViewModel(
    private val beneficiarioRepository: BeneficiarioRepository,
    private val sesionActual: StateFlow<SesionUsuario?>
) : ViewModel() {
    private val _uiState = MutableStateFlow(BeneficiarioUiState())
    val uiState: StateFlow<BeneficiarioUiState> = _uiState.asStateFlow()

    private var sesionVisible: SesionUsuario? = null

    init {
        viewModelScope.launch {
            sesionActual.collectLatest { sesion ->
                sesionVisible = sesion
                val version = _uiState.value.versionSesion + 1
                val propietario = sesion?.email?.trim()?.lowercase(Locale.ROOT)
                    ?.takeIf { it.isNotEmpty() }
                _uiState.value = BeneficiarioUiState(
                    versionSesion = version,
                    cargando = propietario != null,
                    sinSesion = propietario == null
                )
                if (propietario == null) return@collectLatest

                beneficiarioRepository.observarBeneficiarios()
                    .catch { error ->
                        if (error is CancellationException) throw error
                        emit(ResultadoBeneficiario.ErrorOperacion)
                    }
                    .collect { resultado ->
                        if (sesionActual.value !== sesion || _uiState.value.versionSesion != version) {
                            return@collect
                        }
                        when (resultado) {
                            is ResultadoBeneficiario.Exito -> _uiState.value = _uiState.value.copy(
                                beneficiarios = resultado.valor.filter {
                                    it.propietarioEmail.trim().lowercase(Locale.ROOT) == propietario
                                },
                                cargando = false,
                                errorPersistencia = false
                            )
                            ResultadoBeneficiario.SesionNoAutenticada -> _uiState.value =
                                BeneficiarioUiState(versionSesion = version, cargando = false, sinSesion = true)
                            is ResultadoBeneficiario.ErrorPersistencia, ResultadoBeneficiario.ErrorOperacion ->
                                _uiState.value = _uiState.value.copy(
                                    beneficiarios = emptyList(),
                                    cargando = false,
                                    errorPersistencia = true
                                )
                            else -> Unit
                        }
                    }
            }
        }
    }

    fun registrar(
        nombre: String, rut: String, fechaNacimiento: String, parentesco: String,
        onResult: (ResultadoBeneficiario<Nothing>) -> Unit
    ) = ejecutar(
        { beneficiarioRepository.registrar(nombre, rut, fechaNacimiento, parentesco) },
        onResult
    )

    fun actualizar(
        id: Int, nombre: String, rut: String, fechaNacimiento: String, parentesco: String,
        onResult: (ResultadoBeneficiario<Nothing>) -> Unit
    ) = ejecutar(
        { beneficiarioRepository.actualizar(id, nombre, rut, fechaNacimiento, parentesco) },
        onResult
    )

    fun desactivar(id: Int, onResult: (ResultadoBeneficiario<Nothing>) -> Unit) =
        ejecutar({ beneficiarioRepository.desactivar(id) }, onResult)

    fun reactivar(id: Int, onResult: (ResultadoBeneficiario<Nothing>) -> Unit) =
        ejecutar({ beneficiarioRepository.reactivar(id) }, onResult)

    suspend fun obtenerPorId(id: Int): ResultadoBeneficiario<BeneficiarioEntity> =
        beneficiarioRepository.obtenerPorId(id)

    private fun ejecutar(
        operacion: suspend () -> ResultadoBeneficiario<Nothing>,
        onResult: (ResultadoBeneficiario<Nothing>) -> Unit
    ) {
        val sesion = sesionActual.value ?: return
        val version = _uiState.value.versionSesion
        if (sesionVisible !== sesion || _uiState.value.guardando || _uiState.value.cargando) return
        _uiState.value = _uiState.value.copy(guardando = true)
        viewModelScope.launch {
            if (sesionActual.value !== sesion || _uiState.value.versionSesion != version) return@launch
            val resultado = try {
                operacion()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                ResultadoBeneficiario.ErrorOperacion
            }
            if (sesionActual.value !== sesion || _uiState.value.versionSesion != version) return@launch
            _uiState.value = _uiState.value.copy(
                guardando = false,
                errorPersistencia = resultado is ResultadoBeneficiario.ErrorPersistencia ||
                    resultado == ResultadoBeneficiario.ErrorOperacion
            )
            onResult(resultado)
        }
    }
}
