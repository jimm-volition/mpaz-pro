package com.example.pruebaaplicacionesmoviles.data.local

object DatosSemilla {

    val usuarios = listOf(
        UsuarioEntity(1, "UTP01", "UTP"),
        UsuarioEntity(2, "DOC01", "DOCENTE"),
        UsuarioEntity(3, "EST01", "ESTUDIANTE", "2° Básico A", 2)
    )

    val asignaturas = listOf(
        AsignaturaEntity(1, "Lenguaje y Comunicación"),
        AsignaturaEntity(2, "Matemática"),
        AsignaturaEntity(3, "Ciencias Naturales"),
        AsignaturaEntity(4, "Historia, Geografía y Ciencias Sociales")
    )

    val unidades = listOf(
        UnidadEntity(
            id = 1,
            asignaturaId = 1,
            titulo = "Ideas principales en un texto corto",
            codigoOA = "OA1",
            descripcionOA = "Localizar información explícita en textos.",
            ejeCurricular = "Lectura",
            explicacion = "La idea principal resume de qué trata el texto.",
            ejemploGuiado = "Si el texto habla de un perro que juega en el parque, la idea principal es ese juego.",
            textoParaAudio = "La idea principal resume de qué trata el texto.",
            estado = "PUBLICADO",
            origenMaterial = "Propio"
        ),
        UnidadEntity(
            id = 2,
            asignaturaId = 2,
            titulo = "Sumar hasta 20",
            codigoOA = "OA2",
            descripcionOA = "Resolver adiciones con números hasta 20.",
            ejeCurricular = "Números",
            explicacion = "Para sumar puedes contar hacia adelante desde el primer número.",
            ejemploGuiado = "8 + 5: parte en 8 y cuenta 5 más: 9, 10, 11, 12, 13.",
            textoParaAudio = "Para sumar puedes contar hacia adelante.",
            estado = "AUTORIZADO",
            origenMaterial = "Propio"
        ),
        UnidadEntity(
            id = 3,
            asignaturaId = 3,
            titulo = "Partes de una planta",
            codigoOA = "OA3",
            descripcionOA = "Identificar raíz, tallo, hojas y flor.",
            ejeCurricular = "Ciencias de la vida",
            explicacion = "Cada parte de la planta cumple una función.",
            ejemploGuiado = "La raíz absorbe agua; las hojas ayudan a fabricar alimento.",
            textoParaAudio = "Cada parte de la planta cumple una función.",
            estado = "EN_REVISION",
            origenMaterial = "Propio"
        ),
        UnidadEntity(
            id = 4,
            asignaturaId = 4,
            titulo = "Mi comunidad",
            codigoOA = "OA4",
            descripcionOA = "Reconocer lugares y personas de la comunidad.",
            ejeCurricular = "Geografía",
            explicacion = "En la comunidad hay lugares como la escuela, la plaza y el consultorio.",
            ejemploGuiado = "El cartero y la profesora son personas de la comunidad.",
            textoParaAudio = "En la comunidad hay lugares y personas importantes.",
            estado = "BORRADOR",
            origenMaterial = "Propio"
        )
    )

    val actividades = listOf(
        ActividadEntity(1, 1, "Práctica de idea principal", "PRACTICA", 1),
        ActividadEntity(2, 1, "Prueba final de idea principal", "PRUEBA_FINAL", 2),
        ActividadEntity(3, 2, "Práctica de sumas", "PRACTICA", 1),
        ActividadEntity(4, 2, "Prueba final de sumas", "PRUEBA_FINAL", 2)
    )

    val preguntas = listOf(
        PreguntaEntity(
            id = 1,
            actividadId = 1,
            enunciado = "¿Qué es la idea principal?",
            opcionA = "Un detalle del texto",
            opcionB = "De qué trata el texto en general",
            opcionC = "El título solo",
            opcionD = "Una palabra difícil",
            indiceCorrecto = 1,
            explicacionRespuesta = "La idea principal resume de qué trata el texto.",
            orden = 1
        ),
        PreguntaEntity(
            id = 2,
            actividadId = 2,
            enunciado = "Un texto dice: 'Ana riega las plantas cada mañana'. ¿Idea principal?",
            opcionA = "Ana tiene un jardín grande",
            opcionB = "Ana riega las plantas por la mañana",
            opcionC = "Las plantas son verdes",
            opcionD = "La mañana es temprano",
            indiceCorrecto = 1,
            explicacionRespuesta = "El texto habla de que Ana riega las plantas cada mañana.",
            orden = 1
        ),
        PreguntaEntity(
            id = 3,
            actividadId = 2,
            enunciado = "Si el texto habla de un gato que duerme en el sofá, la idea principal es:",
            opcionA = "El color del sofá",
            opcionB = "Que el gato duerme en el sofá",
            opcionC = "Que hay varios animales",
            opcionD = "La hora exacta",
            indiceCorrecto = 1,
            explicacionRespuesta = "La idea principal es que el gato duerme en el sofá.",
            orden = 2
        ),
        PreguntaEntity(
            id = 4,
            actividadId = 3,
            enunciado = "¿Cuánto es 7 + 4?",
            opcionA = "10",
            opcionB = "11",
            opcionC = "12",
            opcionD = "9",
            indiceCorrecto = 1,
            explicacionRespuesta = "7 + 4 = 11.",
            orden = 1
        ),
        PreguntaEntity(
            id = 5,
            actividadId = 4,
            enunciado = "¿Cuánto es 9 + 6?",
            opcionA = "14",
            opcionB = "15",
            opcionC = "16",
            opcionD = "13",
            indiceCorrecto = 1,
            explicacionRespuesta = "9 + 6 = 15.",
            orden = 1
        )
    )

    // Una asignación de ejemplo para que el estudiante ya vea la unidad 1
    val asignaciones = listOf(
        AsignacionEntity(
            id = 1,
            docenteId = 2,
            estudianteId = 3,
            unidadId = 1,
            fechaInicio = "2026-10-01",
            fechaFin = "2026-12-15",
            maxIntentos = 2
        )
    )
}