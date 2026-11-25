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
import kotlinx.coroutines.Job
import com.example.pruebaaplicacionesmoviles.core.network.EnvioDocenteRemoto
import com.example.pruebaaplicacionesmoviles.core.network.IntentoDocenteRemoto
import com.example.pruebaaplicacionesmoviles.core.network.ApiCliente
import com.example.pruebaaplicacionesmoviles.core.network.GuardarAsignacionRequest
import com.example.pruebaaplicacionesmoviles.core.network.ObjetivoDisponibleRemoto
import com.example.pruebaaplicacionesmoviles.core.network.CrearInformeRequest
import com.example.pruebaaplicacionesmoviles.core.network.InformeRemoto
import com.example.pruebaaplicacionesmoviles.core.network.ValidarInformeRequest
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

data class DocenteUiState(
    val docenteId: Int = 2,
    val cursoId: Long = 1,
    val estudiantes: List<Usuario> = emptyList(),
    val unidadesAsignables: List<Unidad> = emptyList(),
    val objetivos: List<ObjetivoDisponibleRemoto> = emptyList(),
    val enviosServidor: List<EnvioDocenteRemoto> = emptyList(),
    val intentosServidor: List<IntentoDocenteRemoto> = emptyList(),
    val informe: InformeRemoto? = null,
    val generandoInforme: Boolean = false,
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
    private var observacion: Job? = null
    private val _estado = MutableStateFlow(DocenteUiState())
    val estado: StateFlow<DocenteUiState> = _estado.asStateFlow()

    init {
        observar()
    }

    fun observar(docenteId: Int = _estado.value.docenteId) {
        observacion?.cancel()
        _estado.update { it.copy(docenteId = docenteId) }
        observacion = viewModelScope.launch {
            val cursoId = try {
                ApiCliente.docente.cursos().items.firstOrNull()?.id ?: 1L
            } catch (_: Exception) {
                1L
            }

            val objetivos = try {
                ApiCliente.docente.objetivos(cursoId)
            } catch (e: Exception) {
                _estado.update {
                    it.copy(mensaje = "No se pudieron cargar los objetivos")
                }
                emptyList()
            }

            val estudiantesRemotos = try {
                ApiCliente.docente.estudiantes(cursoId).items.map { est ->
                    Usuario(
                        id = est.id.toInt(),
                        alias = est.codigoUsuario ?: "EST${est.id}",
                        rol = Rol.ESTUDIANTE,
                        curso = "Curso $cursoId",
                        docenteId = docenteId
                    )
                }
            } catch (_: Exception) {
                listOf(Usuario(3, "EST01", Rol.ESTUDIANTE, "2° Básico A", docenteId))
            }

            val resultados = try {
                val estudianteId = estudiantesRemotos.firstOrNull()?.id?.toLong() ?: 3L
                ApiCliente.docente.resultados(estudianteId)
            } catch (e: Exception) {
                _estado.update {
                    it.copy(mensaje = "No se pudieron cargar los resultados")
                }
                null
            }

            combine(
                repositorio.observarUnidades(),
                repositorio.observarIntentosDocente(docenteId)
            ) { unidades, intentos ->
                unidades to intentos
            }.collect { (unidades, intentos) ->
                // Solo referencia local; la asignación real usa objetivos del servidor.
                val asignables = unidades.filter {
                    it.estado == EstadoUnidad.AUTORIZADO || it.estado == EstadoUnidad.PUBLICADO
                }
                _estado.update {
                    it.copy(
                        cursoId = cursoId,
                        unidadesAsignables = asignables,
                        objetivos = objetivos,
                        enviosServidor = resultados?.envios.orEmpty(),
                        intentosServidor = resultados?.intentos.orEmpty(),
                        intentos = intentos,
                        estudiantes = estudiantesRemotos.ifEmpty {
                            listOf(Usuario(3, "EST01", Rol.ESTUDIANTE, "2° Básico A", docenteId))
                        }
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
            } catch (ex: Exception) {
                _estado.update {
                    it.copy(mensaje = "No se pudo crear la asignación")
                }
            }
        }
    }

    fun generarInforme() {
        if (_estado.value.generandoInforme) return
        _estado.update { it.copy(generandoInforme = true, mensaje = "") }
        viewModelScope.launch {
            try {
                val e = _estado.value
                val informe = ApiCliente.docente.crearInforme(
                    CrearInformeRequest(
                        cursoId = e.cursoId,
                        estudianteId = e.estudiantes.firstOrNull()?.id?.toLong() ?: 3L,
                        objetivoAprendizajeId = e.objetivos.firstOrNull()?.id ?: 31L
                    )
                )
                _estado.update {
                    it.copy(
                        generandoInforme = false,
                        informe = informe,
                        mensaje = "Informe generado"
                    )
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        generandoInforme = false,
                        mensaje = "No se pudo generar el informe"
                    )
                }
            }
        }
    }

    fun validarInforme() {
        val informe = _estado.value.informe ?: return
        if (_estado.value.generandoInforme) return
        _estado.update { it.copy(generandoInforme = true, mensaje = "") }
        viewModelScope.launch {
            try {
                val validado = ApiCliente.docente.validarInforme(
                    informe.id,
                    ValidarInformeRequest(estado = "VALIDADO")
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
                        mensaje = "No se pudo validar el informe"
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
                        mensaje = "No se pudo abrir el PDF"
                    )
                }
            }
        }
    }

    private fun fechaValida(valor: String): Boolean {
        return Regex("""\d{4}-\d{2}-\d{2}""").matches(valor)
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