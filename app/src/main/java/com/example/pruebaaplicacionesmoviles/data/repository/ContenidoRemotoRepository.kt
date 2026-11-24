
package com.example.pruebaaplicacionesmoviles.data.repository

import android.util.Log
import com.example.pruebaaplicacionesmoviles.core.network.ContenidoApiCliente
import com.example.pruebaaplicacionesmoviles.data.model.Asignatura
import com.example.pruebaaplicacionesmoviles.data.model.EstadoUnidad
import com.example.pruebaaplicacionesmoviles.data.model.Unidad
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.example.pruebaaplicacionesmoviles.data.model.MaterialDidactico

class ContenidoRemotoRepository {

    private val api = ContenidoApiCliente.servicio

    suspend fun obtenerAsignaturas(): List<Asignatura> {
        val respuesta = api.listarAsignaturas()

        return respuesta.obtenerLista().map { elemento ->
            val dato = elemento.asJsonObject

            Asignatura(
                id = dato.get("id").asInt,
                nombre = dato.get("nombre").asString
            )
        }
    }

    suspend fun obtenerUnidades(
        asignaturas: List<Asignatura>
    ): List<Unidad> {

        val unidades = mutableListOf<Unidad>()

        for (asignatura in asignaturas) {

            val respuesta = api.listarUnidades(asignatura.id)

            for (elemento in respuesta.obtenerLista()) {

                val dato = elemento.asJsonObject
                val unidadId = dato.get("id").asInt

                val objetivo = try {
                    api.listarObjetivos(unidadId)
                        .obtenerLista()
                        .firstOrNull()
                        ?.asJsonObject
                } catch (e: Exception) {
                    Log.e("MPAZ_API", "Error cargando OA", e)
                    null
                }

                val contenido = if (objetivo != null) {
                    try {
                        api.listarContenidos(
                            objetivo.get("id").asInt
                        )
                            .obtenerLista()
                            .firstOrNull()
                            ?.asJsonObject
                    } catch (e: Exception) {
                        Log.e("MPAZ_API", "Error cargando contenido", e)
                        null
                    }
                } else {
                    null
                }

                unidades.add(
                    Unidad(
                        id = unidadId,
                        asignaturaId = asignatura.id,
                        titulo = dato.texto("titulo"),
                        codigoOA = objetivo
                            ?.texto("codigo").orEmpty(),
                        descripcionOA = objetivo
                            ?.texto("descripcion").orEmpty(),
                        ejeCurricular = objetivo
                            ?.texto("ejeCurricular").orEmpty(),
                        explicacion = contenido
                            ?.texto("explicacionEscrita").orEmpty(),
                        ejemploGuiado = contenido
                            ?.texto("ejemplosGuiados").orEmpty(),
                        estado = EstadoUnidad.valueOf(
                            dato.get("estado").asString
                        )
                    )
                )
            }
        }

        return unidades
    }


    suspend fun obtenerMateriales(
        unidadId: Int
    ): List<MaterialDidactico> {

        val objetivos = api.listarObjetivos(unidadId)
            .obtenerLista()

        val materiales = mutableListOf<MaterialDidactico>()

        for (objetivo in objetivos) {

            val objetivoId = objetivo.asJsonObject
                .get("id").asInt

            val contenidos = api.listarContenidos(objetivoId)
                .obtenerLista()

            for (elemento in contenidos) {

                val dato = elemento.asJsonObject

                val imagenes = dato.get("imagenes")
                    ?.takeIf { it.isJsonArray }
                    ?.asJsonArray
                    ?.mapNotNull {
                        it.takeUnless { valor -> valor.isJsonNull }
                            ?.asString
                    }
                    ?: emptyList()

                materiales.add(
                    MaterialDidactico(
                        id = dato.get("id").asInt,
                        titulo = dato.texto("titulo"),
                        explicacion = dato.texto("explicacionEscrita"),
                        ejemplo = dato.texto("ejemplosGuiados"),
                        instrucciones = dato.texto("instrucciones"),
                        imagenes = imagenes,
                        audio = dato.texto("archivoAudio")
                            .takeIf { it.isNotBlank() }
                    )
                )
            }
        }

        return materiales
    }

private fun JsonElement.obtenerLista(): JsonArray {
    return when {
        isJsonArray -> asJsonArray

        isJsonObject && asJsonObject.has("items") ->
            asJsonObject.getAsJsonArray("items")

        isJsonObject && asJsonObject.has("value") ->
            asJsonObject.getAsJsonArray("value")

        else -> error("Formato JSON inesperado")
    }
}

private fun JsonObject.texto(nombre: String): String =
    get(nombre)?.takeUnless { it.isJsonNull }?.asString.orEmpty()

}