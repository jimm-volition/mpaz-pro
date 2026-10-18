
package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.pruebaaplicacionesmoviles.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnidadesContenido(
    asignaturaId: Int,
    asignaturas: List<Asignatura>,
    unidades: List<Unidad>,
    progresos: List<Progreso>,
    onVolver: () -> Unit,
    onAbrirUnidad: (Int) -> Unit
){

    //PROFESOR TOY COMENTANDO PARA NO PERDERME
    val nombreAsignatura = asignaturas.find{ // Se busca la asignatura seleccionada
        it.id == asignaturaId //Comparacion del id con el recibido en pantalla
    }?.nombre ?: "Asignatura" //Se obtiene el nombre o se muestra Asignatura si no existe pe causa

    val unidadesVisibles = unidades.filter{ it.asignaturaId == asignaturaId} //Filtramos las unidades recibidas y se conservan solo los de la asignatura seleccionada

    //La estructura principal de la pantalla
    Scaffold(
        containerColor = Color(0xFFF5F8FD), // Color del fondo
        topBar = { //LA FAKIN BARRA SUPERIOR
            TopAppBar(
                title = {Text("Unidades")},//EL TITULOO
                navigationIcon = {
                    IconButton(onClick = onVolver){ //Cuando se clickea se ejecuta la funcion Onvolver
                        Icon(
                            imageVector =  Icons.AutoMirrored.Filled.ArrowBack, //Esto pa mostrar la flecha paatras
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 18.dp)
        ){
            Text( //Aca vamos a mostrar el nombre de la asignatura
                text = nombreAsignatura, //ACA UTILIZAMOS EL NOMBRE QUE HABIAMOS ENCONTRADO EN FIND
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Tus unidades de aprendizaje", //un subtitulo chaval chavalib
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Card( //ACA CREAMOS UNA TARJETA INFORMATIVA
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDBEAFE)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column( //ACA COLOCAMOS LOS TEXTOS UNO DEBAJO DEL OTROO
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = "${unidadesVisibles.size} unidades disponibles", //Mostramos cuantas unidades encontramos
                        color = Color(0xFF1D4ED8),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Selecciona una unidad para continuar",
                        color = Color(0xFF1D4ED8),
                        style = MaterialTheme.typography.bodySmall
                    )

                }

            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            if (unidadesVisibles.isEmpty()){ //Se comprueba si la lista filtrada esta vacia
                Text(
                    text = "Todavia no tienes unidades asignadas disponibles en esta asignatura",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }else{ //SI la lista tiene unidades, mostramos las tarjetas de las fakin unidades
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items( //ACA GENERAMOS LOS ELEMENTOS DE LA LISTA
                        items = unidadesVisibles, //ACA UTILIZAMOS LAS UNIDADES FILTRADAS
                        key = {it.id} //IDENTIFICAMOS CADA UNIDAD MEDIANTE SU ID UNICO
                    ) {unidad ->  //UNIDAD REPRESENTA TEMPORALMENTE CADA OBJETO DE LA LISTA
                        val progresoUnidad = progresos.find{it.unidadId == unidad.id}

                        TarjetaUnidad( //AHORA CREAMOS UNA TARJETRA PARA MOSTRAR LA UNIDAD
                            unidad = unidad,
                            progreso = progresoUnidad,
                            onClick = {onAbrirUnidad(unidad.id)}
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaUnidad(

    unidad: Unidad, // Recibimos los datos de la unidad que mostraremos
    progreso: Progreso?, // Recibimos su progreso, que puede ser null
    onClick: () -> Unit // Recibimos la función que ejecutaremos al presionar la tarjeta

) {

    val estado = when { // Determinamos qué mensaje de estado mostrar

        progreso?.actividadCompletada == true -> "Actividad realizada" // Si completó la actividad

        progreso?.contenidoVisto == true -> "Contenido revisado" // Si revisó el contenido

        else -> "Por comenzar" //
    }


    val tieneResultado = progreso?.actividadCompletada == true // Comprobamos si completó la actividad


    val puntaje = progreso?.porcentaje?.coerceIn(0, 100) ?: 0 // OBTENEMOS EL PUNTAJE


    Card( // Creamos la tarjeta que representa una unidad

        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )

    ) {

        Column( // Organizamos el contenido de la tarjeta verticalmente

            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)

        ) {

            Row( // Colocamos el código OA y el estado horizontalmente

                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically

            ) {

                Text(
                    text = unidad.codigoOA, // Mostramos el código del objetivo de aprendizaje
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )


                Surface( // Creamos un fondo para destacar el estado de la unidad

                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(20.dp)

                ) {

                    Text(
                        text = estado, // Mostramos el estado calculado anteriormente

                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),

                        color = Color(0xFF166534),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }


            Text(
                text = unidad.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )


            Text(
                text = unidad.descripcionOA,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )


            if (tieneResultado) { // Comprobamos si existe una actividad completada

                Row( // Colocamos el texto y el puntaje en una fila

                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {

                    Text(
                        text = "Último puntaje registrado",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "$puntaje%",
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold
                    )
                }


                LinearProgressIndicator( // Creamos una barra visual que representa el puntaje

                    progress = { puntaje / 100f },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),

                    color = Color(0xFF2563EB),
                    trackColor = Color(0xFFDBEAFE)
                )

            } else { // Si la actividad aún no está completada

                Text(
                    text = "Sin resultados registrados",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }


            Row( // Creamos una fila para indicar que podemos abrir la unidad

                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically

            ) {

                Text(
                    text = "Abrir unidad",
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF2563EB)
                )
            }
        }
    }
}


// Esta función permite ver cómo quedará la pantalla en Android Studio
@Preview(showBackground = true, widthDp = 390, heightDp = 780) // Simulamos una pantalla de 390 x 780 dp
@Composable // Indicamos que es una función de interfaz
fun UnidadesContenidoPreview() {

    UnidadesContenido( // Llamamos a nuestra pantalla utilizando datos ficticios

        asignaturaId = 2, // Simulamos que seleccionamos Matemática

        asignaturas = listOf( // Creamos una lista de asignaturas para la vista previa

            Asignatura(1, "Lenguaje y Comunicación"), // Asignatura con ID 1
            Asignatura(2, "Matemática") // Asignatura con ID 2
        ),

        unidades = listOf( // Creamos unidades de prueba para visualizar las tarjetas

            Unidad(
                id = 2, // Identificador de la primera unidad
                asignaturaId = 2, // Pertenece a Matemática
                titulo = "Sumas hasta 20", // Título de la unidad
                codigoOA = "MAT-2B-01", // Código de demostración del objetivo
                descripcionOA = "Resolver sumas de dos cantidades hasta 20.", // Descripción del objetivo
                ejeCurricular = "Números y operaciones", // Área de aprendizaje
                explicacion = "Sumar es juntar cantidades.", // Contenido explicativo
                ejemploGuiado = "4 + 3 = 7", // Ejemplo de la unidad
                estado = EstadoUnidad.PUBLICADO // Simulamos que ya está publicada
            ),

            Unidad(
                id = 5, // Identificador de la segunda unidad
                asignaturaId = 2, // También pertenece a Matemática
                titulo = "Multiplicaciones", // Título de la unidad
                codigoOA = "MAT-DEMO-02", // Código de demostración
                descripcionOA = "Practicar multiplicaciones sencillas.", // Descripción del objetivo
                ejeCurricular = "Números y operaciones", // Área de aprendizaje
                explicacion = "Multiplicar es sumar cantidades iguales.", // Explicación de la unidad
                ejemploGuiado = "3 x 2 = 6", // Ejemplo guiado
                estado = EstadoUnidad.PUBLICADO // Simulamos que ya está publicada
            ),

            Unidad(
                id = 1, // Identificador de la tercera unidad
                asignaturaId = 1, // Esta unidad pertenece a Lenguaje
                titulo = "Personajes de un cuento", // Título de la unidad
                codigoOA = "LEN-2B-01", // Código de demostración
                descripcionOA = "Reconocer personajes.", // Objetivo de aprendizaje
                ejeCurricular = "Lectura", // Área de aprendizaje
                explicacion = "Contenido de ejemplo.", // Explicación de prueba
                ejemploGuiado = "Ejemplo de lectura.", // Ejemplo de prueba
                estado = EstadoUnidad.PUBLICADO // Simulamos que está publicada
            )
        ),

        progresos = listOf( // Creamos una lista con datos de progreso ficticios

            Progreso(
                id = 1, // Identificador del registro de progreso
                estudianteId = 3, // Pertenece al estudiante con ID 3
                unidadId = 2, // Corresponde a la unidad de Sumas hasta 20
                contenidoVisto = true, // El estudiante ya revisó el contenido
                actividadCompletada = true, // El estudiante completó la actividad
                porcentaje = 70 // El estudiante obtuvo un 70%
            )
        ),

        onVolver = {}, // En el Preview no ejecutamos ninguna acción para volver
        onAbrirUnidad = {} // En el Preview no navegamos a otra pantalla
    )
}
