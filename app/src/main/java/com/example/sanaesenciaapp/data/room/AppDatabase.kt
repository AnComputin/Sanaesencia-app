package com.example.sanaesenciaapp.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
//ESTE ES EL QUE ESTABAS USANDO
@Database(
    entities = [UsuarioEntity::class, EspecialidadEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun especialidadDao(): EspecialidadDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {

                override fun migrate(
                    database: SupportSQLiteDatabase
                ) {

                    database.execSQL(
                        """
                ALTER TABLE usuarios
                ADD COLUMN rut TEXT NOT NULL DEFAULT ''
                """.trimIndent()
                    )

                    database.execSQL(
                        """
                ALTER TABLE usuarios
                ADD COLUMN telefono TEXT NOT NULL DEFAULT ''
                """.trimIndent()
                    )

                    database.execSQL(
                        """
                ALTER TABLE usuarios
                ADD COLUMN fechaNacimiento TEXT NOT NULL DEFAULT ''
                """.trimIndent()
                    )

                    database.execSQL(
                        """
                ALTER TABLE usuarios
                ADD COLUMN comuna TEXT NOT NULL DEFAULT 'Los Vilos'
                """.trimIndent()
                    )

                    database.execSQL(
                        """
                ALTER TABLE usuarios
                ADD COLUMN rol TEXT NOT NULL DEFAULT 'PACIENTE'
                """.trimIndent()
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
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}