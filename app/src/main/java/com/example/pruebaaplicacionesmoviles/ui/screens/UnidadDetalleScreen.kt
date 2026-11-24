
package com.example.pruebaaplicacionesmoviles.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.data.model.Unidad
import com.example.pruebaaplicacionesmoviles.viewmodel.ContenidoViewModel
import java.io.File
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UnidadDetalleScreen(
    unidad: Unidad,
    viewModel: ContenidoViewModel,
    onVolver: () -> Unit,
    onAbrirActividades: () -> Unit
) {
    val context = LocalContext.current

    val estadoMateriales by viewModel.estadoMateriales.collectAsState()

    val materiales = estadoMateriales.materiales
    val cargando = estadoMateriales.cargando
    val error = estadoMateriales.error

    LaunchedEffect(unidad.id) {
        viewModel.cargarMateriales(unidad.id)
    }

    var motorVoz by remember {
        mutableStateOf<TextToSpeech?>(null)
    }

    var vozLista by remember {
        mutableStateOf(false)
    }

    val textoLectura = unidad.textoParaAudio
        ?.takeIf { it.isNotBlank() }
        ?: "${unidad.titulo}. ${unidad.explicacion}. ${unidad.ejemploGuiado}"

    DisposableEffect(context) {
        val motor = TextToSpeech(context) { resultado ->
            vozLista = resultado == TextToSpeech.SUCCESS
        }

        motorVoz = motor

        onDispose {
            motor.stop()
            motor.shutdown()
            motorVoz = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Button(onClick = onVolver) {
            Text("← Volver")
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = unidad.titulo,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(20.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🎯 Objetivo de aprendizaje",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                Text(unidad.descripcionOA)
            }
        }

        Spacer(Modifier.height(20.dp))

        if (unidad.explicacion.isNotBlank()) {
            Text(
                text = "📖 Explicación",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(unidad.explicacion)

            Spacer(Modifier.height(20.dp))
        }

        if (unidad.ejemploGuiado.isNotBlank()) {
            Text(
                text = "Ejemplo guiado",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(unidad.ejemploGuiado)

            Spacer(Modifier.height(20.dp))
        }

        Button(
            onClick = onAbrirActividades,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📝 Resolver actividades")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = vozLista,
            onClick = {
                motorVoz?.let { motor ->
                    motor.language = Locale.forLanguageTag("es-CL")

                    motor.speak(
                        textoLectura,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "unidad_${unidad.id}"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔊 Escuchar explicación")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                motorVoz?.stop()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Detener lectura")
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Text(
            text = "📚 Materiales educativos",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        if (cargando) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        error?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )

            Button(
                onClick = {
                    viewModel.cargarMateriales(unidad.id)
                }
            ) {
                Text("Reintentar")
            }
        }

        if (!cargando && error == null && materiales.isEmpty()) {
            Text("No hay materiales publicados para esta unidad.")
        }

        if (!cargando && error == null) {
            materiales.forEach { material ->

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = material.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        if (
                            material.explicacion.isNotBlank() &&
                            material.explicacion != unidad.explicacion
                        ) {
                            Text(material.explicacion)
                            Spacer(Modifier.height(8.dp))
                        }

                        if (
                            material.ejemplo.isNotBlank() &&
                            material.ejemplo != unidad.ejemploGuiado
                        ) {
                            Text("Ejemplo: ${material.ejemplo}")
                            Spacer(Modifier.height(8.dp))
                        }

                        if (material.instrucciones.isNotBlank()) {
                            Text(
                                "Instrucciones: ${material.instrucciones}"
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        material.imagenes.forEach { ruta ->
                            ImagenRemota(
                                ruta = ruta,
                                viewModel = viewModel
                            )
                        }

                        material.audio?.let { ruta ->
                            AudioRemoto(
                                ruta = ruta,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagenRemota(
    ruta: String,
    viewModel: ContenidoViewModel
) {
    var imagen by remember(ruta) {
        mutableStateOf<Bitmap?>(null)
    }

    var cargando by remember(ruta) {
        mutableStateOf(true)
    }

    var error by remember(ruta) {
        mutableStateOf(false)
    }

    LaunchedEffect(ruta, viewModel) {
        cargando = true
        error = false
        imagen = null

        try {
            val bytes = viewModel.descargarRecurso(ruta)

            imagen = withContext(Dispatchers.Default) {
                BitmapFactory.decodeByteArray(
                    bytes,
                    0,
                    bytes.size
                )
            }

            error = imagen == null

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            Log.e("MPAZ_IMAGEN", "Error cargando imagen", e)
            error = true

        } finally {
            cargando = false
        }
    }

    Spacer(Modifier.height(12.dp))

    when {
        cargando -> {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        imagen != null -> {
            imagen?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Imagen educativa",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }

        error -> {
            Text("No se pudo mostrar la imagen.")
        }
    }
}

@Composable
private fun AudioRemoto(
    ruta: String,
    viewModel: ContenidoViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var reproductor by remember(ruta) {
        mutableStateOf<MediaPlayer?>(null)
    }

    var archivoTemporal by remember(ruta) {
        mutableStateOf<File?>(null)
    }

    var cargando by remember(ruta) {
        mutableStateOf(false)
    }

    var error by remember(ruta) {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(ruta) {
        onDispose {
            reproductor?.release()
            reproductor = null

            archivoTemporal?.delete()
            archivoTemporal = null
        }
    }

    Spacer(Modifier.height(12.dp))

    Button(
        enabled = !cargando,
        onClick = {
            scope.launch {
                cargando = true
                error = null

                var reproductorNuevo: MediaPlayer? = null

                try {
                    reproductor?.release()
                    reproductor = null

                    archivoTemporal?.delete()
                    archivoTemporal = null

                    val datos = viewModel.descargarRecurso(ruta)

                    val extension = ruta
                        .substringAfterLast(".")
                        .lowercase()
                        .takeIf {
                            it in listOf("wav", "mp3", "m4a", "ogg")
                        } ?: "bin"

                    val archivo = withContext(Dispatchers.IO) {
                        File.createTempFile(
                            "mpaz_audio_",
                            ".$extension",
                            context.cacheDir
                        ).apply {
                            writeBytes(datos)
                        }
                    }

                    archivoTemporal = archivo

                    val nuevo = MediaPlayer()
                    reproductorNuevo = nuevo

                    nuevo.setDataSource(archivo.absolutePath)

                    withContext(Dispatchers.IO) {
                        nuevo.prepare()
                    }

                    nuevo.setOnCompletionListener { terminado ->
                        terminado.release()

                        if (reproductor === terminado) {
                            reproductor = null
                        }

                        archivoTemporal?.delete()
                        archivoTemporal = null
                    }

                    reproductor = nuevo
                    reproductorNuevo = null

                    nuevo.start()

                } catch (e: CancellationException) {
                    reproductorNuevo?.release()
                    archivoTemporal?.delete()
                    archivoTemporal = null
                    throw e

                } catch (e: Exception) {
                    reproductorNuevo?.release()

                    archivoTemporal?.delete()
                    archivoTemporal = null

                    Log.e("MPAZ_AUDIO", "Error reproduciendo audio", e)

                    error = "No se pudo reproducir el audio"

                } finally {
                    cargando = false
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            if (cargando) {
                "Cargando audio..."
            } else {
                "▶ Reproducir audio del contenido"
            }
        )
    }

    if (reproductor != null) {
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                reproductor?.release()
                reproductor = null

                archivoTemporal?.delete()
                archivoTemporal = null
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Detener audio del contenido")
        }
    }

    error?.let { mensaje ->
        Text(
            text = mensaje,
            color = MaterialTheme.colorScheme.error
        )
    }
}
