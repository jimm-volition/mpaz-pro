
package com.example.pruebaaplicacionesmoviles.core.network

import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST



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

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ConfigBackend.API)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val servicio: MpazApi by lazy {
        retrofit.create(MpazApi::class.java)
    }

    val actividades: ActividadApi by lazy {
        retrofit.create(ActividadApi::class.java)
    }

    val progreso: ProgresoApi by lazy {
        retrofit.create(ProgresoApi::class.java)
    }

    val pruebas: PruebaApi by lazy {
        retrofit.create(PruebaApi::class.java)
    }

    val docente: DocenteApi by lazy {
        retrofit.create(DocenteApi::class.java)
    }
}
