package com.example.pruebaaplicacionesmoviles.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaaplicacionesmoviles.MpazApp
import com.example.pruebaaplicacionesmoviles.data.model.Rol
import com.example.pruebaaplicacionesmoviles.data.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Obtenemos la aplicación para acceder a Room
    private val app = application as MpazApp
    private val repositorio = app.repositorio


    // ESTO GUARDA LO QUE ESCRIBE EL USUARIO

    private val _usuario = MutableStateFlow("") // Almacena el texto del usuario
    val usuario: StateFlow<String> = _usuario.asStateFlow() //ESTA PERMITE OBSERVAR EL TEXTO


    // ESTO GUARDA EL ROL QUE SELECCIONO EL USUARIO
    private val _rolSeleccionado = MutableStateFlow<Rol?>(null)
    val rolSeleccionado: StateFlow<Rol?> = _rolSeleccionado.asStateFlow()


    // Guardamos los errores de los campos
    private val _errorUsuario = MutableStateFlow("")
    val errorUsuario = _errorUsuario.asStateFlow()


    private val _errorRol = MutableStateFlow("")
    val errorRol = _errorRol.asStateFlow()

    // Guardamos los mensajes del Login
    private val _mensaje = MutableStateFlow("")
    val mensaje = _mensaje.asStateFlow()

    // Indica si estamos consultando la base de datos
    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()


    // ESTO GUARDA EL USUARIO CUANDO INICIA SESION
    private val _usuarioAutenticado = MutableStateFlow<Usuario?>(null)
    val usuarioAutenticado = _usuarioAutenticado.asStateFlow()




    fun actualizarUsuario(nuevoTexto: String) {
        _usuario.value = nuevoTexto // Actualiza lo escrito
        _errorUsuario.value = "" // Limpia el error anterior
        _mensaje.value = ""
    }

    fun actualizarRol(nuevoRol: Rol) {
        _rolSeleccionado.value = nuevoRol // Guarda el rol elegido
        _errorRol.value = "" // Limpia el error anterior
        _mensaje.value = ""
    }

    fun validarLogin() {

        val alias = _usuario.value.trim().uppercase() // Limpia el alias
        val rol = _rolSeleccionado.value // Obtiene el rol elegido

        // Validamos que se haya ingresado un usuario
        _errorUsuario.value = when {


            alias.isBlank() -> "Debes ingresar un usuario"
            alias.length < 4 -> "Debes ingresar mínimo 4 caracteres"
            else -> ""
        }

        // Validamos que se haya seleccionado un rol
        _errorRol.value = if (rol == null) {
            "Debes seleccionar un rol"
        } else {
            ""
        }

        // Si existe un error, detenemos el Login
        if (_errorUsuario.value.isNotEmpty() || _errorRol.value.isNotEmpty()) {
            return
        }

        if (_cargando.value) return // Evita consultas duplicadas

        viewModelScope.launch {

            _cargando.value = true // Inicia la consulta
            _mensaje.value = ""

            try {
                // Esperamos que Room termine de cargar los datos iniciales
                app.preparacion.await()

                // Buscamos el usuario en la base de datos
                val usuarioEncontrado = repositorio.buscarUsuario(alias)

                when {
                    usuarioEncontrado == null -> {
                        _mensaje.value = "El usuario no existe"
                    }

                    usuarioEncontrado.rol != rol -> {
                        _mensaje.value = "El rol no corresponde a este usuario"
                    }

                    else -> {
                        // El usuario existe y su rol coincide
                        _usuarioAutenticado.value = usuarioEncontrado
                    }
                }

            } catch (e: Exception) {
                Log.e("ERROR_ROOM", "Error al iniciar sesión", e)
                _mensaje.value = "No se pudo consultar la base de datos"
            } finally {
                _cargando.value = false // Finaliza la consulta
            }
        }
    }


    fun cerrarSesion() {
        _usuarioAutenticado.value = null // Regresa al Login
        _usuario.value = "" // Limpia el usuario
        _rolSeleccionado.value = null // Limpia el rol
        _errorUsuario.value = ""
        _errorRol.value = ""
        _mensaje.value = ""
    }
































}