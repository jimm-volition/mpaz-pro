package com.example.pruebaaplicacionesmoviles.data.model

data class Actividad(
    val id: Int,
    val unidadId: Int,
    val titulo: String,
    val tipo: TipoActividad,
    val orden: Int
)
