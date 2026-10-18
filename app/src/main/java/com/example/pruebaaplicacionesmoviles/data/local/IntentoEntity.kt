package com.example.pruebaaplicacionesmoviles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intentos")
data class IntentoEntity(
    @PrimaryKey val id: Int,
    val asignacionId: Int,
    val actividadId: Int,
    val fechaHora: String,
    val correctas: Int,
    val totalPreguntas: Int,
    val porcentaje: Int
)