package com.example.pruebaaplicacionesmoviles.ui.screens



import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.data.model.Unidad
import java.util.Locale

@Composable // Indica que esta función dibuja una pantalla
fun UnidadDetalleScreen(
    unidad: Unidad, //ESTO RECIBE LA UNIDAD QUE QUEREMOS MOSTRAR
    onVolver: () -> Unit,
    onAbrirActividades: () -> Unit
){

    val context = LocalContext.current

    var motorVoz by remember {
        mutableStateOf<TextToSpeech?>(null) // ESTO GUARDA EL MOTOR DE VOZ
    }

    var vozLista by remember {mutableStateOf(false)} //INDICA SI EL MOTOR ESTA PREPARADO


    val textoLectura = unidad.textoParaAudio
        ?.takeIf { it.isNotBlank() } // Usa el audio específico si tiene texto
        ?: "${unidad.titulo}. ${unidad.explicacion}. ${unidad.ejemploGuiado}"



    DisposableEffect(context) { // Ejecuta este bloque al crear el recurso de audio

        val motor = TextToSpeech(context) { resultado -> // Crea el motor de voz de Android
            vozLista = resultado == TextToSpeech.SUCCESS // Comprueba si se inició correctamente

        }
        motorVoz = motor // Guarda el motor para utilizarlo en los botones

        onDispose { // Se ejecuta cuando abandonamos esta pantalla
            motor.stop() // Detiene cualquier audio que esté reproduciéndose
            motor.shutdown() // Libera los recursos del motor de voz
            motorVoz = null // Limpia la referencia almacenada

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Button(
            onClick = onVolver
        ){
            Text("Volver")
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = unidad.titulo,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Objetivo de aprendizaje",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = unidad.descripcionOA
        )

        Spacer(Modifier.height(20.dp))


        Text(
            text = "Explicación",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = unidad.explicacion // Explicación de la unidad
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Ejemplo guiado",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = unidad.ejemploGuiado
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onAbrirActividades, // AVISA A MAINACTIVITY QUE QUEREMOS ABRIR ACTIVIDADES
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📝 Resolver actividades") // TEXTO QUE VERA EL ESTUDIANTE
        }


        Spacer(Modifier.height(24.dp))



        Button(
            enabled = vozLista,
            onClick= {
                motorVoz?.let{
                    motor -> motor.language = Locale.forLanguageTag("es-CL")
                    motor.speak(
                        textoLectura,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "unidad_${unidad.id}"
                    )
                }
            }
        ) {
            Text("Escuchar explicacion") //TEXTO DEL BOTON
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                motorVoz?.stop()
            }
        ) {
            Text("Detener audio")
        }



    }







}

