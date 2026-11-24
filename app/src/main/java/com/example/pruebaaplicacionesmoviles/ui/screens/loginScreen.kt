
package com.example.pruebaaplicacionesmoviles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pruebaaplicacionesmoviles.data.model.Rol
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazAzul
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazAzulClaro
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazFondo
import com.example.pruebaaplicacionesmoviles.ui.theme.MpazTexto
import com.example.pruebaaplicacionesmoviles.viewmodel.LoginViewModel

@Composable
fun LoginScreen(loginViewModel: LoginViewModel) {

    val usuario by loginViewModel.usuario.collectAsState()
    val clave by loginViewModel.clave.collectAsState()

    val rolSeleccionado by
        loginViewModel.rolSeleccionado.collectAsState()

    val mensaje by loginViewModel.mensaje.collectAsState()
    val errorUsuario by loginViewModel.errorUsuario.collectAsState()
    val errorClave by loginViewModel.errorClave.collectAsState()
    val errorRol by loginViewModel.errorRol.collectAsState()
    val cargando by loginViewModel.cargando.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MpazFondo)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MpazAzul
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "MPAZ PRO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Bienvenido. Ingresa con tu usuario ficticio.",
                    color = MpazAzulClaro
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = usuario,
                    onValueChange = loginViewModel::actualizarUsuario,
                    label = {
                        Text("Usuario")
                    },
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

                if (errorUsuario.isNotEmpty()) {

                    Text(
                        text = errorUsuario,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clave,
                    onValueChange = loginViewModel::actualizarClave,
                    label = {
                        Text("Contraseña")
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errorClave.isNotEmpty(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorClave.isNotEmpty()) {

                    Text(
                        text = errorClave,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    text = "Usuarios demo: UTP01, DOC01, EST01",
                    style = MaterialTheme.typography.bodySmall,
                    color = MpazTexto
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Selecciona tu rol",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MpazTexto
                )

                Rol.entries.forEach { rol ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                loginViewModel.actualizarRol(rol)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        RadioButton(
                            selected = rolSeleccionado == rol,
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

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        loginViewModel.validarLogin()
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MpazAzul
                    )
                ) {

                    Text(
                        if (cargando) {
                            "Ingresando..."
                        } else {
                            "Ingresar"
                        }
                    )
                }

                if (mensaje.isNotEmpty()) {

                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
