package com.example.pruebaaplicacionesmoviles.data.repository

import com.example.pruebaaplicacionesmoviles.data.local.ActividadEntity
import com.example.pruebaaplicacionesmoviles.data.local.AsignacionEntity
import com.example.pruebaaplicacionesmoviles.data.local.AsignaturaEntity
import com.example.pruebaaplicacionesmoviles.data.local.DatosSemilla
import com.example.pruebaaplicacionesmoviles.data.local.IntentoEntity
import com.example.pruebaaplicacionesmoviles.data.local.MpazDao
import com.example.pruebaaplicacionesmoviles.data.local.PreguntaEntity
import com.example.pruebaaplicacionesmoviles.data.local.ProgresoEntity
import com.example.pruebaaplicacionesmoviles.data.local.RespuestaEstudianteEntity
import com.example.pruebaaplicacionesmoviles.data.local.UnidadEntity
import com.example.pruebaaplicacionesmoviles.data.local.UsuarioEntity
import com.example.pruebaaplicacionesmoviles.data.model.Actividad
import com.example.pruebaaplicacionesmoviles.data.model.Asignacion
import com.example.pruebaaplicacionesmoviles.data.model.Asignatura
import com.example.pruebaaplicacionesmoviles.data.model.EstadoUnidad
import com.example.pruebaaplicacionesmoviles.data.model.Intento
import com.example.pruebaaplicacionesmoviles.data.model.Pregunta
import com.example.pruebaaplicacionesmoviles.data.model.Progreso
import com.example.pruebaaplicacionesmoviles.data.model.Rol
import com.example.pruebaaplicacionesmoviles.data.model.TipoActividad
import com.example.pruebaaplicacionesmoviles.data.model.Unidad
import com.example.pruebaaplicacionesmoviles.data.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoomMpazRepository(
    private val dao: MpazDao
) : MpazRepository {

    override suspend fun preparar() {
        if (dao.contarUsuarios() > 0) return
        dao.insertarUsuarios(DatosSemilla.usuarios)
        dao.insertarAsignaturas(DatosSemilla.asignaturas)
        dao.insertarUnidades(DatosSemilla.unidades)
        dao.insertarActividades(DatosSemilla.actividades)
        dao.insertarPreguntas(DatosSemilla.preguntas)
        dao.insertarAsignaciones(DatosSemilla.asignaciones)
    }

    override suspend fun buscarUsuario(alias: String): Usuario? {
        return dao.buscarUsuario(alias.trim())?.aModelo()
    }

    override fun observarAsignaturas(): Flow<List<Asignatura>> {
        return dao.observarAsignaturas().map { lista -> lista.map { it.aModelo() } }
    }

    override fun observarUnidades(): Flow<List<Unidad>> {
        return dao.observarUnidades().map { lista -> lista.map { it.aModelo() } }
    }

    override fun observarUnidadesAsignadas(estudianteId: Int): Flow<List<Unidad>> {
        return dao.observarUnidadesAsignadas(estudianteId).map { lista -> lista.map { it.aModelo() } }
    }

    override fun observarProgreso(estudianteId: Int): Flow<List<Progreso>> {
        return dao.observarProgreso(estudianteId).map { lista -> lista.map { it.aModelo() } }
    }

    override fun observarIntentosDocente(docenteId: Int): Flow<List<Intento>> {
        return dao.observarIntentosDocente(docenteId).map { lista -> lista.map { it.aModelo() } }
    }

    override suspend fun obtenerActividades(unidadId: Int): List<Actividad> {
        return dao.obtenerActividades(unidadId).map { it.aModelo() }
    }

    override suspend fun obtenerPreguntas(actividadId: Int): List<Pregunta> {
        return dao.obtenerPreguntas(actividadId).map { it.aModelo() }
    }

    override suspend fun obtenerAsignacion(estudianteId: Int, unidadId: Int): Asignacion? {
        return dao.obtenerAsignacion(estudianteId, unidadId)?.aModelo()
    }

    override suspend fun contarIntentosFinales(asignacionId: Int, actividadId: Int): Int {
        return dao.contarIntentos(asignacionId, actividadId)
    }

    override suspend fun marcarContenidoVisto(estudianteId: Int, unidadId: Int) {
        val actual = dao.obtenerProgreso(estudianteId, unidadId)
        if (actual == null) {
            dao.insertarProgreso(
                ProgresoEntity(
                    id = dao.maxIdProgreso() + 1,
                    estudianteId = estudianteId,
                    unidadId = unidadId,
                    contenidoVisto = true
                )
            )
        } else if (!actual.contenidoVisto) {
            dao.actualizarProgreso(actual.copy(contenidoVisto = true))
        }
    }

    override suspend fun crearAsignacion(asignacion: Asignacion) {
        val id = if (asignacion.id > 0) asignacion.id else dao.maxIdAsignacion() + 1
        dao.insertarAsignacion(asignacion.copy(id = id).aEntity())
    }

    override suspend fun cambiarEstadoUnidad(unidadId: Int, estado: EstadoUnidad) {
        val unidad = dao.obtenerUnidad(unidadId) ?: return
        dao.actualizarUnidad(unidad.copy(estado = estado.name))
    }

    override suspend fun actualizarImagen(unidadId: Int, uriTexto: String) {
        val unidad = dao.obtenerUnidad(unidadId) ?: return
        dao.actualizarUnidad(unidad.copy(imagenReferencia = uriTexto))
    }

    override suspend fun guardarResultado(
        asignacionId: Int,
        actividadId: Int,
        respuestasPorPregunta: Map<Int, Int>
    ): Int {
        val preguntas = dao.obtenerPreguntas(actividadId)
        var correctas = 0

        val detalle = preguntas.map { pregunta ->
            val elegido = respuestasPorPregunta[pregunta.id] ?: -1
            val ok = elegido == pregunta.indiceCorrecto
            if (ok) correctas++
            Triple(pregunta.id, elegido, ok)
        }

        val total = preguntas.size
        val porcentaje = if (total == 0) 0 else (100 * correctas) / total
        val intentoId = dao.maxIdIntento() + 1
        val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        dao.insertarIntento(
            IntentoEntity(
                id = intentoId,
                asignacionId = asignacionId,
                actividadId = actividadId,
                fechaHora = fecha,
                correctas = correctas,
                totalPreguntas = total,
                porcentaje = porcentaje
            )
        )

        var respuestaId = dao.maxIdRespuesta()
        val respuestas = detalle.map { (preguntaId, elegido, ok) ->
            respuestaId++
            RespuestaEstudianteEntity(
                id = respuestaId,
                intentoId = intentoId,
                preguntaId = preguntaId,
                indiceSeleccionado = elegido,
                esCorrecta = ok
            )
        }
        dao.insertarRespuestas(respuestas)

        val asignacion = dao.obtenerAsignacionPorId(asignacionId)
        if (asignacion != null) {
            val progreso = dao.obtenerProgreso(asignacion.estudianteId, asignacion.unidadId)
            if (progreso == null) {
                dao.insertarProgreso(
                    ProgresoEntity(
                        id = dao.maxIdProgreso() + 1,
                        estudianteId = asignacion.estudianteId,
                        unidadId = asignacion.unidadId,
                        contenidoVisto = true,
                        actividadCompletada = true,
                        porcentaje = porcentaje
                    )
                )
            } else {
                dao.actualizarProgreso(
                    progreso.copy(
                        actividadCompletada = true,
                        porcentaje = maxOf(progreso.porcentaje, porcentaje)
                    )
                )
            }
        }

        return intentoId
    }
}

