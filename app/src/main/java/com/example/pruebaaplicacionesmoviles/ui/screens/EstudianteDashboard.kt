package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.pruebaaplicacionesmoviles.data.model.*

@Composable
fun EstudianteDashboard(
    usuario: Usuario?,
    asignaturas: List<Asignatura>,
    unidades: List<Unidad>,
    progresos: List<Progreso>,
    onAbrirAsignatura: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    // Donde se calcula el progreso
    val progresoGeneral = if (unidades.isEmpty()) {
        0
    } else {
        unidades.sumOf { unidad ->
            progresos.find { it.unidadId == unidad.id }?.porcentaje ?: 0
        } / unidades.size
    }

    val progresoAnimado by animateFloatAsState(
        targetValue = progresoGeneral.coerceIn(0, 100) / 100f,
        label = "Progreso general"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8FD))
            .padding(16.dp)
    ) {

        // Bienvenida
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2563EB)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "¡Hola, ${usuario?.alias ?: "estudiante"}! 👋",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = usuario?.curso ?: "MPAZ PRO",
                    color = Color(0xFFDBEAFE)
                )

                Text(
                    text = "¡Hoy es un buen día para aprender!",
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // el progreso real
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Tu progreso",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$progresoGeneral%",
                        color = Color(0xFF2563EB)
                    )
                }

                Spacer(Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progresoAnimado },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = Color(0xFF2563EB),
                    trackColor = Color(0xFFDBEAFE)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Mis asignaturas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(asignaturas, key = { it.id }) { asignatura ->

                val unidadesMateria = unidades.filter {
                    it.asignaturaId == asignatura.id
                }

                val avance = if (unidadesMateria.isEmpty()) {
                    0
                } else {
                    unidadesMateria.sumOf { unidad ->
                        progresos.find {
                            it.unidadId == unidad.id
                        }?.porcentaje ?: 0
                    } / unidadesMateria.size
                }

                TarjetaMateria(
                    asignatura = asignatura,
                    avance = avance,
                    tieneUnidades = unidadesMateria.isNotEmpty(),
                    onClick = {
                        onAbrirAsignatura(asignatura.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaMateria(
    asignatura: Asignatura,
    avance: Int,
    tieneUnidades: Boolean,
    onClick: () -> Unit
) {
    val (icono, color, fondo) = estiloAsignatura(asignatura.id)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(fondo, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = color
                )
            }

            Column {
                Text(
                    text = asignatura.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = if (tieneUnidades) {
                        "Avance: $avance%"
                    } else {
                        "Sin unidades asignadas"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )

                Text(
                    text = "Ver unidades →",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

private data class EstiloMateria(
    val icono: ImageVector,
    val color: Color,
    val fondo: Color
)

private fun estiloAsignatura(id: Int): EstiloMateria {
    return when (id) {
        1 -> EstiloMateria(
            Icons.Default.MenuBook,
            Color(0xFFEA580C),
            Color(0xFFFFEDD5)
        )
        2 -> EstiloMateria(
            Icons.Default.Calculate,
            Color(0xFF2563EB),
            Color(0xFFDBEAFE)
        )
        3 -> EstiloMateria(
            Icons.Default.Science,
            Color(0xFF15803D),
            Color(0xFFDCFCE7)
        )
        else -> EstiloMateria(
            Icons.Default.Public,
            Color(0xFF7C3AED),
            Color(0xFFEDE9FE)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 780)
@Composable
fun EstudianteDashboardPreview() {
    EstudianteDashboard(
        usuario = Usuario(
            id = 3,
            alias = "EST01",
            rol = Rol.ESTUDIANTE,
            curso = "2° Básico A",
            docenteId = 2
        ),
        asignaturas = listOf(
            Asignatura(1, "Lenguaje y Comunicación"),
            Asignatura(2, "Matemática"),
            Asignatura(3, "Ciencias Naturales"),
            Asignatura(4, "Historia, Geografía y Ciencias Sociales")
        ),
        unidades = listOf(
            Unidad(
                id = 2,
                asignaturaId = 2,
                titulo = "Sumas hasta 20",
                codigoOA = "MAT-2B-01",
                descripcionOA = "Resolver sumas hasta 20.",
                ejeCurricular = "Números",
                explicacion = "Sumar es juntar.",
                ejemploGuiado = "4 + 3 = 7"
            )
        ),
        progresos = listOf(
            Progreso(
                id = 1,
                estudianteId = 3,
                unidadId = 2,
                contenidoVisto = true,
                actividadCompletada = true,
                porcentaje = 10
            )
        ),
        onAbrirAsignatura = {}
    )
}