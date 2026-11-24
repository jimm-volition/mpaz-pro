
package com.example.pruebaaplicacionesmoviles.data.model

data class MaterialDidactico(
    val id: Int,
    val titulo: String,
    val explicacion: String,
    val ejemplo: String,
    val instrucciones: String,
    val imagenes: List<String>,
    val audio: String?
)