// --- Conversiones Entity <-> modelo del contrato ---

private fun UsuarioEntity.aModelo() = Usuario(id, alias, Rol.valueOf(rol), curso, docenteId)

private fun AsignaturaEntity.aModelo() = Asignatura(id, nombre)

private fun UnidadEntity.aModelo() = Unidad(
    id, asignaturaId, titulo, codigoOA, descripcionOA, ejeCurricular,
    explicacion, ejemploGuiado, imagenReferencia, textoParaAudio,
    EstadoUnidad.valueOf(estado), origenMaterial
)

private fun ActividadEntity.aModelo() = Actividad(
    id, unidadId, titulo, TipoActividad.valueOf(tipo), orden
)

private fun PreguntaEntity.aModelo() = Pregunta(
    id, actividadId, enunciado, opcionA, opcionB, opcionC, opcionD,
    indiceCorrecto, explicacionRespuesta, orden
)

private fun AsignacionEntity.aModelo() = Asignacion(
    id, docenteId, estudianteId, unidadId, fechaInicio, fechaFin, maxIntentos, activa
)

private fun Asignacion.aEntity() = AsignacionEntity(
    id, docenteId, estudianteId, unidadId, fechaInicio, fechaFin, maxIntentos, activa
)

private fun IntentoEntity.aModelo() = Intento(
    id, asignacionId, actividadId, fechaHora, correctas, totalPreguntas, porcentaje
)

private fun ProgresoEntity.aModelo() = Progreso(
    id, estudianteId, unidadId, contenidoVisto, actividadCompletada, porcentaje
)