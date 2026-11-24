package com.example.pruebaaplicacionesmoviles.data.repository

import com.example.pruebaaplicacionesmoviles.core.network.UtpContenidoApiCliente
import com.google.gson.JsonObject

class EstadoMaterialRepository {

    private val api = UtpContenidoApiCliente.servicio

    suspend fun cambiarEstado(
        materialId: Int,
        estadoNuevo: String
    ) {
        val datos = JsonObject().apply {
            addProperty("estadoNuevo", estadoNuevo)
        }

        val respuesta = api.transicionarContenido(
            materialId,
            datos
        )

        if (!respuesta.isSuccessful) {
            throw IllegalStateException(
                "Error HTTP ${respuesta.code()}"
            )
        }
    }
}
