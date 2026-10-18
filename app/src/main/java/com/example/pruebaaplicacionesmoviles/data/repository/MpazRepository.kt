package com.example.pruebaaplicacionesmoviles.data.repository

import com.example.pruebaaplicacionesmoviles.data.model.Actividad
import com.example.pruebaaplicacionesmoviles.data.model.Asignacion
import com.example.pruebaaplicacionesmoviles.data.model.Asignatura
import com.example.pruebaaplicacionesmoviles.data.model.EstadoUnidad
import com.example.pruebaaplicacionesmoviles.data.model.Intento
import com.example.pruebaaplicacionesmoviles.data.model.Pregunta
import com.example.pruebaaplicacionesmoviles.data.model.Progreso
import com.example.pruebaaplicacionesmoviles.data.model.Unidad
import com.example.pruebaaplicacionesmoviles.data.model.Usuario
import kotlinx.coroutines.flow.Flow

interface MpazRepository {
    suspend fun preparar()
    suspend fun buscarUsuario(alias: String): Usuario?

    fun observarAsignaturas(): Flow<List<Asignatura>>
    fun observarUnidades(): Flow<List<Unidad>>
    fun observarUnidadesAsignadas(estudianteId: Int): Flow<List<Unidad>>
    fun observarProgreso(estudianteId: Int): Flow<List<Progreso>>
    fun observarIntentosDocente(docenteId: Int): Flow<List<Intento>>

    suspend fun obtenerActividades(unidadId: Int): List<Actividad>
    suspend fun obtenerPreguntas(actividadId: Int): List<Pregunta>
    suspend fun obtenerAsignacion(estudianteId: Int, unidadId: Int): Asignacion?
    suspend fun contarIntentosFinales(asignacionId: Int, actividadId: Int): Int

    suspend fun marcarContenidoVisto(estudianteId: Int, unidadId: Int)
    suspend fun crearAsignacion(asignacion: Asignacion)
    suspend fun cambiarEstadoUnidad(unidadId: Int, estado: EstadoUnidad)
    suspend fun actualizarImagen(unidadId: Int, uriTexto: String)

    suspend fun guardarResultado(
        asignacionId: Int,
        actividadId: Int,
        respuestasPorPregunta: Map<Int, Int>
    ): Int
}