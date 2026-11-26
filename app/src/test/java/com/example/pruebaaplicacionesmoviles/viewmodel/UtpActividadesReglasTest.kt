package com.example.pruebaaplicacionesmoviles.viewmodel

import com.google.gson.JsonParser
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class UtpActividadesReglasTest {

    @Test
    fun flujoDeEstadosIgualAlBackend() {
        assertEquals("EN_REVISION", siguienteEstadoEvaluacion("BORRADOR"))
        assertEquals("AUTORIZADO", siguienteEstadoEvaluacion("EN_REVISION"))
        assertEquals("PUBLICADO", siguienteEstadoEvaluacion("AUTORIZADO"))
        assertEquals("DESHABILITADO", siguienteEstadoEvaluacion("PUBLICADO"))
        assertNull(siguienteEstadoEvaluacion("DESHABILITADO"))
        assertNull(siguienteEstadoEvaluacion(null))
    }

    @Test
    fun actividadRequiereTituloYOrdenValido() {
        assertNotNull(errorActividad("", "1"))
        assertNotNull(errorActividad("Práctica", "uno"))
        assertNotNull(errorActividad("Práctica", "-1"))
        assertNotNull(errorActividad("x".repeat(201), "1"))
        assertNull(errorActividad("Práctica de idea principal", "0"))
    }

    @Test
    fun pruebaValidaFechasEIntentos() {
        assertNotNull(errorPrueba(" ", "", "", ""))
        assertNotNull(errorPrueba("Prueba", "2026/11/01", "", ""))
        assertNotNull(errorPrueba("Prueba", "2026-12-10", "2026-12-01", ""))
        assertNotNull(errorPrueba("Prueba", "", "", "0"))
        assertNotNull(errorPrueba("Prueba", "", "", "dos"))
        assertNull(errorPrueba("Prueba", "", "", ""))
        assertNull(errorPrueba("Prueba", "2026-11-01", "2026-12-15", "2"))
    }

    @Test
    fun preguntaExigeDosAlternativasYUnaCorrecta() {
        assertNotNull(errorPregunta("", listOf("a", "b"), 0))
        assertNotNull(errorPregunta("¿?", listOf("a", "", "", ""), 0))
        assertNotNull(errorPregunta("¿?", listOf("a", "b"), null))
        assertNotNull(errorPregunta("¿?", listOf("a", "b", ""), 2))
        assertNull(errorPregunta("¿Qué es la idea principal?", listOf("a", "b", "", ""), 1))
    }

    @Test
    fun armarAlternativasDescartaVaciasYConservaLaCorrecta() {
        val alternativas = armarAlternativas(listOf("Uno", "", "Tres", "  "), indiceCorrecta = 2)

        assertEquals(2, alternativas.size)
        assertEquals(listOf("Uno", "Tres"), alternativas.map { it.texto })
        assertEquals(listOf(1, 2), alternativas.map { it.orden })
        assertFalse(alternativas[0].esCorrecta)
        assertTrue(alternativas[1].esCorrecta)
        assertEquals(1, alternativas.count { it.esCorrecta })
    }

    @Test
    fun mensajeDeErrorUsaElTextoDelServidor() {
        val cuerpo = """{"status":409,"message":"Solo se pueden editar actividades en BORRADOR"}"""
            .toResponseBody()
        val error = HttpException(Response.error<Any>(409, cuerpo))

        assertEquals(
            "Solo se pueden editar actividades en BORRADOR",
            mensajeDeError(error, "fallo")
        )
    }

    @Test
    fun mensajeDeErrorPrefiereCampoMensajeDelContrato() {
        val cuerpo = """{"codigo":"INTENTOS_AGOTADOS","mensaje":"No quedan intentos"}""".toResponseBody()
        val error = HttpException(Response.error<Any>(409, cuerpo))

        assertEquals("No quedan intentos", mensajeDeError(error, "fallo"))
    }

    @Test
    fun mensajeDeErrorSinCuerpoUsaElCodigoHttp() {
        val error = HttpException(Response.error<Any>(403, "".toResponseBody()))
        assertEquals("Tu perfil no tiene permiso para esta acción", mensajeDeError(error, "fallo"))

        assertTrue(mensajeDeError(IOException("sin red"), "fallo").startsWith("Sin conexión"))
        assertEquals("fallo", mensajeDeError(IllegalStateException(), "fallo"))
    }

    @Test
    fun idsDeObjetivosLeePaginadoYListaSimple() {
        val paginado = JsonParser.parseString("""{"items":[{"id":31},{"id":32},{"id":31}],"total":3}""")
        val lista = JsonParser.parseString("""[{"id":7}]""")
        val raro = JsonParser.parseString("""{"otro":1}""")

        assertEquals(listOf(31L, 32L), idsDeObjetivos(paginado))
        assertEquals(listOf(7L), idsDeObjetivos(lista))
        assertEquals(emptyList<Long>(), idsDeObjetivos(raro))
    }
}
