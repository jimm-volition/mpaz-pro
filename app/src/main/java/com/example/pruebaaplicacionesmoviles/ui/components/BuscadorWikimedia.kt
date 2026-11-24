
package com.example.pruebaaplicacionesmoviles.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pruebaaplicacionesmoviles.core.network.ImagenWikimedia
import com.example.pruebaaplicacionesmoviles.core.network.WikimediaRepositorio
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest

@Composable
fun BuscadorWikimedia(
    onElegir: (ImagenWikimedia) -> Unit
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var busqueda by remember { mutableStateOf("") }
    var buscando by remember { mutableStateOf(false) }
    var consultado by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var resultados by remember {
        mutableStateOf<List<ImagenWikimedia>>(emptyList())
    }

    var seleccionada by remember {
        mutableStateOf<String?>(null)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                "Buscar imágenes educativas",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "API externa: Wikimedia Commons",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Ejemplo: sistema solar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                enabled = busqueda.isNotBlank() && !buscando,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        buscando = true
                        consultado = true
                        error = null
                        resultados = emptyList()
                        seleccionada = null

                        try {
                            resultados =
                                WikimediaRepositorio.buscarImagenes(
                                    busqueda
                                )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            error = e.message
                                ?: "Error al consultar Wikimedia"
                        } finally {
                            buscando = false
                        }
                    }
                }
            ) {
                Text("Buscar en Wikimedia")
            }

            if (buscando) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            error?.let {
                Text(
                    text = "Error: $it",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (
                consultado &&
                !buscando &&
                error == null &&
                resultados.isEmpty()
            ) {
                Text(
                    "No se encontraron imágenes compatibles. " +
                            "Prueba con otro término."
                )
            }

            resultados.forEach { imagen ->

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        val context = LocalContext.current

                        val solicitud = remember(imagen.miniatura) {
                            ImageRequest.Builder(context)
                                .data(imagen.miniatura)
                                .addHeader(
                                    "User-Agent",
                                    "MPAZPRO-Android/1.0 " +
                                            "(https://github.com/jimm-volition/mpaz-pro)"
                                )
                                .crossfade(true)
                                .build()
                        }

                        SubcomposeAsyncImage(
                            model = solicitud,
                            contentDescription = imagen.titulo,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No se pudo cargar la miniatura",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            success = {
                                SubcomposeAsyncImageContent()
                            }
                        )

                        Text(
                            imagen.titulo,
                            style =
                                MaterialTheme.typography.titleSmall
                        )

                        Text(
                            "Autor: ${imagen.autor}",
                            style =
                                MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "Licencia: ${imagen.licencia}",
                            style =
                                MaterialTheme.typography.bodySmall
                        )

                        TextButton(
                            onClick = {
                                uriHandler.openUri(imagen.fuente)
                            }
                        ) {
                            Text("Ver fuente y licencia")
                        }

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                seleccionada = imagen.url
                                onElegir(imagen)
                            }
                        ) {
                            Text(
                                if (seleccionada == imagen.url)
                                    "Imagen seleccionada ✓"
                                else
                                    "Usar esta imagen"
                            )
                        }
                    }
                }
            }
        }
    }
}
