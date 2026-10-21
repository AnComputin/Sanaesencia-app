package com.example.sanaesenciaapp.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UsuarioEntity::class, EspecialidadEntity::class, CitaEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun especialidadDao(): EspecialidadDao
    abstract fun citaDao(): CitaDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE usuarios ADD COLUMN rut TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE usuarios ADD COLUMN telefono TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE usuarios ADD COLUMN fechaNacimiento TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE usuarios ADD COLUMN comuna TEXT NOT NULL DEFAULT 'Los Vilos'")
                database.execSQL("ALTER TABLE usuarios ADD COLUMN rol TEXT NOT NULL DEFAULT 'PACIENTE'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS citas (
                        id INTEGER NOT NULL,
                        especialidad TEXT NOT NULL,
                        profesional TEXT NOT NULL,
                        modalidad TEXT NOT NULL,
                        inicioMillis INTEGER NOT NULL,
                        hora TEXT NOT NULL,
                        paciente TEXT NOT NULL,
                        estado TEXT NOT NULL,
                        pacienteEmail TEXT NOT NULL,
                        PRIMARY KEY(id)
                    )"""
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sanaesencia_db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}