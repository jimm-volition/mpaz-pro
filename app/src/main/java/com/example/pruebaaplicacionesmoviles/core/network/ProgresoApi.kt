package com.example.pruebaaplicacionesmoviles.core.network

import retrofit2.http.GET

data class InicioRemoto(
    val estudianteId: Long,
    val objetivos: List<ProgresoRemoto>
)

data class ProgresoRemoto(
    val objetivoAprendizajeId: Long,
    val porcentajeAvance: Int,
    val actualizadoEn: String? = null
)

interface ProgresoApi {
    @GET("estudiante/inicio")
    suspend fun inicio(): InicioRemoto
}