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
    val datosConfirmados: Boolean = false,
    val asignacionActiva: Boolean = true,
    val errorEstudiante: String = "",
    val errorUnidad: String = "",
    val errorFechaInicio: String = "",
    val errorFechaFin: String = "",
    val errorMaxIntentos: String = "",
    val errorConfirmacion: String = "",
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
        _estado.update {
            it.copy(estudianteIdTexto = valor, errorEstudiante = "", mensaje = "")
        }
    }

    fun actualizarUnidadId(valor: String) {
        _estado.update {
            it.copy(unidadIdTexto = valor, errorUnidad = "", mensaje = "")
        }
    }

    fun actualizarFechaInicio(valor: String) {
        _estado.update {
            it.copy(fechaInicio = valor, errorFechaInicio = "", mensaje = "")
        }
    }

    fun actualizarFechaFin(valor: String) {
        _estado.update {
            it.copy(fechaFin = valor, errorFechaFin = "", mensaje = "")
        }
    }

    fun actualizarMaxIntentos(valor: String) {
        _estado.update {
            it.copy(maxIntentosTexto = valor, errorMaxIntentos = "", mensaje = "")
        }
    }

    fun actualizarConfirmacion(valor: Boolean) {
        _estado.update {
            it.copy(datosConfirmados = valor, errorConfirmacion = "", mensaje = "")
        }
    }

    fun actualizarAsignacionActiva(valor: Boolean) {
        _estado.update { it.copy(asignacionActiva = valor, mensaje = "") }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = "") }
    }

    fun crearAsignacion() {
        val e = _estado.value
        val estudianteId = e.estudianteIdTexto.toIntOrNull()
        val unidadId = e.unidadIdTexto.toIntOrNull()
        val maxIntentos = e.maxIntentosTexto.toIntOrNull()

        val errorEstudiante = when {
            e.estudianteIdTexto.isBlank() -> "Ingresa el ID del estudiante"
            estudianteId == null -> "Debe ser un número (ej: 3)"
            e.estudiantes.none { it.id == estudianteId } -> "Ese estudiante no está a tu cargo"
            else -> ""
        }
        val errorUnidad = when {
            e.unidadIdTexto.isBlank() -> "Ingresa el ID de la unidad"
            unidadId == null -> "Debe ser un número (ej: 1)"
            e.unidadesAsignables.none { it.id == unidadId } ->
                "La unidad debe estar AUTORIZADA o PUBLICADA"
            else -> ""
        }
        val errorFechaInicio = when {
            e.fechaInicio.isBlank() -> "Elige la fecha de inicio"
            !fechaValida(e.fechaInicio) -> "Usa el formato AAAA-MM-DD"
            else -> ""
        }
        val errorFechaFin = when {
            e.fechaFin.isBlank() -> "Elige la fecha de fin"
            !fechaValida(e.fechaFin) -> "Usa el formato AAAA-MM-DD"
            errorFechaInicio.isEmpty() && e.fechaFin < e.fechaInicio ->
                "La fecha fin no puede ser anterior al inicio"
            else -> ""
        }
        val errorMaxIntentos = when {
            e.maxIntentosTexto.isBlank() -> "Ingresa el máximo de intentos"
            maxIntentos == null -> "Debe ser un número entero"
            maxIntentos < 1 -> "Debe ser al menos 1"
            else -> ""
        }
        val errorConfirmacion = if (!e.datosConfirmados) {
            "Debes confirmar que los datos son correctos"
        } else {
            ""
        }

        _estado.update {
            it.copy(
                errorEstudiante = errorEstudiante,
                errorUnidad = errorUnidad,
                errorFechaInicio = errorFechaInicio,
                errorFechaFin = errorFechaFin,
                errorMaxIntentos = errorMaxIntentos,
                errorConfirmacion = errorConfirmacion
            )
        }

        val hayErrores = listOf(
            errorEstudiante, errorUnidad, errorFechaInicio,
            errorFechaFin, errorMaxIntentos, errorConfirmacion
        ).any { it.isNotEmpty() }

        if (hayErrores) return

        viewModelScope.launch {
            repositorio.crearAsignacion(
                Asignacion(
                    id = 0,
                    docenteId = e.docenteId,
                    estudianteId = estudianteId!!,
                    unidadId = unidadId!!,
                    fechaInicio = e.fechaInicio,
                    fechaFin = e.fechaFin,
                    maxIntentos = maxIntentos!!,
                    activa = e.asignacionActiva
                )
            )
            _estado.update {
                it.copy(
                    mensaje = if (e.asignacionActiva) {
                        "Asignación creada y disponible"
                    } else {
                        "Asignación creada (no disponible aún)"
                    },
                    estudianteIdTexto = "",
                    unidadIdTexto = "",
                    datosConfirmados = false
                )
            }
        }
    }

    private fun fechaValida(valor: String): Boolean {
        return Regex("""\d{4}-\d{2}-\d{2}""").matches(valor)
    }
}