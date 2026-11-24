
package com.example.pruebaaplicacionesmoviles.data.repository

import com.example.pruebaaplicacionesmoviles.core.network.UtpContenidoApi
import com.example.pruebaaplicacionesmoviles.core.network.UtpHistorialApi
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class UtpContenidoRepositoryTest {

    // Simulamos los servicios REST de Spring Boot.
    private val api = mockk<UtpContenidoApi>()
    private val historialApi = mockk<UtpHistorialApi>()

    // El Repository recibe los servicios simulados.
    private val repositorio = UtpContenidoRepository(
        api = api,
        apiHistorial = historialApi
    )

    private fun json(texto: String): JsonElement =
        JsonParser.parseString(texto)

    @Test
    fun cargarAsignaturas() = runBlocking {

        coEvery {
            api.listarAsignaturas()
        } returns json(
            """
            {
              "items": [
                {"id":1,"nombre":"Lenguaje"},
                {"id":2,"nombre":"Matemática"},
                {"id":3,"nombre":"Ciencias"},
                {"id":4,"nombre":"Historia"}
              ]
            }
            """.trimIndent()
        )

        val resultado = repositorio.obtenerAsignaturas()

        assertEquals(4, resultado.size)
        assertEquals("Lenguaje", resultado[0].nombre)
        assertEquals("Historia", resultado[3].nombre)
    }

    @Test
    fun cargarUnidadPublicada() = runBlocking {

        coEvery {
            api.listarUnidades(1, null)
        } returns json(
            """
            {
              "items": [
                {
                  "id":2,
                  "asignaturaId":1,
                  "titulo":"Comprensión de cuentos",
                  "orden":2,
                  "estado":"PUBLICADO"
                }
              ]
            }
            """.trimIndent()
        )

        val resultado = repositorio.obtenerUnidades(1)

        assertEquals(1, resultado.size)
        assertEquals(2, resultado[0].id)
        assertEquals("PUBLICADO", resultado[0].estado)
    }

    @Test
    fun cargarObjetivoAutorizado() = runBlocking {

        coEvery {
            api.listarObjetivos(2)
        } returns json(
            """
            {
              "items": [
                {
                  "id":2,
                  "codigo":"OA2",
                  "descripcion":"Identificar personajes",
                  "orden":1,
                  "estado":"AUTORIZADO"
                }
              ]
            }
            """.trimIndent()
        )

        val resultado = repositorio.obtenerObjetivos(2)

        assertEquals(1, resultado.size)
        assertEquals("OA2", resultado[0].codigo)
        assertEquals("AUTORIZADO", resultado[0].estado)
    }

    @Test
    fun cargarMaterialConImagenYAudio() = runBlocking {

        coEvery {
            api.listarContenidos(2)
        } returns json(
            """
            {
              "items": [
                {
                  "id":4,
                  "titulo":"Material de lectura",
                  "estado":"PUBLICADO",
                  "explicacionEscrita":"Leer el cuento",
                  "ejemplosGuiados":"Reconocer personajes",
                  "instrucciones":"Leer y responder",
                  "orden":1,
                  "responsableOrigen":"Equipo MPAZ",
                  "licencia":"Creación propia",
                  "imagenes":[
                    "/api/v1/recursos/imagen.png"
                  ],
                  "archivoAudio":"/api/v1/recursos/audio.wav"
                }
              ]
            }
            """.trimIndent()
        )

        val resultado = repositorio.obtenerMateriales(2)

        assertEquals(1, resultado.size)
        assertEquals("PUBLICADO", resultado[0].estado)
        assertEquals(1, resultado[0].imagenes.size)
        assertEquals(
            "/api/v1/recursos/audio.wav",
            resultado[0].audio
        )
    }

    @Test
    fun conservarArchivosAlEditarMaterial() = runBlocking {

        coEvery {
            api.editarContenido(4, any())
        } returns Response.success(
            json("""{"id":4}""")
        )

        repositorio.editarMaterial(
            id = 4,
            objetivoId = 2,
            titulo = "Material actualizado",
            explicacion = "Explicación",
            ejemplo = "Ejemplo",
            instrucciones = "Leer",
            orden = 1,
            responsableOrigen = "Equipo MPAZ",
            licencia = "Creación propia",
            imagenes = listOf(
                "/api/v1/recursos/imagen.png"
            ),
            audio = "/api/v1/recursos/audio.wav"
        )

        coVerify(exactly = 1) {
            api.editarContenido(
                4,
                match { datos ->
                    datos.getAsJsonArray("imagenes").size() == 1 &&
                            datos.get("archivoAudio").asString ==
                            "/api/v1/recursos/audio.wav"
                }
            )
        }
    }

    @Test
    fun enviarCambioDeEstado() = runBlocking {

        coEvery {
            api.transicionarUnidad(2, any())
        } returns Response.success(
            json("""{"id":2,"estado":"PUBLICADO"}""")
        )

        repositorio.cambiarEstadoUnidad(
            unidadId = 2,
            estadoNuevo = "PUBLICADO"
        )

        coVerify(exactly = 1) {
            api.transicionarUnidad(
                2,
                match { datos ->
                    datos.get("estadoNuevo").asString ==
                            "PUBLICADO"
                }
            )
        }
    }

    @Test
    fun consultarHistorial() = runBlocking {

        coEvery {
            historialApi.historialUnidad(2)
        } returns json(
            """
            {
              "items": [
                {
                  "estadoAnterior":"DESHABILITADO",
                  "estadoNuevo":"PUBLICADO",
                  "creadoEn":"2026-11-24T19:27:52Z",
                  "usuarioId":1,
                  "tipoRecurso":"UNIDAD"
                }
              ]
            }
            """.trimIndent()
        )

        val resultado = repositorio.obtenerHistorial(2)

        assertEquals(1, resultado.size)
        assertEquals(
            "DESHABILITADO",
            resultado[0].estadoAnterior
        )
        assertEquals(
            "PUBLICADO",
            resultado[0].estadoNuevo
        )
        assertEquals(1, resultado[0].usuarioId)
    }

    @Test
    fun listaVaciaNoCreaMateriales() = runBlocking {

        coEvery {
            api.listarContenidos(99)
        } returns json("""{"items":[]}""")

        val resultado = repositorio.obtenerMateriales(99)

        assertTrue(resultado.isEmpty())
    }
}
