package com.example.pruebaaplicacionesmoviles.data.model

data class Progreso(
    val id: Int,
    val estudianteId: Int,
    val unidadId: Int,
    val contenidoVisto: Boolean = false,
    val actividadCompletada: Boolean = false,
    val porcentaje: Int = 0
)
