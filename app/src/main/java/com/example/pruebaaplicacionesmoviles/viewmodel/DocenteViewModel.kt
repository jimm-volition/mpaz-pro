package com.example.pruebaaplicacionesmoviles.viewmodel

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaaplicacionesmoviles.core.network.ActividadRemota
import com.example.pruebaaplicacionesmoviles.core.network.ApiCliente
import com.example.pruebaaplicacionesmoviles.core.network.AsignacionRemota
import com.example.pruebaaplicacionesmoviles.core.network.CrearInformeRequest
import com.example.pruebaaplicacionesmoviles.core.network.EnvioDocenteRemoto
import com.example.pruebaaplicacionesmoviles.core.network.GuardarAsignacionRequest
import com.example.pruebaaplicacionesmoviles.core.network.InformeRemoto
import com.example.pruebaaplicacionesmoviles.core.network.IntentoDocenteRemoto
import com.example.pruebaaplicacionesmoviles.core.network.ObjetivoDisponibleRemoto
import com.example.pruebaaplicacionesmoviles.core.network.ProgresoEstudianteRemoto
import com.example.pruebaaplicacionesmoviles.core.network.ValidarInformeRequest
import com.example.pruebaaplicacionesmoviles.data.model.Rol
import com.example.pruebaaplicacionesmoviles.data.model.Usuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class DocenteUiState(
    val docenteId: Int = 2,
    val cursoId: Long = 1,
    val nombreCurso: String = "",
    val cargando: Boolean = false,
    val estudiantes: List<Usuario> = emptyList(),
    val objetivos: List<ObjetivoDisponibleRemoto> = emptyList(),
    val asignaciones: List<AsignacionRemota> = emptyList(),

    // Estudiante y OA con los que se está trabajando (resultados, seguimiento, informe).
    val estudianteSeleccionado: Long? = null,
    val objetivoSeleccionado: Long? = null,

    val enviosServidor: List<EnvioDocenteRemoto> = emptyList(),
    val intentosServidor: List<IntentoDocenteRemoto> = emptyList(),
    val seguimiento: List<ProgresoEstudianteRemoto> = emptyList(),
    val actividadesObjetivo: List<ActividadRemota> = emptyList(),
    val cargandoDetalle: Boolean = false,

    val informe: InformeRemoto? = null,
    val observacionInforme: String = "",
    val generandoInforme: Boolean = false,

    // Formulario de asignación
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
) {
    fun aliasEstudiante(id: Long?): String =
        estudiantes.firstOrNull { it.id.toLong() == id }?.alias ?: id?.let { "Estudiante $it" } ?: "Todo el curso"

    fun codigoObjetivo(id: Long?): String =
        objetivos.firstOrNull { it.id == id }?.codigo ?: id?.let { "OA $it" } ?: "-"
}

class DocenteViewModel(application: Application) : AndroidViewModel(application) {

    private var carga: Job? = null
    private var detalleEstudiante: Job? = null
    private var detalleObjetivo: Job? = null
    private val _estado = MutableStateFlow(DocenteUiState())
    val estado: StateFlow<DocenteUiState> = _estado.asStateFlow()

    fun observar(docenteId: Int = _estado.value.docenteId) {
        carga?.cancel()
        _estado.update { it.copy(docenteId = docenteId, cargando = true) }
        carga = viewModelScope.launch {
            val curso = try {
                ApiCliente.docente.cursos().items.firstOrNull()
            } catch (_: Exception) {
                null
            }
            val cursoId = curso?.id ?: 1L

            val objetivos = try {
                ApiCliente.docente.objetivos(cursoId)
            } catch (e: Exception) {
                _estado.update {
                    it.copy(mensaje = mensajeDeError(e, "No se pudieron cargar los objetivos"))
                }
                emptyList()
            }

            val estudiantes = try {
                ApiCliente.docente.estudiantes(cursoId).items.map { est ->
                    Usuario(
                        id = est.id.toInt(),
                        alias = est.codigoUsuario ?: "EST${est.id}",
                        rol = Rol.ESTUDIANTE,
                        curso = curso?.nombre ?: "Curso $cursoId",
                        docenteId = docenteId
                    )
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(mensaje = mensajeDeError(e, "No se pudieron cargar los estudiantes"))
                }
                emptyList()
            }

            val anterior = _estado.value
            val estudianteElegido = anterior.estudianteSeleccionado
                ?.takeIf { id -> estudiantes.any { it.id.toLong() == id } }
                ?: estudiantes.firstOrNull()?.id?.toLong()
            val objetivoElegido = anterior.objetivoSeleccionado
                ?.takeIf { id -> objetivos.any { it.id == id } }
                ?: objetivos.firstOrNull()?.id

            _estado.update {
                it.copy(
                    cargando = false,
                    cursoId = cursoId,
                    nombreCurso = curso?.nombre.orEmpty(),
                    objetivos = objetivos,
                    estudiantes = estudiantes,
                    estudianteSeleccionado = estudianteElegido,
                    objetivoSeleccionado = objetivoElegido
                )
            }

            cargarAsignaciones()
            estudianteElegido?.let { cargarResultados(it) }
            objetivoElegido?.let { cargarDetalleObjetivo(it) }
        }
    }

