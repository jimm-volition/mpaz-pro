
package com.example.pruebaaplicacionesmoviles.core.network

import android.text.Html
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class ImagenWikimedia(
    val titulo: String,
    val url: String,
    val miniatura: String,
    val autor: String,
    val licencia: String,
    val fuente: String
)

interface WikimediaApi {

    @GET("w/api.php")
    suspend fun buscar(
        @Query("gsrsearch") termino: String,
        @Query("action") accion: String = "query",
        @Query("format") formato: String = "json",
        @Query("formatversion") version: Int = 2,
        @Query("generator") generador: String = "search",
        @Query("gsrnamespace") espacio: Int = 6,
        @Query("gsrlimit") limite: Int = 12,
        @Query("prop") propiedad: String = "imageinfo",
        @Query("iiprop") informacion: String = "url|mime|extmetadata",
        @Query("iiurlwidth") ancho: Int = 400
    ): JsonObject
}

object WikimediaRepositorio {

    private val cliente = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .header(
                    "User-Agent",
                    "MPAZPRO-Android/1.0 " +
                            "(https://github.com/jimm-volition/mpaz-pro)"
                )
                .build()

            chain.proceed(request)
        }
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    private val api: WikimediaApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://commons.wikimedia.org/")
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikimediaApi::class.java)
    }

    private fun limpiarHtml(texto: String?): String {
        if (texto.isNullOrBlank()) return ""

        return Html.fromHtml(
            texto,
            Html.FROM_HTML_MODE_LEGACY
        ).toString().trim()
    }

    suspend fun buscarImagenes(
        termino: String
    ): List<ImagenWikimedia> = withContext(Dispatchers.IO) {

        val respuesta = api.buscar(termino.trim())

        val paginas = respuesta
            .getAsJsonObject("query")
            ?.getAsJsonArray("pages")
            ?: return@withContext emptyList()

        paginas.mapNotNull { elemento ->

            val pagina = elemento.asJsonObject

            val info = pagina
                .getAsJsonArray("imageinfo")
                ?.firstOrNull()
                ?.asJsonObject
                ?: return@mapNotNull null

            val mime = info.get("mime")
                ?.asString.orEmpty()

            if (mime !in setOf(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
                )
            ) {
                return@mapNotNull null
            }

            val url = info.get("url")
                ?.asString
                ?: return@mapNotNull null

            if (url.length > 500) {
                return@mapNotNull null
            }

            val metadata = info
                .getAsJsonObject("extmetadata")

            val autor = limpiarHtml(
                metadata
                    ?.getAsJsonObject("Artist")
                    ?.get("value")
                    ?.asString
            )

            val licencia = limpiarHtml(
                metadata
                    ?.getAsJsonObject("LicenseShortName")
                    ?.get("value")
                    ?.asString
            )

            if (autor.isBlank() || licencia.isBlank()) {
                return@mapNotNull null
            }

            ImagenWikimedia(
                titulo = pagina.get("title")
                    ?.asString
                    .orEmpty()
                    .removePrefix("File:"),
                url = url,
                miniatura = info.get("thumburl")
                    ?.asString ?: url,
                autor = autor.take(195),
                licencia = licencia.take(195),
                fuente = info.get("descriptionurl")
                    ?.asString
                    ?: "https://commons.wikimedia.org/"
            )
        }
    }
}
