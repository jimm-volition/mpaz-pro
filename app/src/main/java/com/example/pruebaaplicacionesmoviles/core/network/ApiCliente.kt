
package com.example.pruebaaplicacionesmoviles.core.network

import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

private const val BASE_URL = "http://127.0.0.1:8081/api/v1/"

data class LoginRequest(
    val codigoUsuario: String,
    val clave: String
)

data class LoginResponse(
    val token: String?,
    val usuario: JsonObject? = null
)

interface MpazApi {
    @POST("auth/demo-login")
    suspend fun login(@Body datos: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun miPerfil(): JsonObject
}

object SesionRemota {
    @Volatile
    var token: String? = null
        private set

    fun guardarToken(valor: String) {
        token = valor
    }

    fun cerrar() {
        token = null
    }
}

object ApiCliente {
    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val solicitud = chain.request().newBuilder()

            SesionRemota.token?.let {
                solicitud.header("Authorization", "Bearer $it")
            }

            chain.proceed(solicitud.build())
        }
        .build()

    val servicio: MpazApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MpazApi::class.java)
    }
}
