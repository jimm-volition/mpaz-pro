package com.example.pruebaaplicacionesmoviles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asignaciones")
data class AsignacionEntity(
    @PrimaryKey val id: Int,
    val docenteId: Int,
    val estudianteId: Int,
    val unidadId: Int,
    val fechaInicio: String,
    val fechaFin: String,
    val maxIntentos: Int,
    val activa: Boolean = true
)