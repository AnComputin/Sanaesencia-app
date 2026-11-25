package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.repository.CatalogoRepository
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.data.room.EspecialistaEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update

data class CatalogoUiState(
    val cargandoEspecialidades: Boolean = true,
    val cargandoEspecialistas: Boolean = true,
    val errorEspecialidades: Boolean = false,
    val errorEspecialistas: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogoViewModel(
    private val catalogoRepository: CatalogoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    private val reintentosEspecialidades = MutableStateFlow(0)
    private val reintentosEspecialistas = MutableStateFlow(0)

    val especialidadId: StateFlow<Int?> =
        savedStateHandle.getStateFlow(ARG_ESPECIALIDAD_ID, null)

    val especialidades: StateFlow<List<EspecialidadEntity>> =
        reintentosEspecialidades
            .flatMapLatest {
                _uiState.update { it.copy(cargandoEspecialidades = true, errorEspecialidades = false) }
                catalogoRepository.observarEspecialidades()
                    .onEach {
                        _uiState.update { estado ->
                            estado.copy(
                                cargandoEspecialidades = false,
                                errorEspecialidades = false
                            )
                        }
                    }
                    .catch {
                        _uiState.update { estado ->
                            estado.copy(
                                cargandoEspecialidades = false,
                                errorEspecialidades = true
                            )
                        }
                        emit(emptyList())
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val especialistas: StateFlow<List<EspecialistaEntity>> =
        combine(reintentosEspecialistas, especialidadId) { _, id -> id }
            .flatMapLatest { id ->
                if (id == null) {
                    _uiState.update { it.copy(cargandoEspecialistas = false, errorEspecialistas = false) }
                    flowOf(emptyList())
                } else {
                    _uiState.update { it.copy(cargandoEspecialistas = true, errorEspecialistas = false) }
                    catalogoRepository.observarEspecialistas(id)
                        .onEach {
                            _uiState.update { estado ->
                                estado.copy(
                                    cargandoEspecialistas = false,
                                    errorEspecialistas = false
                                )
                            }
                        }
                        .catch {
                            _uiState.update { estado ->
                                estado.copy(
                                    cargandoEspecialistas = false,
                                    errorEspecialistas = true
                                )
                            }
                            emit(emptyList())
                        }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun especialidadSeleccionada(): EspecialidadEntity? =
        especialidades.value.firstOrNull { it.id == especialidadId.value }

    fun reintentarEspecialidades() {
        reintentosEspecialidades.update { it + 1 }
    }

    fun reintentarEspecialistas() {
        reintentosEspecialistas.update { it + 1 }
    }

    private companion object {
        const val ARG_ESPECIALIDAD_ID = "especialidadId"
    }
}
