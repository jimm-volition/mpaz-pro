package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.core.network.UtpPreguntaRemota
import com.example.pruebaaplicacionesmoviles.viewmodel.ContenedorPreguntas
import com.example.pruebaaplicacionesmoviles.viewmodel.UtpActividadesViewModel
import com.example.pruebaaplicacionesmoviles.viewmodel.nombreAccionEvaluacion
import com.example.pruebaaplicacionesmoviles.viewmodel.siguienteEstadoEvaluacion

private const val MAX_ALTERNATIVAS = 4

/**
 * Subpantalla UTP (integrante B): actividades de práctica, prueba final y sus preguntas
 * para un OA. Se abre desde el paso "Objetivos" de la gestión de contenidos UTP.
 */
@Composable
fun UtpActividadesScreen(
    objetivoId: Int,
    codigoObjetivo: String,
    viewModel: UtpActividadesViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val desplazamiento = rememberScrollState()

    LaunchedEffect(objetivoId) {
        viewModel.abrir(objetivoId.toLong(), codigoObjetivo)
    }

    val contenedor = estado.contenedor
    BackHandler {
        if (contenedor != null) viewModel.cerrarPreguntas() else onVolver()
    }

    var pestana by remember { mutableIntStateOf(0) }
    var confirmarDeshabilitar by remember { mutableStateOf<Pair<String, Long>?>(null) }

    // Formulario actividad
    var formActividad by remember { mutableStateOf(false) }
    var actividadEditando by remember { mutableStateOf<Long?>(null) }
    var tituloActividad by remember { mutableStateOf("") }
    var ordenActividad by remember { mutableStateOf("1") }

    // Formulario prueba
    var formPrueba by remember { mutableStateOf(false) }
    var pruebaEditando by remember { mutableStateOf<Long?>(null) }
    var tituloPrueba by remember { mutableStateOf("") }
    var desdePrueba by remember { mutableStateOf("") }
    var hastaPrueba by remember { mutableStateOf("") }
    var limitePrueba by remember { mutableStateOf("2") }

    fun cerrarFormularios() {
        formActividad = false
        actividadEditando = null
        tituloActividad = ""
        ordenActividad = ((estado.actividades.maxOfOrNull { it.orden } ?: 0) + 1).toString()
        formPrueba = false
        pruebaEditando = null
        tituloPrueba = ""
        desdePrueba = ""
        hastaPrueba = ""
        limitePrueba = "2"
    }

    // El servidor confirmó un guardado: cerramos el formulario abierto.
    LaunchedEffect(estado.guardadosOk) {
        if (estado.guardadosOk > 0 && contenedor == null) cerrarFormularios()
    }

    LaunchedEffect(contenedor, pestana, formActividad, formPrueba) {
        desplazamiento.scrollTo(0)
    }

    confirmarDeshabilitar?.let { (tipo, id) ->
        AlertDialog(
            onDismissRequest = { confirmarDeshabilitar = null },
            title = { Text("Deshabilitar $tipo") },
            text = { Text("Los estudiantes dejarán de verla y no se puede volver a publicar. ¿Continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    if (tipo == "actividad") {
                        viewModel.transicionarActividad(id, "DESHABILITADO")
                    } else {
                        viewModel.transicionarPrueba(id, "DESHABILITADO")
                    }
                    confirmarDeshabilitar = null
                }) { Text("Deshabilitar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarDeshabilitar = null }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(desplazamiento)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        TextButton(onClick = {
            if (contenedor != null) viewModel.cerrarPreguntas() else onVolver()
        }) {
            Text(if (contenedor != null) "← Volver a la lista" else "← Volver a objetivos")
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("ACTIVIDADES Y EVALUACIONES UTP", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (contenedor != null) "Preguntas: ${contenedor.titulo}" else "Objetivo $codigoObjetivo",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "El estudiante solo verá lo PUBLICADO y asignado por el docente, " +
                        "con la unidad y el OA también publicados.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        if (estado.ocupado) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            if (estado.actividades.isEmpty() && estado.pruebas.isEmpty() && contenedor == null) {
                TextButton(onClick = viewModel::recargar) { Text("Reintentar") }
            }
            Spacer(Modifier.height(8.dp))
        }
        AnimatedVisibility(visible = estado.mensaje != null) {
            Text(estado.mensaje.orEmpty(), color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(8.dp))

        if (contenedor != null) {
            EditorPreguntas(
                contenedor = contenedor,
                preguntas = estado.preguntas,
                ocupado = estado.ocupado,
                guardadosOk = estado.guardadosOk,
                onGuardar = viewModel::guardarPregunta
            )
            return@Column
        }

        TabRow(selectedTabIndex = pestana) {
            Tab(
                selected = pestana == 0,
                onClick = { pestana = 0; viewModel.limpiarAvisos() },
                text = { Text("Prácticas (${estado.actividades.size})") }
            )
            Tab(
                selected = pestana == 1,
                onClick = { pestana = 1; viewModel.limpiarAvisos() },
                text = { Text("Prueba final (${estado.pruebas.size})") }
            )
        }
        Spacer(Modifier.height(12.dp))

        if (pestana == 0) {
            // ------------------ ACTIVIDADES ------------------
            FilledTonalButton(
                onClick = {
                    if (formActividad && actividadEditando == null) {
                        cerrarFormularios()
                    } else {
                        cerrarFormularios()
                        formActividad = true
                    }
                },
                enabled = !estado.ocupado,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (formActividad && actividadEditando == null) "Cerrar formulario" else "＋ Crear actividad de práctica") }

            if (formActividad) {
                Spacer(Modifier.height(10.dp))
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            if (actividadEditando == null) "Nueva actividad" else "Editar actividad #$actividadEditando",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Tipo: selección múltiple", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        CampoEvaluacion(tituloActividad, { tituloActividad = it }, "Título de la actividad")
                        CampoEvaluacion(ordenActividad, { ordenActividad = it }, "Orden")
                        Button(
                            onClick = {
                                viewModel.guardarActividad(actividadEditando, tituloActividad, ordenActividad)
                            },
                            enabled = !estado.ocupado,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (actividadEditando == null) "Guardar actividad" else "Guardar cambios") }
                        TextButton(onClick = { cerrarFormularios() }) { Text("Cancelar") }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            if (estado.actividades.isEmpty() && !estado.ocupado && estado.error == null) {
                Text("Este objetivo aún no tiene actividades.")
            }
            estado.actividades.forEach { actividad ->
                val id = actividad.id ?: return@forEach
                val estadoActual = actividad.estado ?: "BORRADOR"
                TarjetaEvaluacion(
                    titulo = actividad.titulo,
                    detalle = "Orden ${actividad.orden} · ${actividad.tipo}",
                    estado = estadoActual,
                    ocupado = estado.ocupado,
                    onPreguntas = {
                        viewModel.abrirPreguntas(
                            ContenedorPreguntas.DeActividad(id, actividad.titulo, estadoActual)
                        )
                    },
                    onEditar = {
                        cerrarFormularios()
                        actividadEditando = id
                        tituloActividad = actividad.titulo
                        ordenActividad = actividad.orden.toString()
                        formActividad = true
                    },
                    onTransicion = { destino ->
                        if (destino == "DESHABILITADO") {
                            confirmarDeshabilitar = "actividad" to id
                        } else {
                            viewModel.transicionarActividad(id, destino)
                        }
                    }
                )
            }
        } else {
            // ------------------ PRUEBA FINAL ------------------
            FilledTonalButton(
                onClick = {
                    if (formPrueba && pruebaEditando == null) {
                        cerrarFormularios()
                    } else {
                        cerrarFormularios()
                        formPrueba = true
                    }
                },
                enabled = !estado.ocupado,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (formPrueba && pruebaEditando == null) "Cerrar formulario" else "＋ Crear prueba final") }

            if (formPrueba) {
                Spacer(Modifier.height(10.dp))
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            if (pruebaEditando == null) "Nueva prueba final" else "Editar prueba #$pruebaEditando",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        CampoEvaluacion(tituloPrueba, { tituloPrueba = it }, "Título de la prueba")
                        CampoEvaluacion(desdePrueba, { desdePrueba = it }, "Disponible desde (AAAA-MM-DD, opcional)")
                        CampoEvaluacion(hastaPrueba, { hastaPrueba = it }, "Disponible hasta (AAAA-MM-DD, opcional)")
                        CampoEvaluacion(limitePrueba, { limitePrueba = it }, "Límite de intentos (opcional)")
                        Text(
                            "Si el docente también define fechas o intentos, rige lo más restrictivo.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.guardarPrueba(
                                    pruebaEditando, tituloPrueba, desdePrueba, hastaPrueba, limitePrueba
                                )
                            },
                            enabled = !estado.ocupado,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (pruebaEditando == null) "Guardar prueba" else "Guardar cambios") }
                        TextButton(onClick = { cerrarFormularios() }) { Text("Cancelar") }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            if (estado.pruebas.isEmpty() && !estado.ocupado && estado.error == null) {
                Text("Este objetivo aún no tiene prueba final.")
            }
            estado.pruebas.forEach { prueba ->
                val id = prueba.id ?: return@forEach
                val estadoActual = prueba.estado ?: "BORRADOR"
                val ventana = listOfNotNull(
                    prueba.disponibleDesde?.take(10)?.let { "desde $it" },
                    prueba.disponibleHasta?.take(10)?.let { "hasta $it" }
                ).joinToString(" ").ifEmpty { "sin fechas" }
                TarjetaEvaluacion(
                    titulo = prueba.titulo,
                    detalle = "Intentos: ${prueba.limiteIntentos ?: "sin límite"} · $ventana",
                    estado = estadoActual,
                    ocupado = estado.ocupado,
                    onPreguntas = {
                        viewModel.abrirPreguntas(
                            ContenedorPreguntas.DePrueba(id, prueba.titulo, estadoActual)
                        )
                    },
                    onEditar = {
                        cerrarFormularios()
                        pruebaEditando = id
                        tituloPrueba = prueba.titulo
                        desdePrueba = prueba.disponibleDesde?.take(10).orEmpty()
                        hastaPrueba = prueba.disponibleHasta?.take(10).orEmpty()
                        limitePrueba = prueba.limiteIntentos?.toString().orEmpty()
                        formPrueba = true
                    },
                    onTransicion = { destino ->
                        if (destino == "DESHABILITADO") {
                            confirmarDeshabilitar = "prueba" to id
                        } else {
                            viewModel.transicionarPrueba(id, destino)
                        }
                    }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TarjetaEvaluacion(
    titulo: String,
    detalle: String,
    estado: String,
    ocupado: Boolean,
    onPreguntas: () -> Unit,
    onEditar: () -> Unit,
    onTransicion: (String) -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            ChipEstado(estado)
            Text(detalle, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPreguntas,
                    enabled = !ocupado,
                    modifier = Modifier.weight(1f)
                ) { Text(if (estado == "BORRADOR") "Preguntas" else "Ver preguntas") }
                OutlinedButton(
                    onClick = onEditar,
                    enabled = !ocupado && estado == "BORRADOR"
                ) { Text("Editar") }
            }
            siguienteEstadoEvaluacion(estado)?.let { destino ->
                Spacer(Modifier.height(8.dp))
                if (destino == "DESHABILITADO") {
                    OutlinedButton(
                        onClick = { onTransicion(destino) },
                        enabled = !ocupado,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(nombreAccionEvaluacion(destino)) }
                } else {
                    Button(
                        onClick = { onTransicion(destino) },
                        enabled = !ocupado,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(nombreAccionEvaluacion(destino)) }
                }
            }
        }
    }
}

