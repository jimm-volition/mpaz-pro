
package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.data.model.Rol
import com.example.pruebaaplicacionesmoviles.viewmodel.LoginViewModel

@Composable
fun LoginScreen(loginViewModel: LoginViewModel) {

    val usuario by loginViewModel.usuario.collectAsState()
    val rolSeleccionado by loginViewModel.rolSeleccionado.collectAsState()
    val mensaje by loginViewModel.mensaje.collectAsState()
    val errorUsuario by loginViewModel.errorUsuario.collectAsState()
    val errorRol by loginViewModel.errorRol.collectAsState()
    val cargando by loginViewModel.cargando.collectAsState()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Bienvenido a MPAZ PRO",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = usuario, //ESTO MUESTRA EL USUARIO ALMACENADO

            onValueChange = { nuevoTexto ->
                loginViewModel.actualizarUsuario(nuevoTexto) // ESTO ACTUALIZA EL USUARIO AL ESCRIBIR
            },

            label = { Text("Usuario") },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null
                )
            },

            isError = errorUsuario.isNotEmpty(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorUsuario.isNotEmpty()) { // Comprueba si hay error de usuario
            Text(
                text = errorUsuario,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selecciona tu rol",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Recorremos los tres roles disponibles
        Rol.entries.forEach { rol ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        loginViewModel.actualizarRol(rol) // Selecciona el rol
                    }
                    .padding(vertical = 4.dp),

                verticalAlignment = Alignment.CenterVertically
            ) {

                RadioButton(
                    selected = rolSeleccionado == rol, // Marca el rol seleccionado
                    onClick = {
                        loginViewModel.actualizarRol(rol)
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = when (rol) {
                        Rol.ESTUDIANTE -> "Estudiante"
                        Rol.DOCENTE -> "Docente"
                        Rol.UTP -> "UTP"
                    }
                )
            }
        }

        if (errorRol.isNotEmpty()) {
            Text(
                text = errorRol,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                loginViewModel.validarLogin() // Busca al usuario en Room
            },

            enabled = !cargando, // Desactiva el botón mientras consulta
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cargando) "Ingresando..." else "Ingresar")
        }

        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }


    }
}
