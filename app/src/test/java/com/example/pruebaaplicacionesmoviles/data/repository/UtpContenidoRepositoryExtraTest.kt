package com.example.pruebaaplicacionesmoviles.data.repository

import com.example.pruebaaplicacionesmoviles.core.network.UtpContenidoApi
import com.example.pruebaaplicacionesmoviles.core.network.UtpHistorialApi
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Response

/** Casos adicionales para las rutas CRUD, JSON alternativo y errores HTTP de UTP. */
class UtpContenidoRepositoryExtraTest {

    private val api = mockk<UtpContenidoApi>()
    private val historialApi = mockk<UtpHistorialApi>()
    private val repo = UtpContenidoRepository(api = api, apiHistorial = historialApi)

    private fun json(s: String): JsonElement = JsonParser.parseString(s)
    private fun correcto(): Response<JsonElement> = Response.success(json("""{"id":1}"""))
    private fun rechazo(codigo: Int): Response<JsonElement> = Response.error(
        codigo,
        "Rechazado".toResponseBody("text/plain".toMediaType())
    )

    private suspend fun errorEsperado(accion: suspend () -> Unit): IllegalStateException {
        try {
            accion()
            fail("Se esperaba IllegalStateException")
        } catch (ex: IllegalStateException) {
            return ex
        }
        error("No se alcanzó la validación esperada")
    }

    @Test fun asignaturasAceptanArregloDirecto() = runBlocking {
        coEvery { api.listarAsignaturas() } returns json("""[{"id":1,"nombre":"Lenguaje"}]""")
        assertEquals("Lenguaje", repo.obtenerAsignaturas().single().nombre)
    }

    @Test fun asignaturasAceptanWrapperValue() = runBlocking {
        coEvery { api.listarAsignaturas() } returns json("""{"value":[{"id":4,"nombre":"Historia"}]}""")
        assertEquals(4, repo.obtenerAsignaturas().single().id)
    }

    @Test fun formatoJsonInesperadoSeRechaza() = runBlocking {
        coEvery { api.listarAsignaturas() } returns json("""{"otraClave":[]}""")
        val ex = errorEsperado { repo.obtenerAsignaturas() }
        assertTrue(ex.message.orEmpty().contains("Formato JSON inesperado"))
    }

    @Test fun nombreDeAsignaturaNuloNoProvocaFallo() = runBlocking {
        coEvery { api.listarAsignaturas() } returns json("""{"items":[{"id":2,"nombre":null}]}""")
        assertEquals("", repo.obtenerAsignaturas().single().nombre)
    }

    @Test fun unidadSinCamposOpcionalesUtilizaValoresVacios() = runBlocking {
        coEvery { api.listarUnidades(5, null) } returns json("""{"items":[{"id":9}]}""")
        val u = repo.obtenerUnidades(5).single()
        assertEquals(9, u.id)
        assertEquals(0, u.asignaturaId)
        assertEquals(0, u.orden)
        assertEquals("", u.estado)
        assertEquals("", u.titulo)
    }

    @Test fun objetivosAceptanWrapperValue() = runBlocking {
        coEvery { api.listarObjetivos(3) } returns json(
            """{"value":[{"id":8,"codigo":"OA8","descripcion":"Lectura","estado":"BORRADOR","orden":2}]}"""
        )
        val oa = repo.obtenerObjetivos(3).single()
        assertEquals(3, oa.unidadId)
        assertEquals("OA8", oa.codigo)
        assertEquals(2, oa.orden)
    }

    @Test fun objetivoConValoresNulosUtilizaDefectos() = runBlocking {
        coEvery { api.listarObjetivos(7) } returns json("""[{"id":4,"codigo":null,"orden":null}]""")
        val oa = repo.obtenerObjetivos(7).single()
        assertEquals("", oa.codigo)
        assertEquals("", oa.descripcion)
        assertEquals(0, oa.orden)
    }

