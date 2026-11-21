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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

data class CatalogoUiState(
    val cargandoEspecialidades: Boolean = true,
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

    val especialidadId: StateFlow<Int?> =
        savedStateHandle.getStateFlow(ARG_ESPECIALIDAD_ID, null)

    val especialidades: StateFlow<List<EspecialidadEntity>> =
        catalogoRepository.observarEspecialidades()
            .onEach {
                _uiState.value = _uiState.value.copy(
                    cargandoEspecialidades = false,
                    errorEspecialidades = false
                )
            }
            .catch {
                _uiState.value = _uiState.value.copy(
                    cargandoEspecialidades = false,
                    errorEspecialidades = true
                )
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val especialistas: StateFlow<List<EspecialistaEntity>> =
        especialidadId
            .flatMapLatest { id ->
                id?.let(catalogoRepository::observarEspecialistas) ?: flowOf(emptyList())
            }
            .onEach { _uiState.value = _uiState.value.copy(errorEspecialistas = false) }
            .catch {
                _uiState.value = _uiState.value.copy(errorEspecialistas = true)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun especialidadSeleccionada(): EspecialidadEntity? =
        especialidades.value.firstOrNull { it.id == especialidadId.value }

    private companion object {
        const val ARG_ESPECIALIDAD_ID = "especialidadId"
    }
}
