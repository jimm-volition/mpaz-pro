package com.example.pruebaaplicacionesmoviles.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class PruebaRemota(
    val pruebaId: Long,
    val objetivoAprendizajeId: Long,
    val estado: String,
    val disponibleDesde: String?,
    val disponibleHasta: String?,
    val limiteIntentos: Int,
    val intentosUtilizados: Int,
    val intentosRestantes: Int,
    val puedeRendir: Boolean
)

data class InicioIntentoRequest(
    val clientEventId: String
)

data class IntentoRemoto(
    val id: Long,
    val pruebaId: Long,
    val numeroIntento: Int,
    val estado: String,
    val iniciadoEn: String
)

data class EntregaIntentoRequest(
    val respuestas: List<RespuestaEnviadaRequest>,
    val respondidaEn: String
)

data class ResultadoIntentoRemoto(
    val intentoId: Long,
    val pruebaId: Long,
    val puntaje: Int,
    val feedback: List<FeedbackRemoto>,
    val registradoEn: String
)

interface PruebaApi {
    @GET("estudiante/objetivos/{id}/prueba-final")
    suspend fun obtener(@Path("id") objetivoId: Long): PruebaRemota

    @POST("estudiante/pruebas/{id}/intentos")
    suspend fun iniciar(
        @Path("id") pruebaId: Long,
        @Body cuerpo: InicioIntentoRequest
    ): IntentoRemoto

    @GET("estudiante/intentos/{id}/preguntas")
    suspend fun preguntas(@Path("id") intentoId: Long): List<PreguntaRemota>

    @POST("estudiante/intentos/{id}/entrega")
    suspend fun entregar(
        @Path("id") intentoId: Long,
        @Body cuerpo: EntregaIntentoRequest
    ): ResultadoIntentoRemoto
}