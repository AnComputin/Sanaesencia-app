package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sanaesenciaapp.utils.validarComuna
import com.example.sanaesenciaapp.utils.validarEmail
import com.example.sanaesenciaapp.utils.validarFechaNacimiento
import com.example.sanaesenciaapp.utils.validarPassword
import com.example.sanaesenciaapp.utils.validarRut
import com.example.sanaesenciaapp.utils.validarTelefono
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Actualizar campos ──────────────────────────

    fun onEmailChange(nuevoEmail: String) {
        _uiState.update { it.copy(email = nuevoEmail, errorEmail = null) }
    }

    fun onRutChange(nuevoRut: String) {
        _uiState.update { it.copy(rut = nuevoRut, errorRut = null) }
    }

    fun onTelefonoChange(nuevoTelefono: String) {
        _uiState.update { it.copy(telefono = nuevoTelefono, errorTelefono = null) }
    }

    fun onPasswordChange(nuevaPassword: String) {
        _uiState.update { it.copy(password = nuevaPassword, errorPassword = null) }
    }

    fun onConfirmPasswordChange(nuevaConfirmacion: String) {
        _uiState.update { it.copy(confirmPassword = nuevaConfirmacion, errorConfirmPassword = null) }
    }

    fun onFechaNacimientoChange(nuevaFecha: String) {
        _uiState.update { it.copy(fechaNacimiento = nuevaFecha, errorFechaNacimiento = null) }
    }

    fun onComunaChange(nuevaComuna: String) {
        _uiState.update { it.copy(comuna = nuevaComuna, errorComuna = null) }
    }

    fun onRolChange(nuevoRol: String) {
        _uiState.update { it.copy(rol = nuevoRol) }
    }

    fun onAceptarTerminosChange(acepta: Boolean) {
        _uiState.update { it.copy(aceptaTerminos = acepta, errorTerminos = null) }
    }

    // ── Visibilidad de contraseñas ──────────────────

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    // ── Validar campo individual ────────────────────

    fun validarCampoEmail() {
        val error = validarEmail(_uiState.value.email)
        _uiState.update { it.copy(errorEmail = error) }
    }

    fun validarCampoRut() {
        val error = validarRut(_uiState.value.rut)
        _uiState.update { it.copy(errorRut = error) }
    }

    fun validarCampoTelefono() {
        val error = validarTelefono(_uiState.value.telefono)
        _uiState.update { it.copy(errorTelefono = error) }
    }

    fun validarCampoPassword() {
        val error = validarPassword(_uiState.value.password)
        _uiState.update { it.copy(errorPassword = error) }
    }

    fun validarCampoConfirmPassword() {
        val state = _uiState.value
        val error = if (state.password != state.confirmPassword) {
            "Las contraseñas no coinciden"
        } else null
        _uiState.update { it.copy(errorConfirmPassword = error) }
    }

    fun validarCampoFechaNacimiento() {
        val error = validarFechaNacimiento(_uiState.value.fechaNacimiento)
        _uiState.update { it.copy(errorFechaNacimiento = error) }
    }

    fun validarCampoComuna() {
        val error = validarComuna(_uiState.value.comuna)
        _uiState.update { it.copy(errorComuna = error) }
    }

    // ── Validar formulario completo ──────────────────

    fun validarRegistro(): Boolean {
        val state = _uiState.value

        val errorEmail = validarEmail(state.email)
        val errorRut = validarRut(state.rut)
        val errorTelefono = validarTelefono(state.telefono)
        val errorPassword = validarPassword(state.password)
        val errorConfirm = if (state.password != state.confirmPassword) {
            "Las contraseñas no coinciden"
        } else null
        val errorFecha = validarFechaNacimiento(state.fechaNacimiento)
        val errorComuna = validarComuna(state.comuna)
        val errorTerminos = if (!state.aceptaTerminos) {
            "Debe aceptar los términos y condiciones"
        } else null

        _uiState.update {
            it.copy(
                errorEmail = errorEmail,
                errorRut = errorRut,
                errorTelefono = errorTelefono,
                errorPassword = errorPassword,
                errorConfirmPassword = errorConfirm,
                errorFechaNacimiento = errorFecha,
                errorComuna = errorComuna,
                errorTerminos = errorTerminos
            )
        }

        return listOf(
            errorEmail, errorRut, errorTelefono,
            errorPassword, errorConfirm, errorFecha,
            errorComuna, errorTerminos
        ).all { it == null }
    }
}
