package com.example.pruebaaplicacionesmoviles.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaaplicacionesmoviles.MpazApp
import com.example.pruebaaplicacionesmoviles.data.model.Asignacion
import com.example.pruebaaplicacionesmoviles.data.model.EstadoUnidad
import com.example.pruebaaplicacionesmoviles.data.model.Intento
import com.example.pruebaaplicacionesmoviles.data.model.Rol
import com.example.pruebaaplicacionesmoviles.data.model.Unidad
import com.example.pruebaaplicacionesmoviles.data.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocenteUiState(
    val docenteId: Int = 2,
    val estudiantes: List<Usuario> = emptyList(),
    val unidadesAsignables: List<Unidad> = emptyList(),
    val intentos: List<Intento> = emptyList(),
    val estudianteIdTexto: String = "",
    val unidadIdTexto: String = "",
    val fechaInicio: String = "2026-10-01",
    val fechaFin: String = "2026-12-15",
    val maxIntentosTexto: String = "2",
    val errorFormulario: String = "",
    val mensaje: String = ""
)

class DocenteViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = (application as MpazApp).repositorio

    private val _estado = MutableStateFlow(DocenteUiState())
    val estado: StateFlow<DocenteUiState> = _estado.asStateFlow()

    init {
        observar()
    }

    fun observar(docenteId: Int = _estado.value.docenteId) {
        _estado.update { it.copy(docenteId = docenteId) }
        viewModelScope.launch {
            combine(
                repositorio.observarUnidades(),
                repositorio.observarIntentosDocente(docenteId)
            ) { unidades, intentos ->
                unidades to intentos
            }.collect { (unidades, intentos) ->
                val asignables = unidades.filter {
                    it.estado == EstadoUnidad.AUTORIZADO || it.estado == EstadoUnidad.PUBLICADO
                }
                // Estudiantes ficticios del docente (semilla: EST01 con docenteId = 2)
                val estudiantes = listOf(
                    Usuario(3, "EST01", Rol.ESTUDIANTE, "2° Básico A", docenteId)
                )
                _estado.update {
                    it.copy(
                        unidadesAsignables = asignables,
                        intentos = intentos,
                        estudiantes = estudiantes
                    )
                }
            }
        }
    }

    fun actualizarEstudianteId(valor: String) {
        _estado.update { it.copy(estudianteIdTexto = valor, errorFormulario = "", mensaje = "") }
    }

    fun actualizarUnidadId(valor: String) {
        _estado.update { it.copy(unidadIdTexto = valor, errorFormulario = "", mensaje = "") }
    }

    fun actualizarFechaInicio(valor: String) {
        _estado.update { it.copy(fechaInicio = valor, errorFormulario = "", mensaje = "") }
    }

    fun actualizarFechaFin(valor: String) {
        _estado.update { it.copy(fechaFin = valor, errorFormulario = "", mensaje = "") }
    }

    fun actualizarMaxIntentos(valor: String) {
        _estado.update { it.copy(maxIntentosTexto = valor, errorFormulario = "", mensaje = "") }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = "") }
    }

    fun crearAsignacion() {
        val e = _estado.value
        val estudianteId = e.estudianteIdTexto.toIntOrNull()
        val unidadId = e.unidadIdTexto.toIntOrNull()
        val maxIntentos = e.maxIntentosTexto.toIntOrNull()

        val error = when {
            estudianteId == null -> "Elige un ID de estudiante válido (ej: 3)"
            e.estudiantes.none { it.id == estudianteId } -> "Ese estudiante no está a tu cargo"
            unidadId == null -> "Elige un ID de unidad válido"
            e.unidadesAsignables.none { it.id == unidadId } ->
                "La unidad debe estar AUTORIZADA o PUBLICADA"
            maxIntentos == null || maxIntentos < 1 -> "maxIntentos debe ser al menos 1"
            !fechaValida(e.fechaInicio) || !fechaValida(e.fechaFin) ->
                "Usa fechas AAAA-MM-DD"
            e.fechaFin < e.fechaInicio -> "La fecha fin no puede ser anterior al inicio"
            else -> null
        }

        if (error != null) {
            _estado.update { it.copy(errorFormulario = error) }
            return
        }

        viewModelScope.launch {
            repositorio.crearAsignacion(
                Asignacion(
                    id = 0,
                    docenteId = e.docenteId,
                    estudianteId = estudianteId!!,
                    unidadId = unidadId!!,
                    fechaInicio = e.fechaInicio,
                    fechaFin = e.fechaFin,
                    maxIntentos = maxIntentos!!
                )
            )
            _estado.update {
                it.copy(
                    errorFormulario = "",
                    mensaje = "Asignación creada",
                    estudianteIdTexto = "",
                    unidadIdTexto = ""
                )
            }
        }
    }

    private fun fechaValida(valor: String): Boolean {
        return Regex("""\d{4}-\d{2}-\d{2}""").matches(valor)
    }
}