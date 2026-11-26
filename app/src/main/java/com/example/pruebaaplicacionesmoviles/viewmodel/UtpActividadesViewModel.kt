package com.example.pruebaaplicacionesmoviles.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaaplicacionesmoviles.core.network.ApiCliente
import com.example.pruebaaplicacionesmoviles.core.network.TransicionEstadoRequest
import com.example.pruebaaplicacionesmoviles.core.network.UtpActividadApi
import com.example.pruebaaplicacionesmoviles.core.network.UtpActividadRemota
import com.example.pruebaaplicacionesmoviles.core.network.UtpAlternativaRemota
import com.example.pruebaaplicacionesmoviles.core.network.UtpPreguntaRemota
import com.example.pruebaaplicacionesmoviles.core.network.UtpPruebaFinalRemota
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/** A qué pertenece la lista de preguntas abierta: una actividad o una prueba final. */
sealed class ContenedorPreguntas {
    abstract val id: Long
    abstract val titulo: String
    abstract val estado: String

    data class DeActividad(
        override val id: Long,
        override val titulo: String,
        override val estado: String
    ) : ContenedorPreguntas()

    data class DePrueba(
        override val id: Long,
        override val titulo: String,
        override val estado: String
    ) : ContenedorPreguntas()

    val editable: Boolean get() = estado == "BORRADOR"
}

data class UtpActividadesUiState(
    val objetivoId: Long? = null,
    val codigoObjetivo: String = "",
    val actividades: List<UtpActividadRemota> = emptyList(),
    val pruebas: List<UtpPruebaFinalRemota> = emptyList(),
    val contenedor: ContenedorPreguntas? = null,
    val preguntas: List<UtpPreguntaRemota> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null,
    /** Se incrementa cada vez que el servidor confirma un guardado: la UI cierra el formulario. */
    val guardadosOk: Int = 0
) {
    val ocupado: Boolean get() = cargando || guardando
}

