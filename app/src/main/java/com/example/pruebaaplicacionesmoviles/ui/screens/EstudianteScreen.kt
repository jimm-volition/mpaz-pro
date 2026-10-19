
package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.pruebaaplicacionesmoviles.data.model.Asignatura
import com.example.pruebaaplicacionesmoviles.ui.components.AsignaturaCard

@Composable
fun EstudianteScreen() {

    // Datos temporales h
    val asignaturas = remember {
        listOf(
            Asignatura(1, "Matemática"),
            Asignatura(2, "Lenguaje"),
            Asignatura(3, "Ciencias Naturales"),
            Asignatura(4, "Historia y Geografía")
        )
    }

    var asignaturaSeleccionada by remember {
        mutableStateOf<Asignatura?>(null)
    }

    // Progreso temporal: 0% hasta conectar Room
    val progresoGeneral = 0f

    val progresoAnimado by animateFloatAsState(
        targetValue = progresoGeneral,
        animationSpec = tween(durationMillis = 800),
        label = "Progreso general"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8FD))
            .padding(20.dp)
    ) {

        // Cabecera azul
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF2563EB),
                            Color(0xFF1E40AF)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Text(
                text = "¡Hola, estudiante! 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¡Hoy es un buen día para aprender!",
                color = Color(0xFFDBEAFE)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tarjeta de progreso
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tu progreso",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${(progresoGeneral * 100).toInt()}%",
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progresoAnimado },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    color = Color(0xFF2563EB),
                    trackColor = Color(0xFFDBEAFE)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "¡Completa tus actividades para avanzar!",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Mis asignaturas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Selecciona una asignatura para continuar",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cuadrícula de dos columnas
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = asignaturas,
                key = { it.id }
            ) { asignatura ->
                AsignaturaCard(
                    asignatura = asignatura,
                    seleccionada = asignaturaSeleccionada?.id == asignatura.id,
                    onClick = {
                        asignaturaSeleccionada = asignatura
                    }
                )
            }
        }

        // Aparece solamente después de seleccionar una tarjeta
        AnimatedVisibility(
            visible = asignaturaSeleccionada != null
        ) {
            Text(
                text = "Seleccionaste: ${asignaturaSeleccionada?.nombre.orEmpty()}",
                color = Color(0xFF2563EB),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EstudianteScreenPreview() {
    EstudianteScreen()
}
