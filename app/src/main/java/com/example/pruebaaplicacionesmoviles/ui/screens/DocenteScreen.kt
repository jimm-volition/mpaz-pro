package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.viewmodel.DocenteViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazAzul
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazAzulClaro
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazFondo
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocenteScreen(
    viewModel: DocenteViewModel,
    onCerrarSesion: () -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()
    var mostrarInicio by remember { mutableStateOf(false) }
    var mostrarFin by remember { mutableStateOf(false) }

    LaunchedEffect(estado.mensaje) {
        if (estado.mensaje.isNotEmpty()) {
            delay(2000)
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
                                "Asigna unidades y revisa intentos de tus estudiantes.",
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
            Text(
                "Asigna unidades y revisa intentos de tus estudiantes.",
                style = MaterialTheme.typography.bodyMedium
            )

            AnimatedVisibility(
                visible = estado.mensaje.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Text(estado.mensaje, color = MaterialTheme.colorScheme.primary)
            }
            
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
                    "Estudiantes a cargo", 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MpazTexto
                    )
                    estado.estudiantes.forEach { est ->
                        OutlinedButton(
                            onClick = { viewModel.actualizarEstudianteId(est.id.toString()) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Usar ${est.alias} (id ${est.id})")
                        }
                    }

                    Text(
                        "Objetivos asignables (servidor)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (estado.objetivos.isEmpty()) {
                        Text("Ninguno. UTP debe dejar el OA en AUTORIZADO o PUBLICADO.")
                    } else {
                        estado.objetivos.forEach { objetivo ->
                            OutlinedButton(
                                onClick = {
                                    viewModel.actualizarUnidadId(objetivo.id.toString())
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Usar OA ${objetivo.id}: ${objetivo.codigo} · ${objetivo.estado}"
                                )
                            }
                        }
                    }
                }
            }

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
                    "Nueva asignación",
                     style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.Bold,
                     color = MpazTexto
                     )

                    CampoConError(
                        valor = estado.estudianteIdTexto,
                        etiqueta = "ID estudiante (String/Int, ej: 3)",
                        error = estado.errorEstudiante,
                        onChange = viewModel::actualizarEstudianteId
                    )
                    CampoConError(
                        valor = estado.unidadIdTexto,
                        etiqueta = "ID objetivo (Int, ej: 31)",
                        error = estado.errorUnidad,
                        onChange = viewModel::actualizarUnidadId
                    )
                    Text("Curso activo: ${estado.cursoId}")
                    if (estado.objetivos.isEmpty()) {
                        Text("No hay objetivos disponibles en el servidor")
                    }

                    OutlinedTextField(
                        value = estado.fechaInicio,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha inicio (Date)") },
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
                        label = { Text("Fecha fin (Date)") },
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
                        etiqueta = "Máx. intentos (Int)",
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
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Resultados del estudiante 3",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MpazTexto
                    )
                    if (estado.enviosServidor.isEmpty() && estado.intentosServidor.isEmpty()) {
                        Text("Aún no hay resultados del estudiante 3.")
                    } else {
                        estado.enviosServidor.forEach { envio ->
                            Text("Práctica ${envio.actividadId}: ${envio.puntaje}%")
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        estado.intentosServidor.forEach { intento ->
                            val puntaje = intento.puntaje?.let { "$it%" } ?: "sin puntaje"
                            Text(
                                "Prueba ${intento.pruebaId}, intento ${intento.numeroIntento}: " +
                                    "$puntaje · ${intento.estado}"
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

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
                        "Informe pedagógico",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MpazTexto
                    )
                    Button(
                        onClick = viewModel::generarInforme,
                        enabled = !estado.generandoInforme,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (estado.generandoInforme) "Guardando..." else "Generar informe")
                    }
                    estado.informe?.let { informe ->
                        Text("Estado: ${informe.estado}")
                        Text(informe.resumen)
                        Text(informe.areasRefuerzo)
                        if (informe.estado != "VALIDADO") {
                            Button(
                                onClick = viewModel::validarInforme,
                                enabled = !estado.generandoInforme,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Validar informe")
                            }
                        }
                        Button(
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