
package com.example.pruebaaplicacionesmoviles.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaaplicacionesmoviles.data.model.Asignatura
import com.example.pruebaaplicacionesmoviles.data.model.MaterialDidactico
import com.example.pruebaaplicacionesmoviles.data.model.Unidad
import com.example.pruebaaplicacionesmoviles.data.repository.ContenidoRemotoRepository
import com.example.pruebaaplicacionesmoviles.data.repository.RecursoRemotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContenidoUiState(
    val asignaturas: List<Asignatura> = emptyList(),
    val unidades: List<Unidad> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

data class MaterialesUiState(
    val materiales: List<MaterialDidactico> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class ContenidoViewModel : ViewModel() {

    private val repositorio = ContenidoRemotoRepository()

    private val repositorioRecursos = RecursoRemotoRepository()

    private val _estado = MutableStateFlow(ContenidoUiState())
    val estado = _estado.asStateFlow()

    private val _estadoMateriales =
        MutableStateFlow(MaterialesUiState())

    val estadoMateriales = _estadoMateriales.asStateFlow()

    fun cargarContenidos() {
        viewModelScope.launch {

            _estado.value = ContenidoUiState(
                cargando = true
            )

            try {
                val asignaturas = repositorio.obtenerAsignaturas()

                val unidades = repositorio.obtenerUnidades(
                    asignaturas
                )

                _estado.value = ContenidoUiState(
                    asignaturas = asignaturas,
                    unidades = unidades
                )

            } catch (e: Exception) {

                _estado.value = ContenidoUiState(
                    error = e.localizedMessage
                        ?: "Error al cargar contenidos"
                )
            }
        }
    }

    fun cargarMateriales(unidadId: Int) {
        viewModelScope.launch {

            _estadoMateriales.value = MaterialesUiState(
                cargando = true
            )

            try {
                val materiales = repositorio.obtenerMateriales(
                    unidadId
                )

                _estadoMateriales.value = MaterialesUiState(
                    materiales = materiales
                )

            } catch (e: Exception) {

                _estadoMateriales.value = MaterialesUiState(
                    error = e.localizedMessage
                        ?: "Error al cargar materiales"
                )
            }
        }
    }

    suspend fun descargarRecurso(
        ruta: String
    ): ByteArray {
        return repositorioRecursos.descargarRecurso(ruta)
    }

    fun limpiar() {
        _estado.value = ContenidoUiState()
        _estadoMateriales.value = MaterialesUiState()
    }
}
