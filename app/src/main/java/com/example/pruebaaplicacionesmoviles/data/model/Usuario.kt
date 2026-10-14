package com.example.pruebaaplicacionesmoviles.data.model

data class Usuario(
    val id: Int,
    val alias: String,
    val rol: Rol,
    val curso: String? = null,
    val docenteId: Int? = null
)
