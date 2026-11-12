package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.repository.CatalogoRepository
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.data.room.EspecialistaEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogoViewModel(
    private val catalogoRepository: CatalogoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val especialidadId: StateFlow<Int?> =
        savedStateHandle.getStateFlow(ARG_ESPECIALIDAD_ID, null)

    val especialidades: StateFlow<List<EspecialidadEntity>> =
        catalogoRepository.observarEspecialidades()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val especialistas: StateFlow<List<EspecialistaEntity>> =
        especialidadId
            .flatMapLatest { id ->
                id?.let(catalogoRepository::observarEspecialistas) ?: flowOf(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private companion object {
        const val ARG_ESPECIALIDAD_ID = "especialidadId"
    }
}
