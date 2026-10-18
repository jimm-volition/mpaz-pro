package com.example.pruebaaplicacionesmoviles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progresos")
data class ProgresoEntity(
    @PrimaryKey val id: Int,
    val estudianteId: Int,
    val unidadId: Int,
    val contenidoVisto: Boolean = false,
    val actividadCompletada: Boolean = false,
    val porcentaje: Int = 0
)