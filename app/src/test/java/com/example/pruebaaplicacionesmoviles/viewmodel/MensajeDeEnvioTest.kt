package com.example.pruebaaplicacionesmoviles.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MensajeDeEnvioTest {

    @Test
    fun sinPreguntasNoSeEnvia() {
        val mensaje = mensajeDeEnvio(0, emptySet(), emptySet())
        assertEquals("Esta actividad no tiene preguntas", mensaje)
    }

    @Test
    fun faltaUnaRespuesta() {
        val mensaje = mensajeDeEnvio(2, setOf(301), setOf(301, 302))
        assertEquals("Debes responder todas las preguntas", mensaje)
    }

    @Test
    fun todasRespondidasPuedeEnviar() {
        val mensaje = mensajeDeEnvio(2, setOf(301, 302), setOf(301, 302))
        assertNull(mensaje)
    }
}
