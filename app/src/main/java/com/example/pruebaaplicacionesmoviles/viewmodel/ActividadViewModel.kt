package com.example.pruebaaplicacionesmoviles.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaaplicacionesmoviles.MpazApp
import com.example.pruebaaplicacionesmoviles.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.pruebaaplicacionesmoviles.core.network.ApiCliente
import com.example.pruebaaplicacionesmoviles.core.network.ContenidoApiCliente
import com.google.gson.JsonElement
import com.example.pruebaaplicacionesmoviles.core.network.EnvioActividadRequest
import com.example.pruebaaplicacionesmoviles.core.network.FeedbackRemoto
import com.example.pruebaaplicacionesmoviles.core.network.RespuestaEnviadaRequest
import com.example.pruebaaplicacionesmoviles.core.network.RespuestaSeleccionRequest
import java.time.Instant
import java.util.UUID
import com.example.pruebaaplicacionesmoviles.core.network.EntregaIntentoRequest
import com.example.pruebaaplicacionesmoviles.core.network.InicioIntentoRequest
import com.example.pruebaaplicacionesmoviles.core.network.PruebaRemota

data class ActividadUiState(
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val actividades: List<Actividad> = emptyList(),
    val actividad: Actividad? = null,
    val preguntas: List<Pregunta> = emptyList(),
    val respuestas: Map<Int, Int> = emptyMap(),
    val opcionesPorPregunta: Map<Int, List<Long>> = emptyMap(),
    val feedback: List<FeedbackRemoto> = emptyList(),
    val intentoId: Long? = null,
    val intentosUsados: Int = 0,
    val maxIntentos: Int = 0,
    val resultadoPorcentaje: Int? = null,
    val mensaje: String = ""
)

class ActividadViewModel(application: Application):AndroidViewModel(application) {

    //EL REPOSITORY
    private val repositorio = (application as MpazApp).repositorio

    private val _estado = MutableStateFlow(ActividadUiState())

    val estado = _estado.asStateFlow()

    //GUARDA LA ASIGNACION DEL ESTUDIANTE PARA UNA UNIDAD
    private var asignacionActual: Asignacion? = null
    private var pruebaActual: PruebaRemota? = null

    // OA de cada prueba final listada, para volver a consultar su cupo al abrirla.
    private val objetivoDePrueba = mutableMapOf<Int, Long>()

    fun cargarUnidad(estudianteId: Int, unidadId: Int) {
        _estado.value = ActividadUiState(cargando = true)
        asignacionActual = null
        pruebaActual = null
        objetivoDePrueba.clear()

        viewModelScope.launch {
            try {
                // OA publicados y asignados de la unidad (ms-contenidos filtra por el token).
                val objetivos = idsDeObjetivos(
                    ContenidoApiCliente.servicio.listarObjetivos(unidadId)
                )
                val lista = mutableListOf<Actividad>()

                for (objetivoId in objetivos) {
                    ApiCliente.actividades.listar(objetivoId).forEach { remota ->
                        lista += Actividad(
                            id = remota.id.toInt(),
                            unidadId = unidadId,
                            titulo = remota.titulo,
                            tipo = TipoActividad.PRACTICA,
                            orden = lista.size
                        )
                    }
                }

                for (objetivoId in objetivos) {
                    val prueba = try {
                        ApiCliente.pruebas.obtener(objetivoId)
                    } catch (e: Exception) {
                        null
                    } ?: continue
                    pruebaActual = prueba
                    objetivoDePrueba[prueba.pruebaId.toInt()] = objetivoId
                    lista += Actividad(
                        id = prueba.pruebaId.toInt(),
                        unidadId = unidadId,
                        titulo = if (objetivos.size > 1) "Prueba final (OA $objetivoId)" else "Prueba final",
                        tipo = TipoActividad.PRUEBA_FINAL,
                        orden = lista.size
                    )
                }

                _estado.value = ActividadUiState(
                    actividades = lista,
                    mensaje = if (lista.isEmpty()) "Aún no hay actividades publicadas para esta unidad" else ""
                )
            } catch (e: Exception) {
                _estado.value = ActividadUiState(
                    mensaje = mensajeDeError(e, "No se pudieron cargar las actividades")
                )
            }
        }
    }

