package com.example.pruebaaplicacionesmoviles.viewmodel

import android.app.Application
import com.example.pruebaaplicacionesmoviles.data.model.Asignatura
import com.example.pruebaaplicacionesmoviles.data.repository.EstadoMaterialRepository
import com.example.pruebaaplicacionesmoviles.data.repository.UtpContenidoRepository
import com.example.pruebaaplicacionesmoviles.data.repository.UtpHistorial
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

/** Comprueba la lógica de UTP sin Android real, HTTP ni MySQL. */
@OptIn(ExperimentalCoroutinesApi::class)
class UtpContenidosViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repositorio: UtpContenidoRepository
    private lateinit var repositorioEstados: EstadoMaterialRepository
    private lateinit var viewModel: UtpContenidosViewModel

    private val unidad = UtpUnidad(
        id = 2, asignaturaId = 1, titulo = "Comprensión de cuentos",
        orden = 2, estado = "PUBLICADO"
    )
    private val objetivo = UtpObjetivo(
        id = 2, unidadId = 2, codigo = "OA2",
        descripcion = "Identificar personajes", estado = "PUBLICADO", orden = 1
    )
    private val material = UtpMaterial(
        id = 4, titulo = "Lectura", estado = "PUBLICADO",
        explicacion = "Explicación", ejemplo = "Ejemplo",
        instrucciones = "Leer", orden = 1,
        responsableOrigen = "Equipo MPAZ", licencia = "Creación propia",
        imagenes = listOf("/api/v1/recursos/imagen.png"),
        audio = "/api/v1/recursos/audio.wav"
    )

    @Before
    fun configurar() {
        Dispatchers.setMain(dispatcher)
        repositorio = mockk()
        repositorioEstados = mockk()
        viewModel = UtpContenidosViewModel(
            application = mockk<Application>(relaxed = true),
            repositorio = repositorio,
            repositorioEstados = repositorioEstados
        )
    }

    @After
    fun limpiarDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun estadoInicialSinDatosNiErrores() {
        val estado = viewModel.estado.value
        assertTrue(estado.asignaturas.isEmpty())
        assertTrue(estado.unidades.isEmpty())
        assertFalse(estado.cargando)
        assertFalse(estado.guardando)
        assertNull(estado.error)
    }

    @Test
    fun cargaAsignaturasCorrectamente() = runTest {
        coEvery { repositorio.obtenerAsignaturas() } returns
                listOf(Asignatura(1, "Lenguaje"), Asignatura(2, "Matemática"))

        viewModel.cargarAsignaturas()
        advanceUntilIdle()

        assertEquals(2, viewModel.estado.value.asignaturas.size)
        assertEquals("Lenguaje", viewModel.estado.value.asignaturas.first().nombre)
        assertFalse(viewModel.estado.value.cargando)
        coVerify(exactly = 1) { repositorio.obtenerAsignaturas() }
    }

    @Test
    fun errorAlCargarAsignaturasSeMuestra() = runTest {
        coEvery { repositorio.obtenerAsignaturas() } throws
                IllegalStateException("Servidor no disponible")

        viewModel.cargarAsignaturas()
        advanceUntilIdle()

        assertEquals("Servidor no disponible", viewModel.estado.value.error)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun cargaUnidadesDeLaAsignaturaSeleccionada() = runTest {
        coEvery { repositorio.obtenerUnidades(1) } returns listOf(unidad)

        viewModel.cargarUnidades(1)
        advanceUntilIdle()

        assertEquals(listOf(unidad), viewModel.estado.value.unidades)
        assertTrue(viewModel.estado.value.objetivos.isEmpty())
        assertTrue(viewModel.estado.value.materiales.isEmpty())
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun errorAlCargarUnidadesDesactivaIndicadorDeCarga() = runTest {
        coEvery { repositorio.obtenerUnidades(1) } throws
                IllegalStateException("Error HTTP 500")

        viewModel.cargarUnidades(1)
        advanceUntilIdle()

        assertEquals("Error HTTP 500", viewModel.estado.value.error)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun cargaObjetivosDelOASeleccionado() = runTest {
        coEvery { repositorio.obtenerObjetivos(2) } returns listOf(objetivo)

        viewModel.cargarObjetivos(2)
        advanceUntilIdle()

        assertEquals(listOf(objetivo), viewModel.estado.value.objetivos)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun cargaMaterialesConImagenYAudio() = runTest {
        coEvery { repositorio.obtenerMateriales(2) } returns listOf(material)

        viewModel.cargarMateriales(2)
        advanceUntilIdle()

        assertEquals(listOf(material), viewModel.estado.value.materiales)
        assertEquals(1, viewModel.estado.value.materiales.first().imagenes.size)
        assertTrue(viewModel.estado.value.materiales.first().audio!!.endsWith(".wav"))
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun cargaHistorialDeLaUnidad() = runTest {
        val evento = UtpHistorial(
            estadoAnterior = "DESHABILITADO", estadoNuevo = "PUBLICADO",
            creadoEn = "2026-11-24T19:27:52Z", usuarioId = 1, tipoRecurso = "UNIDAD"
        )
        coEvery { repositorio.obtenerHistorial(2) } returns listOf(evento)

        viewModel.cargarHistorial(2)
        advanceUntilIdle()

        assertEquals(listOf(evento), viewModel.estado.value.historial)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun rechazaUnidadSinTitulo() {
        viewModel.crearUnidad(1, "  ", 1)

        assertTrue(viewModel.estado.value.error!!.isNotBlank())
        assertFalse(viewModel.estado.value.guardando)
        coVerify(exactly = 0) { repositorio.crearUnidad(any(), any(), any()) }
    }

    @Test
    fun rechazaUnidadConOrdenCero() {
        viewModel.crearUnidad(1, "Cuentos", 0)

        assertTrue(viewModel.estado.value.error!!.isNotBlank())
        coVerify(exactly = 0) { repositorio.crearUnidad(any(), any(), any()) }
    }

    @Test
    fun creaUnidadRecortandoEspaciosYRefrescaLista() = runTest {
        coEvery { repositorio.crearUnidad(1, "Cuentos", 2) } returns Unit
        coEvery { repositorio.obtenerUnidades(1) } returns listOf(unidad)

        viewModel.crearUnidad(1, "  Cuentos  ", 2)
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.crearUnidad(1, "Cuentos", 2) }
        assertEquals(listOf(unidad), viewModel.estado.value.unidades)
        assertEquals("Unidad creada correctamente", viewModel.estado.value.mensaje)
        assertFalse(viewModel.estado.value.guardando)
    }

    @Test
    fun editaUnidadYActualizaListado() = runTest {
        coEvery { repositorio.editarUnidad(2, 1, "Texto nuevo", 3) } returns Unit
        coEvery { repositorio.obtenerUnidades(1) } returns listOf(unidad)

        viewModel.editarUnidad(2, 1, "  Texto nuevo  ", 3)
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.editarUnidad(2, 1, "Texto nuevo", 3) }
        assertEquals("Unidad actualizada correctamente", viewModel.estado.value.mensaje)
    }

    @Test
    fun falloAlEditarUnidadNoDejaGuardandoActivo() = runTest {
        coEvery { repositorio.editarUnidad(any(), any(), any(), any()) } throws
                IllegalStateException("No autorizado")

        viewModel.editarUnidad(2, 1, "Cuentos", 1)
        advanceUntilIdle()

        assertEquals("No autorizado", viewModel.estado.value.error)
        assertFalse(viewModel.estado.value.guardando)
    }

    @Test
    fun rechazaObjetivoSinCodigo() {
        viewModel.crearObjetivo(2, "  ", "Descripción", 1)

        assertEquals("Completa los datos del objetivo", viewModel.estado.value.error)
        coVerify(exactly = 0) { repositorio.crearObjetivo(any(), any(), any(), any()) }
    }

    @Test
    fun creaObjetivoYRefrescaListado() = runTest {
        coEvery { repositorio.crearObjetivo(2, "OA2", "Personajes", 1) } returns Unit
        coEvery { repositorio.obtenerObjetivos(2) } returns listOf(objetivo)

        viewModel.crearObjetivo(2, "  OA2  ", "  Personajes  ", 1)
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.crearObjetivo(2, "OA2", "Personajes", 1) }
        assertEquals(listOf(objetivo), viewModel.estado.value.objetivos)
        assertEquals("Objetivo creado correctamente", viewModel.estado.value.mensaje)
    }

    @Test
    fun editaObjetivoRecortandoEspacios() = runTest {
        coEvery { repositorio.editarObjetivo(2, 2, "OA3", "Interpretar", 2) } returns Unit
        coEvery { repositorio.obtenerObjetivos(2) } returns listOf(objetivo)

        viewModel.editarObjetivo(2, 2, " OA3 ", " Interpretar ", 2)
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.editarObjetivo(2, 2, "OA3", "Interpretar", 2) }
        assertEquals("Objetivo actualizado correctamente", viewModel.estado.value.mensaje)
    }

    @Test
    fun cambiaEstadoUnidadYRefresca() = runTest {
        coEvery { repositorio.cambiarEstadoUnidad(2, "PUBLICADO") } returns Unit
        coEvery { repositorio.obtenerUnidades(1) } returns listOf(unidad)

        viewModel.cambiarEstadoUnidad(2, 1, "PUBLICADO")
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.cambiarEstadoUnidad(2, "PUBLICADO") }
        assertEquals("Unidad actualizada a PUBLICADO", viewModel.estado.value.mensaje)
    }

    @Test
    fun cambiaEstadoObjetivoYRefresca() = runTest {
        coEvery { repositorio.cambiarEstadoObjetivo(2, "AUTORIZADO") } returns Unit
        coEvery { repositorio.obtenerObjetivos(2) } returns listOf(objetivo)

        viewModel.cambiarEstadoObjetivo(2, 2, "AUTORIZADO")
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.cambiarEstadoObjetivo(2, "AUTORIZADO") }
        assertEquals("Objetivo actualizado a AUTORIZADO", viewModel.estado.value.mensaje)
    }

    @Test
    fun rechazaMaterialSinResponsable() {
        viewModel.crearMaterial(
            objetivoId = 2, titulo = "Texto", explicacion = "Explicación",
            ejemplo = "Ejemplo", instrucciones = "Leer", orden = 1,
            responsableOrigen = " ", licencia = "Creación propia"
        )

        assertTrue(viewModel.estado.value.error!!.contains("responsable"))
        coVerify(exactly = 0) { repositorio.crearMaterial(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun rechazaMaterialConOrdenNegativo() {
        viewModel.crearMaterial(
            objetivoId = 2, titulo = "Texto", explicacion = "Explicación",
            ejemplo = "Ejemplo", instrucciones = "Leer", orden = -1,
            responsableOrigen = "Equipo", licencia = "Propia"
        )

        assertTrue(viewModel.estado.value.error!!.isNotBlank())
    }

    @Test
    fun creaMaterialMultimediaYRefresca() = runTest {
        coEvery { repositorio.crearMaterial(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns Unit
        coEvery { repositorio.obtenerMateriales(2) } returns listOf(material)

        viewModel.crearMaterial(
            objetivoId = 2, titulo = "  Lectura  ",
            explicacion = "  Explicación  ", ejemplo = " Ejemplo ",
            instrucciones = " Leer ", orden = 1,
            responsableOrigen = " Equipo MPAZ ", licencia = " Creación propia ",
            imagenes = listOf("/api/v1/recursos/imagen.png"),
            audio = "/api/v1/recursos/audio.wav"
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repositorio.crearMaterial(
                2, "Lectura", "Explicación", "Ejemplo", "Leer", 1,
                "Equipo MPAZ", "Creación propia",
                listOf("/api/v1/recursos/imagen.png"),
                "/api/v1/recursos/audio.wav"
            )
        }
        assertEquals(listOf(material), viewModel.estado.value.materiales)
        assertEquals("Material creado correctamente", viewModel.estado.value.mensaje)
    }

    @Test
    fun editaMaterialYRefresca() = runTest {
        coEvery { repositorio.editarMaterial(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns Unit
        coEvery { repositorio.obtenerMateriales(2) } returns listOf(material)

        viewModel.editarMaterial(
            id = 4, objetivoId = 2, titulo = " Lectura ",
            explicacion = " Explicación ", ejemplo = " Ejemplo ",
            instrucciones = " Leer ", orden = 1,
            responsableOrigen = " Equipo MPAZ ", licencia = " Creación propia ",
            imagenes = listOf("/api/v1/recursos/imagen.png"),
            audio = "/api/v1/recursos/audio.wav"
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repositorio.editarMaterial(
                4, 2, "Lectura", "Explicación", "Ejemplo", "Leer", 1,
                "Equipo MPAZ", "Creación propia",
                listOf("/api/v1/recursos/imagen.png"),
                "/api/v1/recursos/audio.wav"
            )
        }
        assertEquals("Material actualizado correctamente", viewModel.estado.value.mensaje)
        assertEquals(listOf(material), viewModel.estado.value.materiales)
    }

    @Test
    fun falloAlCambiarEstadoMaterialSeInforma() = runTest {
        coEvery { repositorioEstados.cambiarEstado(4, "PUBLICADO") } throws
                IllegalStateException("Transición no permitida")

        viewModel.cambiarEstadoMaterial(4, 2, "PUBLICADO")
        advanceUntilIdle()

        assertEquals("Transición no permitida", viewModel.estado.value.error)
        assertFalse(viewModel.estado.value.guardando)
    }

    @Test
    fun limpiarRestauraEstadoInicial() = runTest {
        coEvery { repositorio.obtenerAsignaturas() } returns listOf(Asignatura(1, "Lenguaje"))

        viewModel.cargarAsignaturas()
        advanceUntilIdle()
        assertTrue(viewModel.estado.value.asignaturas.isNotEmpty())

        viewModel.limpiar()
        assertEquals(UtpContenidosEstado(), viewModel.estado.value)
    }
}
