package com.example.sanaesenciaapp.data.room

/**
 * Fuente única de verdad para los datos iniciales de las especialidades.
 */
val ESPECIALIDADES_INICIALES: List<EspecialidadEntity> = listOf(
    EspecialidadEntity(
        id = 1,
        nombre = "Psicología Infanto-Juvenil",
        descripcion = "Acompañamiento psicológico integral para niños y jóvenes.",
        icono = "🌱",
        profesional = "5 Especialistas",
        modalidad = "Ambas",
        agendaPausada = false
    ),
    EspecialidadEntity(
        id = 2,
        nombre = "Psicología Adulto",
        descripcion = "Atención psicoterapéutica especializada para adultos.",
        icono = "🧠",
        profesional = "5 Especialistas",
        modalidad = "Ambas",
        agendaPausada = false
    ),
    EspecialidadEntity(
        id = 3,
        nombre = "Terapia Ocupacional",
        descripcion = "Estimulación sensorial y desarrollo de autonomía.",
        icono = "🧩",
        profesional = "4 Especialistas",
        modalidad = "Ambas",
        agendaPausada = false
    ),
    EspecialidadEntity(
        id = 4,
        nombre = "Nutrición Integrativa",
        descripcion = "Planes nutricionales enfocados en el bienestar holístico.",
        icono = "🥗",
        profesional = "4 Especialistas",
        modalidad = "Ambas",
        agendaPausada = false
    ),
    EspecialidadEntity(
        id = 5,
        nombre = "Terapia Integrativa",
        descripcion = "Tratamientos holísticos para el equilibrio cuerpo-mente.",
        icono = "🌿",
        profesional = "4 Especialistas",
        modalidad = "Ambas",
        agendaPausada = false
    ),
    EspecialidadEntity(
        id = 6,
        nombre = "Acupuntura",
        descripcion = "Medicina tradicional china para alivio de dolores y estrés.",
        icono = "🪡",
        profesional = "4 Especialistas",
        modalidad = "Presencial",
        agendaPausada = false
    ),
    EspecialidadEntity(
        id = 7,
        nombre = "Quiropraxia",
        descripcion = "Alineación vertebral y salud del sistema neuromusculoesquelético.",
        icono = "🦴",
        profesional = "4 Especialistas",
        modalidad = "Presencial",
        agendaPausada = false
    )
)

/**
 * Fuente única de verdad para los 30 especialistas distribuidos en las 7 especialidades.
 */
val ESPECIALISTAS_INICIALES: List<EspecialistaEntity> = listOf(
    // 1. Psicología Infanto-Juvenil (5)
    EspecialistaEntity(1, "Psic. Andrea Soto", 1, "Ambas"),
    EspecialistaEntity(2, "Psic. Felipe Morales", 1, "Online"),
    EspecialistaEntity(3, "Psic. Francisca Araya", 1, "Ambas"),
    EspecialistaEntity(4, "Psic. Esteban Paredes", 1, "Presencial"),
    EspecialistaEntity(5, "Psic. Lucía Fernández", 1, "Ambas"),

    // 2. Psicología Adulto (5)
    EspecialistaEntity(6, "Psic. Roberto Gómez", 2, "Ambas"),
    EspecialistaEntity(7, "Psic. María José Silva", 2, "Online"),
    EspecialistaEntity(8, "Psic. Andrés Fuentes", 2, "Ambas"),
    EspecialistaEntity(9, "Psic. Carla Morales", 2, "Presencial"),
    EspecialistaEntity(10, "Psic. Diego Castro", 2, "Ambas"),

    // 3. Terapia Ocupacional (4)
    EspecialistaEntity(11, "TO. Javiera Paz", 3, "Ambas"),
    EspecialistaEntity(12, "TO. Camila Rojas", 3, "Presencial"),
    EspecialistaEntity(13, "TO. Gonzalo Vera", 3, "Ambas"),
    EspecialistaEntity(14, "TO. Valentina Soto", 3, "Presencial"),

    // 4. Nutrición Integrativa (4)
    EspecialistaEntity(15, "Nut. Felipe Morales", 4, "Ambas"),
    EspecialistaEntity(16, "Nut. Paula Valenzuela", 4, "Online"),
    EspecialistaEntity(17, "Nut. Hernán Saavedra", 4, "Ambas"),
    EspecialistaEntity(18, "Nut. Constanza Pizarro", 4, "Ambas"),

    // 5. Terapia Integrativa (4)
    EspecialistaEntity(19, "Esp. Claudia Parra", 5, "Ambas"),
    EspecialistaEntity(20, "Esp. Gabriel Espinoza", 5, "Presencial"),
    EspecialistaEntity(21, "Esp. Sofía Loyola", 5, "Ambas"),
    EspecialistaEntity(22, "Esp. Tomás Reyes", 5, "Online"),

    // 6. Acupuntura (4 - Presencial únicamente)
    EspecialistaEntity(23, "Dr. Chen Wei", 6, "Presencial"),
    EspecialistaEntity(24, "Acup. Beatriz Mendoza", 6, "Presencial"),
    EspecialistaEntity(25, "Acup. Matías Navarrete", 6, "Presencial"),
    EspecialistaEntity(26, "Acup. Daniela Godoy", 6, "Presencial"),

    // 7. Quiropraxia (4 - Presencial únicamente)
    EspecialistaEntity(27, "Quiro. Rodrigo Tapia", 7, "Presencial"),
    EspecialistaEntity(28, "Quiro. Nicolás Sepúlveda", 7, "Presencial"),
    EspecialistaEntity(29, "Quiro. María Ignacia Bravo", 7, "Presencial"),
    EspecialistaEntity(30, "Quiro. Sebastián Salinas", 7, "Presencial")
)
