package com.example.pruebaaplicacionesmoviles.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PATCH
import okhttp3.ResponseBody
import retrofit2.http.Streaming

data class ObjetivoDisponibleRemoto(
    val id: Long,
    val unidadId: Long,
    val codigo: String,
    val descripcion: String,
    val estado: String
)

data class CursoRemoto(
    val id: Long,
    val nombre: String,
    val anioEscolar: Int? = null,
    val docenteId: Long? = null,
    val activo: Boolean? = null
)

data class EstudianteRemoto(
    val id: Long,
    val codigoUsuario: String? = null,
    val nombreCompleto: String? = null,
    val rol: String? = null
)

data class PaginaDocente<T>(
    val items: List<T> = emptyList(),
    val total: Long = 0,
    val pagina: Int = 0,
    val tamano: Int = 20
)

data class AsignacionRemota(
    val id: Long,
    val cursoId: Long,
    val estudianteId: Long?,
    val objetivoAprendizajeId: Long,
    val docenteId: Long,
    val disponibleDesde: String,
    val disponibleHasta: String,
    val limiteIntentosPrueba: Int,
    val activa: Boolean
)

data class GuardarAsignacionRequest(
    val cursoId: Long,
    val estudianteId: Long?,
    val objetivoAprendizajeId: Long,
    val disponibleDesde: String,
    val disponibleHasta: String,
    val limiteIntentosPrueba: Int,
    val activa: Boolean
)

data class PaginaAsignaciones(
    val items: List<AsignacionRemota>,
    val total: Long,
    val pagina: Int,
    val tamano: Int
)

data class ResultadoDocenteRemoto(
    val estudianteId: Long,
    val envios: List<EnvioDocenteRemoto>,
    val intentos: List<IntentoDocenteRemoto>
)

data class EnvioDocenteRemoto(
    val id: Long,
    val actividadId: Long,
    val objetivoAprendizajeId: Long,
    val puntaje: Int,
    val registradoEn: String
)

data class IntentoDocenteRemoto(
    val id: Long,
    val pruebaId: Long,
    val numeroIntento: Int,
    val estado: String,
    val puntaje: Int?,
    val iniciadoEn: String,
    val entregadoEn: String?
)

data class InformeRemoto(
    val id: Long,
    val cursoId: Long,
    val estudianteId: Long,
    val objetivoAprendizajeId: Long,
    val docenteId: Long,
    val estado: String,
    val resumen: String,
    val areasRefuerzo: String,
    val creadoEn: String
)

data class CrearInformeRequest(
    val cursoId: Long,
    val estudianteId: Long,
    val objetivoAprendizajeId: Long
)

data class ValidarInformeRequest(
    val estado: String,
    val observacion: String? = null
)

data class ProgresoEstudianteRemoto(
    val estudianteId: Long,
    val porcentajeAvance: Int,
    val actualizadoEn: String?
)

data class SeguimientoRemoto(
    val cursoId: Long,
    val objetivoAprendizajeId: Long,
    val estudiantes: List<ProgresoEstudianteRemoto> = emptyList()
)

interface DocenteApi {
    @GET("docente/progreso")
    suspend fun progreso(
        @Query("cursoId") cursoId: Long,
        @Query("objetivoAprendizajeId") objetivoId: Long
    ): SeguimientoRemoto

    @GET("docente/cursos")
    suspend fun cursos(
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 20
    ): PaginaDocente<CursoRemoto>

    @GET("docente/estudiantes")
    suspend fun estudiantes(
        @Query("cursoId") cursoId: Long,
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 20
    ): PaginaDocente<EstudianteRemoto>

    @GET("docente/objetivos-disponibles")
    suspend fun objetivos(
        @Query("cursoId") cursoId: Long
    ): List<ObjetivoDisponibleRemoto>

    @GET("docente/asignaciones")
    suspend fun asignaciones(
        @Query("cursoId") cursoId: Long,
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 20
    ): PaginaAsignaciones

    @POST("docente/asignaciones")
    suspend fun crear(
        @Body cuerpo: GuardarAsignacionRequest
    ): AsignacionRemota

    @PUT("docente/asignaciones/{id}")
    suspend fun actualizar(
        @Path("id") id: Long,
        @Body cuerpo: GuardarAsignacionRequest
    ): AsignacionRemota
    @GET("docente/estudiantes/{id}/resultados")
    suspend fun resultados(
        @Path("id") estudianteId: Long
    ): ResultadoDocenteRemoto
    @POST("docente/informes")
    suspend fun crearInforme(
        @Body cuerpo: CrearInformeRequest
    ): InformeRemoto

    @GET("docente/informes/{id}")
    suspend fun informe(
        @Path("id") id: Long
    ): InformeRemoto

    @PATCH("docente/informes/{id}/validacion")
    suspend fun validarInforme(
        @Path("id") id: Long,
        @Body cuerpo: ValidarInformeRequest
    ): InformeRemoto
    @Streaming
    @GET("docente/informes/{id}/descarga")
    suspend fun descargarInforme(
        @Path("id") id: Long
    ): ResponseBody
}