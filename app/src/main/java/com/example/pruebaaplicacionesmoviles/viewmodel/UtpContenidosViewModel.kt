
package com.example.pruebaaplicacionesmoviles.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaaplicacionesmoviles.data.model.Asignatura
import com.example.pruebaaplicacionesmoviles.data.repository.UtpContenidoRepository
import com.example.pruebaaplicacionesmoviles.data.repository.UtpMaterial
import com.example.pruebaaplicacionesmoviles.data.repository.UtpObjetivo
import com.example.pruebaaplicacionesmoviles.data.repository.UtpUnidad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UtpContenidosEstado(
    val asignaturas: List<Asignatura> = emptyList(),
    val unidades: List<UtpUnidad> = emptyList(),
    val objetivos: List<UtpObjetivo> = emptyList(),
    val materiales: List<UtpMaterial> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class UtpContenidosViewModel : ViewModel() {

    private val repositorio = UtpContenidoRepository()

    private val _estado = MutableStateFlow(UtpContenidosEstado())

    val estado = _estado.asStateFlow()

    fun cargarAsignaturas() {

        viewModelScope.launch {

            _estado.value = _estado.value.copy(
                cargando = true,
                error = null
            )

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

            _estado.value = _estado.value.copy(
                cargando = true,
                error = null,
                unidades = emptyList(),
                objetivos = emptyList(),
                materiales = emptyList()
            )

            try {

                val datos = repositorio.obtenerUnidades(
                    asignaturaId
                )

                _estado.value = _estado.value.copy(
                    unidades = datos,
                    cargando = false
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun cargarObjetivos(unidadId: Int) {

        viewModelScope.launch {

            _estado.value = _estado.value.copy(
                cargando = true,
                error = null,
                objetivos = emptyList(),
                materiales = emptyList()
            )

            try {

                val datos = repositorio.obtenerObjetivos(
                    unidadId
                )

                _estado.value = _estado.value.copy(
                    objetivos = datos,
                    cargando = false
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun cargarMateriales(objetivoId: Int) {

        viewModelScope.launch {

            _estado.value = _estado.value.copy(
                cargando = true,
                error = null,
                materiales = emptyList()
            )

            try {

                val datos = repositorio.obtenerMateriales(
                    objetivoId
                )

                _estado.value = _estado.value.copy(
                    materiales = datos,
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
            _estado.value = _estado.value.copy(
                error = "Ingresa un título y un orden válido"
            )
            return
        }

        viewModelScope.launch {

            iniciarGuardado()

            try {

                repositorio.crearUnidad(
                    asignaturaId,
                    titulo.trim(),
                    orden
                )

                val actualizadas = repositorio.obtenerUnidades(
                    asignaturaId
                )

                _estado.value = _estado.value.copy(
                    unidades = actualizadas,
                    guardando = false,
                    mensaje = "Unidad creada correctamente"
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun editarUnidad(
        id: Int,
        asignaturaId: Int,
        titulo: String,
        orden: Int
    ) {

        if (titulo.isBlank() || orden < 1) {
            _estado.value = _estado.value.copy(
                error = "Ingresa un título y un orden válido"
            )
            return
        }

        viewModelScope.launch {

            iniciarGuardado()

            try {

                repositorio.editarUnidad(
                    id,
                    asignaturaId,
                    titulo.trim(),
                    orden
                )

                val actualizadas = repositorio.obtenerUnidades(
                    asignaturaId
                )

                _estado.value = _estado.value.copy(
                    unidades = actualizadas,
                    guardando = false,
                    mensaje = "Unidad actualizada correctamente"
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
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
            _estado.value = _estado.value.copy(
                error = "Completa los campos del objetivo"
            )
            return
        }

        viewModelScope.launch {

            iniciarGuardado()

            try {

                repositorio.crearObjetivo(
                    unidadId,
                    codigo.trim(),
                    descripcion.trim(),
                    orden
                )

                val actualizados = repositorio.obtenerObjetivos(
                    unidadId
                )

                _estado.value = _estado.value.copy(
                    objetivos = actualizados,
                    guardando = false,
                    mensaje = "Objetivo creado correctamente"
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
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
            _estado.value = _estado.value.copy(
                error = "Completa los campos del objetivo"
            )
            return
        }

        viewModelScope.launch {

            iniciarGuardado()

            try {

                repositorio.editarObjetivo(
                    id,
                    unidadId,
                    codigo.trim(),
                    descripcion.trim(),
                    orden
                )

                val actualizados = repositorio.obtenerObjetivos(
                    unidadId
                )

                _estado.value = _estado.value.copy(
                    objetivos = actualizados,
                    guardando = false,
                    mensaje = "Objetivo actualizado correctamente"
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun cambiarEstadoUnidad(
        unidadId: Int,
        asignaturaId: Int,
        estadoNuevo: String
    ) {

        viewModelScope.launch {

            iniciarGuardado()

            try {

                repositorio.cambiarEstadoUnidad(
                    unidadId,
                    estadoNuevo
                )

                val unidades = repositorio.obtenerUnidades(
                    asignaturaId
                )

                _estado.value = _estado.value.copy(
                    unidades = unidades,
                    guardando = false,
                    mensaje = "Estado actualizado a $estadoNuevo"
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun cambiarEstadoObjetivo(
        objetivoId: Int,
        unidadId: Int,
        estadoNuevo: String
    ) {

        viewModelScope.launch {

            iniciarGuardado()

            try {

                repositorio.cambiarEstadoObjetivo(
                    objetivoId,
                    estadoNuevo
                )

                val objetivos = repositorio.obtenerObjetivos(
                    unidadId
                )

                _estado.value = _estado.value.copy(
                    objetivos = objetivos,
                    guardando = false,
                    mensaje = "Objetivo actualizado a $estadoNuevo"
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    fun crearMaterial(
        objetivoId: Int,
        titulo: String,
        explicacion: String,
        ejemplo: String,
        instrucciones: String,
        orden: Int,
        responsableOrigen: String,
        licencia: String
    ) {

        if (
            titulo.isBlank() ||
            responsableOrigen.isBlank() ||
            licencia.isBlank() ||
            orden < 0
        ) {
            _estado.value = _estado.value.copy(
                error = "Completa título, autoría y licencia"
            )
            return
        }

        viewModelScope.launch {

            iniciarGuardado()

            try {

                repositorio.crearMaterial(
                    objetivoId = objetivoId,
                    titulo = titulo.trim(),
                    explicacion = explicacion.trim(),
                    ejemplo = ejemplo.trim(),
                    instrucciones = instrucciones.trim(),
                    orden = orden,
                    responsableOrigen = responsableOrigen.trim(),
                    licencia = licencia.trim()
                )

                val materiales = repositorio.obtenerMateriales(
                    objetivoId
                )

                _estado.value = _estado.value.copy(
                    materiales = materiales,
                    guardando = false,
                    mensaje = "Material creado correctamente"
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
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
        licencia: String
    ) {

        if (
            titulo.isBlank() ||
            responsableOrigen.isBlank() ||
            licencia.isBlank() ||
            orden < 0
        ) {
            _estado.value = _estado.value.copy(
                error = "Completa título, autoría y licencia"
            )
            return
        }

        viewModelScope.launch {

            iniciarGuardado()

            try {

                repositorio.editarMaterial(
                    id = id,
                    objetivoId = objetivoId,
                    titulo = titulo.trim(),
                    explicacion = explicacion.trim(),
                    ejemplo = ejemplo.trim(),
                    instrucciones = instrucciones.trim(),
                    orden = orden,
                    responsableOrigen = responsableOrigen.trim(),
                    licencia = licencia.trim()
                )

                val materiales = repositorio.obtenerMateriales(
                    objetivoId
                )

                _estado.value = _estado.value.copy(
                    materiales = materiales,
                    guardando = false,
                    mensaje = "Material actualizado correctamente"
                )

            } catch (e: Exception) {
                mostrarError(e)
            }
        }
    }

    private fun iniciarGuardado() {

        _estado.value = _estado.value.copy(
            guardando = true,
            error = null,
            mensaje = null
        )
    }

    private fun mostrarError(e: Exception) {

        if (e is CancellationException) {
            throw e
        }

        _estado.value = _estado.value.copy(
            cargando = false,
            guardando = false,
            error = e.localizedMessage
                ?: "Error de comunicación con el servidor"
        )
    }

    fun limpiar() {
        _estado.value = UtpContenidosEstado()
    }
}
