
package com.example.pruebaaplicacionesmoviles.data.repository

import com.example.pruebaaplicacionesmoviles.core.network.SesionRemota
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class RecursoRemotoRepository {

    private val cliente = OkHttpClient()

    private val urlBase = "http://127.0.0.1:8082"

    suspend fun descargarRecurso(ruta: String): ByteArray =
        withContext(Dispatchers.IO) {

            require(ruta.startsWith("/api/v1/recursos/")) {
                "Ruta de recurso no válida"
            }

            val token = SesionRemota.token
                ?: error("La sesión ha expirado")

            val solicitud = Request.Builder()
                .url("$urlBase$ruta")
                .header("Authorization", "Bearer $token")
                .build()

            cliente.newCall(solicitud).execute().use { respuesta ->

                if (!respuesta.isSuccessful) {
                    throw IOException(
                        "Error HTTP ${respuesta.code}"
                    )
                }

                respuesta.body?.bytes()
                    ?: throw IOException("Archivo vacío")
            }
        }
}
