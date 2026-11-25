
package com.example.pruebaaplicacionesmoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.pruebaaplicacionesmoviles.data.model.Rol
import com.example.pruebaaplicacionesmoviles.ui.screens.ActividadScreen
import com.example.pruebaaplicacionesmoviles.ui.screens.DocenteScreen
import com.example.pruebaaplicacionesmoviles.ui.screens.EstudianteDashboard
import com.example.pruebaaplicacionesmoviles.ui.screens.LoginScreen
import com.example.pruebaaplicacionesmoviles.ui.screens.UnidadDetalleScreen
import com.example.pruebaaplicacionesmoviles.ui.screens.UnidadesContenido
import com.example.pruebaaplicacionesmoviles.ui.screens.UtpScreen
import com.example.pruebaaplicacionesmoviles.ui.screens.UtpContenidoRemotoScreen
import com.example.pruebaaplicacionesmoviles.ui.theme.PruebaAplicacionesMovilesTheme
import com.example.pruebaaplicacionesmoviles.viewmodel.ActividadViewModel
import com.example.pruebaaplicacionesmoviles.viewmodel.ContenidoViewModel
import com.example.pruebaaplicacionesmoviles.viewmodel.DocenteViewModel
import com.example.pruebaaplicacionesmoviles.viewmodel.LoginViewModel
import com.example.pruebaaplicacionesmoviles.viewmodel.UtpViewModel
import com.example.pruebaaplicacionesmoviles.viewmodel.UtpContenidosViewModel
import com.example.pruebaaplicacionesmoviles.core.network.ApiCliente

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val repositorio = (application as MpazApp).repositorio

        val loginViewModel =
            ViewModelProvider(this)[LoginViewModel::class.java]

        val docenteViewModel =
            ViewModelProvider(this)[DocenteViewModel::class.java]

        val utpViewModel =
            ViewModelProvider(this)[UtpViewModel::class.java]

        val actividadViewModel =
            ViewModelProvider(this)[ActividadViewModel::class.java]

        val contenidoViewModel =
            ViewModelProvider(this)[ContenidoViewModel::class.java]

        val utpContenidosViewModel =
            ViewModelProvider(this)[UtpContenidosViewModel::class.java]

        setContent {
            PruebaAplicacionesMovilesTheme {

                val usuarioActual by
                loginViewModel.usuarioAutenticado.collectAsState()

                when (usuarioActual?.rol) {

                    null -> {
                        LoginScreen(loginViewModel)
                    }

                    Rol.ESTUDIANTE -> {
                        usuarioActual?.let { estudiante ->

                            val contenidoEstado by
                            contenidoViewModel.estado.collectAsState()

                            val asignaturas = contenidoEstado.asignaturas
                            val unidades = contenidoEstado.unidades

                            LaunchedEffect(estudiante.id) {
                                contenidoViewModel.cargarContenidos()
                            }

                            val flujoProgresos = remember(estudiante.id) {
                                repositorio.observarProgreso(estudiante.id)
                            }

                            val progresos by flujoProgresos.collectAsState(
                                initial = emptyList()
                            )

                            var asignaturaSeleccionada by
                            remember(estudiante.id) {
                                mutableStateOf<Int?>(null)
                            }

                            var unidadSeleccionada by
                            remember(estudiante.id) {
                                mutableStateOf<Int?>(null)
                            }

                            var actividadAbierta by
                            remember(estudiante.id) {
                                mutableStateOf(false)
                            }

                            when {

                                unidadSeleccionada != null -> {

                                    val unidad = unidades.find {
                                        it.id == unidadSeleccionada
                                    }

                                    if (unidad != null) {

                                        if (actividadAbierta) {

                                            ActividadScreen(
                                                estudianteId = estudiante.id,
                                                unidadId = unidad.id,
                                                viewModel = actividadViewModel,
                                                onVolver = {
                                                    actividadAbierta = false
                                                }
                                            )

                                        } else {

                                            LaunchedEffect(
                                                estudiante.id,
                                                unidad.id
                                            ) {
                                                repositorio.marcarContenidoVisto(
                                                    estudiante.id,
                                                    unidad.id
                                                )
                                            }

                                            UnidadDetalleScreen(
                                                unidad = unidad,
                                                viewModel = contenidoViewModel,
                                                onVolver = {
                                                    unidadSeleccionada = null
                                                },
                                                onAbrirActividades = {
                                                    actividadAbierta = true
                                                }
                                            )
                                        }

                                    } else {
                                        LaunchedEffect(unidadSeleccionada) {
                                            unidadSeleccionada = null
                                        }
                                    }
                                }

                                asignaturaSeleccionada != null -> {

                                    val idAsignatura = asignaturaSeleccionada

                                    if (idAsignatura != null) {
                                        UnidadesContenido(
                                            asignaturaId = idAsignatura,
                                            asignaturas = asignaturas,
                                            unidades = unidades,
                                            progresos = progresos,
                                            onVolver = {
                                                asignaturaSeleccionada = null
                                            },
                                            onAbrirUnidad = { unidadId ->
                                                unidadSeleccionada = unidadId
                                            }
                                        )
                                    }
                                }

                                else -> {

                                    var porcentajeServidor by remember {
                                        mutableStateOf<Int?>(null)
                                    }

                                    LaunchedEffect(estudiante.id) {
                                        porcentajeServidor = try {
                                            val objetivos = ApiCliente.progreso.inicio().objetivos
                                            if (objetivos.isEmpty()) {
                                                0
                                            } else {
                                                objetivos.sumOf { it.porcentajeAvance } / objetivos.size
                                            }
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .statusBarsPadding()
                                    ) {

                                        Button(
                                            onClick = {
                                                contenidoViewModel.limpiar()
                                                loginViewModel.cerrarSesion()
                                            },
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text("Cerrar sesión")
                                        }

                                        if (contenidoEstado.cargando) {
                                            LinearProgressIndicator(
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        contenidoEstado.error?.let { mensaje ->

                                            Text(
                                                text = "Error: $mensaje",
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(12.dp)
                                            )

                                            Button(
                                                onClick = {
                                                    contenidoViewModel.cargarContenidos()
                                                },
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp
                                                )
                                            ) {
                                                Text("Reintentar")
                                            }
                                        }

                                        EstudianteDashboard(
                                            usuario = estudiante,
                                            asignaturas = asignaturas,
                                            unidades = unidades,
                                            progresos = progresos,
                                            onAbrirAsignatura = {
                                                asignaturaSeleccionada = it
                                            },
                                            modifier = Modifier.weight(1f),
                                            porcentajeServidor = porcentajeServidor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Rol.DOCENTE -> {

                        DocenteScreen(
                            viewModel = docenteViewModel,
                            onCerrarSesion = {
                                contenidoViewModel.limpiar()
                                loginViewModel.cerrarSesion()
                            }
                        )
                    }

                    Rol.UTP -> {
                        UtpContenidoRemotoScreen(
                            viewModel = utpContenidosViewModel,
                            onVolver = {
                                utpContenidosViewModel.limpiar()
                                loginViewModel.cerrarSesion()
                            }
                        )
                    }
                }
            }
        }
    }
}
