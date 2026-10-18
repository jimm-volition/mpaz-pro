package com.example.pruebaaplicacionesmoviles.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MpazDao {

    // --- Inserts (datos semilla y nuevos registros) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAsignaturas(asignaturas: List<AsignaturaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUnidades(unidades: List<UnidadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarActividades(actividades: List<ActividadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPreguntas(preguntas: List<PreguntaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAsignaciones(asignaciones: List<AsignacionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAsignacion(asignacion: AsignacionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarIntento(intento: IntentoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRespuestas(respuestas: List<RespuestaEstudianteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProgreso(progreso: ProgresoEntity)

    @Update
    suspend fun actualizarUnidad(unidad: UnidadEntity)

    @Update
    suspend fun actualizarProgreso(progreso: ProgresoEntity)

    // --- Consultas que usa el repositorio ---

    @Query("SELECT * FROM usuarios WHERE alias = :alias LIMIT 1")
    suspend fun buscarUsuario(alias: String): UsuarioEntity?

    @Query("SELECT * FROM asignaturas")
    fun observarAsignaturas(): Flow<List<AsignaturaEntity>>

    @Query("SELECT * FROM unidades")
    fun observarUnidades(): Flow<List<UnidadEntity>>

    @Query(
        """
        SELECT u.* FROM unidades u
        INNER JOIN asignaciones a ON a.unidadId = u.id
        WHERE a.estudianteId = :estudianteId
        """
    )
    fun observarUnidadesAsignadas(estudianteId: Int): Flow<List<UnidadEntity>>

    @Query("SELECT * FROM progresos WHERE estudianteId = :estudianteId")
    fun observarProgreso(estudianteId: Int): Flow<List<ProgresoEntity>>

    @Query("SELECT * FROM actividades WHERE unidadId = :unidadId ORDER BY orden ASC")
    suspend fun obtenerActividades(unidadId: Int): List<ActividadEntity>

    @Query("SELECT * FROM preguntas WHERE actividadId = :actividadId ORDER BY orden ASC")
    suspend fun obtenerPreguntas(actividadId: Int): List<PreguntaEntity>

    @Query(
        """
        SELECT * FROM asignaciones
        WHERE estudianteId = :estudianteId AND unidadId = :unidadId
        LIMIT 1
        """
    )
    suspend fun obtenerAsignacion(estudianteId: Int, unidadId: Int): AsignacionEntity?

    @Query(
        """
        SELECT COUNT(*) FROM intentos
        WHERE asignacionId = :asignacionId AND actividadId = :actividadId
        """
    )
    suspend fun contarIntentos(asignacionId: Int, actividadId: Int): Int

    @Query("SELECT * FROM unidades WHERE id = :unidadId LIMIT 1")
    suspend fun obtenerUnidad(unidadId: Int): UnidadEntity?

    @Query(
        """
        SELECT * FROM progresos
        WHERE estudianteId = :estudianteId AND unidadId = :unidadId
        LIMIT 1
        """
    )
    suspend fun obtenerProgreso(estudianteId: Int, unidadId: Int): ProgresoEntity?

    @Query(
        """
        SELECT i.* FROM intentos i
        INNER JOIN asignaciones a ON a.id = i.asignacionId
        WHERE a.docenteId = :docenteId
        """
    )
    fun observarIntentosDocente(docenteId: Int): Flow<List<IntentoEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int

    @Query("SELECT COALESCE(MAX(id), 0) FROM intentos")
    suspend fun maxIdIntento(): Int

    @Query("SELECT COALESCE(MAX(id), 0) FROM respuestas_estudiante")
    suspend fun maxIdRespuesta(): Int

    @Query("SELECT COALESCE(MAX(id), 0) FROM progresos")
    suspend fun maxIdProgreso(): Int

    @Query("SELECT COALESCE(MAX(id), 0) FROM asignaciones")
    suspend fun maxIdAsignacion(): Int

    @Query("SELECT * FROM asignaciones WHERE id = :id LIMIT 1")
    suspend fun obtenerAsignacionPorId(id: Int): AsignacionEntity?
}