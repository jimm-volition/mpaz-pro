package com.example.pruebaaplicacionesmoviles.viewmodel

import android.app.Application
import android.net.Uri
import com.example.pruebaaplicacionesmoviles.data.model.Asignatura
import com.example.pruebaaplicacionesmoviles.data.repository.EstadoMaterialRepository
import com.example.pruebaaplicacionesmoviles.data.repository.UtpContenidoRepository
import com.example.pruebaaplicacionesmoviles.data.repository.UtpMaterial
import com.example.pruebaaplicacionesmoviles.data.repository.UtpObjetivo
import com.example.pruebaaplicacionesmoviles.data.repository.UtpUnidad
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Más ramas de validación, refresco, errores y archivos del ViewModel UTP. */
@OptIn(ExperimentalCoroutinesApi::class)
class UtpContenidosViewModelExtraTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var api: UtpContenidoRepository
    private lateinit var estados: EstadoMaterialRepository
    private lateinit var aplicacion: Application
    private lateinit var vm: UtpContenidosViewModel

    private val unidad = UtpUnidad(2, 1, "Lectura", 1, "PUBLICADO")
    private val objetivo = UtpObjetivo(3, 2, "OA1", "Comprender", "PUBLICADO", 1)
    private val material = UtpMaterial(4, "Cuento", "PUBLICADO", "Texto", "Ejemplo", "Lee", 1,
        "Equipo", "Propia", emptyList(), null)

    @Before fun configurar() {
        Dispatchers.setMain(dispatcher)
        api = mockk()
        estados = mockk()
        aplicacion = mockk(relaxed = true)
        vm = UtpContenidosViewModel(aplicacion, api, estados)
    }

    @After fun terminar() { Dispatchers.resetMain() }

    @Test fun historialVacioNoSeConsideraError() = runTest {
        coEvery { api.obtenerHistorial(2) } returns emptyList()
        vm.cargarHistorial(2)
        advanceUntilIdle()
        assertTrue(vm.estado.value.historial.isEmpty())
        assertNull(vm.estado.value.error)
        assertFalse(vm.estado.value.cargando)
    }

    @Test fun errorDeHistorialSeMuestra() = runTest {
        coEvery { api.obtenerHistorial(2) } throws IllegalStateException("Historial no disponible")
        vm.cargarHistorial(2)
        advanceUntilIdle()
        assertEquals("Historial no disponible", vm.estado.value.error)
        assertFalse(vm.estado.value.cargando)
    }

    @Test fun errorAlCargarObjetivosSeMuestra() = runTest {
        coEvery { api.obtenerObjetivos(2) } throws IllegalStateException("Error en OA")
        vm.cargarObjetivos(2)
        advanceUntilIdle()
        assertEquals("Error en OA", vm.estado.value.error)
        assertFalse(vm.estado.value.cargando)
    }

    @Test fun errorAlCargarMaterialesSeMuestra() = runTest {
        coEvery { api.obtenerMateriales(3) } throws IllegalStateException("Error en materiales")
        vm.cargarMateriales(3)
        advanceUntilIdle()
        assertEquals("Error en materiales", vm.estado.value.error)
        assertFalse(vm.estado.value.cargando)
    }

    @Test fun editarUnidadVaciaSeRechazaAntesDeIrAlServidor() {
        vm.editarUnidad(2, 1, " ", 1)
        assertTrue(vm.estado.value.error.orEmpty().isNotBlank())
        coVerify(exactly = 0) { api.editarUnidad(any(), any(), any(), any()) }
    }

    @Test fun editarUnidadOrdenCeroSeRechazaAntesDeIrAlServidor() {
        vm.editarUnidad(2, 1, "Cuentos", 0)
        assertTrue(vm.estado.value.error.orEmpty().isNotBlank())
        coVerify(exactly = 0) { api.editarUnidad(any(), any(), any(), any()) }
    }

    @Test fun crearObjetivoSinDescripcionSeRechaza() {
        vm.crearObjetivo(2, "OA2", "  ", 1)
        assertTrue(vm.estado.value.error.orEmpty().isNotBlank())
        coVerify(exactly = 0) { api.crearObjetivo(any(), any(), any(), any()) }
    }

    @Test fun crearObjetivoConOrdenCeroSeRechaza() {
        vm.crearObjetivo(2, "OA2", "Texto", 0)
        assertTrue(vm.estado.value.error.orEmpty().isNotBlank())
        coVerify(exactly = 0) { api.crearObjetivo(any(), any(), any(), any()) }
    }

    @Test fun editarObjetivoConCodigoVacioSeRechaza() {
        vm.editarObjetivo(3, 2, "  ", "Texto", 1)
        assertTrue(vm.estado.value.error.orEmpty().isNotBlank())
        coVerify(exactly = 0) { api.editarObjetivo(any(), any(), any(), any(), any()) }
    }

    @Test fun editarObjetivoConOrdenInvalidoSeRechaza() {
        vm.editarObjetivo(3, 2, "OA3", "Texto", -1)
        assertTrue(vm.estado.value.error.orEmpty().isNotBlank())
        coVerify(exactly = 0) { api.editarObjetivo(any(), any(), any(), any(), any()) }
    }

    @Test fun crearMaterialConTituloVacioSeRechaza() {
        vm.crearMaterial(3, " ", "Texto", "Ejemplo", "Lee", 1, "UTP", "Propia")
        assertTrue(vm.estado.value.error.orEmpty().isNotBlank())
        coVerify(exactly = 0) {
            api.crearMaterial(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test fun editarMaterialSinLicenciaSeRechaza() {
        vm.editarMaterial(4, 3, "Cuento", "Texto", "Ejemplo", "Lee", 1,
            "UTP", "  ", emptyList(), null)
        assertTrue(vm.estado.value.error.orEmpty().isNotBlank())
        coVerify(exactly = 0) {
            api.editarMaterial(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test fun materialConOrdenCeroEsPermitido() = runTest {
        coEvery {
            api.crearMaterial(3, "Cuento", "Texto", "Ejemplo", "Lee", 0,
                "UTP", "Propia", emptyList(), null)
        } returns Unit
        coEvery { api.obtenerMateriales(3) } returns listOf(material)
        vm.crearMaterial(3, "Cuento", "Texto", "Ejemplo", "Lee", 0, "UTP", "Propia")
        advanceUntilIdle()
        assertEquals("Material creado correctamente", vm.estado.value.mensaje)
        assertEquals(1, vm.estado.value.materiales.size)
        assertNull(vm.estado.value.error)
    }

    @Test fun cambioEstadoMaterialExitosoRefrescaListado() = runTest {
        coEvery { estados.cambiarEstado(4, "PUBLICADO") } returns Unit
        coEvery { api.obtenerMateriales(3) } returns listOf(material)
        vm.cambiarEstadoMaterial(4, 3, "PUBLICADO")
        advanceUntilIdle()
        coVerify(exactly = 1) { estados.cambiarEstado(4, "PUBLICADO") }
        assertEquals(listOf(material), vm.estado.value.materiales)
        assertEquals("Material actualizado a PUBLICADO", vm.estado.value.mensaje)
        assertFalse(vm.estado.value.guardando)
    }

    @Test fun errorAlRefrescarDespuesDeGuardarSeMuestra() = runTest {
        coEvery { api.crearUnidad(1, "Cuentos", 1) } returns Unit
        coEvery { api.obtenerUnidades(1) } throws IllegalStateException("No se pudo refrescar")
        vm.crearUnidad(1, "Cuentos", 1)
        advanceUntilIdle()
        assertEquals("No se pudo refrescar", vm.estado.value.error)
        assertFalse(vm.estado.value.guardando)
    }

    @Test fun unaCargaCorrectaLimpiaUnErrorDeValidacion() = runTest {
        vm.crearUnidad(1, " ", 0)
        assertTrue(vm.estado.value.error.orEmpty().isNotBlank())
        coEvery { api.obtenerAsignaturas() } returns listOf(Asignatura(1, "Lenguaje"))
        vm.cargarAsignaturas()
        advanceUntilIdle()
        assertNull(vm.estado.value.error)
        assertEquals(1, vm.estado.value.asignaturas.size)
    }

    @Test fun subirImagenGuardaRutaYMensaje() = runTest {
        val uri = mockk<Uri>()
        coEvery { api.subirArchivo(aplicacion, uri) } returns "/api/v1/recursos/imagen.png"
        vm.subirImagen(uri)
        advanceUntilIdle()
        assertEquals("/api/v1/recursos/imagen.png", vm.estado.value.imagenSubida)
        assertEquals("Imagen subida correctamente", vm.estado.value.mensaje)
        assertFalse(vm.estado.value.subiendoArchivo)
    }

    @Test fun subirAudioGuardaRutaYMensaje() = runTest {
        val uri = mockk<Uri>()
        coEvery { api.subirArchivo(aplicacion, uri) } returns "/api/v1/recursos/audio.wav"
        vm.subirAudio(uri)
        advanceUntilIdle()
        assertEquals("/api/v1/recursos/audio.wav", vm.estado.value.audioSubido)
        assertEquals("Audio subido correctamente", vm.estado.value.mensaje)
        assertFalse(vm.estado.value.subiendoArchivo)
    }

    @Test fun errorAlSubirAudioLimpiaIndicadorDeCarga() = runTest {
        val uri = mockk<Uri>()
        coEvery { api.subirArchivo(aplicacion, uri) } throws IllegalStateException("Archivo vacío")
        vm.subirAudio(uri)
        advanceUntilIdle()
        assertEquals("Archivo vacío", vm.estado.value.error)
        assertFalse(vm.estado.value.subiendoArchivo)
    }

    @Test fun limpiarSubidasBorraLasRutas() = runTest {
        val uri = mockk<Uri>()
        coEvery { api.subirArchivo(aplicacion, uri) } returns "/api/v1/recursos/a.wav"
        vm.subirAudio(uri)
        advanceUntilIdle()
        assertTrue(vm.estado.value.audioSubido != null)
        vm.limpiarSubidas()
        assertNull(vm.estado.value.audioSubido)
        assertNull(vm.estado.value.imagenSubida)
    }
}
