package com.example.sanaesenciaapp.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {

    // ════════════════════════════════════════════
    // VALIDAR EMAIL
    // ════════════════════════════════════════════

    @Test
    fun `email valido retorna null`() {
        assertNull(validarEmail("valentina@gmail.com"))
    }

    @Test
    fun `email vacio retorna mensaje de error`() {
        val resultado = validarEmail("")
        assertEquals("El correo es obligatorio", resultado)
    }

    @Test
    fun `email sin arroba retorna mensaje de error`() {
        val resultado = validarEmail("valentina")
        assertEquals("Formato de correo inválido (ej: nombre@gmail.cl)", resultado)
    }

    @Test
    fun `email sin dominio retorna mensaje de error`() {
        val resultado = validarEmail("valentina@")
        assertEquals("Formato de correo inválido (ej: nombre@gmail.cl)", resultado)
    }

    @Test
    fun `email sin extension retorna mensaje de error`() {
        val resultado = validarEmail("valentina@gmail")
        assertEquals("Formato de correo inválido (ej: nombre@gmail.cl)", resultado)
    }

    // ════════════════════════════════════════════
    // VALIDAR RUT
    // ════════════════════════════════════════════

    @Test
    fun `rut valido con guion y puntos retorna null`() {
        assertNull(validarRut("12.345.678-5"))
    }

    @Test
    fun `rut valido sin formato retorna null`() {
        assertNull(validarRut("12345678-5"))
    }

    @Test
    fun `rut con digito verificador incorrecto retorna error`() {
        val resultado = validarRut("12.345.678-3")
        assertEquals("Dígito verificador incorrecto. Debería ser 5", resultado)
    }

    @Test
    fun `rut muy corto retorna error`() {
        val resultado = validarRut("123456")
        assertEquals("RUT inválido: debe tener al menos 8 caracteres", resultado)
    }

    @Test
    fun `rut con letras en el cuerpo retorna error`() {
        val resultado = validarRut("1234567A-5")
        assertEquals("El RUT solo debe contener números", resultado)
    }

    @Test
    fun `rut con digito verificador K retorna null`() {
        assertNull(validarRut("8.888.888-K"))
    }

    // ════════════════════════════════════════════
    // VALIDAR TELÉFONO
    // ════════════════════════════════════════════

    @Test
    fun `telefono con prefijo +56 9 retorna null`() {
        assertNull(validarTelefono("+56 9 87654321"))
    }

    @Test
    fun `telefono solo con 9 inicial retorna null`() {
        assertNull(validarTelefono("987654321"))
    }

    @Test
    fun `telefono sin 9 inicial retorna error`() {
        val resultado = validarTelefono("87654321")
        assertEquals("Formato inválido. Use +56 9 XXXXXXXX o 9XXXXXXXX", resultado)
    }

    @Test
    fun `telefono con menos de 8 digitos retorna error`() {
        val resultado = validarTelefono("9876543")
        assertEquals("Debe tener 8 dígitos después del 9", resultado)
    }

    @Test
    fun `telefono con letras retorna error`() {
        val resultado = validarTelefono("9ABCDEFGH")
        assertEquals("Solo se permiten números", resultado)
    }

    // ════════════════════════════════════════════
    // VALIDAR CONTRASEÑA
    // ════════════════════════════════════════════

    @Test
    fun `password valida retorna null`() {
        assertNull(validarPassword("Pass123!"))
    }

    @Test
    fun `password muy corta retorna error`() {
        val resultado = validarPassword("Ab1!")
        assertEquals("La contraseña debe tener al menos 8 caracteres", resultado)
    }

    @Test
    fun `password sin mayuscula retorna error`() {
        val resultado = validarPassword("pass123!")
        assertEquals("Debe contener al menos una letra mayúscula", resultado)
    }

    @Test
    fun `password sin numero retorna error`() {
        val resultado = validarPassword("Password!")
        assertEquals("Debe contener al menos un número", resultado)
    }

    @Test
    fun `password sin caracter especial retorna error`() {
        val resultado = validarPassword("Password1")
        assertEquals("Debe contener al menos un carácter especial", resultado)
    }

    // ════════════════════════════════════════════
    // VALIDAR FECHA DE NACIMIENTO
    // ════════════════════════════════════════════

    @Test
    fun `fecha valida en el pasado retorna null`() {
        assertNull(validarFechaNacimiento("15/06/2000"))
    }

    @Test
    fun `fecha vacia retorna error`() {
        val resultado = validarFechaNacimiento("")
        assertEquals("La fecha de nacimiento es obligatoria", resultado)
    }

    @Test
    fun `fecha con formato incorrecto retorna error`() {
        val resultado = validarFechaNacimiento("2000-06-15")
        assertEquals("Formato inválido. Utilice DD/MM/AAAA", resultado)
    }

    @Test
    fun `fecha en el futuro retorna error`() {
        val resultado = validarFechaNacimiento("15/06/2030")
        assertEquals("la fecha debe ser anterior al día de hoy", resultado)
    }

    @Test
    fun `fecha invalida como 32 de enero retorna error`() {
        val resultado = validarFechaNacimiento("32/01/2000")
        assertEquals("Formato inválido. Utilice DD/MM/AAAA", resultado)
    }

    // ════════════════════════════════════════════
    // VALIDAR COMUNA
    // ════════════════════════════════════════════

    @Test
    fun `comuna no vacia retorna null`() {
        assertNull(validarComuna("Los Vilos"))
    }

    @Test
    fun `comuna vacia retorna error`() {
        val resultado = validarComuna("")
        assertEquals("Debe seleccionar o ingresar una comuna", resultado)
    }

    @Test
    fun `comuna con solo espacios retorna error`() {
        val resultado = validarComuna("   ")
        assertEquals("Debe seleccionar o ingresar una comuna", resultado)
    }

    // ════════════════════════════════════════════
    // VALIDAR NOMBRE
    // ════════════════════════════════════════════

    @Test
    fun `nombre valido retorna null`() {
        assertNull(validarNombre("Valentina Guerrero"))
    }

    @Test
    fun `nombre vacio retorna error`() {
        val resultado = validarNombre("")
        assertEquals("El nombre es obligatorio", resultado)
    }

    @Test
    fun `nombre muy corto retorna error`() {
        val resultado = validarNombre("Ab")
        assertEquals("El nombre debe tener al menos 3 caracteres", resultado)
    }

    @Test
    fun `nombre con numeros retorna error`() {
        val resultado = validarNombre("Juan123")
        assertEquals("El nombre solo debe contener letras", resultado)
    }

    // ════════════════════════════════════════════
    // VALIDAR PARENTESCO
    // ════════════════════════════════════════════

    @Test
    fun `parentesco valido retorna null`() {
        assertNull(validarParentesco("Hijo/a"))
    }

    @Test
    fun `parentesco vacio retorna error`() {
        val resultado = validarParentesco("")
        assertEquals("Debe indicar el parentesco", resultado)
    }

    @Test
    fun `parentesco muy corto retorna error`() {
        val resultado = validarParentesco("Ab")
        assertEquals("El parentesco debe tener al menos 3 caracteres", resultado)
    }
}
