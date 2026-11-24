
package com.example.pruebaaplicacionesmoviles.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaaplicacionesmoviles.MpazApp
import com.example.pruebaaplicacionesmoviles.core.network.ApiCliente
import com.example.pruebaaplicacionesmoviles.core.network.LoginRequest
import com.example.pruebaaplicacionesmoviles.core.network.SesionRemota
import com.example.pruebaaplicacionesmoviles.data.model.Rol
import com.example.pruebaaplicacionesmoviles.data.model.Usuario
import com.google.gson.JsonObject
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MpazApp
    private val repositorio = app.repositorio

    private val _usuario = MutableStateFlow("")
    val usuario = _usuario.asStateFlow()

    private val _clave = MutableStateFlow("")
    val clave = _clave.asStateFlow()

    private val _rolSeleccionado = MutableStateFlow<Rol?>(null)
    val rolSeleccionado = _rolSeleccionado.asStateFlow()

    private val _errorUsuario = MutableStateFlow("")
    val errorUsuario = _errorUsuario.asStateFlow()

    private val _errorClave = MutableStateFlow("")
    val errorClave = _errorClave.asStateFlow()

    private val _errorRol = MutableStateFlow("")
    val errorRol = _errorRol.asStateFlow()

    private val _mensaje = MutableStateFlow("")
    val mensaje = _mensaje.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    private val _usuarioAutenticado = MutableStateFlow<Usuario?>(null)
    val usuarioAutenticado = _usuarioAutenticado.asStateFlow()

    fun actualizarUsuario(valor: String) {
        _usuario.value = valor
        _errorUsuario.value = ""
        _mensaje.value = ""
    }

    fun actualizarClave(valor: String) {
        _clave.value = valor
        _errorClave.value = ""
        _mensaje.value = ""
    }

    fun actualizarRol(valor: Rol) {
        _rolSeleccionado.value = valor
        _errorRol.value = ""
        _mensaje.value = ""
    }

    fun validarLogin() {
        if (_cargando.value) return

        val codigo = _usuario.value.trim().uppercase()
        val contrasena = _clave.value
        val rolElegido = _rolSeleccionado.value

        _errorUsuario.value =
            if (codigo.isBlank()) "Ingresa tu usuario" else ""

        _errorClave.value =
            if (contrasena.isBlank()) "Ingresa tu contraseña" else ""

        _errorRol.value =
            if (rolElegido == null) "Selecciona un rol" else ""

        if (_errorUsuario.value.isNotEmpty() ||
            _errorClave.value.isNotEmpty() ||
            _errorRol.value.isNotEmpty()
        ) return

        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""
            SesionRemota.cerrar()

            try {
                val respuesta = ApiCliente.servicio.login(
                    LoginRequest(codigo, contrasena)
                )

                val token = respuesta.token
                    ?.takeIf { it.isNotBlank() }
                    ?: error("El servidor no entregó un token")

                SesionRemota.guardarToken(token)

                val perfil = respuesta.usuario
                    ?: ApiCliente.servicio.miPerfil()

                val datos = if (
                    perfil.has("usuario") &&
                    perfil.get("usuario").isJsonObject
                ) {
                    perfil.getAsJsonObject("usuario")
                } else {
                    perfil
                }

                val alias = datos.campo("codigoUsuario")
                    ?: datos.campo("alias")
                    ?: codigo

                val rolBackend = Rol.valueOf(
                    datos.campo("rol")
                        ?: error("El servidor no entregó el rol")
                )

                val id = datos.campo("id")?.toIntOrNull()
                    ?: datos.campo("usuarioId")?.toIntOrNull()
                    ?: error("El servidor no entregó el ID")

                if (rolBackend != rolElegido ||
                    alias.uppercase() != codigo
                ) {
                    error("El usuario no corresponde al rol seleccionado")
                }

                app.preparacion.await()

                val fichaLocal = repositorio.buscarUsuario(alias)
                    ?.takeIf { it.id == id }

                _usuarioAutenticado.value = Usuario(
                    id = id,
                    alias = alias,
                    rol = rolBackend,
                    curso = fichaLocal?.curso,
                    docenteId = fichaLocal?.docenteId
                )

            } catch (e: HttpException) {
                _mensaje.value = when (e.code()) {
                    400, 401, 403 -> "Usuario o contraseña incorrectos"
                    else -> "Error del servidor: ${e.code()}"
                }
            } catch (e: IOException) {
                _mensaje.value = "Error de conexión: ${e.localizedMessage}"
            } catch (e: Exception) {
                _mensaje.value = e.message ?: "Error al iniciar sesión"
            } finally {
                if (_usuarioAutenticado.value == null) {
                    SesionRemota.cerrar()
                }
                _cargando.value = false
            }
        }
    }

    fun cerrarSesion() {
        SesionRemota.cerrar()
        _usuarioAutenticado.value = null
        _usuario.value = ""
        _clave.value = ""
        _rolSeleccionado.value = null
        _errorUsuario.value = ""
        _errorClave.value = ""
        _errorRol.value = ""
        _mensaje.value = ""
    }
}

private fun JsonObject.campo(nombre: String): String? =
    get(nombre)?.takeUnless { it.isJsonNull }?.asString
