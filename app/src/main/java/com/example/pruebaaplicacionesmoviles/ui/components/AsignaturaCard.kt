
package com.example.pruebaaplicacionesmoviles.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.pruebaaplicacionesmoviles.data.model.Asignatura

@Composable
fun AsignaturaCard(
    asignatura: Asignatura,
    seleccionada: Boolean,
    onClick: () -> Unit
) {

    // Iconos y colores
    val (icono, colorPrincipal, colorSuave) = when {

        asignatura.nombre.contains("Matem", ignoreCase = true) ->
            Triple(
                Icons.Default.Calculate,
                Color(0xFF2563EB),
                Color(0xFFDBEAFE)
            )

        asignatura.nombre.contains("Leng", ignoreCase = true) ->
            Triple(
                Icons.Default.MenuBook,
                Color(0xFFEA580C),
                Color(0xFFFFEDD5)
            )

        asignatura.nombre.contains("Ciencias", ignoreCase = true) ->
            Triple(
                Icons.Default.Science,
                Color(0xFF15803D),
                Color(0xFFDCFCE7)
            )

        else ->
            Triple(
                Icons.Default.Public,
                Color(0xFF7C3AED),
                Color(0xFFEDE9FE)
            )
    }

    val colorTarjeta by animateColorAsState(
        targetValue = if (seleccionada) colorSuave else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "Color de tarjeta"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorTarjeta
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (seleccionada) {
                colorPrincipal
            } else {
                Color(0xFFE2E8F0)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colorSuave),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorPrincipal,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column {

                Text(
                    text = asignatura.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (seleccionada) {
                            "Seleccionada"
                        } else {
                            "Ver unidades"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colorPrincipal
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = colorPrincipal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