    fun abrirActividad(actividad: Actividad) {
        _estado.value = _estado.value.copy(
            cargando = true,
            actividad = actividad,
            preguntas = emptyList(),
            respuestas = emptyMap(),
            opcionesPorPregunta = emptyMap(),
            feedback = emptyList(),
            intentoId = null,
            resultadoPorcentaje = null,
            mensaje = ""
        )

        viewModelScope.launch {
            try {
                if (actividad.tipo == TipoActividad.PRUEBA_FINAL) {
                    val objetivoId = objetivoDePrueba[actividad.id]
                        ?: throw IllegalStateException("Prueba sin objetivo")
                    val prueba = ApiCliente.pruebas.obtener(objetivoId)
                    pruebaActual = prueba
                    if (!prueba.puedeRendir) {
                        val aviso = if (prueba.intentosRestantes <= 0) {
                            "Ya utilizaste todos los intentos de esta prueba"
                        } else {
                            "La prueba esta fuera de plazo"
                        }
                        _estado.value = _estado.value.copy(
                            cargando = false,
                            intentosUsados = prueba.limiteIntentos,
                            maxIntentos = prueba.limiteIntentos,
                            mensaje = aviso
                        )
                        return@launch
                    }

                    val intento = ApiCliente.pruebas.iniciar(
                        prueba.pruebaId,
                        InicioIntentoRequest(UUID.randomUUID().toString())
                    )
                    val remotas = ApiCliente.pruebas.preguntas(intento.id)
                    val preguntas = mutableListOf<Pregunta>()
                    val ids = mutableMapOf<Int, List<Long>>()

                    remotas.forEachIndexed { orden, pregunta ->
                        val textos = pregunta.opciones.map { it.texto }
                        ids[pregunta.id.toInt()] = pregunta.opciones.map { it.id }
                        preguntas += Pregunta(
                            id = pregunta.id.toInt(),
                            actividadId = actividad.id,
                            enunciado = pregunta.enunciado,
                            opcionA = textos.getOrElse(0) { "" },
                            opcionB = textos.getOrElse(1) { "" },
                            opcionC = textos.getOrElse(2) { "" },
                            opcionD = textos.getOrElse(3) { "" },
                            indiceCorrecto = -1,
                            explicacionRespuesta = "",
                            orden = orden
                        )
                    }

                    _estado.value = _estado.value.copy(
                        cargando = false,
                        preguntas = preguntas,
                        opcionesPorPregunta = ids,
                        intentoId = intento.id,
                        intentosUsados = prueba.intentosUtilizados + 1,
                        maxIntentos = prueba.limiteIntentos
                    )
                    return@launch
                }

                val remota = ApiCliente.actividades.obtener(actividad.id.toLong())
                val preguntas = mutableListOf<Pregunta>()
                val ids = mutableMapOf<Int, List<Long>>()

                remota.preguntas.orEmpty().forEachIndexed { orden, pregunta ->
                    val textos = pregunta.opciones.map { it.texto }
                    ids[pregunta.id.toInt()] = pregunta.opciones.map { it.id }
                    preguntas += Pregunta(
                        id = pregunta.id.toInt(),
                        actividadId = actividad.id,
                        enunciado = pregunta.enunciado,
                        opcionA = textos.getOrElse(0) { "" },
                        opcionB = textos.getOrElse(1) { "" },
                        opcionC = textos.getOrElse(2) { "" },
                        opcionD = textos.getOrElse(3) { "" },
                        indiceCorrecto = -1,
                        explicacionRespuesta = "",
                        orden = orden
                    )
                }

                _estado.value = _estado.value.copy(
                    cargando = false,
                    preguntas = preguntas,
                    opcionesPorPregunta = ids
                )
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(
                    cargando = false,
                    mensaje = "No se pudieron cargar las preguntas"
                )
            }
        }
    }


    fun seleccionarRespuesta(preguntaId: Int, alternativa: Int) {

        // IMPIDE MODIFICAR LAS RESPUESTAS SI YA ESTAMOS GUARDANDO O YA TERMINAMOS
        if (_estado.value.guardando || _estado.value.resultadoPorcentaje != null) {
            return
        }

        // GUARDA LA ALTERNATIVA ASOCIANDOLA AL IDENTIFICADOR DE SU PREGUNTA
        _estado.value = _estado.value.copy(
            respuestas = _estado.value.respuestas + (preguntaId to alternativa),
            mensaje = ""
        )

    }

