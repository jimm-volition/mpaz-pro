package com.example.pruebaaplicacionesmoviles.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ErroresAsignacionTest {

    @Test
    fun estudianteVacio() {
        val errores = erroresDeAsignacion(
            estudianteTexto = "",
            objetivoTexto = "31",
            fechaInicio = "2026-11-21",
            fechaFin = "2026-12-15",
            maxIntentosTexto = "2",
            confirmado = true,
            estudiantesIds = setOf(3),
            objetivosIds = setOf(31)
        )
        assertEquals("Ingresa el ID del estudiante", errores.errorEstudiante)
    }

    @Test
    fun objetivoQueNoEstaEnLaLista() {
        val errores = erroresDeAsignacion(
            estudianteTexto = "3",
            objetivoTexto = "99",
            fechaInicio = "2026-11-21",
            fechaFin = "2026-12-15",
            maxIntentosTexto = "2",
            confirmado = true,
            estudiantesIds = setOf(3),
            objetivosIds = setOf(31)
        )
        assertEquals("Ese objetivo no está disponible", errores.errorUnidad)
    }

    @Test
    fun fechaFinAnteriorAlInicio() {
        val errores = erroresDeAsignacion(
            estudianteTexto = "3",
            objetivoTexto = "31",
            fechaInicio = "2026-12-15",
            fechaFin = "2026-11-21",
            maxIntentosTexto = "2",
            confirmado = true,
            estudiantesIds = setOf(3),
            objetivosIds = setOf(31)
        )
        assertEquals(
            "La fecha fin no puede ser anterior al inicio",
            errores.errorFechaFin
        )
    }

    @Test
    fun estudianteQueNoEsNumero() {
        val errores = erroresDeAsignacion(
            estudianteTexto = "abc",
            objetivoTexto = "31",
            fechaInicio = "2026-11-21",
            fechaFin = "2026-12-15",
            maxIntentosTexto = "2",
            confirmado = true,
            estudiantesIds = setOf(3),
            objetivosIds = setOf(31)
        )
        assertEquals("Debe ser un número (ej: 3)", errores.errorEstudiante)
    }

    @Test
    fun estudianteQueNoEstaACargo() {
        val errores = erroresDeAsignacion(
            estudianteTexto = "9",
            objetivoTexto = "31",
            fechaInicio = "2026-11-21",
            fechaFin = "2026-12-15",
            maxIntentosTexto = "2",
            confirmado = true,
            estudiantesIds = setOf(3),
            objetivosIds = setOf(31)
        )
        assertEquals("Ese estudiante no está a tu cargo", errores.errorEstudiante)
    }

    @Test
    fun sinObjetivosDisponibles() {
        val errores = erroresDeAsignacion(
            estudianteTexto = "3",
            objetivoTexto = "31",
            fechaInicio = "2026-11-21",
            fechaFin = "2026-12-15",
            maxIntentosTexto = "2",
            confirmado = true,
            estudiantesIds = setOf(3),
            objetivosIds = emptySet()
        )
        assertEquals("No hay objetivos disponibles", errores.errorUnidad)
    }

    @Test
    fun fechaConFormatoIncorrecto() {
        val errores = erroresDeAsignacion(
            estudianteTexto = "3",
            objetivoTexto = "31",
            fechaInicio = "21-11-2026",
            fechaFin = "2026-12-15",
            maxIntentosTexto = "2",
            confirmado = true,
            estudiantesIds = setOf(3),
            objetivosIds = setOf(31)
        )
        assertEquals("Usa el formato AAAA-MM-DD", errores.errorFechaInicio)
    }

    @Test
    fun maximoDeIntentosMenorAUno() {
        val errores = erroresDeAsignacion(
            estudianteTexto = "3",
            objetivoTexto = "31",
            fechaInicio = "2026-11-21",
            fechaFin = "2026-12-15",
            maxIntentosTexto = "0",
            confirmado = true,
            estudiantesIds = setOf(3),
            objetivosIds = setOf(31)
        )
        assertEquals("Debe ser al menos 1", errores.errorMaxIntentos)
    }

    @Test
    fun sinConfirmacion() {
        val errores = erroresDeAsignacion(
            estudianteTexto = "3",
            objetivoTexto = "31",
            fechaInicio = "2026-11-21",
            fechaFin = "2026-12-15",
            maxIntentosTexto = "2",
            confirmado = false,
            estudiantesIds = setOf(3),
            objetivosIds = setOf(31)
        )
        assertEquals(
            "Debes confirmar que los datos son correctos",
            errores.errorConfirmacion
        )
    }
    @Test
    fun datosCorrectosNoTienenError() {
        val errores = erroresDeAsignacion(
            estudianteTexto = "3",
            objetivoTexto = "31",
            fechaInicio = "2026-11-21",
            fechaFin = "2026-12-15",
            maxIntentosTexto = "2",
            confirmado = true,
            estudiantesIds = setOf(3),
            objetivosIds = setOf(31)
        )
        assertFalse(errores.hayErrores())
    }
}