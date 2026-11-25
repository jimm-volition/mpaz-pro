
package com.example.pruebaaplicacionesmoviles.core.network

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UtpContenidoApi {

    @GET("asignaturas")
    suspend fun listarAsignaturas(): JsonElement

    @GET("utp/unidades")
    suspend fun listarUnidades(
        @Query("asignaturaId") asignaturaId: Int,
        @Query("estado") estado: String? = null
    ): JsonElement


    @POST("utp/contenidos/{id}/transiciones")
    suspend fun transicionarContenido(
        @Path("id") id: Int,
        @Body datos: JsonObject
    ): Response<JsonElement>

    @POST("utp/unidades")
    suspend fun crearUnidad(
        @Body datos: JsonObject
    ): Response<JsonElement>

    @PUT("utp/unidades/{id}")
    suspend fun editarUnidad(
        @Path("id") id: Int,
        @Body datos: JsonObject
    ): Response<JsonElement>

    @POST("utp/unidades/{id}/transiciones")
    suspend fun transicionarUnidad(
        @Path("id") id: Int,
        @Body datos: JsonObject
    ): Response<JsonElement>

    @GET("utp/objetivos")
    suspend fun listarObjetivos(
        @Query("unidadId") unidadId: Int
    ): JsonElement

    @POST("utp/objetivos")
    suspend fun crearObjetivo(
        @Body datos: JsonObject
    ): Response<JsonElement>

    @PUT("utp/objetivos/{id}")
    suspend fun editarObjetivo(
        @Path("id") id: Int,
        @Body datos: JsonObject
    ): Response<JsonElement>

    @POST("utp/objetivos/{id}/transiciones")
    suspend fun transicionarObjetivo(
        @Path("id") id: Int,
        @Body datos: JsonObject
    ): Response<JsonElement>

    @GET("utp/contenidos")
    suspend fun listarContenidos(
        @Query("objetivoAprendizajeId") objetivoId: Int
    ): JsonElement

    @POST("utp/contenidos")
    suspend fun crearContenido(
        @Body datos: JsonObject
    ): Response<JsonElement>

    @PUT("utp/contenidos/{id}")
    suspend fun editarContenido(
        @Path("id") id: Int,
        @Body datos: JsonObject
    ): Response<JsonElement>

    @Multipart
    @POST("utp/recursos")
    suspend fun subirRecurso(
        @Part archivo: MultipartBody.Part
    ): Response<JsonObject>
}

interface UtpHistorialApi {

    @GET("utp/unidades/{id}/historial")
    suspend fun historialUnidad(
        @Path("id") unidadId: Int
    ): JsonElement
}

object UtpContenidoApiCliente {

    private val cliente = OkHttpClient.Builder()
        .addInterceptor { cadena ->

            val token = SesionRemota.token

            val solicitud = cadena.request()
                .newBuilder()
                .apply {
                    if (!token.isNullOrBlank()) {
                        header(
                            "Authorization",
                            "Bearer $token"
                        )
                    }
                }
                .build()

            cadena.proceed(solicitud)
        }
        .build()

    val servicio: UtpContenidoApi = Retrofit.Builder()
        .baseUrl(ConfigBackend.API)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UtpContenidoApi::class.java)

    val historial: UtpHistorialApi = Retrofit.Builder()
        .baseUrl(ConfigBackend.API)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UtpHistorialApi::class.java)
}
