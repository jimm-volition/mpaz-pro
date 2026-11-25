package com.example.pruebaaplicacionesmoviles.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class ActividadRemota(
    val id: Long,
    val objetivoAprendizajeId: Long,
    val tipo: String,
    val titulo: String,
    val preguntas: List<PreguntaRemota>? = null
)

data class PreguntaRemota(
    val id: Long,
    val enunciado: String,
    val tipo: String,
    val opciones: List<OpcionRemota>
)

data class OpcionRemota(
    val id: Long,
    val texto: String
)

data class EnvioActividadRequest(
    val clientEventId: String,
    val respuestas: List<RespuestaEnviadaRequest>,
    val respondidaEn: String
)

data class RespuestaEnviadaRequest(
    val preguntaId: Long,
    val respuesta: RespuestaSeleccionRequest
)

data class RespuestaSeleccionRequest(
    val tipo: String,
    val alternativaId: Long
)

data class ResultadoEnvioRemoto(
    val actividadId: Long,
    val puntaje: Int,
    val porcentajeAvance: Int,
    val feedback: List<FeedbackRemoto>,
    val registradoEn: String
)

data class FeedbackRemoto(
    val preguntaId: Long,
    val correcta: Boolean,
    val explicacion: String?
)

interface ActividadApi {
    @GET("estudiante/objetivos/{id}/actividades")
    suspend fun listar(@Path("id") objetivoId: Long): List<ActividadRemota>

    @GET("estudiante/actividades/{id}")
    suspend fun obtener(@Path("id") actividadId: Long): ActividadRemota

    @POST("estudiante/actividades/{id}/respuestas")
    suspend fun enviar(
        @Path("id") actividadId: Long,
        @Body cuerpo: EnvioActividadRequest
    ): ResultadoEnvioRemoto
}