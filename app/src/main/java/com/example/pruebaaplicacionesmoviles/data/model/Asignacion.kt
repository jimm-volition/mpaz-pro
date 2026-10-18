package com.example.pruebaaplicacionesmoviles.data.model

data class Asignacion(
    val id: Int,
    val docenteId: Int,
    val estudianteId: Int,
    val unidadId: Int,
    val fechaInicio: String,
    val fechaFin: String,
    val maxIntentos: Int
)