    // ---------- Selección ----------

    fun seleccionarEstudiante(id: Long) {
        _estado.update {
            it.copy(
                estudianteSeleccionado = id,
                estudianteIdTexto = id.toString(),
                errorEstudiante = "",
                informe = null,
                mensaje = ""
            )
        }
        cargarResultados(id)
    }

    fun seleccionarObjetivo(id: Long) {
        _estado.update {
            it.copy(
                objetivoSeleccionado = id,
                unidadIdTexto = id.toString(),
                errorUnidad = "",
                informe = null,
                mensaje = ""
            )
        }
        cargarDetalleObjetivo(id)
    }

    private fun cargarResultados(estudianteId: Long) {
        detalleEstudiante?.cancel()
        detalleEstudiante = viewModelScope.launch {
            try {
                val resultados = ApiCliente.docente.resultados(estudianteId)
                _estado.update {
                    it.copy(
                        enviosServidor = resultados.envios.orEmpty(),
                        intentosServidor = resultados.intentos.orEmpty()
                    )
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        enviosServidor = emptyList(),
                        intentosServidor = emptyList(),
                        mensaje = mensajeDeError(e, "No se pudieron cargar los resultados")
                    )
                }
            }
        }
    }

    /** Avance del curso en el OA + vista previa (sin solucionario) de sus prácticas publicadas. */
    private fun cargarDetalleObjetivo(objetivoId: Long) {
        detalleObjetivo?.cancel()
        _estado.update { it.copy(cargandoDetalle = true) }
        detalleObjetivo = viewModelScope.launch {
            val cursoId = _estado.value.cursoId
            val seguimiento = try {
                ApiCliente.docente.progreso(cursoId, objetivoId).estudiantes.orEmpty()
            } catch (_: Exception) {
                emptyList()
            }
            // El listado no trae preguntas: pedimos el detalle (sin clave de corrección).
            val actividades = try {
                ApiCliente.actividades.listar(objetivoId).map { resumen ->
                    try {
                        ApiCliente.actividades.obtener(resumen.id)
                    } catch (_: Exception) {
                        resumen
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
            _estado.update {
                it.copy(
                    cargandoDetalle = false,
                    seguimiento = seguimiento,
                    actividadesObjetivo = actividades
                )
            }
        }
    }

    private suspend fun cargarAsignaciones() {
        try {
            val lista = ApiCliente.docente.asignaciones(_estado.value.cursoId, tamano = 100).items
            _estado.update { it.copy(asignaciones = lista.sortedByDescending { a -> a.id }) }
        } catch (e: Exception) {
            _estado.update {
                it.copy(mensaje = mensajeDeError(e, "No se pudieron cargar las asignaciones"))
            }
        }
    }

    // ---------- Formulario de asignación ----------

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

    fun actualizarObservacion(valor: String) {
        _estado.update { it.copy(observacionInforme = valor.take(2000)) }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = "") }
    }

    fun crearAsignacion() {
        val e = _estado.value
        val errores = erroresDeAsignacion(
            estudianteTexto = e.estudianteIdTexto,
            objetivoTexto = e.unidadIdTexto,
            fechaInicio = e.fechaInicio,
            fechaFin = e.fechaFin,
            maxIntentosTexto = e.maxIntentosTexto,
            confirmado = e.datosConfirmados,
            estudiantesIds = e.estudiantes.map { it.id }.toSet(),
            objetivosIds = e.objetivos.map { it.id }.toSet()
        )

        _estado.update {
            it.copy(
                errorEstudiante = errores.errorEstudiante,
                errorUnidad = errores.errorUnidad,
                errorFechaInicio = errores.errorFechaInicio,
                errorFechaFin = errores.errorFechaFin,
                errorMaxIntentos = errores.errorMaxIntentos,
                errorConfirmacion = errores.errorConfirmacion
            )
        }

        if (errores.hayErrores()) return

        val estudianteId = e.estudianteIdTexto.toLong()
        val objetivoId = e.unidadIdTexto.toLong()
        val maxIntentos = e.maxIntentosTexto.toInt()

        viewModelScope.launch {
            try {
                ApiCliente.docente.crear(
                    GuardarAsignacionRequest(
                        cursoId = e.cursoId,
                        estudianteId = estudianteId,
                        objetivoAprendizajeId = objetivoId,
                        disponibleDesde = "${e.fechaInicio}T00:00:00Z",
                        disponibleHasta = "${e.fechaFin}T23:59:59Z",
                        limiteIntentosPrueba = maxIntentos,
                        activa = e.asignacionActiva
                    )
                )
                _estado.update {
                    it.copy(
                        mensaje = "Asignación creada en el servidor",
                        estudianteIdTexto = "",
                        unidadIdTexto = "",
                        datosConfirmados = false
                    )
                }
                cargarAsignaciones()
            } catch (ex: Exception) {
                _estado.update {
                    it.copy(mensaje = mensajeDeError(ex, "No se pudo crear la asignación"))
                }
            }
        }
    }

    /** Activa o pausa una asignación existente (PUT con los mismos datos). */
    fun alternarAsignacion(asignacion: AsignacionRemota) {
        viewModelScope.launch {
            try {
                ApiCliente.docente.actualizar(
                    asignacion.id,
                    GuardarAsignacionRequest(
                        cursoId = asignacion.cursoId,
                        estudianteId = asignacion.estudianteId,
                        objetivoAprendizajeId = asignacion.objetivoAprendizajeId,
                        disponibleDesde = asignacion.disponibleDesde,
                        disponibleHasta = asignacion.disponibleHasta,
                        limiteIntentosPrueba = asignacion.limiteIntentosPrueba,
                        activa = !asignacion.activa
                    )
                )
                _estado.update {
                    it.copy(mensaje = if (asignacion.activa) "Asignación pausada" else "Asignación activada")
                }
                cargarAsignaciones()
            } catch (e: Exception) {
                _estado.update {
                    it.copy(mensaje = mensajeDeError(e, "No se pudo actualizar la asignación"))
                }
            }
        }
    }

    // ---------- Informe ----------

    fun generarInforme() {
        val e = _estado.value
        if (e.generandoInforme) return
        val estudianteId = e.estudianteSeleccionado
        val objetivoId = e.objetivoSeleccionado
        if (estudianteId == null || objetivoId == null) {
            _estado.update { it.copy(mensaje = "Elige un estudiante y un objetivo para el informe") }
            return
        }
        _estado.update { it.copy(generandoInforme = true, mensaje = "") }
        viewModelScope.launch {
            try {
                val informe = ApiCliente.docente.crearInforme(
                    CrearInformeRequest(
                        cursoId = e.cursoId,
                        estudianteId = estudianteId,
                        objetivoAprendizajeId = objetivoId
                    )
                )
                _estado.update {
                    it.copy(
                        generandoInforme = false,
                        informe = informe,
                        observacionInforme = "",
                        mensaje = "Informe generado"
                    )
                }
            } catch (ex: Exception) {
                _estado.update {
                    it.copy(
                        generandoInforme = false,
                        mensaje = mensajeDeError(ex, "No se pudo generar el informe")
                    )
                }
            }
        }
    }

    fun validarInforme() {
        val informe = _estado.value.informe ?: return
        if (_estado.value.generandoInforme) return
        val observacion = _estado.value.observacionInforme.trim().ifEmpty { null }
        _estado.update { it.copy(generandoInforme = true, mensaje = "") }
        viewModelScope.launch {
            try {
                val validado = ApiCliente.docente.validarInforme(
                    informe.id,
                    ValidarInformeRequest(estado = "VALIDADO", observacion = observacion)
                )
                _estado.update {
                    it.copy(
                        generandoInforme = false,
                        informe = validado,
                        mensaje = "Informe validado"
                    )
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        generandoInforme = false,
                        mensaje = mensajeDeError(e, "No se pudo validar el informe")
                    )
                }
            }
        }
    }

    fun descargarPdf() {
        val informe = _estado.value.informe ?: return
        if (_estado.value.generandoInforme) return
        _estado.update { it.copy(generandoInforme = true, mensaje = "") }
        viewModelScope.launch {
            try {
                val cuerpo = ApiCliente.docente.descargarInforme(informe.id)
                val carpeta = File(getApplication<Application>().cacheDir, "informes")
                carpeta.mkdirs()
                val archivo = File(carpeta, "informe-${informe.id}.pdf")
                cuerpo.byteStream().use { entrada ->
                    archivo.outputStream().use { salida ->
                        entrada.copyTo(salida)
                    }
                }
                val uri = FileProvider.getUriForFile(
                    getApplication(),
                    "com.example.pruebaaplicacionesmoviles.archivo",
                    archivo
                )
                val abrir = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    getApplication<Application>().startActivity(abrir)
                    _estado.update {
                        it.copy(generandoInforme = false, mensaje = "PDF listo")
                    }
                } catch (e: ActivityNotFoundException) {
                    _estado.update {
                        it.copy(
                            generandoInforme = false,
                            mensaje = "PDF guardado, pero no hay una app para abrirlo"
                        )
                    }
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        generandoInforme = false,
                        mensaje = mensajeDeError(e, "No se pudo abrir el PDF")
                    )
                }
            }
        }
    }
}

