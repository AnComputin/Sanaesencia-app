package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.repository.BeneficiarioRepository
import com.example.sanaesenciaapp.data.repository.ResultadoBeneficiario
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class BeneficiarioUiState(
    val beneficiarios: List<BeneficiarioEntity> = emptyList(),
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val sinSesion: Boolean = false,
    val errorPersistencia: Boolean = false
)

class BeneficiarioViewModel(
    private val beneficiarioRepository: BeneficiarioRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BeneficiarioUiState())
    val uiState: StateFlow<BeneficiarioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            beneficiarioRepository.observarBeneficiarios()
                .catch { error ->
                    if (error is CancellationException) throw error
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        beneficiarios = emptyList(),
                        errorPersistencia = true,
                        sinSesion = false
                    )
                }
                .collect { resultado ->
                    when (resultado) {
                        is ResultadoBeneficiario.Exito -> _uiState.value =
                            _uiState.value.copy(
                                beneficiarios = resultado.valor,
                                cargando = false,
                                sinSesion = false,
                                errorPersistencia = false
                            )
                        ResultadoBeneficiario.SesionNoAutenticada -> _uiState.value =
                            _uiState.value.copy(
                                beneficiarios = emptyList(),
                                cargando = false,
                                sinSesion = true,
                                errorPersistencia = false
                            )
                        is ResultadoBeneficiario.ErrorPersistencia -> _uiState.value =
                            _uiState.value.copy(
                                beneficiarios = emptyList(),
                                cargando = false,
                                sinSesion = false,
                                errorPersistencia = true
                            )
                        else -> Unit
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
        if (_uiState.value.guardando) return
        _uiState.value = _uiState.value.copy(guardando = true)
        viewModelScope.launch {
            val resultado = try {
                operacion()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                ResultadoBeneficiario.ErrorOperacion
            }
            _uiState.value = _uiState.value.copy(
                guardando = false,
                errorPersistencia = resultado is ResultadoBeneficiario.ErrorPersistencia ||
                    resultado == ResultadoBeneficiario.ErrorOperacion
            )
            onResult(resultado)
        }
    }
}