    @Test fun materialConImagenesYAudioNulosEsValido() = runBlocking {
        coEvery { api.listarContenidos(2) } returns json(
            """{"items":[{"id":11,"titulo":"Lectura","imagenes":null,"archivoAudio":null}]}"""
        )
        val m = repo.obtenerMateriales(2).single()
        assertTrue(m.imagenes.isEmpty())
        assertEquals(null, m.audio)
        assertEquals("Lectura", m.titulo)
    }

    @Test fun materialIgnoraImagenesNulas() = runBlocking {
        coEvery { api.listarContenidos(2) } returns json(
            """[{"id":10,"imagenes":["/img/a.png",null,"/img/b.png"],"archivoAudio":""}]"""
        )
        val m = repo.obtenerMateriales(2).single()
        assertEquals(listOf("/img/a.png", "/img/b.png"), m.imagenes)
        assertEquals(null, m.audio)
    }

    @Test fun materialSinCamposMultimediaNoFalla() = runBlocking {
        coEvery { api.listarContenidos(2) } returns json("""[{"id":3,"orden":0}]""")
        val m = repo.obtenerMateriales(2).single()
        assertTrue(m.imagenes.isEmpty())
        assertEquals(null, m.audio)
        assertEquals("", m.licencia)
    }

    @Test fun historialAceptaArregloSinEnvoltorio() = runBlocking {
        coEvery { historialApi.historialUnidad(2) } returns json(
            """[{"estadoAnterior":"BORRADOR","estadoNuevo":"EN_REVISION","usuarioId":1,"tipoRecurso":"UNIDAD"}]"""
        )
        val evento = repo.obtenerHistorial(2).single()
        assertEquals("EN_REVISION", evento.estadoNuevo)
        assertEquals("", evento.creadoEn)
    }

    @Test fun crearUnidadEnviaCamposCorrectos() = runBlocking {
        coEvery { api.crearUnidad(any()) } returns correcto()
        repo.crearUnidad(1, "Cuentos", 2)
        coVerify(exactly = 1) {
            api.crearUnidad(match {
                it.get("asignaturaId").asInt == 1 &&
                        it.get("titulo").asString == "Cuentos" &&
                        it.get("orden").asInt == 2
            })
        }
    }

    @Test fun editarUnidadEnviaIdYCambios() = runBlocking {
        coEvery { api.editarUnidad(7, any()) } returns correcto()
        repo.editarUnidad(7, 2, "Unidad revisada", 3)
        coVerify(exactly = 1) {
            api.editarUnidad(7, match {
                it.get("asignaturaId").asInt == 2 &&
                        it.get("titulo").asString == "Unidad revisada" &&
                        it.get("orden").asInt == 3
            })
        }
    }

    @Test fun crearObjetivoEnviaCodigoDescripcionYOrden() = runBlocking {
        coEvery { api.crearObjetivo(any()) } returns correcto()
        repo.crearObjetivo(2, "OA2", "Identificar personajes", 1)
        coVerify(exactly = 1) {
            api.crearObjetivo(match {
                it.get("unidadId").asInt == 2 &&
                        it.get("codigo").asString == "OA2" &&
                        it.get("descripcion").asString == "Identificar personajes" &&
                        it.get("orden").asInt == 1
            })
        }
    }

    @Test fun editarObjetivoEnviaIdYCampos() = runBlocking {
        coEvery { api.editarObjetivo(8, any()) } returns correcto()
        repo.editarObjetivo(8, 4, "OA5", "Aprender", 3)
        coVerify(exactly = 1) {
            api.editarObjetivo(8, match {
                it.get("unidadId").asInt == 4 &&
                        it.get("codigo").asString == "OA5" &&
                        it.get("descripcion").asString == "Aprender" &&
                        it.get("orden").asInt == 3
            })
        }
    }

    @Test fun cambioEstadoOAEnviaEstadoNuevo() = runBlocking {
        coEvery { api.transicionarObjetivo(8, any()) } returns correcto()
        repo.cambiarEstadoObjetivo(8, "AUTORIZADO")
        coVerify(exactly = 1) {
            api.transicionarObjetivo(8, match {
                it.get("estadoNuevo").asString == "AUTORIZADO"
            })
        }
    }