class UtpActividadesViewModel(
    private val api: UtpActividadApi = ApiCliente.utpActividades
) : ViewModel() {

    private val _estado = MutableStateFlow(UtpActividadesUiState())
    val estado: StateFlow<UtpActividadesUiState> = _estado.asStateFlow()

    fun abrir(objetivoId: Long, codigo: String) {
        _estado.value = UtpActividadesUiState(objetivoId = objetivoId, codigoObjetivo = codigo)
        recargar()
    }

    fun recargar() {
        val objetivoId = _estado.value.objetivoId ?: return
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val actividades = api.listarActividades(objetivoId).items.sortedBy { it.orden }
                val pruebas = api.listarPruebas(objetivoId).items
                _estado.update {
                    it.copy(cargando = false, actividades = actividades, pruebas = pruebas)
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(cargando = false, error = mensajeDeError(e, "No se pudieron cargar las actividades"))
                }
            }
        }
    }

    fun limpiarAvisos() {
        _estado.update { it.copy(mensaje = null, error = null) }
    }

    // ---------- Actividades ----------

    fun guardarActividad(id: Long?, titulo: String, ordenTexto: String) {
        val objetivoId = _estado.value.objetivoId ?: return
        val error = errorActividad(titulo, ordenTexto)
        if (error != null) {
            _estado.update { it.copy(error = error, mensaje = null) }
            return
        }
        val cuerpo = UtpActividadRemota(
            objetivoAprendizajeId = objetivoId,
            tipo = "SELECCION",
            titulo = titulo.trim(),
            orden = ordenTexto.trim().toInt()
        )
        ejecutarGuardado(
            exito = if (id == null) "Actividad creada en BORRADOR" else "Actividad actualizada",
            falla = "No se pudo guardar la actividad"
        ) {
            if (id == null) api.crearActividad(cuerpo) else api.editarActividad(id, cuerpo)
            recargarListas()
        }
    }

    fun transicionarActividad(id: Long, estadoNuevo: String) {
        ejecutarGuardado(
            exito = "Actividad en ${estadoNuevo.replace('_', ' ')}",
            falla = "No se pudo cambiar el estado de la actividad",
            cerrarFormulario = false
        ) {
            api.transicionarActividad(id, TransicionEstadoRequest(estadoNuevo))
            recargarListas()
        }
    }

    // ---------- Pruebas finales ----------

    fun guardarPrueba(
        id: Long?,
        titulo: String,
        desde: String,
        hasta: String,
        limiteTexto: String
    ) {
        val objetivoId = _estado.value.objetivoId ?: return
        val error = errorPrueba(titulo, desde, hasta, limiteTexto)
        if (error != null) {
            _estado.update { it.copy(error = error, mensaje = null) }
            return
        }
        val cuerpo = UtpPruebaFinalRemota(
            objetivoAprendizajeId = objetivoId,
            titulo = titulo.trim(),
            disponibleDesde = desde.trim().takeIf { it.isNotEmpty() }?.let { "${it}T00:00:00Z" },
            disponibleHasta = hasta.trim().takeIf { it.isNotEmpty() }?.let { "${it}T23:59:59Z" },
            limiteIntentos = limiteTexto.trim().takeIf { it.isNotEmpty() }?.toInt()
        )
        ejecutarGuardado(
            exito = if (id == null) "Prueba final creada en BORRADOR" else "Prueba final actualizada",
            falla = "No se pudo guardar la prueba final"
        ) {
            if (id == null) api.crearPrueba(cuerpo) else api.editarPrueba(id, cuerpo)
            recargarListas()
        }
    }

    fun transicionarPrueba(id: Long, estadoNuevo: String) {
        ejecutarGuardado(
            exito = "Prueba final en ${estadoNuevo.replace('_', ' ')}",
            falla = "No se pudo cambiar el estado de la prueba",
            cerrarFormulario = false
        ) {
            api.transicionarPrueba(id, TransicionEstadoRequest(estadoNuevo))
            recargarListas()
        }
    }

    // ---------- Preguntas ----------

    fun abrirPreguntas(contenedor: ContenedorPreguntas) {
        _estado.update {
            it.copy(contenedor = contenedor, preguntas = emptyList(), mensaje = null, error = null)
        }
        cargarPreguntas()
    }

    fun cerrarPreguntas() {
        _estado.update { it.copy(contenedor = null, preguntas = emptyList(), mensaje = null, error = null) }
    }

    private fun cargarPreguntas() {
        val contenedor = _estado.value.contenedor ?: return
        _estado.update { it.copy(cargando = true) }
        viewModelScope.launch {
            try {
                val preguntas = obtenerPreguntas(contenedor)
                _estado.update { it.copy(cargando = false, preguntas = preguntas) }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(cargando = false, error = mensajeDeError(e, "No se pudieron cargar las preguntas"))
                }
            }
        }
    }

    /**
     * Crea o edita una pregunta de selección. [opciones] son los textos escritos (vacíos se
     * descartan) y [indiceCorrecta] apunta a la posición dentro de esa misma lista.
     */
    fun guardarPregunta(
        id: Long?,
        enunciado: String,
        explicacion: String,
        opciones: List<String>,
        indiceCorrecta: Int?
    ) {
        val contenedor = _estado.value.contenedor ?: return
        val error = errorPregunta(enunciado, opciones, indiceCorrecta)
        if (error != null) {
            _estado.update { it.copy(error = error, mensaje = null) }
            return
        }

        val alternativas = armarAlternativas(opciones, indiceCorrecta!!)
        val orden = if (id == null) {
            (_estado.value.preguntas.maxOfOrNull { it.orden } ?: 0) + 1
        } else {
            _estado.value.preguntas.firstOrNull { it.id == id }?.orden ?: 1
        }
        val cuerpo = UtpPreguntaRemota(
            actividadId = (contenedor as? ContenedorPreguntas.DeActividad)?.id,
            pruebaFinalId = (contenedor as? ContenedorPreguntas.DePrueba)?.id,
            enunciado = enunciado.trim(),
            tipo = "SELECCION",
            orden = orden,
            explicacion = explicacion.trim().ifEmpty { null },
            opciones = alternativas
        )

        ejecutarGuardado(
            exito = if (id == null) "Pregunta agregada" else "Pregunta actualizada",
            falla = "No se pudo guardar la pregunta"
        ) {
            if (id == null) api.crearPregunta(cuerpo) else api.editarPregunta(id, cuerpo)
            val preguntas = obtenerPreguntas(contenedor)
            _estado.update { it.copy(preguntas = preguntas) }
        }
    }

    // ---------- Apoyo ----------

    private suspend fun obtenerPreguntas(contenedor: ContenedorPreguntas): List<UtpPreguntaRemota> =
        when (contenedor) {
            is ContenedorPreguntas.DeActividad -> api.preguntasDeActividad(contenedor.id)
            is ContenedorPreguntas.DePrueba -> api.preguntasDePrueba(contenedor.id)
        }.sortedBy { it.orden }

    private suspend fun recargarListas() {
        val objetivoId = _estado.value.objetivoId ?: return
        val actividades = api.listarActividades(objetivoId).items.sortedBy { it.orden }
        val pruebas = api.listarPruebas(objetivoId).items
        _estado.update { it.copy(actividades = actividades, pruebas = pruebas) }
    }

    private fun ejecutarGuardado(
        exito: String,
        falla: String,
        cerrarFormulario: Boolean = true,
        accion: suspend () -> Unit
    ) {
        if (_estado.value.guardando) return
        _estado.update { it.copy(guardando = true, mensaje = null, error = null) }
        viewModelScope.launch {
            try {
                accion()
                _estado.update {
                    it.copy(
                        guardando = false,
                        mensaje = exito,
                        guardadosOk = if (cerrarFormulario) it.guardadosOk + 1 else it.guardadosOk
                    )
                }
            } catch (e: Exception) {
                _estado.update { it.copy(guardando = false, error = mensajeDeError(e, falla)) }
            }
        }
    }

    fun limpiar() {
        _estado.value = UtpActividadesUiState()
    }
}

