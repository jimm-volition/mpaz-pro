package com.example.pruebaaplicacionesmoviles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preguntas")
data class PreguntaEntity(
    @PrimaryKey val id: Int,
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