    @Test fun crearMaterialEnviaTodasLasPropiedades() = runBlocking {
        coEvery { api.crearContenido(any()) } returns correcto()
        repo.crearMaterial(
            3, "Lectura", "Explicación", "Ejemplo", "Instrucciones", 1,
            "Equipo MPAZ", "Creación propia",
            listOf("/img/1.png", "/img/2.png"), "/audio/1.wav"
        )
        coVerify(exactly = 1) {
            api.crearContenido(match { d ->
                d.get("objetivoAprendizajeId").asInt == 3 &&
                        d.get("titulo").asString == "Lectura" &&
                        d.get("explicacionEscrita").asString == "Explicación" &&
                        d.get("ejemplosGuiados").asString == "Ejemplo" &&
                        d.get("instrucciones").asString == "Instrucciones" &&
                        d.get("orden").asInt == 1 &&
                        d.get("responsableOrigen").asString == "Equipo MPAZ" &&
                        d.get("licencia").asString == "Creación propia" &&
                        d.getAsJsonArray("imagenes").size() == 2 &&
                        d.get("archivoAudio").asString == "/audio/1.wav"
            })
        }
    }

    @Test fun crearMaterialSinMultimediaEnviaArregloVacioYAudioNull() = runBlocking {
        coEvery { api.crearContenido(any()) } returns correcto()
        repo.crearMaterial(3, "Texto", "A", "B", "C", 0, "UTP", "Propia")
        coVerify(exactly = 1) {
            api.crearContenido(match {
                it.getAsJsonArray("imagenes").size() == 0 &&
                        it.get("archivoAudio").isJsonNull &&
                        it.get("orden").asInt == 0
            })
        }
    }

    @Test fun editarMaterialPuedeQuitarMultimedia() = runBlocking {
        coEvery { api.editarContenido(12, any()) } returns correcto()
        repo.editarMaterial(12, 3, "Texto", "A", "B", "C", 0, "UTP", "Propia", emptyList(), null)
        coVerify(exactly = 1) {
            api.editarContenido(12, match {
                it.getAsJsonArray("imagenes").size() == 0 && it.get("archivoAudio").isJsonNull
            })
        }
    }

    @Test fun error403EnCrearUnidadSePropaga() = runBlocking {
        coEvery { api.crearUnidad(any()) } returns rechazo(403)
        val ex = errorEsperado { repo.crearUnidad(1, "Cuentos", 1) }
        assertTrue(ex.message.orEmpty().contains("403"))
    }

    @Test fun error409EnPublicarObjetivoSePropaga() = runBlocking {
        coEvery { api.transicionarObjetivo(2, any()) } returns rechazo(409)
        val ex = errorEsperado { repo.cambiarEstadoObjetivo(2, "PUBLICADO") }
        assertTrue(ex.message.orEmpty().contains("409"))
    }

    @Test fun respuestaSinCuerpoSeRechaza() = runBlocking {
        coEvery { api.crearUnidad(any()) } returns Response.success<JsonElement>(204, null)
        val ex = errorEsperado { repo.crearUnidad(1, "Sin cuerpo", 1) }
        assertTrue(ex.message.orEmpty().contains("no devolvi"))
    }

    @Test fun errorDeRedSePropaga() = runBlocking {
        coEvery { api.listarAsignaturas() } throws java.io.IOException("Sin conexión")
        try {
            repo.obtenerAsignaturas()
            fail("Se esperaba IOException")
        } catch (ex: java.io.IOException) {
            assertEquals("Sin conexión", ex.message)
        }
    }

    @Test fun listaDeObjetivosConservaTodosLosResultados() = runBlocking {
        coEvery { api.listarObjetivos(2) } returns json(
            """{"items":[{"id":1,"codigo":"OA1"},{"id":2,"codigo":"OA2"},{"id":3,"codigo":"OA3"}]}"""
        )
        val items = repo.obtenerObjetivos(2)
        assertEquals(3, items.size)
        assertEquals(listOf("OA1", "OA2", "OA3"), items.map { it.codigo })
    }
}
