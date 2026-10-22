
package com.example.pruebaaplicacionesmoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.pruebaaplicacionesmoviles.ui.screens.UnidadesContenido
import com.example.pruebaaplicacionesmoviles.ui.screens.UnidadDetalleScreen
import com.example.pruebaaplicacionesmoviles.ui.screens.ActividadScreen
import com.example.pruebaaplicacionesmoviles.viewmodel.ActividadViewModel

import com.example.pruebaaplicacionesmoviles.data.model.Rol
import com.example.pruebaaplicacionesmoviles.ui.screens.LoginScreen
import com.example.pruebaaplicacionesmoviles.ui.screens.DocenteScreen
import com.example.pruebaaplicacionesmoviles.ui.screens.UtpScreen
import com.example.pruebaaplicacionesmoviles.ui.screens.EstudianteDashboard
import com.example.pruebaaplicacionesmoviles.ui.theme.PruebaAplicacionesMovilesTheme
import com.example.pruebaaplicacionesmoviles.viewmodel.LoginViewModel
import com.example.pruebaaplicacionesmoviles.viewmodel.DocenteViewModel
import com.example.pruebaaplicacionesmoviles.viewmodel.UtpViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val repositorio = (application as MpazApp).repositorio
        val loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        val docenteViewModel = ViewModelProvider(this)[DocenteViewModel::class.java]
        val utpViewModel = ViewModelProvider(this)[UtpViewModel::class.java]
        val actividadViewModel = ViewModelProvider(this)[ActividadViewModel::class.java]

        setContent {

            PruebaAplicacionesMovilesTheme {


                val usuarioActual by loginViewModel.usuarioAutenticado.collectAsState()

                when (usuarioActual?.rol) {

                    null -> {

                        LoginScreen(loginViewModel)
                    }

                    Rol.ESTUDIANTE -> {


                        usuarioActual?.let { estudiante ->


                            val flujoAsignaturas = remember {
                                repositorio.observarAsignaturas()
                            }

                            val asignaturas by flujoAsignaturas.collectAsState(
                                initial = emptyList()
                            )


                            val flujoUnidades = remember(estudiante.id) {
                                repositorio.observarUnidadesAsignadas(estudiante.id)
                            }

                            val unidades by flujoUnidades.collectAsState(
                                initial = emptyList()
                            )


                            val flujoProgresos = remember(estudiante.id) {
                                repositorio.observarProgreso(estudiante.id)
                            }

                            val progresos by flujoProgresos.collectAsState(
                                initial = emptyList()
                            )


                            var asignaturaSeleccionada by remember(estudiante.id) {
                                mutableStateOf<Int?>(null)
                            }

                            var unidadSeleccionada by remember(estudiante.id) {
                                mutableStateOf<Int?>(null)
                            }

                            var actividadAbierta by remember(estudiante.id) {
                                mutableStateOf(false)
                            }



                            when {

                                // PRIMER CASO: El estudiante seleccionó una unidad
                                unidadSeleccionada != null -> {

                                    // Buscamos la unidad seleccionada en la lista que vino desde Room
                                    val unidad = unidades.find {
                                        it.id == unidadSeleccionada // Comparamos los identificadores
                                    }

                                    // Comprobamos que realmente exista esa unidad
                                    if (unidad != null) {

                                        // NUEVO: COMPROBAMOS SI EL ESTUDIANTE ABRIO LAS ACTIVIDADES
                                        if (actividadAbierta) {

                                            // NUEVO: MOSTRAMOS LA PANTALLA DE ACTIVIDADES
                                            ActividadScreen(
                                                estudianteId = estudiante.id, // ENVIAMOS EL ID DEL ESTUDIANTE

                                                unidadId = unidad.id, // ENVIAMOS EL ID DE LA UNIDAD

                                                viewModel = actividadViewModel, // ENVIAMOS EL VIEWMODEL DE ACTIVIDADES

                                                onVolver = {
                                                    actividadAbierta = false // REGRESAMOS AL CONTENIDO EDUCATIVO
                                                }
                                            )

                                        } else {

                                            // Ejecutamos una operación cuando se abre una unidad diferente
                                            LaunchedEffect(estudiante.id, unidad.id) {

                                                // Guardamos en Room que el estudiante revisó su contenido
                                                repositorio.marcarContenidoVisto(estudiante.id, unidad.id)
                                            }

                                            // Mostramos la pantalla con la explicación y el audio
                                            UnidadDetalleScreen(
                                                unidad = unidad, // Enviamos la unidad encontrada

                                                onVolver = {
                                                    unidadSeleccionada = null // Regresamos a la lista de unidades
                                                },

                                                // NUEVO: PERMITIMOS ABRIR LAS ACTIVIDADES
                                                onAbrirActividades = {
                                                    actividadAbierta = true // CAMBIAMOS A ACTIVIDADSCREEN
                                                }
                                            )
                                        }

                                    } else {

                                        // Si la unidad ya no existe, regresamos a la lista
                                        LaunchedEffect(unidadSeleccionada) {
                                            unidadSeleccionada = null
                                        }
                                    }
                                }

                                // SEGUNDO CASO: Seleccionó una asignatura, pero ninguna unidad
                                asignaturaSeleccionada != null -> {

                                    // Guardamos el ID de la asignatura en una variable local
                                    val idAsignatura = asignaturaSeleccionada

                                    // Comprobamos que el ID no sea null
                                    if (idAsignatura != null) {

                                        // Mostramos tu pantalla real de unidades
                                        UnidadesContenido(
                                            asignaturaId = idAsignatura, // Asignatura seleccionada
                                            asignaturas = asignaturas, // Lista de asignaturas de Room
                                            unidades = unidades, // Unidades asignadas al estudiante
                                            progresos = progresos, // Progresos almacenados

                                            onVolver = {
                                                asignaturaSeleccionada = null // Regresamos al Dashboard
                                            },

                                            onAbrirUnidad = { unidadId ->
                                                unidadSeleccionada = unidadId // Abrimos la unidad seleccionada
                                            }
                                        )
                                    }
                                }

                                // TERCER CASO: No ha seleccionado ninguna asignatura
                                else -> {

                                    // Colocamos el botón de salir encima del Dashboard
                                    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

                                        // Botón para cerrar sesión
                                        Button(
                                            onClick = {
                                                loginViewModel.cerrarSesion() // Borra la sesión actual
                                            },
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text("Cerrar sesión") // Texto del botón
                                        }

                                        // Mostramos el Dashboard que ya programaste
                                        EstudianteDashboard(
                                            usuario = estudiante, // Usuario autenticado
                                            asignaturas = asignaturas, // Asignaturas de Room
                                            unidades = unidades, // Unidades del estudiante
                                            progresos = progresos, // Progresos guardados

                                            onAbrirAsignatura = { asignaturaId ->
                                                asignaturaSeleccionada = asignaturaId // Abre la asignatura
                                            },

                                            modifier = Modifier.weight(1f) // Usa el espacio debajo del botón
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
                                loginViewModel.cerrarSesion()
                            }
                        )
                    }

                    Rol.UTP -> {


                        UtpScreen(
                            viewModel = utpViewModel,

                            onCerrarSesion = {
                                loginViewModel.cerrarSesion()
                            }
                        )
                    }
                }
            }
        }
    }
}
