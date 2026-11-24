
package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.viewmodel.UtpContenidosViewModel

@Composable
fun UtpContenidoRemotoScreen(
    viewModel: UtpContenidosViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    var asignaturaId by remember {
        mutableStateOf<Int?>(null)
    }

    var unidadId by remember {
        mutableStateOf<Int?>(null)
    }

    var objetivoId by remember {
        mutableStateOf<Int?>(null)
    }

    var unidadEditando by remember {
        mutableStateOf<Int?>(null)
    }

    var objetivoEditando by remember {
        mutableStateOf<Int?>(null)
    }

    var tituloUnidad by remember {
        mutableStateOf("")
    }

    var ordenUnidad by remember {
        mutableStateOf("1")
    }

    var codigoOA by remember {
        mutableStateOf("")
    }

    var descripcionOA by remember {
        mutableStateOf("")
    }

    var ordenOA by remember {
        mutableStateOf("1")
    }

    var tituloMaterial by remember {
        mutableStateOf("")
    }

    var explicacionMaterial by remember {
        mutableStateOf("")
    }

    var ejemploMaterial by remember {
        mutableStateOf("")
    }

    var instruccionesMaterial by remember {
        mutableStateOf("")
    }

    var ordenMaterial by remember {
        mutableStateOf("0")
    }

    var responsableOrigen by remember {
        mutableStateOf("")
    }

    var licencia by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        viewModel.cargarAsignaturas()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        OutlinedButton(onClick = onVolver) {
            Text("← Volver al panel UTP")
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Gestión de contenidos UTP",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        Text("Administración conectada a Spring Boot")

        Spacer(Modifier.height(16.dp))

        if (estado.cargando || estado.guardando) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }

        estado.error?.let { mensaje ->
            Text(
                text = "Error: $mensaje",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))
        }

        estado.mensaje?.let { mensaje ->
            Text(mensaje)
            Spacer(Modifier.height(12.dp))
        }

        Text(
            text = "1. Seleccionar asignatura",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(8.dp))

        estado.asignaturas.forEach { asignatura ->

            if (asignaturaId == asignatura.id) {

                Button(
                    onClick = {
                        viewModel.cargarUnidades(asignatura.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(asignatura.nombre)
                }

            } else {

                OutlinedButton(
                    onClick = {
                        asignaturaId = asignatura.id
                        unidadId = null
                        objetivoId = null
                        unidadEditando = null
                        objetivoEditando = null

                        tituloUnidad = ""
                        ordenUnidad = "1"

                        viewModel.cargarUnidades(asignatura.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(asignatura.nombre)
                }
            }
        }

        if (asignaturaId != null) {

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = "2. Unidades de aprendizaje",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        asignaturaId?.let {
                            viewModel.cargarUnidades(it)
                        }
                    }
                ) {
                    Text("Actualizar")
                }
            }

            Spacer(Modifier.height(12.dp))

            if (!estado.cargando && estado.unidades.isEmpty()) {
                Text("No hay unidades registradas.")
            }

            estado.unidades.forEach { unidad ->

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {

                        Text(
                            text = unidad.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(4.dp))

                        Text("Estado: ${unidad.estado}")
                        Text("Orden: ${unidad.orden}")

                        Spacer(Modifier.height(10.dp))

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Button(
                                onClick = {
                                    unidadId = unidad.id
                                    objetivoId = null
                                    objetivoEditando = null

                                    viewModel.cargarObjetivos(
                                        unidad.id
                                    )
                                }
                            ) {
                                Text("Ver OA")
                            }

                            OutlinedButton(
                                enabled = unidad.estado == "BORRADOR",
                                onClick = {
                                    unidadEditando = unidad.id
                                    tituloUnidad = unidad.titulo
                                    ordenUnidad =
                                        unidad.orden.toString()
                                }
                            ) {
                                Text("Editar")
                            }
                        }

                        val siguiente = siguienteEstado(
                            unidad.estado
                        )

                        if (siguiente != null) {

                            Spacer(Modifier.height(10.dp))

                            Button(
                                enabled = !estado.guardando &&
                                        !estado.cargando,
                                onClick = {
                                    viewModel.cambiarEstadoUnidad(
                                        unidadId = unidad.id,
                                        asignaturaId =
                                            unidad.asignaturaId,
                                        estadoNuevo = siguiente
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    textoAccionEstado(siguiente)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = if (unidadEditando == null) {
                    "Crear nueva unidad"
                } else {
                    "Editar unidad #$unidadEditando"
                },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = tituloUnidad,
                onValueChange = {
                    tituloUnidad = it
                },
                label = {
                    Text("Título de la unidad")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = ordenUnidad,
                onValueChange = {
                    ordenUnidad = it
                },
                label = {
                    Text("Orden")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Button(
                enabled = !estado.guardando &&
                        tituloUnidad.isNotBlank() &&
                        (ordenUnidad.toIntOrNull()?.let {
                            it > 0
                        } == true),
                onClick = {

                    val idAsignatura = asignaturaId
                        ?: return@Button

                    val orden = ordenUnidad.toIntOrNull()
                        ?: return@Button

                    val idEditar = unidadEditando

                    if (idEditar == null) {

                        viewModel.crearUnidad(
                            asignaturaId = idAsignatura,
                            titulo = tituloUnidad,
                            orden = orden
                        )

                    } else {

                        viewModel.editarUnidad(
                            id = idEditar,
                            asignaturaId = idAsignatura,
                            titulo = tituloUnidad,
                            orden = orden
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    if (unidadEditando == null) {
                        "Guardar nueva unidad"
                    } else {
                        "Guardar cambios"
                    }
                )
            }

            if (unidadEditando != null) {

                OutlinedButton(
                    onClick = {
                        unidadEditando = null
                        tituloUnidad = ""
                        ordenUnidad = "1"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar edición")
                }
            }
        }

        if (unidadId != null) {

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp)
            )

            Text(
                text = "3. Objetivos de aprendizaje",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(8.dp))

            estado.objetivos.forEach { objetivo ->

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {

                        Text(
                            text = objetivo.codigo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(objetivo.descripcion)

                        Spacer(Modifier.height(4.dp))

                        Text("Estado: ${objetivo.estado}")

                        Spacer(Modifier.height(10.dp))

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Button(
                                onClick = {
                                    objetivoId = objetivo.id

                                    viewModel.cargarMateriales(
                                        objetivo.id
                                    )
                                }
                            ) {
                                Text("Ver materiales")
                            }

                            OutlinedButton(
                                enabled =
                                    objetivo.estado == "BORRADOR",
                                onClick = {
                                    objetivoEditando = objetivo.id
                                    codigoOA = objetivo.codigo
                                    descripcionOA =
                                        objetivo.descripcion

                                    // El modelo UtpObjetivo actual
                                    // no contiene el orden.
                                    // Pedimos indicarlo al editar.
                                    ordenOA = ""
                                }
                            ) {
                                Text("Editar")
                            }
                        }

                        val siguiente = siguienteEstado(
                            objetivo.estado
                        )

                        if (siguiente != null) {

                            Spacer(Modifier.height(10.dp))

                            Button(
                                enabled = !estado.guardando &&
                                        !estado.cargando,
                                onClick = {

                                    val idUnidad = unidadId
                                        ?: return@Button

                                    viewModel.cambiarEstadoObjetivo(
                                        objetivoId = objetivo.id,
                                        unidadId = idUnidad,
                                        estadoNuevo = siguiente
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    textoAccionEstado(siguiente)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (objetivoEditando == null) {
                    "Crear objetivo de aprendizaje"
                } else {
                    "Editar objetivo #$objetivoEditando"
                },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = codigoOA,
                onValueChange = {
                    codigoOA = it
                },
                label = {
                    Text("Código OA")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = descripcionOA,
                onValueChange = {
                    descripcionOA = it
                },
                label = {
                    Text("Descripción del OA")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = ordenOA,
                onValueChange = {
                    ordenOA = it
                },
                label = {
                    Text("Orden OA")
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (objetivoEditando != null) {
                Text(
                    "Al editar, indica el orden que debe conservar el OA.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                enabled = !estado.guardando &&
                        codigoOA.isNotBlank() &&
                        descripcionOA.isNotBlank() &&
                        (ordenOA.toIntOrNull()?.let {
                            it > 0
                        } == true),
                onClick = {

                    val idUnidad = unidadId
                        ?: return@Button

                    val orden = ordenOA.toIntOrNull()
                        ?: return@Button

                    val idEditar = objetivoEditando

                    if (idEditar == null) {

                        viewModel.crearObjetivo(
                            unidadId = idUnidad,
                            codigo = codigoOA,
                            descripcion = descripcionOA,
                            orden = orden
                        )

                    } else {

                        viewModel.editarObjetivo(
                            id = idEditar,
                            unidadId = idUnidad,
                            codigo = codigoOA,
                            descripcion = descripcionOA,
                            orden = orden
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    if (objetivoEditando == null) {
                        "Guardar objetivo"
                    } else {
                        "Guardar cambios del OA"
                    }
                )
            }

            if (objetivoEditando != null) {

                OutlinedButton(
                    onClick = {
                        objetivoEditando = null
                        codigoOA = ""
                        descripcionOA = ""
                        ordenOA = "1"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar edición del OA")
                }
            }
        }

        if (objetivoId != null) {

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp)
            )

            Text(
                text = "4. Materiales educativos",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(8.dp))

            if (!estado.cargando && estado.materiales.isEmpty()) {
                Text("No hay materiales registrados.")
            }

            estado.materiales.forEach { material ->

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {

                        Text(
                            text = material.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("Estado: ${material.estado}")

                        Text("ID: ${material.id}")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Crear material educativo",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = tituloMaterial,
                onValueChange = {
                    tituloMaterial = it
                },
                label = {
                    Text("Título del material")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = explicacionMaterial,
                onValueChange = {
                    explicacionMaterial = it
                },
                label = {
                    Text("Explicación escrita")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = ejemploMaterial,
                onValueChange = {
                    ejemploMaterial = it
                },
                label = {
                    Text("Ejemplo guiado")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = instruccionesMaterial,
                onValueChange = {
                    instruccionesMaterial = it
                },
                label = {
                    Text("Instrucciones")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = ordenMaterial,
                onValueChange = {
                    ordenMaterial = it
                },
                label = {
                    Text("Orden del material")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = responsableOrigen,
                onValueChange = {
                    responsableOrigen = it
                },
                label = {
                    Text("Responsable o autor")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = licencia,
                onValueChange = {
                    licencia = it
                },
                label = {
                    Text("Licencia del material")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Button(
                enabled = !estado.guardando &&
                        tituloMaterial.isNotBlank() &&
                        responsableOrigen.isNotBlank() &&
                        licencia.isNotBlank() &&
                        (ordenMaterial.toIntOrNull()?.let {
                            it >= 0
                        } == true),
                onClick = {

                    val idObjetivo = objetivoId
                        ?: return@Button

                    val orden = ordenMaterial.toIntOrNull()
                        ?: return@Button

                    viewModel.crearMaterial(
                        objetivoId = idObjetivo,
                        titulo = tituloMaterial,
                        explicacion = explicacionMaterial,
                        ejemplo = ejemploMaterial,
                        instrucciones = instruccionesMaterial,
                        orden = orden,
                        responsableOrigen = responsableOrigen,
                        licencia = licencia
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar nuevo material")
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Los archivos de imagen y audio se incorporarán con la carga multimedia UTP.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

private fun siguienteEstado(
    actual: String
): String? {

    return when (actual) {
        "BORRADOR" -> "EN_REVISION"
        "EN_REVISION" -> "AUTORIZADO"
        "AUTORIZADO" -> "PUBLICADO"
        else -> null
    }
}

private fun textoAccionEstado(
    destino: String
): String {

    return when (destino) {
        "EN_REVISION" -> "Enviar a revisión"
        "AUTORIZADO" -> "Autorizar"
        "PUBLICADO" -> "Publicar"
        else -> destino
    }
}
