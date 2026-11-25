package com.example.pruebaaplicacionesmoviles.core.network

import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ContenidoApi {

    @GET("asignaturas")
    suspend fun listarAsignaturas(): JsonElement

    @GET("estudiante/unidades")
    suspend fun listarUnidades(
        @Query("asignaturaId") id: Int
    ): JsonElement

    @GET("estudiante/objetivos")
    suspend fun listarObjetivos(
        @Query("unidadId") id: Int
    ): JsonElement

    @GET("estudiante/objetivos/{id}/contenidos")
    suspend fun listarContenidos(
        @Path("id") id: Int
    ): JsonElement
}

object ContenidoApiCliente {

    private val cliente = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val solicitud = chain.request().newBuilder()

            SesionRemota.token?.let { token ->
                solicitud.header(
                    "Authorization",
                    "Bearer $token"
                )
            }

            chain.proceed(solicitud.build())
        }
        .build()

    val servicio: ContenidoApi by lazy {
        Retrofit.Builder()
            .baseUrl(ConfigBackend.API)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ContenidoApi::class.java)
    }
}
