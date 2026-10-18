package com.example.pruebaaplicacionesmoviles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actividades")
data class ActividadEntity(
    @PrimaryKey val id: Int,
    val unidadId: Int,
    val titulo: String,
    val tipo: String,
    val orden: Int
)