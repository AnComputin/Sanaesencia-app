package com.example.sanaesenciaapp.data.repository

object ReglasAgenda {
    const val DURACION_CITA_MINUTOS = 60

    const val TOTAL_BOXES = 8

    const val MODALIDAD_PRESENCIAL = "PRESENCIAL"
    const val MODALIDAD_ONLINE = "ONLINE"

    const val ESTADO_CONFIRMADA = "CONFIRMADA"
    const val ESTADO_COMPLETADA = "COMPLETADA"
    const val ESTADO_CANCELADA = "CANCELADA"

    fun calcularFin(inicioMillis: Long): Long{
        return inicioMillis + (DURACION_CITA_MINUTOS * 60 * 1000L)
    }

    /*VERIFCA SI ES VALIDO SOLO SE PUEDE PRESENCIAL O ONLINE*/
    fun esModalidadValida(modalidad: String): Boolean{
        return modalidad.equals(MODALIDAD_PRESENCIAL, ignoreCase = true) ||
                modalidad.equals(MODALIDAD_ONLINE, ignoreCase = true)
    }

    /*verifica si es presencial*/
    fun esPresencial(modalidad: String): Boolean{
        return modalidad.equals(MODALIDAD_PRESENCIAL, ignoreCase = true)
    }

    fun haySolapamiento(
        inicio1: Long,fin1: Long,
        inicio2: Long,fin2: Long
    ): Boolean{
        return inicio1 < fin2 && inicio2 <fin1
    }

    /*ve si la cita esta en un horario valido*/
    fun esHorarioValido(inicioMillis: Long, finMillis: Long): Boolean{
        return inicioMillis < finMillis
    }

    /*verifica si el box solicitado esta dentro del rango valido(1-8)*/
    fun esBoxValido(box: Int): Boolean{
        return box in 1..TOTAL_BOXES
    }

    /*determina si reserva es valida segun las reglas*/
    fun validarReserva(
        modalidad: String,
        inicioMillis: Long,
        finMillis: Long,
        boxAsignado: Int?
    ): String?{
        if (!esModalidadValida(modalidad)){
            return "Modalidad inválida. Use PRESENCIAL u ONLINE"
        }
        if (!esHorarioValido(inicioMillis,finMillis)){
            return "El horario de inicio debe ser anterior al de fin"
        }

        if (esPresencial(modalidad)){
            if (boxAsignado == null){
                return "Las citas presenciales requieren un box asignado"
            }
            if (!esBoxValido(boxAsignado)){
                return "El box debe estar entre 1 y $TOTAL_BOXES"
            }
        }
        return null
    }

    fun obtenerBoxLibre(boxesOcupados: Set<Int>): Int?{
        return (1..TOTAL_BOXES).firstOrNull { it !in boxesOcupados }
    }
}