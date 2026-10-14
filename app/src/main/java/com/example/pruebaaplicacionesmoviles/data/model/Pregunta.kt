package com.example.pruebaaplicacionesmoviles.data.model

data class Pregunta(
    val id: Int,
    val actividadId: Int,
    val enunciado: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val opcionD: String,
    val indiceCorrecto: Int,
    val explicacionRespuesta: String,
    val orden: Int
)
