package com.example.pruebaaplicacionesmoviles.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaaplicacionesmoviles.MpazApp
import com.example.pruebaaplicacionesmoviles.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActividadUiState(
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val actividades: List<Actividad> = emptyList(),
    val actividad: Actividad? = null,
    val preguntas: List<Pregunta> = emptyList(),
    val respuestas: Map<Int, Int> = emptyMap(),
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


    fun cargarUnidad(estudianteId: Int, unidadId: Int) {

        _estado.value = ActividadUiState(cargando = true) // REINICIA LA PANTALLA
        asignacionActual = null // LIMPIA LA ASIGNACION ANTERIOR


        viewModelScope.launch { //INICIA LA CONSULTA SIN BLOQUEAR LA PANTALLA
            try {

                // VERIFICA SI EL ESTUDIANTE TIENE ASIGNADA ESTA UNIDAD
                val asignacion = repositorio.obtenerAsignacion(
                    estudianteId,
                    unidadId
                )


                // SI NO EXISTE ASIGNACION, SE IMPIDE ABRIR LA ACTIVIDAD
                if (asignacion == null) {
                    _estado.value = ActividadUiState(
                        mensaje = "Esta unidad no está asignada a tu perfil"
                    )
                    return@launch // Finaliza esta corrutina
                }

                asignacionActual = asignacion // GUARDA LA ASIGNACION ENCONTRADA

                // OBTIENE LAS ACTIVIDADES QUE PERTENECEN A LA UNIDAD
                val lista = repositorio.obtenerActividades(unidadId)
                    .sortedBy { it.orden }

                //ESTO ACTUALIZA LA VISTA QUE OBSERVARA LA PANTALLA

                _estado.value = ActividadUiState(
                    actividades = lista
                )

            } catch (e: Exception) {

                // ESTO INFORMA SI FALLO LA FAKIN CONSULTA
                _estado.value = ActividadUiState(
                    mensaje = "No se pudieron cargar las actividades"
                )
            }
        }
    }

    fun abrirActividad(actividad: Actividad) {

        val asignacion = asignacionActual ?: return // Necesitamos una asignación válida


        _estado.value = _estado.value.copy(
            cargando = true,
            actividad = actividad,
            preguntas = emptyList(),
            respuestas = emptyMap(),
            resultadoPorcentaje = null,
            mensaje = ""
        )

        viewModelScope.launch {
            try {


                val preguntas = repositorio.obtenerPreguntas(actividad.id)
                    .sortedBy { it.orden }


                val usados = if (actividad.tipo == TipoActividad.PRUEBA_FINAL) {
                    repositorio.contarIntentosFinales(asignacion.id, actividad.id)
                } else {
                    0
                }


                _estado.value = _estado.value.copy(
                    cargando = false,
                    preguntas = preguntas,
                    intentosUsados = usados,
                    maxIntentos = asignacion.maxIntentos
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

    fun finalizarActividad() { //ESTO COMPRUEBA LAS RESPUESTAS Y SOLICITA GUARDARLAS EN ROOM

        val datos = _estado.value // OBTIENE UNA COPIA DEL ESTADO ACTUAL
        val actividad = datos.actividad ?: return // NECESITAMOS QUE DEVUELVA UNA ACTIVIDAD
        val asignacion = asignacionActual ?: return // NECESITAMOS QUE DEVUELVA UNA ASIGNACIO

        if (datos.guardando || datos.resultadoPorcentaje != null) return // EVITA QUE HAYAN DATOS DUPLICADOS


        //ESTE IF NO DEJARA QUE FINALIZAR ACTIVIDADES SIN PREGUNTAS
        if (datos.preguntas.isEmpty()) {
            _estado.value = datos.copy(mensaje = "Esta actividad no tiene preguntas")
            return
        }

        // ESTO COMPROBARA QUE TODAS LAS ACTIVIDADES TENGAN UNA ALTERNATIVA ELEGIDDA
        val faltanRespuestas = datos.preguntas.any {
            it.id !in datos.respuestas
        }


        if (faltanRespuestas) {
            _estado.value = datos.copy(mensaje = "Debes responder todas las preguntas")
            return
        }

        //ESTO CALCULA CUANTAS RESPUESTAS SON CORRECTAS
        val correctas = datos.preguntas.count {
            datos.respuestas[it.id] == it.indiceCorrecto
        }


        val porcentaje = correctas * 100 / datos.preguntas.size

        _estado.value = datos.copy(guardando = true, mensaje = "")

        viewModelScope.launch {

            try {
                var intentos = datos.intentosUsados //GUARDA EL CONTADOR ACTUAL


                // ANTES DE GUARDAR VERIFICA NUEVAMENTE EL LIMITE DE PRUEBA FINAL
                if (actividad.tipo == TipoActividad.PRUEBA_FINAL) {

                    intentos = repositorio.contarIntentosFinales(
                        asignacion.id,
                        actividad.id
                    )

                }

                //INPIDE GUARDAR SI YA SE USARON TODOS LOS INTENTOS WUAJAJJA
                if (intentos >= asignacion.maxIntentos) {
                    _estado.value = _estado.value.copy(
                        guardando = false,
                        intentosUsados = intentos,
                        mensaje = "Ya utilizaste todos tus intentos"
                    )
                    return@launch
                }


                // EL REPOSITORY GUARDA LOS INTENTOS, LAS RESPUESTAS Y EL PROGRESO
                repositorio.guardarResultado(
                    asignacionId = asignacion.id,
                    actividadId = actividad.id,
                    respuestasPorPregunta = datos.respuestas
                )

                //SOLO DESPUES DE HABER GHUARDADO SE MOSTRARA EL RESULTADO
                _estado.value = _estado.value.copy(
                    guardando = false,
                    resultadoPorcentaje = porcentaje,
                    intentosUsados = if (actividad.tipo == TipoActividad.PRUEBA_FINAL) {
                        intentos + 1
                    } else {
                        intentos
                    },
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