// ---------- Reglas puras (se prueban en test unitario) ----------

private val FORMATO_FECHA = Regex("""\d{4}-\d{2}-\d{2}""")

/** Siguiente estado permitido para actividades y pruebas (mismo flujo que el backend). */
fun siguienteEstadoEvaluacion(actual: String?): String? = when (actual) {
    "BORRADOR" -> "EN_REVISION"
    "EN_REVISION" -> "AUTORIZADO"
    "AUTORIZADO" -> "PUBLICADO"
    "PUBLICADO" -> "DESHABILITADO"
    else -> null
}

fun nombreAccionEvaluacion(destino: String): String = when (destino) {
    "EN_REVISION" -> "Enviar a revisión"
    "AUTORIZADO" -> "Autorizar"
    "PUBLICADO" -> "Publicar"
    "DESHABILITADO" -> "Deshabilitar"
    else -> destino
}

fun errorActividad(titulo: String, ordenTexto: String): String? {
    val orden = ordenTexto.trim().toIntOrNull()
    return when {
        titulo.isBlank() -> "Escribe el título de la actividad"
        titulo.trim().length > 200 -> "El título admite hasta 200 caracteres"
        orden == null -> "El orden debe ser un número entero"
        orden < 0 -> "El orden no puede ser negativo"
        else -> null
    }
}

fun errorPrueba(titulo: String, desde: String, hasta: String, limiteTexto: String): String? {
    val d = desde.trim()
    val h = hasta.trim()
    val limite = limiteTexto.trim()
    return when {
        titulo.isBlank() -> "Escribe el título de la prueba"
        titulo.trim().length > 200 -> "El título admite hasta 200 caracteres"
        d.isNotEmpty() && !FORMATO_FECHA.matches(d) -> "Fecha inicio: usa el formato AAAA-MM-DD"
        h.isNotEmpty() && !FORMATO_FECHA.matches(h) -> "Fecha fin: usa el formato AAAA-MM-DD"
        d.isNotEmpty() && h.isNotEmpty() && h < d -> "La fecha fin no puede ser anterior al inicio"
        limite.isNotEmpty() && limite.toIntOrNull() == null -> "El límite de intentos debe ser un número"
        limite.isNotEmpty() && limite.toInt() < 1 -> "El límite de intentos debe ser al menos 1"
        else -> null
    }
}

fun errorPregunta(enunciado: String, opciones: List<String>, indiceCorrecta: Int?): String? {
    val llenas = opciones.count { it.isNotBlank() }
    return when {
        enunciado.isBlank() -> "Escribe el enunciado de la pregunta"
        llenas < 2 -> "Escribe al menos dos alternativas"
        indiceCorrecta == null -> "Marca cuál alternativa es la correcta"
        opciones.getOrNull(indiceCorrecta).isNullOrBlank() -> "La alternativa correcta no puede estar vacía"
        else -> null
    }
}

/** Descarta alternativas vacías y conserva la marca de la correcta. */
fun armarAlternativas(opciones: List<String>, indiceCorrecta: Int): List<UtpAlternativaRemota> =
    opciones.withIndex()
        .filter { it.value.isNotBlank() }
        .mapIndexed { orden, opcion ->
            UtpAlternativaRemota(
                texto = opcion.value.trim(),
                esCorrecta = opcion.index == indiceCorrecta,
                orden = orden + 1
            )
        }

/** Traduce errores HTTP o de red a un mensaje para la interfaz (contrato 8.10). */
fun mensajeDeError(e: Throwable, porDefecto: String): String = when (e) {
    is HttpException -> {
        val delServidor = try {
            e.response()?.errorBody()?.string()?.let { cuerpo ->
                val json = JsonParser.parseString(cuerpo).asJsonObject
                listOf("mensaje", "message")
                    .firstNotNullOfOrNull { clave ->
                        json.get(clave)?.takeUnless { it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
                    }
            }
        } catch (_: Exception) {
            null
        }
        delServidor ?: when (e.code()) {
            401 -> "Tu sesión expiró. Vuelve a iniciar sesión"
            403 -> "Tu perfil no tiene permiso para esta acción"
            404 -> "El recurso no existe o no está visible"
            409 -> "La acción no está permitida en el estado actual"
            else -> porDefecto
        }
    }
    is IOException -> "Sin conexión con el servidor (revisa Docker y adb reverse)"
    else -> porDefecto
}
