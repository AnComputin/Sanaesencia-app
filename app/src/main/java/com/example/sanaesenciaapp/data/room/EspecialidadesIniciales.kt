package com.example.sanaesenciaapp.data.room

/**
 * Fuente única de verdad para los datos iniciales de las especialidades.
 */
val ESPECIALIDADES_INICIALES: List<EspecialidadEntity> = listOf(
    EspecialidadEntity(
        id = 1,
        nombre = "Psicología Infanto-Juvenil",
        descripcion = "Acompañamiento psicológico integral para niños y jóvenes.",
        icono = "🧠",
        profesional = "Mg. Andrea Soto",
        modalidad = "Ambas",
        agendaPausada = false
    ),
    EspecialidadEntity(
        id = 2,
        nombre = "Fonoaudiología",
        descripcion = "Evaluación y tratamiento de trastornos del habla y lenguaje.",
        icono = "🗣️",
        profesional = "Klga. Camila Rojas",
        modalidad = "Presencial",
        agendaPausada = false
    ),
    EspecialidadEntity(
        id = 3,
        nombre = "Terapia Ocupacional",
        descripcion = "Estimulación sensorial y desarrollo de autonomía.",
        icono = "🧩",
        profesional = "TO. Javiera Paz",
        modalidad = "Presencial",
        agendaPausada = true
    ),
    EspecialidadEntity(
        id = 4,
        nombre = "Nutrición Integrativa",
        descripcion = "Planes nutricionales enfocados en el bienestar holístico.",
        icono = "🥗",
        profesional = "Nut. Felipe Morales",
        modalidad = "Ambas",
        agendaPausada = false
    )
)
