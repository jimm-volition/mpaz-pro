package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.data.model.EstadoUnidad
import com.example.pruebaaplicacionesmoviles.data.model.Unidad
import com.example.pruebaaplicacionesmoviles.viewmodel.UtpViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazAzul
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazAzulClaro
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazFondo
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazTexto
import androidx.compose.material3.ButtonDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtpScreen(
    viewModel: UtpViewModel,
    onCerrarSesion: () -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()
    var unidadParaImagen by remember { mutableStateOf<Int?>(null) }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val id = unidadParaImagen
        if (uri != null && id != null) {
            viewModel.actualizarImagen(id, uri.toString())
        }
        unidadParaImagen = null
    }

    LaunchedEffect(estado.mensaje) {
        if (estado.mensaje.isNotEmpty()) {
            delay(2000)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        containerColor = MpazFondo,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Panel UTP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MpazAzul),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Panel UTP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Autoriza o publica unidades antes de que el docente las asigne.",
                        color = MpazAzulClaro
                    )
                }
            }

Spacer(modifier = Modifier.height(12.dp))


            AnimatedVisibility(
                visible = estado.mensaje.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Text(
                    text = estado.mensaje,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(estado.unidades, key = { it.id }) { unidad ->
                    UnidadUtpCard(
                        unidad = unidad,
                        onAutorizar = { viewModel.autorizar(unidad.id) },
                        onPublicar = { viewModel.publicar(unidad.id) },
                        onDeshabilitar = { viewModel.deshabilitar(unidad.id) },
                        onElegirImagen = {
                            unidadParaImagen = unidad.id
                            selectorImagen.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UnidadUtpCard(
    unidad: Unidad,
    onAutorizar: () -> Unit,
    onPublicar: () -> Unit,
    onDeshabilitar: () -> Unit,
    onElegirImagen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                unidad.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MpazTexto
            )
            Text("OA: ${unidad.codigoOA}", color = MpazAzul)
            Text("Estado: ${unidad.estado.name}")
            if (!unidad.imagenReferencia.isNullOrBlank()) {
                Text("Imagen: cargada", color = MpazAzul)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (unidad.estado != EstadoUnidad.AUTORIZADO &&
                    unidad.estado != EstadoUnidad.PUBLICADO
                ) {
                    Button(onClick = onAutorizar) { Text("Autorizar") }
                }
                if (unidad.estado == EstadoUnidad.AUTORIZADO) {
                    Button(onClick = onPublicar) { Text("Publicar") }
                }
                if (unidad.estado == EstadoUnidad.PUBLICADO) {
                    Button(onClick = onDeshabilitar) { Text("Deshabilitar") }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(onClick = onElegirImagen) {
                Text("Elegir imagen")
            }
        }
    }
}