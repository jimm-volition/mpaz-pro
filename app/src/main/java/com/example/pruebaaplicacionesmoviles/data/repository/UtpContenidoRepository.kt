
package com.example.pruebaaplicacionesmoviles.data.repository

import com.example.pruebaaplicacionesmoviles.core.network.UtpContenidoApiCliente
import com.example.pruebaaplicacionesmoviles.data.model.Asignatura
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Response

data class UtpUnidad(
    val id: Int,
    val asignaturaId: Int,
    val titulo: String,
    val orden: Int,
    val estado: String
)

data class UtpObjetivo(
    val id: Int,
    val unidadId: Int,
    val codigo: String,
    val descripcion: String,
    val estado: String
)

data class UtpMaterial(
    val id: Int,
    val titulo: String,
    val estado: String
)

class UtpContenidoRepository {

    private val api = UtpContenidoApiCliente.servicio

    suspend fun obtenerAsignaturas(): List<Asignatura> {
        return api.listarAsignaturas()
            .lista()
            .map { elemento ->

                val dato = elemento.asJsonObject

                Asignatura(
                    id = dato.get("id").asInt,
                    nombre = dato.get("nombre").asString
                )
            }
    }

    suspend fun obtenerUnidades(
        asignaturaId: Int
    ): List<UtpUnidad> {

        return api.listarUnidades(asignaturaId)
            .lista()
            .map { elemento ->

                val dato = elemento.asJsonObject

                UtpUnidad(
                    id = dato.get("id").asInt,
                    asignaturaId = dato.get("asignaturaId").asInt,
                    titulo = dato.texto("titulo"),
                    orden = dato.entero("orden"),
                    estado = dato.texto("estado")
                )
            }
    }

    suspend fun obtenerObjetivos(
        unidadId: Int
    ): List<UtpObjetivo> {

        return api.listarObjetivos(unidadId)
            .lista()
            .map { elemento ->

                val dato = elemento.asJsonObject

                UtpObjetivo(
                    id = dato.get("id").asInt,
                    unidadId = unidadId,
                    codigo = dato.texto("codigo"),
                    descripcion = dato.texto("descripcion"),
                    estado = dato.texto("estado")
                )
            }
    }

    suspend fun obtenerMateriales(
        objetivoId: Int
    ): List<UtpMaterial> {

        return api.listarContenidos(objetivoId)
            .lista()
            .map { elemento ->

                val dato = elemento.asJsonObject

                UtpMaterial(
                    id = dato.get("id").asInt,
                    titulo = dato.texto("titulo"),
                    estado = dato.texto("estado")
                )
            }
    }

    suspend fun crearUnidad(
        asignaturaId: Int,
        titulo: String,
        orden: Int
    ) {

        val datos = JsonObject().apply {
            addProperty("asignaturaId", asignaturaId)
            addProperty("titulo", titulo)
            addProperty("orden", orden)
        }

        api.crearUnidad(datos).verificar()
    }

    suspend fun editarUnidad(
        id: Int,
        asignaturaId: Int,
        titulo: String,
        orden: Int
    ) {

        val datos = JsonObject().apply {
            addProperty("asignaturaId", asignaturaId)
            addProperty("titulo", titulo)
            addProperty("orden", orden)
        }

        api.editarUnidad(id, datos).verificar()
    }

    suspend fun crearObjetivo(
        unidadId: Int,
        codigo: String,
        descripcion: String,
        orden: Int
    ) {

        val datos = JsonObject().apply {
            addProperty("unidadId", unidadId)
            addProperty("codigo", codigo)
            addProperty("descripcion", descripcion)
            addProperty("orden", orden)
        }

        api.crearObjetivo(datos).verificar()
    }

    suspend fun editarObjetivo(
        id: Int,
        unidadId: Int,
        codigo: String,
        descripcion: String,
        orden: Int
    ) {

        val datos = JsonObject().apply {
            addProperty("unidadId", unidadId)
            addProperty("codigo", codigo)
            addProperty("descripcion", descripcion)
            addProperty("orden", orden)
        }

        api.editarObjetivo(id, datos).verificar()
    }

    suspend fun cambiarEstadoUnidad(
        unidadId: Int,
        estadoNuevo: String
    ) {

        val datos = JsonObject().apply {
            addProperty("estadoNuevo", estadoNuevo)
        }

        api.transicionarUnidad(
            unidadId,
            datos
        ).verificar()
    }

    suspend fun cambiarEstadoObjetivo(
        objetivoId: Int,
        estadoNuevo: String
    ) {

        val datos = JsonObject().apply {
            addProperty("estadoNuevo", estadoNuevo)
        }

        api.transicionarObjetivo(
            objetivoId,
            datos
        ).verificar()
    }

    suspend fun crearMaterial(
        objetivoId: Int,
        titulo: String,
        explicacion: String,
        ejemplo: String,
        instrucciones: String,
        orden: Int,
        responsableOrigen: String,
        licencia: String
    ) {

        val datos = JsonObject().apply {

            addProperty(
                "objetivoAprendizajeId",
                objetivoId
            )

            addProperty("titulo", titulo)

            addProperty(
                "explicacionEscrita",
                explicacion
            )

            addProperty(
                "ejemplosGuiados",
                ejemplo
            )

            addProperty(
                "instrucciones",
                instrucciones
            )

            addProperty("orden", orden)

            addProperty(
                "responsableOrigen",
                responsableOrigen
            )

            addProperty("licencia", licencia)

            add("imagenes", JsonArray())
        }

        api.crearContenido(datos).verificar()
    }

    suspend fun editarMaterial(
        id: Int,
        objetivoId: Int,
        titulo: String,
        explicacion: String,
        ejemplo: String,
        instrucciones: String,
        orden: Int,
        responsableOrigen: String,
        licencia: String
    ) {

        val datos = JsonObject().apply {

            addProperty(
                "objetivoAprendizajeId",
                objetivoId
            )

            addProperty("titulo", titulo)

            addProperty(
                "explicacionEscrita",
                explicacion
            )

            addProperty(
                "ejemplosGuiados",
                ejemplo
            )

            addProperty("instrucciones", instrucciones)

            addProperty("orden", orden)

            addProperty(
                "responsableOrigen",
                responsableOrigen
            )

            addProperty("licencia", licencia)
        }

        api.editarContenido(id, datos).verificar()
    }
}

private fun JsonElement.lista(): List<JsonElement> {

    return when {

        isJsonArray -> asJsonArray.toList()

        isJsonObject && asJsonObject.has("items") ->
            asJsonObject
                .getAsJsonArray("items")
                .toList()

        isJsonObject && asJsonObject.has("value") ->
            asJsonObject
                .getAsJsonArray("value")
                .toList()

        else -> error("Formato de lista desconocido")
    }
}

private fun JsonObject.texto(nombre: String): String =
    get(nombre)
        ?.takeUnless { it.isJsonNull }
        ?.asString
        .orEmpty()

private fun JsonObject.entero(nombre: String): Int =
    get(nombre)
        ?.takeUnless { it.isJsonNull }
        ?.asInt ?: 0

private fun Response<JsonElement>.verificar() {

    if (!isSuccessful) {
        throw IllegalStateException(
            "El servidor rechazó la operación (HTTP ${code()})"
        )
    }
}
