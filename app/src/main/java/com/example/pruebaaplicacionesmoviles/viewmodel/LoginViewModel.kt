package com.example.pruebaaplicacionesmoviles.viewmodel


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    private val _usuario = MutableStateFlow("")
    val usuario : StateFlow<String> = _usuario.asStateFlow()
    private val _contrasena = MutableStateFlow("")
    val contrasena = _contrasena.asStateFlow()
    private val _errorUsuario = MutableStateFlow("")
    val errorUsuario = _errorUsuario.asStateFlow()

    private val _errorContrasena = MutableStateFlow("")
    val errorContrasena = _errorContrasena.asStateFlow()

    private val _mensaje = MutableStateFlow("")
    val mensaje = _mensaje.asStateFlow()

    fun actualizarContrasena(nuevoTexto: String) {
        _contrasena.value = nuevoTexto
        _errorContrasena.value = ""
        _mensaje.value = ""
    }

    fun actualizarUsuario(nuevoTexto: String){
        _usuario.value = nuevoTexto
        _errorUsuario.value = ""
        _mensaje.value = ""
    }

    fun validarLogin(){
        if (_usuario.value.isBlank()){
            _errorUsuario.value = "Debes ingresar un usuario"
        }else if(_usuario.value.trim().length < 4){
            _errorUsuario.value = "Debes ingrear minimo 4 caracteres"
        }else{
            _errorUsuario.value = ""
        }

        if (_contrasena.value.isBlank()){
            _errorContrasena.value = "Debes ingresar una contraseña"

        }else if(_contrasena.value.length <6){
            _errorContrasena.value = "Debes ingresar minimo 6 caracteres"
        }else {
            _errorContrasena.value = ""
        }

        if (_errorUsuario.value.isEmpty()&& _errorContrasena.value.isEmpty()){
            _mensaje.value = "Formulario invalidp"
        }else {
            _mensaje.value= ""
        }
    }


}