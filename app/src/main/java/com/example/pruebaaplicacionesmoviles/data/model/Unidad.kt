package com.example.pruebaaplicacionesmoviles.data.model

data class Unidad(
    val id: Int,
    val asignaturaId: Int,
    val titulo: String,
    val codigoOA: String,
    val descripcionOA: String,
    val ejeCurricular: String,
    val explicacion: String,
    val ejemploGuiado: String,
    val imagenReferencia: String? = null,
    val textoParaAudio: String? = null,
    val estado: EstadoUnidad = EstadoUnidad.BORRADOR,
    val origenMaterial: String = "Propio"
)
