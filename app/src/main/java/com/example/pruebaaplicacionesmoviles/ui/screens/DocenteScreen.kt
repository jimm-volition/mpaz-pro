package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazAzul
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazAzulClaro
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazFondo
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazTexto
import com.example.pruebaaplicacionesmoviles.viewmodel.DocenteViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocenteScreen(
    viewModel: DocenteViewModel,
    onCerrarSesion: () -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()
    var mostrarInicio by remember { mutableStateOf(false) }
    var mostrarFin by remember { mutableStateOf(false) }
    var actividadExpandida by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(estado.mensaje) {
        if (estado.mensaje.isNotEmpty()) {
            delay(3000)
            viewModel.limpiarMensaje()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.observar()
    }

    if (mostrarInicio) {
        SelectorFechaDialog(
            onDismiss = { mostrarInicio = false },
            onFechaElegida = {
                viewModel.actualizarFechaInicio(it)
                mostrarInicio = false
            }
        )
    }
    if (mostrarFin) {
        SelectorFechaDialog(
            onDismiss = { mostrarFin = false },
            onFechaElegida = {
                viewModel.actualizarFechaFin(it)
                mostrarFin = false
            }
        )
    }

    val aliasEstudiante = estado.aliasEstudiante(estado.estudianteSeleccionado)
    val codigoObjetivo = estado.codigoObjetivo(estado.objetivoSeleccionado)

    Scaffold(
        containerColor = MpazFondo,
        topBar = {
            TopAppBar(
                title = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MpazAzul),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Panel docente",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                estado.nombreCurso.ifEmpty { "Curso ${estado.cursoId}" },
                                color = MpazAzulClaro
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = onCerrarSesion,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MpazAzul
                        )
                    ) {
                        Text("Salir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MpazAzul
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (estado.cargando) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            AnimatedVisibility(
                visible = estado.mensaje.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Text(estado.mensaje, color = MaterialTheme.colorScheme.primary)
            }

            // ---------- Selección de estudiante y OA ----------
            TarjetaDocente("¿Con quién y qué objetivo trabajas?") {
                Text("Estudiantes del curso", style = MaterialTheme.typography.labelLarge)
                if (estado.estudiantes.isEmpty() && !estado.cargando) {
                    Text("No hay estudiantes matriculados.")
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    estado.estudiantes.forEach { est ->
                        FilterChip(
                            selected = estado.estudianteSeleccionado == est.id.toLong(),
                            onClick = { viewModel.seleccionarEstudiante(est.id.toLong()) },
                            label = { Text(est.alias) }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text("Objetivos autorizados por UTP", style = MaterialTheme.typography.labelLarge)
                if (estado.objetivos.isEmpty() && !estado.cargando) {
                    Text("Ninguno. UTP debe dejar el OA en AUTORIZADO o PUBLICADO.")
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    estado.objetivos.forEach { objetivo ->
                        FilterChip(
                            selected = estado.objetivoSeleccionado == objetivo.id,
                            onClick = { viewModel.seleccionarObjetivo(objetivo.id) },
                            label = { Text("${objetivo.codigo} · ${objetivo.estado.lowercase()}") }
                        )
                    }
                }
                estado.objetivos.firstOrNull { it.id == estado.objetivoSeleccionado }?.let {
                    Text(it.descripcion, style = MaterialTheme.typography.bodySmall)
                }
            }

            // ---------- Seguimiento del OA + actividades (ms-progreso + ms-actividades) ----------
            if (estado.objetivoSeleccionado != null) {
                TarjetaDocente("Avance del curso en $codigoObjetivo") {
                    if (estado.cargandoDetalle) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    if (estado.seguimiento.isEmpty() && !estado.cargandoDetalle) {
                        Text("Aún no hay avance registrado en este objetivo.")
                    }
                    estado.seguimiento.forEach { avance ->
                        Text(
                            "${estado.aliasEstudiante(avance.estudianteId)}: ${avance.porcentajeAvance}%",
                            fontWeight = FontWeight.Medium
                        )
                        LinearProgressIndicator(
                            progress = { avance.porcentajeAvance.coerceIn(0, 100) / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    Text(
                        "Prácticas publicadas (${estado.actividadesObjetivo.size})",
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (estado.actividadesObjetivo.isEmpty() && !estado.cargandoDetalle) {
                        Text(
                            "UTP aún no publica prácticas para este objetivo.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    estado.actividadesObjetivo.forEach { actividad ->
                        val preguntas = actividad.preguntas.orEmpty()
                        val abierta = actividadExpandida == actividad.id
                        Column(Modifier.fillMaxWidth().animateContentSize()) {
                            TextButton(
                                onClick = { actividadExpandida = if (abierta) null else actividad.id }
                            ) {
                                Text(
                                    (if (abierta) "▾ " else "▸ ") +
                                        "${actividad.titulo} · ${preguntas.size} pregunta(s)"
                                )
                            }
                            if (abierta) {
                                preguntas.forEachIndexed { i, pregunta ->
                                    Text("${i + 1}. ${pregunta.enunciado}", fontWeight = FontWeight.Medium)
                                    pregunta.opciones.forEach { opcion ->
                                        Text("   • ${opcion.texto}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ---------- Resultados del estudiante elegido ----------
            TarjetaDocente("Resultados de $aliasEstudiante") {
                if (estado.enviosServidor.isEmpty() && estado.intentosServidor.isEmpty()) {
                    Text("Aún no hay resultados de $aliasEstudiante.")
                } else {
                    if (estado.enviosServidor.isNotEmpty()) {
                        Text("Prácticas", style = MaterialTheme.typography.labelLarge)
                    }
                    estado.enviosServidor.forEach { envio ->
                        Text(
                            "Práctica ${envio.actividadId} (${estado.codigoObjetivo(envio.objetivoAprendizajeId)}): " +
                                "${envio.puntaje}% · ${envio.registradoEn.take(10)}"
                        )
                    }
                    if (estado.intentosServidor.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Prueba final", style = MaterialTheme.typography.labelLarge)
                    }
                    estado.intentosServidor.forEach { intento ->
                        val puntaje = intento.puntaje?.let { "$it%" } ?: "sin puntaje"
                        Text(
                            "Prueba ${intento.pruebaId}, intento ${intento.numeroIntento}: " +
                                "$puntaje · ${intento.estado}"
                        )
                    }
                }
            }

            // ---------- Nueva asignación ----------
            TarjetaDocente("Nueva asignación") {
                Text(
                    "Toca un estudiante y un objetivo arriba para rellenar los campos.",
                    style = MaterialTheme.typography.bodySmall
                )
                CampoConError(
                    valor = estado.estudianteIdTexto,
                    etiqueta = "ID estudiante (ej: 3)",
                    error = estado.errorEstudiante,
                    onChange = viewModel::actualizarEstudianteId
                )
                CampoConError(
                    valor = estado.unidadIdTexto,
                    etiqueta = "ID objetivo (ej: 31)",
                    error = estado.errorUnidad,
                    onChange = viewModel::actualizarUnidadId
                )

                OutlinedTextField(
                    value = estado.fechaInicio,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha inicio") },
                    isError = estado.errorFechaInicio.isNotEmpty(),
                    supportingText = {
                        if (estado.errorFechaInicio.isNotEmpty()) {
                            Text(estado.errorFechaInicio)
                        }
                    },
                    trailingIcon = {
                        if (estado.errorFechaInicio.isNotEmpty()) {
                            Icon(Icons.Filled.Error, contentDescription = "Error")
                        } else {
                            IconButton(onClick = { mostrarInicio = true }) {
                                Icon(Icons.Filled.DateRange, contentDescription = "Elegir fecha")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.fechaFin,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha fin") },
                    isError = estado.errorFechaFin.isNotEmpty(),
                    supportingText = {
                        if (estado.errorFechaFin.isNotEmpty()) {
                            Text(estado.errorFechaFin)
                        }
                    },
                    trailingIcon = {
                        if (estado.errorFechaFin.isNotEmpty()) {
                            Icon(Icons.Filled.Error, contentDescription = "Error")
                        } else {
                            IconButton(onClick = { mostrarFin = true }) {
                                Icon(Icons.Filled.DateRange, contentDescription = "Elegir fecha")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                CampoConError(
                    valor = estado.maxIntentosTexto,
                    etiqueta = "Máx. intentos de la prueba final",
                    error = estado.errorMaxIntentos,
                    onChange = viewModel::actualizarMaxIntentos
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = estado.datosConfirmados,
                        onCheckedChange = viewModel::actualizarConfirmacion
                    )
                    Text("Confirmo que los datos de la asignación son correctos")
                }
                if (estado.errorConfirmacion.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            estado.errorConfirmacion,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Disponible para el estudiante")
                    Switch(
                        checked = estado.asignacionActiva,
                        onCheckedChange = viewModel::actualizarAsignacionActiva
                    )
                }

                Button(
                    onClick = viewModel::crearAsignacion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear asignación")
                }
            }

            // ---------- Asignaciones existentes ----------
            TarjetaDocente("Asignaciones del curso (${estado.asignaciones.size})") {
                if (estado.asignaciones.isEmpty()) {
                    Text("Todavía no hay asignaciones.")
                }
                estado.asignaciones.forEach { asignacion ->
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "${estado.codigoObjetivo(asignacion.objetivoAprendizajeId)} → " +
                                estado.aliasEstudiante(asignacion.estudianteId),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${asignacion.disponibleDesde.take(10)} a ${asignacion.disponibleHasta.take(10)} · " +
                                "${asignacion.limiteIntentosPrueba} intento(s) · " +
                                if (asignacion.activa) "activa" else "pausada",
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = { viewModel.alternarAsignacion(asignacion) }) {
                            Text(if (asignacion.activa) "Pausar" else "Activar")
                        }
                        HorizontalDivider()
                    }
                }
            }

            // ---------- Informe ----------
            TarjetaDocente("Informe pedagógico") {
                Text(
                    "Estudiante: $aliasEstudiante · Objetivo: $codigoObjetivo",
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = viewModel::generarInforme,
                    enabled = !estado.generandoInforme &&
                        estado.estudianteSeleccionado != null && estado.objetivoSeleccionado != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.generandoInforme) "Procesando..." else "Generar informe")
                }
                estado.informe?.let { informe ->
                    Text("Estado: ${informe.estado.replace('_', ' ')}", fontWeight = FontWeight.Medium)
                    Text(informe.resumen)
                    Text("Áreas de refuerzo: ${informe.areasRefuerzo}")
                    if (informe.estado != "VALIDADO") {
                        OutlinedTextField(
                            value = estado.observacionInforme,
                            onValueChange = viewModel::actualizarObservacion,
                            label = { Text("Observación docente (opcional)") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = viewModel::validarInforme,
                            enabled = !estado.generandoInforme,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Validar informe")
                        }
                    }
                    OutlinedButton(
                        onClick = viewModel::descargarPdf,
                        enabled = !estado.generandoInforme,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Descargar PDF")
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaDocente(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MpazTexto
            )
            contenido()
        }
    }
}

@Composable
private fun CampoConError(
    valor: String,
    etiqueta: String,
    error: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error.isNotEmpty(),
        supportingText = {
            if (error.isNotEmpty()) Text(error)
        },
        trailingIcon = {
            if (error.isNotEmpty()) {
                Icon(Icons.Filled.Error, contentDescription = "Error")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFechaDialog(
    onDismiss: () -> Unit,
    onFechaElegida: (String) -> Unit
) {
    val pickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        onFechaElegida(millisAFecha(millis))
                    } else {
                        onDismiss()
                    }
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = pickerState)
    }
}

private fun millisAFecha(millis: Long): String {
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    formato.timeZone = TimeZone.getTimeZone("UTC")
    return formato.format(Date(millis))
}