@Composable
private fun EditorPreguntas(
    contenedor: ContenedorPreguntas,
    preguntas: List<UtpPreguntaRemota>,
    ocupado: Boolean,
    guardadosOk: Int,
    onGuardar: (Long?, String, String, List<String>, Int?) -> Unit
) {
    var formulario by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<Long?>(null) }
    var enunciado by remember { mutableStateOf("") }
    var explicacion by remember { mutableStateOf("") }
    var opciones by remember { mutableStateOf(List(MAX_ALTERNATIVAS) { "" }) }
    var correcta by remember { mutableStateOf<Int?>(null) }

    fun limpiar() {
        formulario = false
        editando = null
        enunciado = ""
        explicacion = ""
        opciones = List(MAX_ALTERNATIVAS) { "" }
        correcta = null
    }

    val guardadosAlAbrir = remember { guardadosOk }
    LaunchedEffect(guardadosOk) {
        if (guardadosOk > guardadosAlAbrir) limpiar()
    }

    ChipEstado(contenedor.estado)
    Spacer(Modifier.height(6.dp))
    Text(
        if (contenedor.editable) {
            "Agrega preguntas de selección múltiple y marca la alternativa correcta. " +
                "El estudiante nunca recibe la respuesta correcta antes de responder."
        } else {
            "Solo lectura: las preguntas se editan mientras está en BORRADOR."
        },
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(10.dp))

    if (contenedor.editable) {
        FilledTonalButton(
            onClick = { if (formulario && editando == null) limpiar() else { limpiar(); formulario = true } },
            enabled = !ocupado,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (formulario && editando == null) "Cerrar formulario" else "＋ Agregar pregunta") }
    }

    if (formulario) {
        Spacer(Modifier.height(10.dp))
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    if (editando == null) "Nueva pregunta" else "Editar pregunta",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                CampoEvaluacion(enunciado, { enunciado = it }, "Enunciado", filas = 2)
                Text("Alternativas (marca la correcta)", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                opciones.forEachIndexed { indice, texto ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = correcta == indice,
                            onClick = { correcta = indice }
                        )
                        OutlinedTextField(
                            value = texto,
                            onValueChange = { nuevo ->
                                opciones = opciones.toMutableList().also { it[indice] = nuevo }
                            },
                            label = { Text("Alternativa ${'A' + indice}") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
                CampoEvaluacion(explicacion, { explicacion = it }, "Explicación (feedback al responder)", filas = 2)
                Button(
                    onClick = { onGuardar(editando, enunciado, explicacion, opciones, correcta) },
                    enabled = !ocupado,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (editando == null) "Guardar pregunta" else "Guardar cambios") }
                TextButton(onClick = { limpiar() }) { Text("Cancelar") }
            }
        }
    }

    Spacer(Modifier.height(14.dp))
    Text("Preguntas registradas (${preguntas.size})", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    if (preguntas.isEmpty() && !ocupado) {
        Text("Todavía no hay preguntas. Agrega al menos una antes de enviar a revisión.")
    }
    preguntas.forEachIndexed { numero, pregunta ->
        ElevatedCard(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("${numero + 1}. ${pregunta.enunciado}", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                pregunta.opciones.sortedBy { it.orden }.forEachIndexed { i, opcion ->
                    Text(
                        "${'A' + i}) ${opcion.texto}" + if (opcion.esCorrecta) "  ✓ correcta" else "",
                        fontWeight = if (opcion.esCorrecta) FontWeight.Bold else FontWeight.Normal,
                        color = if (opcion.esCorrecta) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                pregunta.explicacion?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Explicación: $it", style = MaterialTheme.typography.bodySmall)
                }
                if (contenedor.editable) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val ordenadas = pregunta.opciones.sortedBy { it.orden }
                            editando = pregunta.id
                            enunciado = pregunta.enunciado
                            explicacion = pregunta.explicacion.orEmpty()
                            opciones = List(MAX_ALTERNATIVAS) { i -> ordenadas.getOrNull(i)?.texto.orEmpty() }
                            correcta = ordenadas.indexOfFirst { it.esCorrecta }.takeIf { it >= 0 }
                            formulario = true
                        },
                        enabled = !ocupado,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Editar pregunta") }
                }
            }
        }
    }
}

@Composable
private fun ChipEstado(estado: String) {
    val color = when (estado) {
        "PUBLICADO" -> MaterialTheme.colorScheme.primaryContainer
        "DESHABILITADO" -> MaterialTheme.colorScheme.errorContainer
        "AUTORIZADO" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(color = color, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = estado.replace('_', ' '),
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun CampoEvaluacion(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    filas: Int = 1
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        minLines = filas,
        singleLine = filas == 1,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
}
