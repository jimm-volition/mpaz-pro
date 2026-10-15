package com.example.pruebaaplicacionesmoviles.data.model

data class Intento(
    val id: Int,
    val asignacionId: Int,
    val actividadId: Int,
    val fechaHora: String,
    val correctas: Int,
    val totalPreguntas: Int,
    val porcentaje: Int
)
