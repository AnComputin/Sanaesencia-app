package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.repository.CitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Estado del proceso de pago.
 *
 * NOTA: La duración del pago y las reglas de expiración quedan
 * pendientes de aprobación del equipo. No se implementa expiración
 * automática ni cancelación de reservas por temporizador.
 */
data class PagoUiState(
    val citaId: Int = 0,
    val especialidadNombre: String = "",
    val especialistaNombre: String = "",
    val modalidad: String = "",
    val fechaHora: String = "",
    val nombrePaciente: String = "",
    val totalAPagar: Int = 35000,
    val isLoading: Boolean = false,
    val pagoExitoso: Boolean = false,
    val errorGeneral: String? = null
)

/**
 * ViewModel para el proceso de pago.
 * No implementa expiración automática ni cancelación por temporizador.
 */
class PagoViewModel(
    private val citaRepository: CitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    /**
     * Procesa el pago.
     * No valida expiración de tiempo — esa regla no está aprobada.
     */
    fun procesarPago() {
        val state = _uiState.value

        // Prevenir doble envío
        if (state.isLoading || state.pagoExitoso) return

        _uiState.update { it.copy(isLoading = true, errorGeneral = null) }

        viewModelScope.launch {
            try {
                // Simular procesamiento de pago
                kotlinx.coroutines.delay(1500)

                _uiState.update {
                    it.copy(isLoading = false, pagoExitoso = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorGeneral = "Error al procesar el pago")
                }
            }
        }
    }

    /**
     * Carga los datos de la cita para mostrar en el resumen.
     */
    fun cargarDatosCita(
        citaId: Int,
        especialidad: String,
        especialista: String,
        modalidad: String,
        fechaHora: String,
        paciente: String
    ) {
        _uiState.update {
            it.copy(
                citaId = citaId,
                especialidadNombre = especialidad,
                especialistaNombre = especialista,
                modalidad = modalidad,
                fechaHora = fechaHora,
                nombrePaciente = paciente
            )
        }
    }

    /**
     * Limpia el error general.
     */
    fun limpiarError() {
        _uiState.update { it.copy(errorGeneral = null) }
    }

    // ── Compatibilidad con PagoScreen ──────────────

    val puedeContinuar: StateFlow<Boolean> = MutableStateFlow(true)

    val duracionPagoSegundos: Int = 0  // Sin temporizador aprobado

    fun iniciarPago() {
        // No hay temporizador aprobado, solo carga datos
    }

    fun segundosRestantesPago(): Int = 0  // Sin temporizador

    fun formatearFecha(millis: Long): String {
        val formato = SimpleDateFormat("EEE d MMM", Locale("es", "CL"))
        return formato.format(Date(millis))
    }

    fun confirmarCita(usuarioEmail: String, onResultado: (Boolean) -> Unit) {
        procesarPago()
        viewModelScope.launch {
            kotlinx.coroutines.delay(1600)
            onResultado(_uiState.value.pagoExitoso)
        }
    }
}
