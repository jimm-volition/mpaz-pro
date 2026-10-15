package com.example.pruebaaplicacionesmoviles.data.model

data class RespuestaEstudiante(
    val id: Int,
    val intentoId: Int,
    val preguntaId: Int,
    val indiceSeleccionado: Int,
    val esCorrecta: Boolean
)
