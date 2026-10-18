package com.example.pruebaaplicacionesmoviles.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        AsignaturaEntity::class,
        UnidadEntity::class,
        ActividadEntity::class,
        PreguntaEntity::class,
        AsignacionEntity::class,
        IntentoEntity::class,
        RespuestaEstudianteEntity::class,
        ProgresoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MpazDatabase : RoomDatabase() {

    abstract fun mpazDao(): MpazDao

    companion object {
        @Volatile
        private var instancia: MpazDatabase? = null

        fun obtener(context: Context): MpazDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    MpazDatabase::class.java,
                    "mpaz.db"
                ).build().also { instancia = it }
            }
        }
    }
}