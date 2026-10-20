
package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.data.model.TipoActividad
import com.example.pruebaaplicacionesmoviles.viewmodel.ActividadViewModel

@Composable
fun ActividadScreen(
    estudianteId: Int,
    unidadId: Int,
    viewModel: ActividadViewModel,
    onVolver: () -> Unit
) {

    // OBSERVAMOS TODOS LOS CAMBIOS QUE REALIZA EL VIEWMODEL
    val estado by viewModel.estado.collectAsState()

    //  CARGA LAS ACTIVIDADES AL ENTRAR EN ESTA PANTALLA
    LaunchedEffect(estudianteId, unidadId) {
        viewModel.cargarUnidad(estudianteId, unidadId)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // CAMBIA EL BOTON SEGUN SI ESTA EN LA LISTA O EUNA ACTIVIDAD
        Button(
            onClick = {
                if (estado.actividad == null) {
                    onVolver()
                } else {
                    viewModel.volverALista()
                }
            }
        ) {
            Text("Volver")
        }


        Text(
            text = estado.actividad?.titulo ?: "Actividades disponibles",
            style = MaterialTheme.typography.headlineSmall
        )

        //  MUESTRA LOS MENSAJES GENERADOR POR EL VIEMODEL
        if (estado.mensaje.isNotEmpty()) {
            Text(
                text = estado.mensaje,
                color = if (estado.resultadoPorcentaje != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        //MIENTRAS CONSULTA EL ROOM, MUESTRA UN INDICADOR DE CARGA
        if (estado.cargando) {

            CircularProgressIndicator()

        } else if (estado.actividad == null) {

            // PRIMERA VISTA, LA LISTA DE ACTIVIDADES

            if (estado.actividades.isEmpty()) {
                Text("No hay actividades disponibles para esta unidad")
            }

            // RECORRE LAS ACTIVIDADES OBTENIDAS DEDE EL ROOM
            estado.actividades.forEach { actividad ->

                Card(
                    onClick = {
                        viewModel.abrirActividad(actividad)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = actividad.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = if (actividad.tipo == TipoActividad.PRUEBA_FINAL) {
                                "Prueba final"
                            } else {
                                "Práctica"
                            }
                        )

                        Text("Presiona para comenzar →")
                    }
                }
            }

        } else {

            // SEGUNDA VISTA: Preguntas de una actividad

            val actividad = estado.actividad!! // La actividad no es null en esta rama

            // Comprobamos si alcanzó el límite de intentos finales
            val bloqueada =
                actividad.tipo == TipoActividad.PRUEBA_FINAL &&
                        estado.intentosUsados >= estado.maxIntentos

            // Indicamos si es práctica o prueba final
            Text(
                text = if (actividad.tipo == TipoActividad.PRUEBA_FINAL) {
                    "Prueba final"
                } else {
                    "Actividad de práctica"
                },
                style = MaterialTheme.typography.titleMedium
            )

            // Para la prueba final mostramos cuántos intentos lleva
            if (actividad.tipo == TipoActividad.PRUEBA_FINAL) {
                Text(
                    "Intentos utilizados: ${estado.intentosUsados} / ${estado.maxIntentos}"
                )
            }

            // Si terminó y guardó, mostramos el porcentaje obtenido
            if (estado.resultadoPorcentaje != null) {

                Text(
                    text = "¡Actividad completada!",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Tu puntaje: ${estado.resultadoPorcentaje}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Si ya utilizó sus intentos, informamos el bloqueo
            if (bloqueada && estado.resultadoPorcentaje == null) {
                Text(
                    text = "Ya utilizaste todos los intentos de esta prueba",
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Si no existen preguntas, informamos la situación
            if (estado.preguntas.isEmpty()) {
                Text("Esta actividad todavía no tiene preguntas")
            }

            // Recorremos todas las preguntas en orden
            estado.preguntas.forEachIndexed { indicePregunta, pregunta ->

                Card(
                    modifier = Modifier.fillMaxWidth() // Tarjeta de pregunta
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = "Pregunta ${indicePregunta + 1} de ${estado.preguntas.size}",
                            style = MaterialTheme.typography.labelLarge
                        )

                        Text(
                            text = pregunta.enunciado, // Enunciado de la pregunta
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Convertimos las cuatro alternativas en una lista temporal
                        val alternativas = listOf(
                            pregunta.opcionA, // Índice 0
                            pregunta.opcionB, // Índice 1
                            pregunta.opcionC, // Índice 2
                            pregunta.opcionD // Índice 3
                        )

                        // Recorremos las cuatro alternativas de esta pregunta
                        alternativas.forEachIndexed { indiceAlternativa, texto ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        enabled = !bloqueada &&
                                                estado.resultadoPorcentaje == null &&
                                                !estado.guardando
                                    ) {
                                        // Informa al ViewModel qué opción escogimos
                                        viewModel.seleccionarRespuesta(
                                            pregunta.id,
                                            indiceAlternativa
                                        )
                                    },

                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                RadioButton(
                                    selected = estado.respuestas[pregunta.id] == indiceAlternativa,
                                    onClick = {
                                        viewModel.seleccionarRespuesta(
                                            pregunta.id,
                                            indiceAlternativa
                                        )
                                    },
                                    enabled = !bloqueada &&
                                            estado.resultadoPorcentaje == null &&
                                            !estado.guardando
                                )

                                Spacer(Modifier.width(8.dp))

                                Text(
                                    text = "${'A' + indiceAlternativa}. $texto"
                                )
                            }
                        }

                        // La retroalimentación aparece solo después de guardar
                        if (estado.resultadoPorcentaje != null) {

                            // Comparamos la alternativa elegida con la correcta
                            val correcta =
                                estado.respuestas[pregunta.id] == pregunta.indiceCorrecto

                            Text(
                                text = if (correcta) {
                                    "✅ Respuesta correcta"
                                } else {
                                    "❌ Respuesta incorrecta"
                                },
                                color = if (correcta) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )

                            // Mostramos cuál era la respuesta correcta
                            Text(
                                text = "Respuesta correcta: " +
                                        alternativas[pregunta.indiceCorrecto]
                            )

                            // Mostramos la explicación creada para esa pregunta
                            Text(pregunta.explicacionRespuesta)
                        }
                    }
                }
            }

            // Antes de terminar, permitimos enviar las respuestas
            if (estado.resultadoPorcentaje == null &&
                estado.preguntas.isNotEmpty()) {

                Button(
                    onClick = {
                        viewModel.finalizarActividad() // Valida y guarda las respuestas
                    },
                    enabled = !bloqueada && !estado.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (estado.guardando) "Guardando..." else "Finalizar actividad"
                    )
                }
            }

            // Después del resultado, ofrecemos volver a las actividades
            if (estado.resultadoPorcentaje != null) {

                Button(
                    onClick = {
                        viewModel.volverALista() // Vuelve a la lista de actividades
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver a las actividades")
                }
            }
        }
    }
}


