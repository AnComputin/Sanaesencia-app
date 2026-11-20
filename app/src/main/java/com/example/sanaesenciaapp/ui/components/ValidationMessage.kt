package com.example.sanaesenciaapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R

/**
 * Localizes the legacy String? validation result at the UI boundary.
 *
 * The validation API remains unchanged for existing callers and tests. Messages not produced by
 * Validaciones are returned verbatim, so repository and server errors retain their current text.
 */
@Composable
fun validationMessageText(message: String?): String {
    if (message == null) return ""

    val rutCheckDigitPrefix = "Dígito verificador incorrecto. Debería ser "
    if (message.startsWith(rutCheckDigitPrefix)) {
        return stringResource(
            R.string.validation_rut_check_digit,
            message.removePrefix(rutCheckDigitPrefix)
        )
    }

    return when (message) {
        "El correo es obligatorio" -> stringResource(R.string.validation_email_required)
        "Formato de correo inválido (ej: nombre@gmail.cl)" ->
            stringResource(R.string.validation_email_format)
        "RUT inválido: debe tener al menos 8 caracteres" ->
            stringResource(R.string.validation_rut_too_short)
        "El RUT solo debe contener números" ->
            stringResource(R.string.validation_rut_digits_only)
        "Formato inválido. Use +56 9 XXXXXXXX o 9XXXXXXXX" ->
            stringResource(R.string.validation_phone_format)
        "Debe tener 8 dígitos después del 9" ->
            stringResource(R.string.validation_phone_length)
        "Solo se permiten números" ->
            stringResource(R.string.validation_phone_digits_only)
        "La contraseña debe tener al menos 8 caracteres" ->
            stringResource(R.string.validation_password_length)
        "Debe contener al menos una letra mayúscula" ->
            stringResource(R.string.validation_password_uppercase)
        "Debe contener al menos un número" ->
            stringResource(R.string.validation_password_digit)
        "Debe contener al menos un carácter especial" ->
            stringResource(R.string.validation_password_special)
        "La fecha de nacimiento es obligatoria" ->
            stringResource(R.string.validation_birth_date_required)
        "Formato inválido. Utilice DD/MM/AAAA" ->
            stringResource(R.string.validation_birth_date_format)
        "la fecha debe ser anterior al día de hoy" ->
            stringResource(R.string.validation_birth_date_past)
        "Debe seleccionar o ingresar una comuna" ->
            stringResource(R.string.validation_city_required)
        "El nombre es obligatorio" -> stringResource(R.string.validation_name_required)
        "El nombre debe tener al menos 3 caracteres" ->
            stringResource(R.string.validation_name_min_length)
        "El nombre solo debe contener letras" ->
            stringResource(R.string.validation_name_letters_only)
        "Debe indicar el parentesco" ->
            stringResource(R.string.validation_relationship_required)
        "El parentesco debe tener al menos 3 caracteres" ->
            stringResource(R.string.validation_relationship_min_length)
        else -> message
    }
}
