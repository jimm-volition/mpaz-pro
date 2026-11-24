package com.example.pruebaaplicacionesmoviles.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaaplicacionesmoviles.data.model.Asignatura
import com.example.pruebaaplicacionesmoviles.data.repository.UtpContenidoRepository
import com.example.pruebaaplicacionesmoviles.data.repository.UtpUnidad
import com.example.pruebaaplicacionesmoviles.data.repository.UtpObjetivo
import com.example.pruebaaplicacionesmoviles.data.repository.UtpMaterial
import com.example.pruebaaplicacionesmoviles.data.repository.UtpHistorial
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.pruebaaplicacionesmoviles.data.repository.EstadoMaterialRepository
data class UtpContenidosEstado(
    val asignaturas: List<Asignatura> = emptyList(),
    val unidades: List<UtpUnidad> = emptyList(),
    val objetivos: List<UtpObjetivo> = emptyList(),
    val materiales: List<UtpMaterial> = emptyList(),
    val historial: List<UtpHistorial> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val subiendoArchivo: Boolean = false,
    val imagenSubida: String? = null,
    val audioSubido: String? = null,
    val error: String? = null,
    val mensaje: String? = null
)

// @JvmOverloads conserva el constructor (Application) usado por Android.
// Los repositorios opcionales permiten inyectar simulaciones en las pruebas unitarias.
class UtpContenidosViewModel @JvmOverloads constructor(
    application: Application,
    private val repositorio: UtpContenidoRepository = UtpContenidoRepository(),
    private val repositorioEstados: EstadoMaterialRepository = EstadoMaterialRepository()
) : AndroidViewModel(application) {
    private val _estado = MutableStateFlow(UtpContenidosEstado())
    val estado = _estado.asStateFlow()

    fun cargarAsignaturas() {
        viewModelScope.launch {
            iniciarCarga()

            try {
                val datos = repositorio.obtenerAsignaturas()

                _estado.value = _estado.value.copy(
                    asignaturas = datos,
                    cargando = false
                )
            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun cargarUnidades(asignaturaId: Int) {
        viewModelScope.launch {
            iniciarCarga()

            _estado.value = _estado.value.copy(
                unidades = emptyList(),
                objetivos = emptyList(),
                materiales = emptyList(),
                historial = emptyList()
            )

            try {
                actualizarUnidades(asignaturaId)

                _estado.value = _estado.value.copy(
                    cargando = false
                )
            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun cargarObjetivos(unidadId: Int) {
        viewModelScope.launch {
            iniciarCarga()

            _estado.value = _estado.value.copy(
                objetivos = emptyList(),
                materiales = emptyList()
            )

            try {
                actualizarObjetivos(unidadId)

                _estado.value = _estado.value.copy(
                    cargando = false
                )
            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun cargarMateriales(objetivoId: Int) {
        viewModelScope.launch {
            iniciarCarga()

            _estado.value = _estado.value.copy(
                materiales = emptyList()
            )

            try {
                actualizarMateriales(objetivoId)

                _estado.value = _estado.value.copy(
                    cargando = false
                )
            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun cargarHistorial(unidadId: Int) {
        viewModelScope.launch {
            iniciarCarga()

            _estado.value = _estado.value.copy(
                historial = emptyList()
            )

            try {
                val datos = repositorio.obtenerHistorial(
                    unidadId
                )

                _estado.value = _estado.value.copy(
                    historial = datos,
                    cargando = false
                )
            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun crearUnidad(
        asignaturaId: Int,
        titulo: String,
        orden: Int
    ) {
        if (titulo.isBlank() || orden < 1) {
            errorValidacion("Ingresa un título y un orden válido")
            return
        }

        ejecutarGuardado(
            mensaje = "Unidad creada correctamente",
            accion = {
                repositorio.crearUnidad(
                    asignaturaId,
                    titulo.trim(),
                    orden
                )
            },
            refrescar = {
                actualizarUnidades(asignaturaId)
            }
        )
    }

    fun editarUnidad(
        id: Int,
        asignaturaId: Int,
        titulo: String,
        orden: Int
    ) {
        if (titulo.isBlank() || orden < 1) {
            errorValidacion("Ingresa un título y un orden válido")
            return
        }

        ejecutarGuardado(
            mensaje = "Unidad actualizada correctamente",
            accion = {
                repositorio.editarUnidad(
                    id,
                    asignaturaId,
                    titulo.trim(),
                    orden
                )
            },
            refrescar = {
                actualizarUnidades(asignaturaId)
            }
        )
    }

    fun crearObjetivo(
        unidadId: Int,
        codigo: String,
        descripcion: String,
        orden: Int
    ) {
        if (
            codigo.isBlank() ||
            descripcion.isBlank() ||
            orden < 1
        ) {
            errorValidacion("Completa los datos del objetivo")
            return
        }

        ejecutarGuardado(
            mensaje = "Objetivo creado correctamente",
            accion = {
                repositorio.crearObjetivo(
                    unidadId,
                    codigo.trim(),
                    descripcion.trim(),
                    orden
                )
            },
            refrescar = {
                actualizarObjetivos(unidadId)
            }
        )
    }

    fun editarObjetivo(
        id: Int,
        unidadId: Int,
        codigo: String,
        descripcion: String,
        orden: Int
    ) {
        if (
            codigo.isBlank() ||
            descripcion.isBlank() ||
            orden < 1
        ) {
            errorValidacion("Completa los datos del objetivo")
            return
        }

        ejecutarGuardado(
            mensaje = "Objetivo actualizado correctamente",
            accion = {
                repositorio.editarObjetivo(
                    id,
                    unidadId,
                    codigo.trim(),
                    descripcion.trim(),
                    orden
                )
            },
            refrescar = {
                actualizarObjetivos(unidadId)
            }
        )
    }

    fun cambiarEstadoUnidad(
        unidadId: Int,
        asignaturaId: Int,
        estadoNuevo: String
    ) {
        ejecutarGuardado(
            mensaje = "Unidad actualizada a $estadoNuevo",
            accion = {
                repositorio.cambiarEstadoUnidad(
                    unidadId,
                    estadoNuevo
                )
            },
            refrescar = {
                actualizarUnidades(asignaturaId)
            }
        )
    }

    fun cambiarEstadoObjetivo(
        objetivoId: Int,
        unidadId: Int,
        estadoNuevo: String
    ) {
        ejecutarGuardado(
            mensaje = "Objetivo actualizado a $estadoNuevo",
            accion = {
                repositorio.cambiarEstadoObjetivo(
                    objetivoId,
                    estadoNuevo
                )
            },
            refrescar = {
                actualizarObjetivos(unidadId)
            }
        )
    }

    fun crearMaterial(
        objetivoId: Int,
        titulo: String,
        explicacion: String,
        ejemplo: String,
        instrucciones: String,
        orden: Int,
        responsableOrigen: String,
        licencia: String,
        imagenes: List<String> = emptyList(),
        audio: String? = null
    ) {
        if (!materialValido(
                titulo,
                responsableOrigen,
                licencia,
                orden
            )
        ) return

        ejecutarGuardado(
            mensaje = "Material creado correctamente",
            accion = {
                repositorio.crearMaterial(
                    objetivoId,
                    titulo.trim(),
                    explicacion.trim(),
                    ejemplo.trim(),
                    instrucciones.trim(),
                    orden,
                    responsableOrigen.trim(),
                    licencia.trim(),
                    imagenes,
                    audio
                )
            },
            refrescar = {
                actualizarMateriales(objetivoId)
                limpiarSubidas()
            }
        )
    }

    fun editarMaterial(
        id: Int,
        objetivoId: Int,
        titulo: String,
        explicacion: String,
        ejemplo: String,
        instrucciones: String,
        orden: Int,
        responsableOrigen: String,
        licencia: String,
        imagenes: List<String>,
        audio: String?
    ) {
        if (!materialValido(
                titulo,
                responsableOrigen,
                licencia,
                orden
            )
        ) return

        ejecutarGuardado(
            mensaje = "Material actualizado correctamente",
            accion = {
                repositorio.editarMaterial(
                    id,
                    objetivoId,
                    titulo.trim(),
                    explicacion.trim(),
                    ejemplo.trim(),
                    instrucciones.trim(),
                    orden,
                    responsableOrigen.trim(),
                    licencia.trim(),
                    imagenes,
                    audio
                )
            },
            refrescar = {
                actualizarMateriales(objetivoId)
                limpiarSubidas()
            }
        )
    }

    fun subirImagen(uri: Uri) {
        subirArchivo(uri, esImagen = true)
    }

    fun subirAudio(uri: Uri) {
        subirArchivo(uri, esImagen = false)
    }

    private fun subirArchivo(
        uri: Uri,
        esImagen: Boolean
    ) {
        viewModelScope.launch {

            _estado.value = _estado.value.copy(
                subiendoArchivo = true,
                error = null,
                mensaje = null
            )

            try {
                val ruta = repositorio.subirArchivo(
                    getApplication<Application>(),
                    uri
                )

                _estado.value = if (esImagen) {
                    _estado.value.copy(
                        subiendoArchivo = false,
                        imagenSubida = ruta,
                        mensaje = "Imagen subida correctamente"
                    )
                } else {
                    _estado.value.copy(
                        subiendoArchivo = false,
                        audioSubido = ruta,
                        mensaje = "Audio subido correctamente"
                    )
                }

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun limpiarSubidas() {
        _estado.value = _estado.value.copy(
            imagenSubida = null,
            audioSubido = null
        )
    }

    private fun materialValido(
        titulo: String,
        responsable: String,
        licencia: String,
        orden: Int
    ): Boolean {
        if (
            titulo.isBlank() ||
            responsable.isBlank() ||
            licencia.isBlank() ||
            orden < 0
        ) {
            errorValidacion(
                "Completa título, responsable, licencia y orden"
            )
            return false
        }

        return true
    }

    private fun errorValidacion(mensaje: String) {
        _estado.value = _estado.value.copy(
            error = mensaje,
            mensaje = null
        )
    }

    private fun iniciarCarga() {
        _estado.value = _estado.value.copy(
            cargando = true,
            error = null
        )
    }

    private fun ejecutarGuardado(
        mensaje: String,
        accion: suspend () -> Unit,
        refrescar: suspend () -> Unit
    ) {
        viewModelScope.launch {

            _estado.value = _estado.value.copy(
                guardando = true,
                error = null,
                mensaje = null
            )

            try {
                accion()
                refrescar()

                _estado.value = _estado.value.copy(
                    guardando = false,
                    mensaje = mensaje
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    private suspend fun actualizarUnidades(
        asignaturaId: Int
    ) {
        val unidades = repositorio.obtenerUnidades(asignaturaId)

        _estado.value = _estado.value.copy(
            unidades = unidades
        )
    }

    private suspend fun actualizarObjetivos(
        unidadId: Int
    ) {
        val objetivos = repositorio.obtenerObjetivos(unidadId)

        _estado.value = _estado.value.copy(
            objetivos = objetivos
        )
    }

    private suspend fun actualizarMateriales(
        objetivoId: Int
    ) {
        val materiales = repositorio.obtenerMateriales(objetivoId)

        _estado.value = _estado.value.copy(
            materiales = materiales
        )
    }

    private fun mostrarError(e: Exception) {
        if (e is CancellationException) {
            throw e
        }

        _estado.value = _estado.value.copy(
            cargando = false,
            guardando = false,
            subiendoArchivo = false,
            error = e.localizedMessage
                ?: "Error de comunicación con el servidor"
        )
    }

    fun limpiar() {
        _estado.value = UtpContenidosEstado()
    }


    fun cambiarEstadoMaterial(
        materialId: Int,
        objetivoId: Int,
        estadoNuevo: String
    ) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(
                guardando = true,
                error = null,
                mensaje = null
            )

            try {
                repositorioEstados.cambiarEstado(
                    materialId,
                    estadoNuevo
                )

                val materiales = repositorio.obtenerMateriales(
                    objetivoId
                )

                _estado.value = _estado.value.copy(
                    materiales = materiales,
                    guardando = false,
                    mensaje = "Material actualizado a $estadoNuevo"
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

}
