package com.example.sanaesenciaapp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val EMAIL_REGEX =
    Regex(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

fun validarEmail(email: String): String?{
    if (email.isBlank()) return "El correo es obligatorio"
    if (!EMAIL_REGEX.matches(email.trim())) return "Formato de correo inválido (ej: nombre@gmail.cl)"
    return null
}

//rut

fun validarRut(rut: String): String? {

    // Eliminar puntos y guion para validar el RUT.
    val limpio = rut.trim()
        .replace(".", "")
        .replace("-", "")
        .uppercase()

    if (limpio.isBlank()) {
        return "El RUT es obligatorio"
    }

    // Un RUT puede tener 7 u 8 dígitos en el cuerpo,
    // más el dígito verificador.
    if (limpio.length !in 8..9) {
        return "RUT inválido. Revisa que esté correctamente escrito"
    }

    val cuerpo = limpio.dropLast(1)
    val dvEsperado = limpio.last()

    // El cuerpo debe contener solamente números.
    if (cuerpo.length !in 7..8 || !cuerpo.all { it in '0'..'9' }) {
        return "RUT inválido. Revisa que esté correctamente escrito"
    }

    // El dígito verificador debe ser un número o K.
    if (dvEsperado !in '0'..'9' && dvEsperado != 'K') {
        return "RUT inválido. Revisa que esté correctamente escrito"
    }

    // Cálculo del dígito verificador mediante módulo 11.
    var suma = 0
    var multiplicador = 2

    for (i in cuerpo.indices.reversed()) {
        suma += (cuerpo[i] - '0') * multiplicador

        multiplicador = if (multiplicador == 7) {
            2
        } else {
            multiplicador + 1
        }
    }

    val resto = suma % 11

    val dvCalculado = when (11 - resto) {
        11 -> '0'
        10 -> 'K'
        else -> ('0'.code + (11 - resto)).toChar()
    }

    if (dvEsperado != dvCalculado) {
        return "RUT inválido. Revisa que esté correctamente escrito"
    }

    return null
}


//telefono
fun validarTelefono(telefono: String): String? {
    val limpio = telefono.trim().replace(" ","").replace("+","")
    val sinPrefijo = when{
        limpio.startsWith("569") -> limpio.drop(3)
        limpio.startsWith("9") -> limpio.drop(1)
        else ->return "Formato inválido. Use +56 9 XXXXXXXX o 9XXXXXXXX"
    }

    if (sinPrefijo.length !=8)return "Debe tener 8 dígitos después del 9"
    if (!sinPrefijo.all { it.isDigit() }) return "Solo se permiten números"
    return null
}

//contraseña
fun validarPassword(password: String): String? {
    if (password.length <8) return "La contraseña debe tener al menos 8 caracteres"
    if (!password.any { it.isUpperCase() }) return "Debe contener al menos una letra mayúscula"
    if (!password.any { it.isDigit() }) return "Debe contener al menos un número"
    if (!password.any { !it.isLetterOrDigit() }) return "Debe contener al menos un carácter especial"
    return null
}

//fecha de nacimiento
fun validarFechaNacimiento(fecha: String): String?{
    if (fecha.isBlank()) return "La fecha de nacimiento es obligatoria"

    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formato.isLenient = false

    val fechaParseada = try {
        formato.parse(fecha.trim())
    }catch (e: Exception){
        return "Formato inválido. Utilice DD/MM/AAAA"
    }

    val hoy = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    if (fechaParseada.after(hoy.time)) return "la fecha debe ser anterior al día de hoy"
    return null
}

//comuna
fun validarComuna(comuna: String): String?{
    if (comuna.isBlank()) return "Debe seleccionar o ingresar una comuna"
    return null
}

//nombre
fun validarNombre(nombre: String): String? {
    val limpio = nombre.trim()
    if (limpio.isEmpty()) return "El nombre es obligatorio"
    if (limpio.length < 3) return "El nombre debe tener al menos 3 caracteres"
    if (!limpio.all { it.isLetter() || it == ' ' }) return "El nombre solo debe contener letras"
    return null
}

//parentesco
fun validarParentesco(parentesco: String): String? {
    val limpio = parentesco.trim()
    if (limpio.isEmpty()) return "Debe indicar el parentesco"
    if (limpio.length < 3) return "El parentesco debe tener al menos 3 caracteres"
    return null
}