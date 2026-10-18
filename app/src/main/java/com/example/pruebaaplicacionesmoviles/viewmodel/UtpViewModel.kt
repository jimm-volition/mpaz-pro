package com.example.pruebaaplicacionesmoviles.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaaplicacionesmoviles.MpazApp
import com.example.pruebaaplicacionesmoviles.data.model.EstadoUnidad
import com.example.pruebaaplicacionesmoviles.data.model.Unidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UtpUiState(
    val unidades: List<Unidad> = emptyList(),
    val mensaje: String = ""
)

class UtpViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = (application as MpazApp).repositorio

    private val _estado = MutableStateFlow(UtpUiState())
    val estado: StateFlow<UtpUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            repositorio.observarUnidades().collect { lista ->
                _estado.update { it.copy(unidades = lista) }
            }
        }
    }

    fun autorizar(unidadId: Int) {
        cambiarEstado(unidadId, EstadoUnidad.AUTORIZADO, "Unidad autorizada")
    }

    fun publicar(unidadId: Int) {
        cambiarEstado(unidadId, EstadoUnidad.PUBLICADO, "Unidad publicada")
    }

    fun deshabilitar(unidadId: Int) {
        cambiarEstado(unidadId, EstadoUnidad.DESHABILITADO, "Unidad deshabilitada")
    }

    fun actualizarImagen(unidadId: Int, uriTexto: String) {
        viewModelScope.launch {
            repositorio.actualizarImagen(unidadId, uriTexto)
            _estado.update { it.copy(mensaje = "Imagen actualizada") }
        }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = "") }
    }

    private fun cambiarEstado(unidadId: Int, estado: EstadoUnidad, mensaje: String) {
        viewModelScope.launch {
            repositorio.cambiarEstadoUnidad(unidadId, estado)
            _estado.update { it.copy(mensaje = mensaje) }
        }
    }
}