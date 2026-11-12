
package com.example.sanaesenciaapp.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class RutVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val rut = text.text.uppercase()

        // Cuando tiene 9 caracteres, el último corresponde al dígito verificador.
        val rutCompleto = rut.length == 9

        val cuerpo = if (rutCompleto) {
            rut.dropLast(1)
        } else {
            rut
        }

        val digitoVerificador = if (rutCompleto) {
            rut.last().toString()
        } else {
            ""
        }

        val cuerpoFormateado = formatearCuerpo(cuerpo)

        val resultado = if (rutCompleto) {
            "$cuerpoFormateado-$digitoVerificador"
        } else {
            cuerpoFormateado
        }

        fun mapearOffsetCuerpo(offset: Int): Int {
            val limite = offset.coerceIn(0, cuerpo.length)

            var caracteresOriginales = 0
            var posicionTransformada = 0

            while (
                posicionTransformada < cuerpoFormateado.length &&
                caracteresOriginales < limite
            ) {
                if (cuerpoFormateado[posicionTransformada] != '.') {
                    caracteresOriginales++
                }

                posicionTransformada++
            }

            return posicionTransformada
        }

        val offsetMapping = object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {
                val offsetSeguro = offset.coerceIn(0, rut.length)

                return when {
                    rutCompleto && offsetSeguro == rut.length -> {
                        resultado.length
                    }

                    rutCompleto && offsetSeguro == cuerpo.length -> {
                        cuerpoFormateado.length + 1
                    }

                    else -> {
                        mapearOffsetCuerpo(offsetSeguro)
                    }
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val offsetSeguro = offset.coerceIn(0, resultado.length)

                return resultado
                    .take(offsetSeguro)
                    .count { it != '.' && it != '-' }
                    .coerceIn(0, rut.length)
            }
        }

        return TransformedText(
            AnnotatedString(resultado),
            offsetMapping
        )
    }

    private fun formatearCuerpo(cuerpo: String): String {
        return buildString {
            cuerpo.forEachIndexed { index, caracter ->
                append(caracter)

                val caracteresRestantes = cuerpo.length - index - 1

                if (
                    caracteresRestantes > 0 &&
                    caracteresRestantes % 3 == 0
                ) {
                    append('.')
                }
            }
        }
    }
}
