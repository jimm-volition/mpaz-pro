package com.example.pruebaaplicacionesmoviles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "respuestas_estudiante")
data class RespuestaEstudianteEntity(
    @PrimaryKey val id: Int,
    val intentoId: Int,
    val preguntaId: Int,
    val indiceSeleccionado: Int,
    val esCorrecta: Boolean
)