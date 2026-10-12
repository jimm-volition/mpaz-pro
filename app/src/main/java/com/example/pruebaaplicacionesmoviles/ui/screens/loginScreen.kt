package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.viewmodel.LoginViewModel

@Composable
fun LoginScreen(loginViewModel: LoginViewModel){
    val usuario by loginViewModel.usuario.collectAsState()
    val contrasena by loginViewModel.contrasena.collectAsState()
    val mensaje by loginViewModel.mensaje.collectAsState()
    val errorUsuario by loginViewModel.errorUsuario.collectAsState()
    val errorContraseña by loginViewModel.errorContrasena.collectAsState()
    var mostrarContrasena by remember {mutableStateOf(false) }


    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Text("Bienvenido a MPAZ PRO")
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = {nuevoTexto -> loginViewModel.actualizarUsuario(nuevoTexto)},
            label = {Text("Usuario")},
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null
                )
            },
            isError = errorUsuario.isNotEmpty(),
            singleLine = true
        )

        if (errorUsuario.isNotEmpty()){
            Text(text = errorUsuario,color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = {nuevoTexto -> loginViewModel.actualizarContrasena(nuevoTexto) },
            label = {Text("Contraseña")},
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        mostrarContrasena = !mostrarContrasena
                    }
                ){
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
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            isError = errorContraseña.isNotEmpty(),
            singleLine = true
        )

        if (errorContraseña.isNotEmpty()) {
            Text(
                text = errorContraseña,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {loginViewModel.validarLogin()}
        ) {
            Text("Ingresar")}

        if (mensaje.isNotEmpty()){
            Text(mensaje)
        }

    }

}