
package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.viewmodel.LoginViewModel

@Composable
fun LoginScreen(loginViewModel: LoginViewModel) {

    val usuario by loginViewModel.usuario.collectAsState() // Observamos el usuario
    val contrasena by loginViewModel.contrasena.collectAsState() // Observamos la contraseña
    val mensaje by loginViewModel.mensaje.collectAsState() // Observamos el mensaje del formulario
    val errorUsuario by loginViewModel.errorUsuario.collectAsState() // Observamos errores del usuario
    val errorContrasena by loginViewModel.errorContrasena.collectAsState() // Observamos errores de contraseña

    var mostrarContrasena by remember {
        mutableStateOf(false) // Controlamos si la contraseña se muestra o se oculta
    }

    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda la pantalla
            .padding(24.dp), // Agrega espacio alrededor

        verticalArrangement = Arrangement.Center, // Centra verticalmente los elementos
        horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente
    ) {

        Text("Bienvenido a MPAZ PRO")

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = usuario, // Muestra el usuario almacenado en el ViewModel

            onValueChange = { nuevoTexto ->
                loginViewModel.actualizarUsuario(nuevoTexto) // Actualiza el usuario al escribir
            },

            label = { Text("Usuario") },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person, // Ícono de usuario
                    contentDescription = null
                )
            },

            isError = errorUsuario.isNotEmpty(), // Marca el campo si existe un error
            singleLine = true // Permite una sola línea
        )

        if (errorUsuario.isNotEmpty()) { // Comprueba si hay error de usuario
            Text(
                text = errorUsuario,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena, // Muestra el valor de la contraseña

            onValueChange = { nuevoTexto ->
                loginViewModel.actualizarContrasena(nuevoTexto) // Actualiza la contraseña
            },

            label = { Text("Contraseña") },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock, // Ícono de candado
                    contentDescription = null
                )
            },

            trailingIcon = {
                IconButton(
                    onClick = {
                        mostrarContrasena = !mostrarContrasena // Alterna entre mostrar y ocultar
                    }
                ) {

                    Icon(
                        imageVector = if (mostrarContrasena)
                            Icons.Filled.VisibilityOff
                        else
                            Icons.Filled.Visibility,

                        contentDescription = if (mostrarContrasena)
                            "Ocultar contraseña"
                        else
                            "Mostrar contraseña"
                    )
                }
            },

            visualTransformation = if (mostrarContrasena)
                VisualTransformation.None // Muestra el texto normalmente
            else
                PasswordVisualTransformation(), // Oculta los caracteres

            isError = errorContrasena.isNotEmpty(), // Marca el campo si existe un error
            singleLine = true
        )

        if (errorContrasena.isNotEmpty()) { // Comprueba si hay error de contraseña
            Text(
                text = errorContrasena,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                loginViewModel.validarLogin() // Ejecuta las validaciones del ViewModel
            }
        ) {
            Text("Ingresar")
        }

        if (mensaje.isNotEmpty()) { // Muestra el mensaje de validación si existe
            Text(mensaje)
        }
    }
}
