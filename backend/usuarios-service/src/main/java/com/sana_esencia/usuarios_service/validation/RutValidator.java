
package com.sana_esencia.usuarios_service.validation;

import java.util.Locale;

public class RutValidator {

    public static String normalizar(String rut) {
        return rut.trim()
                .replace(".", "")
                .replace("-", "")
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
    }

    public static boolean esValido(String rut) {

        if (rut == null || rut.isBlank()) {
            return false;
        }

        String limpio = normalizar(rut);

        if (limpio.length() < 8 || limpio.length() > 9) {
            return false;
        }

        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dvIngresado = limpio.charAt(limpio.length() - 1);

        if (!cuerpo.matches("\\d{7,8}")) {
            return false;
        }

        if (!(dvIngresado >= '0' && dvIngresado <= '9')
                && dvIngresado != 'K') {
            return false;
        }

        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {

            suma += Character.getNumericValue(cuerpo.charAt(i))
                    * multiplicador;

            multiplicador++;

            if (multiplicador > 7) {
                multiplicador = 2;
            }
        }

        int resultado = 11 - (suma % 11);

        char dvCalculado;

        if (resultado == 11) {
            dvCalculado = '0';
        } else if (resultado == 10) {
            dvCalculado = 'K';
        } else {
            dvCalculado = (char) ('0' + resultado);
        }

        return dvIngresado == dvCalculado;
    }
}
