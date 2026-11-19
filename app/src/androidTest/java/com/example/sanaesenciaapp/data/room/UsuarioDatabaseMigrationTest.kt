package com.example.sanaesenciaapp.data.room

import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsuarioDatabaseMigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetContext = instrumentation.targetContext

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation,
        AppDatabase::class.java
    )

    private val migration23 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE usuarios ADD COLUMN rut TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE usuarios ADD COLUMN telefono TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE usuarios ADD COLUMN fechaNacimiento TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE usuarios ADD COLUMN comuna TEXT NOT NULL DEFAULT 'Los Vilos'")
            database.execSQL("ALTER TABLE usuarios ADD COLUMN rol TEXT NOT NULL DEFAULT 'PACIENTE'")
        }
    }

    private val migration34 = object : Migration(3, 4) {
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

    private val migration45 = object : Migration(4, 5) {
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

    private val migration56 = object : Migration(5, 6) {
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

    @Test
    fun migracionCompletaDesdeV2aV7ConservaEstructuraYDatos() {
        val databaseName = "usuario-migration-v2-to-v7"
        targetContext.deleteDatabase(databaseName)

        val dbPath = targetContext.getDatabasePath(databaseName).path
        val v2Db = SQLiteDatabase.openOrCreateDatabase(dbPath, null)
        try {
            v2Db.execSQL("PRAGMA user_version = 2")
            v2Db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `usuarios` (
                    `email` TEXT NOT NULL,
                    `nombre` TEXT NOT NULL,
                    `password` TEXT NOT NULL,
                    PRIMARY KEY(`email`)
                )
                """.trimIndent()
            )
            v2Db.execSQL(
                """
                INSERT INTO `usuarios` (`email`, `nombre`, `password`)
                VALUES ('Persona@dominio.cl', 'Anaís', 'hash-v2-original')
                """.trimIndent()
            )
            v2Db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `especialidades` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `nombre` TEXT NOT NULL,
                    `descripcion` TEXT NOT NULL,
                    `icono` TEXT NOT NULL,
                    `profesional` TEXT NOT NULL,
                    `modalidad` TEXT NOT NULL,
                    `agendaPausada` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            v2Db.execSQL(
                """
                INSERT INTO `especialidades` (`id`, `nombre`, `descripcion`, `icono`, `profesional`, `modalidad`, `agendaPausada`)
                VALUES (1, 'Psicología Infanto-Juvenil', 'Antigua desc', '🌱', 'Prof', 'Presencial', 0),
                       (2, 'Fonoaudiología', 'Antigua fono', '🗣️', 'Prof', 'Presencial', 0),
                       (3, 'Terapia Ocupacional', 'Antigua desc', '🧩', 'Prof', 'Presencial', 0),
                       (4, 'Nutrición Integrativa', 'Antigua desc', '🥗', 'Prof', 'Presencial', 0)
                """.trimIndent()
            )
        } finally {
            v2Db.close()
        }

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            7,
            true,
            migration23,
            migration34,
            migration45,
            migration56,
            AppDatabase.MIGRATION_6_7
        )

        try {
            val userVersion = migrated.query("PRAGMA user_version").use { cursor ->
                assertTrue(cursor.moveToFirst())
                cursor.getInt(0)
            }
            assertEquals(7, userVersion)

            migrated.query(
                "SELECT email, nombre, password, rut, telefono, fechaNacimiento, comuna, rol FROM usuarios WHERE email = ?",
                arrayOf("persona@dominio.cl")
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Persona@dominio.cl", cursor.getString(0))
                assertEquals("Anaís", cursor.getString(1))
                assertEquals("hash-v2-original", cursor.getString(2))
                assertEquals("", cursor.getString(3))
                assertEquals("", cursor.getString(4))
                assertEquals("", cursor.getString(5))
                assertEquals("Los Vilos", cursor.getString(6))
                assertEquals("PACIENTE", cursor.getString(7))
            }

            val especialidadesCount = migrated.query("SELECT COUNT(*) FROM especialidades").use { cursor ->
                assertTrue(cursor.moveToFirst())
                cursor.getInt(0)
            }
            assertEquals(7, especialidadesCount)

            val createSql = migrated.query(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'usuarios'"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                cursor.getString(0)
            }
            assertTrue(createSql.contains("COLLATE NOCASE", ignoreCase = true))

            migrated.execSQL(
                """
                INSERT OR IGNORE INTO usuarios (
                    email, nombre, password, rut, telefono, fechaNacimiento, comuna, rol
                ) VALUES ('PERSONA@DOMINIO.CL', 'Duplicado', 'other-hash', '', '', '', 'Los Vilos', 'PACIENTE')
                """.trimIndent()
            )
            val totalUsuarios = migrated.query("SELECT COUNT(*) FROM usuarios").use { cursor ->
                assertTrue(cursor.moveToFirst())
                cursor.getInt(0)
            }
            assertEquals(1, totalUsuarios)
        } finally {
            migrated.close()
            targetContext.deleteDatabase(databaseName)
        }
    }

    @Test
    fun migracionPreservaUsuariosReferenciasMayusculasYEspaciosHistoricos() {
        val databaseName = "usuario-migration-success"
        targetContext.deleteDatabase(databaseName)

        val oldDatabase = helper.createDatabase(databaseName, 6)
        insertUser(oldDatabase, "Persona@dominio.cl", "hash-original", "TERAPEUTA")
        insertUser(oldDatabase, "  legado@dominio.cl ", "hash-legado", "PACIENTE")
        oldDatabase.execSQL(
            """
            INSERT INTO citas (
                id, usuarioEmail, especialidadId, especialidadNombre,
                especialistaId, especialistaNombre, modalidad, inicioMillis,
                finMillis, hora, pacienteNombre, beneficiarioId, estado, boxAsignado
            ) VALUES (1, ?, 1, 'Especialidad', 1, 'Profesional', 'ONLINE',
                      1000, 2000, '10:00', 'Persona', NULL, 'CONFIRMADA', NULL)
            """.trimIndent(),
            arrayOf("Persona@dominio.cl")
        )
        oldDatabase.execSQL(
            """
            INSERT INTO beneficiarios (
                id, propietarioEmail, nombre, rut, fechaNacimiento, parentesco, activo
            ) VALUES (1, ?, 'Beneficiario', '98.765.432-1', '01/01/2010', 'Hijo', 1)
            """.trimIndent(),
            arrayOf("Persona@dominio.cl")
        )
        oldDatabase.close()

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            7,
            true,
            AppDatabase.MIGRATION_6_7
        )

        try {
            val user = migrated.query(
                "SELECT email, password, rol FROM usuarios WHERE email = ?",
                arrayOf("persona@dominio.cl")
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                Triple(cursor.getString(0), cursor.getString(1), cursor.getString(2))
            }
            assertEquals(Triple("Persona@dominio.cl", "hash-original", "TERAPEUTA"), user)

            val legacyEmail = migrated.query(
                "SELECT email FROM usuarios WHERE email LIKE '%legado@dominio.cl%'"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                cursor.getString(0)
            }
            assertEquals("  legado@dominio.cl ", legacyEmail)

            assertEquals(
                "Persona@dominio.cl",
                migrated.query("SELECT usuarioEmail FROM citas WHERE id = 1").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getString(0)
                }
            )
            assertEquals(
                "Persona@dominio.cl",
                migrated.query(
                    "SELECT propietarioEmail FROM beneficiarios WHERE id = 1"
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getString(0)
                }
            )

            val createSql = migrated.query(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'usuarios'"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                cursor.getString(0)
            }
            assertTrue(createSql.contains("COLLATE NOCASE", ignoreCase = true))

            migrated.execSQL(
                """
                INSERT OR IGNORE INTO usuarios (
                    email, nombre, password, rut, telefono, fechaNacimiento, comuna, rol
                ) VALUES ('PERSONA@DOMINIO.CL', 'Should not replace', 'other-hash',
                          '00.000.000-0', '', '', '', 'ADMIN')
                """.trimIndent()
            )
            assertEquals(
                2,
                migrated.query("SELECT COUNT(*) FROM usuarios").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getInt(0)
                }
            )
        } finally {
            migrated.close()
            targetContext.deleteDatabase(databaseName)
        }
    }

    @Test
    fun migracionConColisionAbortaYRoomRevierteLaTransaccion() = runBlocking {
        val databaseName = "usuario-migration-collision"
        targetContext.deleteDatabase(databaseName)

        val oldDatabase = helper.createDatabase(databaseName, 6)
        insertUser(oldDatabase, "Persona@dominio.cl", "hash-one", "PACIENTE")
        insertUser(oldDatabase, "persona@dominio.cl", "hash-two", "TERAPEUTA")
        oldDatabase.close()

        val roomDatabase = androidx.room.Room.databaseBuilder(
            targetContext,
            AppDatabase::class.java,
            databaseName
        ).addMigrations(AppDatabase.MIGRATION_6_7).build()

        var failure: Throwable? = null
        try {
            roomDatabase.usuarioDao().obtenerTodos()
        } catch (error: Throwable) {
            failure = error
        } finally {
            roomDatabase.close()
        }

        assertTrue("Expected migration to fail on a NOCASE collision", failure != null)
        val errorText = generateSequence(failure) { it.cause }
            .joinToString(" ") { it.message.orEmpty() }
        assertTrue(errorText.contains("collide under NOCASE"))
        assertFalse(errorText.contains("Persona@dominio.cl"))

        val rawDatabase = SQLiteDatabase.openDatabase(
            targetContext.getDatabasePath(databaseName).path,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
        try {
            assertEquals(
                6,
                rawDatabase.rawQuery("PRAGMA user_version", null).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getInt(0)
                }
            )
            assertEquals(
                2,
                rawDatabase.rawQuery("SELECT COUNT(*) FROM usuarios", null).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getInt(0)
                }
            )
            assertEquals(
                0,
                rawDatabase.rawQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'usuarios_nuevo'",
                    null
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getInt(0)
                }
            )
        } finally {
            rawDatabase.close()
            targetContext.deleteDatabase(databaseName)
        }
    }

    private fun insertUser(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        email: String,
        password: String,
        role: String
    ) {
        database.execSQL(
            """
            INSERT INTO usuarios (
                email, nombre, password, rut, telefono, fechaNacimiento, comuna, rol
            ) VALUES (?, 'Persona', ?, '12.345.678-9', '+56912345678',
                      '01/01/1990', 'Santiago', ?)
            """.trimIndent(),
            arrayOf(email, password, role)
        )
    }
}
