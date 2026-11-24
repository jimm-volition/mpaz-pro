package com.example.pruebaaplicacionesmoviles.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.viewmodel.UtpContenidosViewModel

@Composable
fun UtpContenidoRemotoScreen(
    viewModel: UtpContenidosViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current
    val desplazamiento = rememberScrollState()

    // 1 = Asignaturas, 2 = Unidades, 3 = OA, 4 = Materiales.
    var paso by remember { mutableIntStateOf(1) }
    var asignaturaId by remember { mutableStateOf<Int?>(null) }
    var unidadId by remember { mutableStateOf<Int?>(null) }
    var objetivoId by remember { mutableStateOf<Int?>(null) }
    var historialId by remember { mutableStateOf<Int?>(null) }

    // Formularios inicialmente cerrados para que el listado sea fácil de recorrer.
    var mostrarFormularioUnidad by remember { mutableStateOf(false) }
    var mostrarFormularioObjetivo by remember { mutableStateOf(false) }
    var mostrarFormularioMaterial by remember { mutableStateOf(false) }

    var unidadEditando by remember { mutableStateOf<Int?>(null) }
    var objetivoEditando by remember { mutableStateOf<Int?>(null) }
    var materialEditando by remember { mutableStateOf<Int?>(null) }

    var tituloUnidad by remember { mutableStateOf("") }
    var ordenUnidad by remember { mutableStateOf("1") }

    var codigoOA by remember { mutableStateOf("") }
    var descripcionOA by remember { mutableStateOf("") }
    var ordenOA by remember { mutableStateOf("1") }

    var tituloMaterial by remember { mutableStateOf("") }
    var explicacionMaterial by remember { mutableStateOf("") }
    var ejemploMaterial by remember { mutableStateOf("") }
    var instruccionesMaterial by remember { mutableStateOf("") }
    var ordenMaterial by remember { mutableStateOf("0") }
    var responsableOrigen by remember { mutableStateOf("") }
    var licencia by remember { mutableStateOf("") }
    var imagenes by remember { mutableStateOf<List<String>>(emptyList()) }
    var audio by remember { mutableStateOf<String?>(null) }

    var confirmarDeshabilitar by remember { mutableStateOf<Int?>(null) }

    val ocupado = estado.cargando || estado.guardando || estado.subiendoArchivo

    fun limpiarUnidad() {
        unidadEditando = null
        tituloUnidad = ""
        ordenUnidad = "1"
    }

    fun limpiarObjetivo() {
        objetivoEditando = null
        codigoOA = ""
        descripcionOA = ""
        ordenOA = "1"
    }

    fun limpiarMaterial() {
        materialEditando = null
        tituloMaterial = ""
        explicacionMaterial = ""
        ejemploMaterial = ""
        instruccionesMaterial = ""
        ordenMaterial = "0"
        responsableOrigen = ""
        licencia = ""
        imagenes = emptyList()
        audio = null
        viewModel.limpiarSubidas()
    }

    val elegirImagen = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.subirImagen(it) }
    }

    val elegirAudio = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.subirAudio(it) }
    }

    LaunchedEffect(Unit) { viewModel.cargarAsignaturas() }

    // Subimos al encabezado cuando se entra a otra etapa o se abre un formulario.
    LaunchedEffect(
        paso, mostrarFormularioUnidad, mostrarFormularioObjetivo,
        mostrarFormularioMaterial, unidadEditando, objetivoEditando, materialEditando
    ) {
        desplazamiento.scrollTo(0)
    }

    LaunchedEffect(estado.error) {
        estado.error?.let { mensaje ->
            Toast.makeText(context, "Error: $mensaje", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(estado.imagenSubida) {
        estado.imagenSubida?.takeIf { it.isNotBlank() && it !in imagenes }?.let {
            imagenes = imagenes + it
        }
    }

    LaunchedEffect(estado.audioSubido) {
        estado.audioSubido?.takeIf { it.isNotBlank() }?.let { audio = it }
    }

    // Cerramos el formulario sólo cuando el servidor confirma un guardado correcto.
    LaunchedEffect(estado.mensaje) {
        when (estado.mensaje) {
            "Unidad creada correctamente", "Unidad actualizada correctamente" -> {
                limpiarUnidad()
                mostrarFormularioUnidad = false
            }
            "Objetivo creado correctamente", "Objetivo actualizado correctamente" -> {
                limpiarObjetivo()
                mostrarFormularioObjetivo = false
            }
            "Material creado correctamente", "Material actualizado correctamente" -> {
                limpiarMaterial()
                mostrarFormularioMaterial = false
            }
        }
    }

    // La deshabilitación afecta lo que ve el estudiante: exige confirmación.
    if (confirmarDeshabilitar != null) {
        AlertDialog(
            onDismissRequest = { confirmarDeshabilitar = null },
            title = { Text("Deshabilitar unidad") },
            text = { Text("Esta unidad dejará de estar disponible para los estudiantes. ¿Continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    val id = confirmarDeshabilitar
                    val asignatura = asignaturaId
                    if (id != null && asignatura != null) {
                        viewModel.cambiarEstadoUnidad(
                            unidadId = id,
                            asignaturaId = asignatura,
                            estadoNuevo = "DESHABILITADO"
                        )
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
        val encabezado = when (paso) {
            1 -> "Asignaturas"
            2 -> "Unidades de aprendizaje"
            3 -> "Objetivos de aprendizaje"
            else -> "Materiales educativos"
        }

        TextButton(
            onClick = {
                if (paso == 1) onVolver() else paso -= 1
            },
            modifier = Modifier.padding(bottom = 4.dp)
        ) { Text(if (paso == 1) "← Salir de UTP" else "← Volver al paso anterior") }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("GESTIÓN DE CONTENIDOS UTP", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Text(encabezado, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("Paso $paso de 4", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { paso / 4f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (paso >= 2) {
            val nombreAsignatura = estado.asignaturas
                .firstOrNull { it.id == asignaturaId }?.nombre ?: "Asignatura seleccionada"
            Spacer(Modifier.height(10.dp))
            Text("Asignatura: $nombreAsignatura", style = MaterialTheme.typography.bodyMedium)
        }
        if (paso >= 3) {
            val nombreUnidad = estado.unidades.firstOrNull { it.id == unidadId }?.titulo
                ?: "Unidad seleccionada"
            Text("Unidad: $nombreUnidad", style = MaterialTheme.typography.bodyMedium)
        }
        if (paso >= 4) {
            val nombreObjetivo = estado.objetivos.firstOrNull { it.id == objetivoId }?.codigo
                ?: "Objetivo seleccionado"
            Text("Objetivo: $nombreObjetivo", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(16.dp))

        if (ocupado) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
        }

        estado.error?.let {
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(10.dp))
        }
        estado.mensaje?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
        }

        when (paso) {
            // ETAPA 1: elegir asignatura, sin enseñar todavía el resto de la administración.
            1 -> {
                Text(
                    "Selecciona la asignatura que quieres administrar.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(14.dp))
                if (estado.asignaturas.isEmpty() && !ocupado && estado.error == null) {
                    Text("No hay asignaturas disponibles.")
                }
                estado.asignaturas.forEach { asignatura ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(asignatura.nombre, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    asignaturaId = asignatura.id
                                    unidadId = null
                                    objetivoId = null
                                    historialId = null
                                    limpiarUnidad()
                                    limpiarObjetivo()
                                    limpiarMaterial()
                                    mostrarFormularioUnidad = false
                                    mostrarFormularioObjetivo = false
                                    mostrarFormularioMaterial = false
                                    viewModel.cargarUnidades(asignatura.id)
                                    paso = 2
                                },
                                enabled = !ocupado,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Administrar asignatura") }
                        }
                    }
                }
            }

            // ETAPA 2: unidades. El formulario sólo se muestra al crear o editar.
            2 -> {
                Text("Selecciona una unidad o crea una nueva.")
                Spacer(Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = {
                        if (mostrarFormularioUnidad && unidadEditando == null) {
                            mostrarFormularioUnidad = false
                        } else {
                            limpiarUnidad()
                            mostrarFormularioUnidad = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !ocupado
                ) { Text(if (mostrarFormularioUnidad && unidadEditando == null) "Cerrar formulario" else "＋ Crear unidad") }

                if (mostrarFormularioUnidad) {
                    Spacer(Modifier.height(12.dp))
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                if (unidadEditando == null) "Nueva unidad" else "Editar unidad #$unidadEditando",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(10.dp))
                            CampoUtpRemoto(tituloUnidad, { tituloUnidad = it }, "Título de la unidad")
                            CampoUtpRemoto(ordenUnidad, { ordenUnidad = it }, "Orden")
                            Button(
                                enabled = !ocupado && tituloUnidad.isNotBlank() &&
                                        (ordenUnidad.toIntOrNull() ?: 0) > 0,
                                onClick = {
                                    val asignatura = asignaturaId ?: return@Button
                                    val orden = ordenUnidad.toIntOrNull() ?: return@Button
                                    val id = unidadEditando
                                    if (id == null) {
                                        viewModel.crearUnidad(asignatura, tituloUnidad, orden)
                                    } else {
                                        viewModel.editarUnidad(id, asignatura, tituloUnidad, orden)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (unidadEditando == null) "Guardar unidad" else "Guardar cambios") }
                            TextButton(onClick = {
                                limpiarUnidad()
                                mostrarFormularioUnidad = false
                            }) { Text("Cancelar") }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Unidades registradas", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (estado.unidades.isEmpty() && !ocupado && estado.error == null) {
                    Text("Aún no hay unidades en esta asignatura.")
                }
                estado.unidades.forEach { unidad ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(unidad.titulo, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(5.dp))
                            EstadoUtpRemoto(unidad.estado)
                            Text("Orden: ${unidad.orden}", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        unidadId = unidad.id
                                        objetivoId = null
                                        historialId = null
                                        limpiarObjetivo()
                                        limpiarMaterial()
                                        mostrarFormularioObjetivo = false
                                        mostrarFormularioMaterial = false
                                        viewModel.cargarObjetivos(unidad.id)
                                        paso = 3
                                    },
                                    enabled = !ocupado,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Ver objetivos") }
                                OutlinedButton(
                                    onClick = {
                                        unidadEditando = unidad.id
                                        tituloUnidad = unidad.titulo
                                        ordenUnidad = unidad.orden.toString()
                                        mostrarFormularioUnidad = true
                                    },
                                    enabled = !ocupado && unidad.estado == "BORRADOR"
                                ) { Text("Editar") }
                            }

                            TextButton(
                                onClick = {
                                    if (historialId == unidad.id) {
                                        historialId = null
                                    } else {
                                        historialId = unidad.id
                                        viewModel.cargarHistorial(unidad.id)
                                    }
                                },
                                enabled = !ocupado
                            ) { Text(if (historialId == unidad.id) "Ocultar historial" else "Ver historial") }

                            if (historialId == unidad.id) {
                                Text("Historial de autorizaciones", style = MaterialTheme.typography.titleSmall)
                                estado.historial.forEach { evento ->
                                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                                    Text("${evento.estadoAnterior} → ${evento.estadoNuevo}")
                                    Text("Usuario ID: ${evento.usuarioId}", style = MaterialTheme.typography.bodySmall)
                                    Text(evento.creadoEn, style = MaterialTheme.typography.bodySmall)
                                }
                                if (!estado.cargando && estado.error == null && estado.historial.isEmpty()) {
                                    Text("Sin eventos registrados")
                                }
                            }

                            val siguiente = siguienteEstadoUnidad(unidad.estado)
                            if (siguiente != null) {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (siguiente == "DESHABILITADO") {
                                            confirmarDeshabilitar = unidad.id
                                        } else {
                                            val asignatura = asignaturaId ?: return@Button
                                            viewModel.cambiarEstadoUnidad(
                                                unidadId = unidad.id,
                                                asignaturaId = asignatura,
                                                estadoNuevo = siguiente
                                            )
                                        }
                                    },
                                    enabled = !ocupado,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (unidad.estado == "DESHABILITADO") "Reactivar unidad" else nombreAccionUtp(siguiente))
                                }
                            }
                        }
                    }
                }
            }

            // ETAPA 3: OA de la unidad elegida.
            3 -> {
                Text("Selecciona un objetivo o crea uno nuevo.")
                Spacer(Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = {
                        if (mostrarFormularioObjetivo && objetivoEditando == null) {
                            mostrarFormularioObjetivo = false
                        } else {
                            limpiarObjetivo()
                            mostrarFormularioObjetivo = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !ocupado
                ) { Text(if (mostrarFormularioObjetivo && objetivoEditando == null) "Cerrar formulario" else "＋ Crear objetivo") }

                if (mostrarFormularioObjetivo) {
                    Spacer(Modifier.height(12.dp))
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                if (objetivoEditando == null) "Nuevo objetivo" else "Editar objetivo #$objetivoEditando",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(10.dp))
                            CampoUtpRemoto(codigoOA, { codigoOA = it }, "Código OA")
                            CampoUtpRemoto(descripcionOA, { descripcionOA = it }, "Descripción del OA", filas = 3)
                            CampoUtpRemoto(ordenOA, { ordenOA = it }, "Orden OA")
                            Button(
                                enabled = !ocupado && codigoOA.isNotBlank() && descripcionOA.isNotBlank() &&
                                        (ordenOA.toIntOrNull() ?: 0) > 0,
                                onClick = {
                                    val unidad = unidadId ?: return@Button
                                    val orden = ordenOA.toIntOrNull() ?: return@Button
                                    val id = objetivoEditando
                                    if (id == null) {
                                        viewModel.crearObjetivo(unidad, codigoOA, descripcionOA, orden)
                                    } else {
                                        viewModel.editarObjetivo(id, unidad, codigoOA, descripcionOA, orden)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (objetivoEditando == null) "Guardar objetivo" else "Guardar cambios") }
                            TextButton(onClick = {
                                limpiarObjetivo()
                                mostrarFormularioObjetivo = false
                            }) { Text("Cancelar") }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Objetivos de esta unidad", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (estado.objetivos.isEmpty() && !ocupado && estado.error == null) {
                    Text("Todavía no hay objetivos en esta unidad.")
                }
                estado.objetivos.forEach { objetivo ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(objetivo.codigo, style = MaterialTheme.typography.titleMedium)
                            Text(objetivo.descripcion)
                            Spacer(Modifier.height(6.dp))
                            EstadoUtpRemoto(objetivo.estado)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        objetivoId = objetivo.id
                                        limpiarMaterial()
                                        mostrarFormularioMaterial = false
                                        viewModel.cargarMateriales(objetivo.id)
                                        paso = 4
                                    },
                                    enabled = !ocupado,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Ver materiales") }
                                OutlinedButton(
                                    onClick = {
                                        objetivoEditando = objetivo.id
                                        codigoOA = objetivo.codigo
                                        descripcionOA = objetivo.descripcion
                                        ordenOA = objetivo.orden.toString()
                                        mostrarFormularioObjetivo = true
                                    },
                                    enabled = !ocupado && objetivo.estado == "BORRADOR"
                                ) { Text("Editar") }
                            }

                            // Las transiciones de OA mantienen las reglas del backend.
                            val siguiente = siguienteEstadoOA(objetivo.estado)
                            if (siguiente != null) {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val unidad = unidadId ?: return@Button
                                        viewModel.cambiarEstadoObjetivo(
                                            objetivoId = objetivo.id,
                                            unidadId = unidad,
                                            estadoNuevo = siguiente
                                        )
                                    },
                                    enabled = !ocupado,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(nombreAccionUtp(siguiente)) }
                            }
                        }
                    }
                }
            }

            // ETAPA 4: materiales del OA elegido, incluidas imágenes y audio.
            4 -> {
                Text("Administra los textos, imágenes y audios de este objetivo.")
                Spacer(Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = {
                        if (mostrarFormularioMaterial && materialEditando == null) {
                            mostrarFormularioMaterial = false
                        } else {
                            limpiarMaterial()
                            mostrarFormularioMaterial = true
                        }
                    },
                    enabled = !ocupado,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (mostrarFormularioMaterial && materialEditando == null) "Cerrar formulario" else "＋ Crear material educativo") }

                if (mostrarFormularioMaterial) {
                    Spacer(Modifier.height(12.dp))
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                if (materialEditando == null) "Nuevo material" else "Editar material #$materialEditando",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(10.dp))
                            CampoUtpRemoto(tituloMaterial, { tituloMaterial = it }, "Título del material")
                            CampoUtpRemoto(explicacionMaterial, { explicacionMaterial = it }, "Explicación escrita", 3)
                            CampoUtpRemoto(ejemploMaterial, { ejemploMaterial = it }, "Ejemplo guiado", 2)
                            CampoUtpRemoto(instruccionesMaterial, { instruccionesMaterial = it }, "Instrucciones", 2)
                            CampoUtpRemoto(ordenMaterial, { ordenMaterial = it }, "Orden")
                            CampoUtpRemoto(responsableOrigen, { responsableOrigen = it }, "Responsable o autor")
                            CampoUtpRemoto(licencia, { licencia = it }, "Licencia")

                            Spacer(Modifier.height(8.dp))
                            Text("Imágenes del material", style = MaterialTheme.typography.titleSmall)
                            if (imagenes.isEmpty()) Text("Sin imágenes seleccionadas")
                            imagenes.forEach { ruta ->
                                Text(ruta, style = MaterialTheme.typography.bodySmall)
                                TextButton(onClick = {
                                    imagenes = imagenes.filterNot { it == ruta }
                                }) { Text("Quitar esta imagen") }
                            }
                            OutlinedButton(
                                enabled = !ocupado,
                                onClick = { elegirImagen.launch(arrayOf("image/*")) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Seleccionar y subir imagen") }

                            Spacer(Modifier.height(14.dp))
                            Text("Audio del material", style = MaterialTheme.typography.titleSmall)
                            Text(audio ?: "Sin audio asignado", style = MaterialTheme.typography.bodySmall)
                            if (audio != null) {
                                TextButton(onClick = { audio = null }) { Text("Quitar audio") }
                            }
                            OutlinedButton(
                                enabled = !ocupado,
                                onClick = { elegirAudio.launch(arrayOf("audio/*")) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Seleccionar y subir audio") }

                            Spacer(Modifier.height(16.dp))
                            val materialValido = tituloMaterial.isNotBlank() &&
                                    responsableOrigen.isNotBlank() && licencia.isNotBlank() &&
                                    (ordenMaterial.toIntOrNull()?.let { it >= 0 } == true)
                            Button(
                                enabled = materialValido && !ocupado,
                                onClick = {
                                    val objetivo = objetivoId ?: return@Button
                                    val orden = ordenMaterial.toIntOrNull() ?: return@Button
                                    val id = materialEditando
                                    if (id == null) {
                                        viewModel.crearMaterial(
                                            objetivoId = objetivo,
                                            titulo = tituloMaterial,
                                            explicacion = explicacionMaterial,
                                            ejemplo = ejemploMaterial,
                                            instrucciones = instruccionesMaterial,
                                            orden = orden,
                                            responsableOrigen = responsableOrigen,
                                            licencia = licencia,
                                            imagenes = imagenes,
                                            audio = audio
                                        )
                                    } else {
                                        viewModel.editarMaterial(
                                            id = id,
                                            objetivoId = objetivo,
                                            titulo = tituloMaterial,
                                            explicacion = explicacionMaterial,
                                            ejemplo = ejemploMaterial,
                                            instrucciones = instruccionesMaterial,
                                            orden = orden,
                                            responsableOrigen = responsableOrigen,
                                            licencia = licencia,
                                            imagenes = imagenes,
                                            audio = audio
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (materialEditando == null) "Guardar material" else "Guardar cambios") }
                            TextButton(onClick = {
                                limpiarMaterial()
                                mostrarFormularioMaterial = false
                            }) { Text("Cancelar") }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Materiales registrados", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (estado.materiales.isEmpty() && !ocupado && estado.error == null) {
                    Text("Aún no hay materiales en este objetivo.")
                }
                estado.materiales.forEach { material ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(material.titulo, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(5.dp))
                            EstadoUtpRemoto(material.estado)
                            Text("Imágenes: ${material.imagenes.size}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                if (material.audio != null) "Audio: disponible" else "Audio: no asignado",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    materialEditando = material.id
                                    tituloMaterial = material.titulo
                                    explicacionMaterial = material.explicacion
                                    ejemploMaterial = material.ejemplo
                                    instruccionesMaterial = material.instrucciones
                                    ordenMaterial = material.orden.toString()
                                    responsableOrigen = material.responsableOrigen
                                    licencia = material.licencia
                                    imagenes = material.imagenes
                                    audio = material.audio
                                    viewModel.limpiarSubidas()
                                    mostrarFormularioMaterial = true
                                },
                                enabled = !ocupado && material.estado == "BORRADOR",
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Editar material") }

                            val siguiente = siguienteEstadoMaterial(material.estado)
                            if (siguiente != null) {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val objetivo = objetivoId ?: return@Button
                                        viewModel.cambiarEstadoMaterial(
                                            materialId = material.id,
                                            objetivoId = objetivo,
                                            estadoNuevo = siguiente
                                        )
                                    },
                                    enabled = !ocupado,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(nombreAccionUtp(siguiente)) }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Indicador sencillo y uniforme de estado en las tarjetas. */
@Composable
private fun EstadoUtpRemoto(estado: String) {
    val color = when (estado) {
        "PUBLICADO" -> MaterialTheme.colorScheme.primaryContainer
        "DESHABILITADO" -> MaterialTheme.colorScheme.errorContainer
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
}

@Composable
private fun CampoUtpRemoto(
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

// La unidad deshabilitada puede volver a publicarse, según el backend actual.
private fun siguienteEstadoUnidad(actual: String): String? = when (actual) {
    "BORRADOR" -> "EN_REVISION"
    "EN_REVISION" -> "AUTORIZADO"
    "AUTORIZADO" -> "PUBLICADO"
    "PUBLICADO" -> "DESHABILITADO"
    "DESHABILITADO" -> "PUBLICADO"
    else -> null
}

// Los OA no se reactivan desde DESHABILITADO.
private fun siguienteEstadoOA(actual: String): String? = when (actual) {
    "BORRADOR" -> "EN_REVISION"
    "EN_REVISION" -> "AUTORIZADO"
    "AUTORIZADO" -> "PUBLICADO"
    else -> null
}

// Los materiales mantienen las transiciones existentes, sin reactivación.
private fun siguienteEstadoMaterial(actual: String): String? = when (actual) {
    "BORRADOR" -> "EN_REVISION"
    "EN_REVISION" -> "AUTORIZADO"
    "AUTORIZADO" -> "PUBLICADO"
    else -> null
}

private fun nombreAccionUtp(destino: String): String = when (destino) {
    "EN_REVISION" -> "Enviar a revisión"
    "AUTORIZADO" -> "Autorizar"
    "PUBLICADO" -> "Publicar"
    "DESHABILITADO" -> "Deshabilitar"
    else -> destino
}
