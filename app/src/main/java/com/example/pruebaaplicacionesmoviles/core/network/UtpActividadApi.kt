package com.example.pruebaaplicacionesmoviles.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Rutas UTP de ms-actividades-evaluaciones (contrato 6.3). Solo rol UTP.

data class UtpActividadRemota(
    val id: Long? = null,
    val objetivoAprendizajeId: Long,
    val tipo: String = "SELECCION",
    val titulo: String,
    val orden: Int,
    val estado: String? = null
)

data class UtpPruebaFinalRemota(
    val id: Long? = null,
    val objetivoAprendizajeId: Long,
    val titulo: String,
    val estado: String? = null,
    val disponibleDesde: String? = null,
    val disponibleHasta: String? = null,
    val limiteIntentos: Int? = null
)

data class UtpAlternativaRemota(
    val id: Long? = null,
    val texto: String,
    val esCorrecta: Boolean,
    val orden: Int
)

data class UtpPreguntaRemota(
    val id: Long? = null,
    val actividadId: Long? = null,
    val pruebaFinalId: Long? = null,
    val enunciado: String,
    val tipo: String = "SELECCION",
    val orden: Int,
    val explicacion: String? = null,
    val opciones: List<UtpAlternativaRemota>
)

data class TransicionEstadoRequest(
    val estadoNuevo: String
)

data class PaginaUtp<T>(
    val items: List<T> = emptyList(),
    val total: Long = 0,
    val pagina: Int = 0,
    val tamano: Int = 20
)

interface UtpActividadApi {

    @GET("utp/actividades")
    suspend fun listarActividades(
        @Query("objetivoAprendizajeId") objetivoId: Long,
        @Query("tamano") tamano: Int = 100
    ): PaginaUtp<UtpActividadRemota>

    @POST("utp/actividades")
    suspend fun crearActividad(@Body cuerpo: UtpActividadRemota): UtpActividadRemota

    @PUT("utp/actividades/{id}")
    suspend fun editarActividad(
        @Path("id") id: Long,
        @Body cuerpo: UtpActividadRemota
    ): UtpActividadRemota

    @POST("utp/actividades/{id}/transiciones")
    suspend fun transicionarActividad(
        @Path("id") id: Long,
        @Body cuerpo: TransicionEstadoRequest
    ): UtpActividadRemota

    @GET("utp/actividades/{id}/preguntas")
    suspend fun preguntasDeActividad(@Path("id") id: Long): List<UtpPreguntaRemota>

    @GET("utp/pruebas-finales")
    suspend fun listarPruebas(
        @Query("objetivoAprendizajeId") objetivoId: Long,
        @Query("tamano") tamano: Int = 100
    ): PaginaUtp<UtpPruebaFinalRemota>

    @POST("utp/pruebas-finales")
    suspend fun crearPrueba(@Body cuerpo: UtpPruebaFinalRemota): UtpPruebaFinalRemota

    @PUT("utp/pruebas-finales/{id}")
    suspend fun editarPrueba(
        @Path("id") id: Long,
        @Body cuerpo: UtpPruebaFinalRemota
    ): UtpPruebaFinalRemota

    @POST("utp/pruebas-finales/{id}/transiciones")
    suspend fun transicionarPrueba(
        @Path("id") id: Long,
        @Body cuerpo: TransicionEstadoRequest
    ): UtpPruebaFinalRemota

    @GET("utp/pruebas-finales/{id}/preguntas")
    suspend fun preguntasDePrueba(@Path("id") id: Long): List<UtpPreguntaRemota>

    @POST("utp/preguntas")
    suspend fun crearPregunta(@Body cuerpo: UtpPreguntaRemota): UtpPreguntaRemota

    @PUT("utp/preguntas/{id}")
    suspend fun editarPregunta(
        @Path("id") id: Long,
        @Body cuerpo: UtpPreguntaRemota
    ): UtpPreguntaRemota
}
