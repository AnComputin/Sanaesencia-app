package com.example.sanaesenciaapp.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        EspecialidadEntity::class,
        EspecialistaEntity::class,
        CitaEntity::class,
        BeneficiarioEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun especialidadDao(): EspecialidadDao
    abstract fun especialistaDao(): EspecialistaDao
    abstract fun citaDao(): CitaDao
    abstract fun beneficiarioDao(): BeneficiarioDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE usuarios ADD COLUMN rut TEXT NOT NULL DEFAULT ''")
                    database.execSQL("ALTER TABLE usuarios ADD COLUMN telefono TEXT NOT NULL DEFAULT ''")
                    database.execSQL("ALTER TABLE usuarios ADD COLUMN fechaNacimiento TEXT NOT NULL DEFAULT ''")
                    database.execSQL("ALTER TABLE usuarios ADD COLUMN comuna TEXT NOT NULL DEFAULT 'Los Vilos'")
                    database.execSQL("ALTER TABLE usuarios ADD COLUMN rol TEXT NOT NULL DEFAULT 'PACIENTE'")
                }
            }

        /**
         * Agrega especialistas y citas sin tocar la tabla de usuarios.
         * IMPORTANTE: la definición SQL debe coincidir exactamente con las Entity de Room.
         */
        private val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `especialistas` (
                            `id` INTEGER NOT NULL,
                            `nombre` TEXT NOT NULL,
                            `especialidadId` INTEGER NOT NULL,
                            `modalidad` TEXT NOT NULL,
                            `activo` INTEGER NOT NULL,
                            PRIMARY KEY(`id`),
                            FOREIGN KEY(`especialidadId`) REFERENCES `especialidades`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                        """.trimIndent()
                    )
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_especialistas_especialidadId` ON `especialistas` (`especialidadId`)"
                    )

                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `citas` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `usuarioEmail` TEXT NOT NULL,
                            `especialidadId` INTEGER NOT NULL,
                            `especialidadNombre` TEXT NOT NULL,
                            `especialistaId` INTEGER NOT NULL,
                            `especialistaNombre` TEXT NOT NULL,
                            `modalidad` TEXT NOT NULL,
                            `inicioMillis` INTEGER NOT NULL,
                            `finMillis` INTEGER NOT NULL,
                            `hora` TEXT NOT NULL,
                            `pacienteNombre` TEXT NOT NULL,
                            `estado` TEXT NOT NULL,
                            `boxAsignado` INTEGER
                        )
                        """.trimIndent()
                    )
                }
            }

        /**
         * Corrige la primera versión de la tabla especialistas (que podía tener un DEFAULT
         * incompatible con la Entity de Room) y normaliza el catálogo a las 7 especialidades.
         * No borra usuarios ni citas y conserva modalidad/agenda ya administradas, salvo el
         * antiguo id 2 de Fonoaudiología que pasa a ser Psicología Adulto.
         */
        private val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    fun existeTabla(nombre: String): Boolean {
                        return database.query(
                            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                            arrayOf(nombre)
                        ).use { it.moveToFirst() }
                    }

                    fun columnas(nombreTabla: String): Set<String> {
                        val resultado = mutableSetOf<String>()
                        database.query("PRAGMA table_info(`$nombreTabla`)").use { cursor ->
                            val indiceNombre = cursor.getColumnIndex("name")
                            while (cursor.moveToNext()) {
                                if (indiceNombre >= 0) resultado += cursor.getString(indiceNombre)
                            }
                        }
                        return resultado
                    }

                    // La rama develop v4 no tenía especialistas. Una versión intermedia de
                    // Rama-Mark sí podía tenerlos. La migración soporta ambos orígenes.
                    val veniaDeDevelop = !existeTabla("especialistas")

                    if (veniaDeDevelop) {
                        database.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `especialistas` (
                                `id` INTEGER NOT NULL,
                                `nombre` TEXT NOT NULL,
                                `especialidadId` INTEGER NOT NULL,
                                `modalidad` TEXT NOT NULL,
                                `activo` INTEGER NOT NULL,
                                PRIMARY KEY(`id`),
                                FOREIGN KEY(`especialidadId`) REFERENCES `especialidades`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                            )
                            """.trimIndent()
                        )
                        database.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_especialistas_especialidadId` ON `especialistas` (`especialidadId`)"
                        )
                    } else {
                        // Normaliza una posible tabla intermedia para que coincida exactamente con Room.
                        database.execSQL(
                            """
                            CREATE TABLE `especialistas_nuevo` (
                                `id` INTEGER NOT NULL,
                                `nombre` TEXT NOT NULL,
                                `especialidadId` INTEGER NOT NULL,
                                `modalidad` TEXT NOT NULL,
                                `activo` INTEGER NOT NULL,
                                PRIMARY KEY(`id`),
                                FOREIGN KEY(`especialidadId`) REFERENCES `especialidades`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                            )
                            """.trimIndent()
                        )
                        database.execSQL(
                            """
                            INSERT INTO `especialistas_nuevo` (`id`, `nombre`, `especialidadId`, `modalidad`, `activo`)
                            SELECT `id`, `nombre`, `especialidadId`, `modalidad`, `activo`
                            FROM `especialistas`
                            """.trimIndent()
                        )
                        database.execSQL("DROP TABLE `especialistas`")
                        database.execSQL("ALTER TABLE `especialistas_nuevo` RENAME TO `especialistas`")
                        database.execSQL(
                            "CREATE INDEX `index_especialistas_especialidadId` ON `especialistas` (`especialidadId`)"
                        )
                    }

                    // Develop v4 tenía otra estructura de citas. La convertimos sin borrar historial.
                    if (existeTabla("citas")) {
                        val columnasCitas = columnas("citas")
                        if ("usuarioEmail" !in columnasCitas) {
                            database.execSQL(
                                """
                                CREATE TABLE `citas_nuevo` (
                                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                    `usuarioEmail` TEXT NOT NULL,
                                    `especialidadId` INTEGER NOT NULL,
                                    `especialidadNombre` TEXT NOT NULL,
                                    `especialistaId` INTEGER NOT NULL,
                                    `especialistaNombre` TEXT NOT NULL,
                                    `modalidad` TEXT NOT NULL,
                                    `inicioMillis` INTEGER NOT NULL,
                                    `finMillis` INTEGER NOT NULL,
                                    `hora` TEXT NOT NULL,
                                    `pacienteNombre` TEXT NOT NULL,
                                    `estado` TEXT NOT NULL,
                                    `boxAsignado` INTEGER
                                )
                                """.trimIndent()
                            )

                            database.execSQL(
                                """
                                INSERT INTO `citas_nuevo` (
                                    `id`, `usuarioEmail`, `especialidadId`, `especialidadNombre`,
                                    `especialistaId`, `especialistaNombre`, `modalidad`,
                                    `inicioMillis`, `finMillis`, `hora`, `pacienteNombre`, `estado`, `boxAsignado`
                                )
                                SELECT
                                    `id`,
                                    `pacienteEmail`,
                                    CASE `especialidad`
                                        WHEN 'Psicología Infanto-Juvenil' THEN 1
                                        WHEN 'Terapia Ocupacional' THEN 3
                                        WHEN 'Nutrición Integrativa' THEN 4
                                        ELSE 0
                                    END,
                                    `especialidad`,
                                    CASE `profesional`
                                        WHEN 'Mg. Andrea Soto' THEN 1
                                        WHEN 'TO. Javiera Paz' THEN 11
                                        WHEN 'Nut. Felipe Morales' THEN 15
                                        ELSE 0
                                    END,
                                    `profesional`,
                                    UPPER(`modalidad`),
                                    `inicioMillis`,
                                    `inicioMillis` + 3600000,
                                    `hora`,
                                    `paciente`,
                                    UPPER(`estado`),
                                    NULL
                                FROM `citas`
                                """.trimIndent()
                            )
                            database.execSQL("DROP TABLE `citas`")
                            database.execSQL("ALTER TABLE `citas_nuevo` RENAME TO `citas`")
                        }
                    } else {
                        database.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `citas` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `usuarioEmail` TEXT NOT NULL,
                                `especialidadId` INTEGER NOT NULL,
                                `especialidadNombre` TEXT NOT NULL,
                                `especialistaId` INTEGER NOT NULL,
                                `especialistaNombre` TEXT NOT NULL,
                                `modalidad` TEXT NOT NULL,
                                `inicioMillis` INTEGER NOT NULL,
                                `finMillis` INTEGER NOT NULL,
                                `hora` TEXT NOT NULL,
                                `pacienteNombre` TEXT NOT NULL,
                                `estado` TEXT NOT NULL,
                                `boxAsignado` INTEGER
                            )
                            """.trimIndent()
                        )
                    }

                    // Normaliza catálogo. Cuando venimos de develop sabemos que son datos seed antiguos,
                    // por lo que Terapia Ocupacional puede adoptar la configuración actual.
                    database.execSQL(
                        """
                        UPDATE especialidades
                        SET nombre = 'Psicología Infanto-Juvenil',
                            descripcion = 'Acompañamiento psicológico integral para niños y jóvenes.',
                            icono = '🌱',
                            profesional = '5 Especialistas'
                        WHERE id = 1
                        """.trimIndent()
                    )

                    database.execSQL(
                        """
                        UPDATE especialidades
                        SET nombre = 'Psicología Adulto',
                            descripcion = 'Atención psicoterapéutica especializada para adultos.',
                            icono = '🧠',
                            profesional = '5 Especialistas',
                            modalidad = CASE WHEN nombre = 'Fonoaudiología' THEN 'Ambas' ELSE modalidad END,
                            agendaPausada = CASE WHEN nombre = 'Fonoaudiología' THEN 0 ELSE agendaPausada END
                        WHERE id = 2
                        """.trimIndent()
                    )

                    database.execSQL(
                        """
                        UPDATE especialidades
                        SET nombre = 'Terapia Ocupacional',
                            descripcion = 'Estimulación sensorial y desarrollo de autonomía.',
                            icono = '🧩',
                            profesional = '4 Especialistas'
                            ${if (veniaDeDevelop) ", modalidad = 'Ambas', agendaPausada = 0" else ""}
                        WHERE id = 3
                        """.trimIndent()
                    )

                    database.execSQL(
                        """
                        UPDATE especialidades
                        SET nombre = 'Nutrición Integrativa',
                            descripcion = 'Planes nutricionales enfocados en el bienestar holístico.',
                            icono = '🥗',
                            profesional = '4 Especialistas'
                        WHERE id = 4
                        """.trimIndent()
                    )

                    database.execSQL(
                        """
                        INSERT OR IGNORE INTO especialidades
                        (id, nombre, descripcion, icono, profesional, modalidad, agendaPausada)
                        VALUES
                        (5, 'Terapia Integrativa', 'Tratamientos holísticos para el equilibrio cuerpo-mente.', '🌿', '4 Especialistas', 'Ambas', 0),
                        (6, 'Acupuntura', 'Medicina tradicional china para alivio de dolores y estrés.', '🪡', '4 Especialistas', 'Presencial', 0),
                        (7, 'Quiropraxia', 'Alineación vertebral y salud del sistema neuromusculoesquelético.', '🦴', '4 Especialistas', 'Presencial', 0)
                        """.trimIndent()
                    )

                    database.execSQL(
                        """
                        UPDATE especialidades
                        SET nombre = 'Terapia Integrativa',
                            descripcion = 'Tratamientos holísticos para el equilibrio cuerpo-mente.',
                            icono = '🌿',
                            profesional = '4 Especialistas'
                        WHERE id = 5
                        """.trimIndent()
                    )
                    database.execSQL(
                        """
                        UPDATE especialidades
                        SET nombre = 'Acupuntura',
                            descripcion = 'Medicina tradicional china para alivio de dolores y estrés.',
                            icono = '🪡',
                            profesional = '4 Especialistas'
                        WHERE id = 6
                        """.trimIndent()
                    )
                    database.execSQL(
                        """
                        UPDATE especialidades
                        SET nombre = 'Quiropraxia',
                            descripcion = 'Alineación vertebral y salud del sistema neuromusculoesquelético.',
                            icono = '🦴',
                            profesional = '4 Especialistas'
                        WHERE id = 7
                        """.trimIndent()
                    )
                }
            }

        /**
         * Agrega beneficiarios persistentes y vincula opcionalmente una cita con el
         * beneficiario seleccionado. No modifica usuarios, especialistas ni reservas existentes.
         */
        private val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `beneficiarios` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `propietarioEmail` TEXT NOT NULL,
                            `nombre` TEXT NOT NULL,
                            `rut` TEXT NOT NULL,
                            `fechaNacimiento` TEXT NOT NULL,
                            `parentesco` TEXT NOT NULL,
                            `activo` INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_beneficiarios_propietarioEmail` ON `beneficiarios` (`propietarioEmail`)"
                    )
                    database.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_beneficiarios_propietarioEmail_rut` ON `beneficiarios` (`propietarioEmail`, `rut`)"
                    )
                    database.execSQL(
                        "ALTER TABLE `citas` ADD COLUMN `beneficiarioId` INTEGER"
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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}
