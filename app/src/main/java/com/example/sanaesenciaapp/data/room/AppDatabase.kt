package com.example.sanaesenciaapp.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
<<<<<<< HEAD
    entities = [UsuarioEntity::class, EspecialidadEntity::class],
    version = 2,
=======
    entities = [UsuarioEntity::class],
    version = 1,
>>>>>>> 0c342bb8295f777899d0c8481aac18b66bff87b5
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
<<<<<<< HEAD
    abstract fun especialidadDao(): EspecialidadDao
=======
>>>>>>> 0c342bb8295f777899d0c8481aac18b66bff87b5

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sanaesencia_db"
                )
<<<<<<< HEAD
                    .fallbackToDestructiveMigration(true)                    .build()
=======
                    .build()
>>>>>>> 0c342bb8295f777899d0c8481aac18b66bff87b5
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}