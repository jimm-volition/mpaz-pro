package com.example.pruebaaplicacionesmoviles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unidades")
data class UnidadEntity(
    @PrimaryKey val id: Int,
    val asignaturaId: Int,
    val titulo: String,
    val codigoOA: String,
    val descripcionOA: String,
    val ejeCurricular: String,
    val explicacion: String,
    val ejemploGuiado: String,
    val imagenReferencia: String? = null,
    val textoParaAudio: String? = null,
    val estado: String = "BORRADOR",
    val origenMaterial: String = "Propio"
)