data class ErroresAsignacion(
    val errorEstudiante: String = "",
    val errorUnidad: String = "",
    val errorFechaInicio: String = "",
    val errorFechaFin: String = "",
    val errorMaxIntentos: String = "",
    val errorConfirmacion: String = ""
) {
    fun hayErrores(): Boolean {
        return listOf(
            errorEstudiante, errorUnidad, errorFechaInicio,
            errorFechaFin, errorMaxIntentos, errorConfirmacion
        ).any { it.isNotEmpty() }
    }
}

fun erroresDeAsignacion(
    estudianteTexto: String,
    objetivoTexto: String,
    fechaInicio: String,
    fechaFin: String,
    maxIntentosTexto: String,
    confirmado: Boolean,
    estudiantesIds: Set<Int>,
    objetivosIds: Set<Long>
): ErroresAsignacion {
    val estudianteId = estudianteTexto.toIntOrNull()
    val objetivoId = objetivoTexto.toLongOrNull()
    val maxIntentos = maxIntentosTexto.toIntOrNull()
    val inicioValido = Regex("""\d{4}-\d{2}-\d{2}""").matches(fechaInicio)
    val finValido = Regex("""\d{4}-\d{2}-\d{2}""").matches(fechaFin)

    val errorEstudiante = when {
        estudianteTexto.isBlank() -> "Ingresa el ID del estudiante"
        estudianteId == null -> "Debe ser un número (ej: 3)"
        estudianteId !in estudiantesIds -> "Ese estudiante no está a tu cargo"
        else -> ""
    }
    val errorUnidad = when {
        objetivoTexto.isBlank() -> "Ingresa el ID del objetivo"
        objetivoId == null -> "Debe ser un número (ej: 31)"
        objetivosIds.isEmpty() -> "No hay objetivos disponibles"
        objetivoId !in objetivosIds -> "Ese objetivo no está disponible"
        else -> ""
    }
    val errorFechaInicio = when {
        fechaInicio.isBlank() -> "Elige la fecha de inicio"
        !inicioValido -> "Usa el formato AAAA-MM-DD"
        else -> ""
    }
    val errorFechaFin = when {
        fechaFin.isBlank() -> "Elige la fecha de fin"
        !finValido -> "Usa el formato AAAA-MM-DD"
        errorFechaInicio.isEmpty() && fechaFin < fechaInicio ->
            "La fecha fin no puede ser anterior al inicio"
        else -> ""
    }
    val errorMaxIntentos = when {
        maxIntentosTexto.isBlank() -> "Ingresa el máximo de intentos"
        maxIntentos == null -> "Debe ser un número entero"
        maxIntentos < 1 -> "Debe ser al menos 1"
        else -> ""
    }
    val errorConfirmacion = if (!confirmado) {
        "Debes confirmar que los datos son correctos"
    } else {
        ""
    }

    return ErroresAsignacion(
        errorEstudiante = errorEstudiante,
        errorUnidad = errorUnidad,
        errorFechaInicio = errorFechaInicio,
        errorFechaFin = errorFechaFin,
        errorMaxIntentos = errorMaxIntentos,
        errorConfirmacion = errorConfirmacion
    )
}