    fun finalizarActividad() {
        val datos = _estado.value
        val actividad = datos.actividad ?: return

        if (datos.guardando || datos.resultadoPorcentaje != null) return

        val aviso = mensajeDeEnvio(
            datos.preguntas.size,
            datos.respuestas.keys,
            datos.preguntas.map { it.id }.toSet()
        )
        if (aviso != null) {
            _estado.value = datos.copy(mensaje = aviso)
            return
        }

        val enviadas = mutableListOf<RespuestaEnviadaRequest>()
        for (pregunta in datos.preguntas) {
            val indice = datos.respuestas[pregunta.id] ?: return
            val alternativaId = datos.opcionesPorPregunta[pregunta.id]?.getOrNull(indice)
            if (alternativaId == null) {
                _estado.value = datos.copy(mensaje = "La alternativa elegida no es valida")
                return
            }
            enviadas += RespuestaEnviadaRequest(
                preguntaId = pregunta.id.toLong(),
                respuesta = RespuestaSeleccionRequest(
                    tipo = "SELECCION",
                    alternativaId = alternativaId
                )
            )
        }

        _estado.value = datos.copy(guardando = true, mensaje = "")

        viewModelScope.launch {
            try {
                val puntaje: Int
                val feedback: List<FeedbackRemoto>
                if (actividad.tipo == TipoActividad.PRUEBA_FINAL) {
                    val intentoId = datos.intentoId
                        ?: throw IllegalStateException("No hay un intento abierto")
                    val resultado = ApiCliente.pruebas.entregar(
                        intentoId,
                        EntregaIntentoRequest(
                            respuestas = enviadas,
                            respondidaEn = Instant.now().toString()
                        )
                    )
                    puntaje = resultado.puntaje
                    feedback = resultado.feedback
                } else {
                    val resultado = ApiCliente.actividades.enviar(
                        actividad.id.toLong(),
                        EnvioActividadRequest(
                            clientEventId = UUID.randomUUID().toString(),
                            respuestas = enviadas,
                            respondidaEn = Instant.now().toString()
                        )
                    )
                    puntaje = resultado.puntaje
                    feedback = resultado.feedback
                }
                _estado.value = _estado.value.copy(
                    guardando = false,
                    resultadoPorcentaje = puntaje,
                    feedback = feedback,
                    mensaje = "Resultado guardado correctamente"
                )
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(
                    guardando = false,
                    mensaje = "No se pudo guardar el resultado"
                )
            }
        }
    }


    fun volverALista() {
        _estado.value = _estado.value.copy(
            actividad = null, // Deja de mostrar las preguntas
            preguntas = emptyList(), // Limpia la lista de preguntas
            respuestas = emptyMap(), // Limpia las respuestas elegidas
            resultadoPorcentaje = null, // Limpia el resultado
            mensaje = "" // Limpia los mensajes
        )
    }
}

fun mensajeDeEnvio(
    cantidadPreguntas: Int,
    idsRespondidos: Set<Int>,
    idsPreguntas: Set<Int>
): String? {
    if (cantidadPreguntas == 0) {
        return "Esta actividad no tiene preguntas"
    }
    if (idsPreguntas.any { it !in idsRespondidos }) {
        return "Debes responder todas las preguntas"
    }
    return null
}

/** Extrae los id de OA de una respuesta paginada ({items:[]}) o de una lista simple. */
fun idsDeObjetivos(respuesta: JsonElement): List<Long> {
    val lista = when {
        respuesta.isJsonArray -> respuesta.asJsonArray
        respuesta.isJsonObject && respuesta.asJsonObject.has("items") ->
            respuesta.asJsonObject.getAsJsonArray("items")
        else -> return emptyList()
    }
    return lista.mapNotNull { elemento ->
        elemento.takeIf { it.isJsonObject }?.asJsonObject?.get("id")
            ?.takeUnless { it.isJsonNull }?.asLong
    }.distinct()
}
