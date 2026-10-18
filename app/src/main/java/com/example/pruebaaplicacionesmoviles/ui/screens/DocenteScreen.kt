package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.viewmodel.DocenteViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocenteScreen(
    viewModel: DocenteViewModel,
    onCerrarSesion: () -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado.mensaje) {
        if (estado.mensaje.isNotEmpty()) {
            delay(2000)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel docente") },
                actions = {
                    Button(onClick = onCerrarSesion) { Text("Salir") }
                }
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

            AnimatedVisibility(visible = estado.mensaje.isNotEmpty()) {
                Text(estado.mensaje, color = MaterialTheme.colorScheme.primary)
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Estudiantes a cargo", style = MaterialTheme.typography.titleMedium)
                    estado.estudiantes.forEach { est ->
                        Text("- ${est.alias} (id ${est.id}) · ${est.curso}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Unidades asignables", style = MaterialTheme.typography.titleMedium)
                    if (estado.unidadesAsignables.isEmpty()) {
                        Text("Ninguna. Pide a UTP autorizar/publicar.")
                    } else {
                        estado.unidadesAsignables.forEach { u ->
                            Text("- id ${u.id}: ${u.titulo} [${u.estado.name}]")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Nueva asignación", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = estado.estudianteIdTexto,
                        onValueChange = viewModel::actualizarEstudianteId,
                        label = { Text("ID estudiante (ej: 3)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.unidadIdTexto,
                        onValueChange = viewModel::actualizarUnidadId,
                        label = { Text("ID unidad (ej: 2)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.fechaInicio,
                        onValueChange = viewModel::actualizarFechaInicio,
                        label = { Text("Fecha inicio AAAA-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.fechaFin,
                        onValueChange = viewModel::actualizarFechaFin,
                        label = { Text("Fecha fin AAAA-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.maxIntentosTexto,
                        onValueChange = viewModel::actualizarMaxIntentos,
                        label = { Text("Máx. intentos prueba final") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (estado.errorFormulario.isNotEmpty()) {
                        Text(
                            estado.errorFormulario,
                            color = MaterialTheme.colorScheme.error
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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Resultados (intentos)", style = MaterialTheme.typography.titleMedium)
                    if (estado.intentos.isEmpty()) {
                        Text("Aún no hay intentos registrados.")
                    } else {
                        estado.intentos.forEach { intento ->
                            Text(
                                "Intento ${intento.id}: ${intento.correctas}/" +
                                        "${intento.totalPreguntas} (${intento.porcentaje}%) · ${intento.fechaHora}"
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}