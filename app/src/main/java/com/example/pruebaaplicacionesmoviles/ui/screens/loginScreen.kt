package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.viewmodel.LoginViewModel

@Composable
fun LoginScreen(loginViewModel: LoginViewModel) {
    val usuario by loginViewModel.usuario.collectAsState()
    val contrasena by loginViewModel.contrasena.collectAsState()
    val mensaje by loginViewModel.mensaje.collectAsState()
    val errorUsuario by loginViewModel.errorUsuario.collectAsState()
    val errorContraseña by loginViewModel.errorContrasena.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido a MPAZ PRO")
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { nuevoTexto -> loginViewModel.actualizarUsuario(nuevoTexto) },
            label = { Text("Usuario") },
            isError = errorUsuario.isNotEmpty(),
            singleLine = true
        )

        if (errorUsuario.isNotEmpty()) {
            Text(text = errorUsuario, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { nuevoTexto -> loginViewModel.actualizarContrasena(nuevoTexto) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
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
            onClick = { loginViewModel.validarLogin() }
        ) {
            Text("Ingresar")
        }

        if (mensaje.isNotEmpty()) {
            Text(mensaje)
        }
    }
}
