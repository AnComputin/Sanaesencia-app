package com.example.sanaesenciaapp.data.room

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsuarioDatabaseMigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetContext = instrumentation.targetContext

    @get:Rule
    val helper = MigrationTestHelper(instrumentation, AppDatabase::class.java)

    @Test
    fun migracionesProductivasDesdeVersion6Hasta8ConservanDatosYTablas() {
        val databaseName = "room-migration-v6-to-v8"
        targetContext.deleteDatabase(databaseName)

        val oldDatabase = helper.createDatabase(databaseName, 6)
        insertarUsuario(oldDatabase, "Persona@dominio.cl")
        insertarUsuario(oldDatabase, "  legado@dominio.cl ")
        insertarCita(oldDatabase, "Persona@dominio.cl")
        insertarBeneficiario(oldDatabase, "persona@dominio.cl")
        oldDatabase.close()

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            8,
            true,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8
        )

        try {
            assertEquals(8, version(migrated))
            assertEquals(2, contar(migrated, "usuarios"))
            assertEquals(1, contar(migrated, "citas"))
            assertEquals(1, contar(migrated, "beneficiarios"))
            assertEquals(6, contar(migrated, "productos"))
            assertTrue(existeTabla(migrated, "ventas"))
            assertTrue(existeTabla(migrated, "detalle_ventas"))
            assertEquals(
                "hash-original",
                migrated.query(
                    "SELECT password FROM usuarios WHERE email = ?",
                    arrayOf("persona@dominio.cl")
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getString(0)
                }
            )
            assertEquals(
                "Persona@dominio.cl",
                migrated.query(
                    "SELECT email FROM usuarios WHERE email = ?",
                    arrayOf("persona@dominio.cl")
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getString(0)
                }
            )
            assertEquals(
                "  legado@dominio.cl ",
                migrated.query(
                    "SELECT email FROM usuarios WHERE email LIKE '%legado@dominio.cl%'"
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getString(0)
                }
            )
            val usuariosSql = migrated.query(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'usuarios'"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                cursor.getString(0)
            }
            assertTrue(usuariosSql.contains("COLLATE NOCASE", ignoreCase = true))

            migrated.execSQL(
                """
                INSERT OR IGNORE INTO usuarios (
                    email, nombre, password, rut, telefono, fechaNacimiento, comuna, rol
                ) VALUES ('PERSONA@DOMINIO.CL', 'Duplicado', 'otro-hash', '', '', '', '', 'ADMIN')
                """.trimIndent()
            )
            assertEquals(2, contar(migrated, "usuarios"))
            assertEquals(
                "Persona",
                migrated.query(
                    "SELECT nombre FROM usuarios WHERE email = ?",
                    arrayOf("persona@dominio.cl")
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getString(0)
                }
            )
            assertEquals(
                "Paciente de prueba",
                migrated.query(
                    "SELECT pacienteNombre FROM citas WHERE id = 1"
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getString(0)
                }
            )
            assertEquals(
                "Beneficiario de prueba",
                migrated.query(
                    "SELECT nombre FROM beneficiarios WHERE id = 1"
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getString(0)
                }
            )
        } finally {
            migrated.close()
            targetContext.deleteDatabase(databaseName)
        }
    }

    @Test
    fun migracionProductivaDeVersion7a8ConservaLosDatosExistentes() {
        val databaseName = "room-migration-v7-to-v8"
        targetContext.deleteDatabase(databaseName)

        val oldDatabase = helper.createDatabase(databaseName, 7)
        insertarUsuario(oldDatabase, "persona@dominio.cl")
        insertarCita(oldDatabase, "persona@dominio.cl")
        insertarBeneficiario(oldDatabase, "persona@dominio.cl")
        oldDatabase.close()

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            8,
            true,
            AppDatabase.MIGRATION_7_8
        )

        try {
            assertEquals(8, version(migrated))
            assertEquals(1, contar(migrated, "usuarios"))
            assertEquals(1, contar(migrated, "citas"))
            assertEquals(1, contar(migrated, "beneficiarios"))
            assertEquals(6, contar(migrated, "productos"))
            assertTrue(existeTabla(migrated, "ventas"))
            assertTrue(existeTabla(migrated, "detalle_ventas"))
        } finally {
            migrated.close()
            targetContext.deleteDatabase(databaseName)
        }
    }

    @Test
    fun migracionNoDisponibleFallaYConservaLaBaseOriginal() {
        val databaseName = "room-migration-missing-v7-to-v8"
        targetContext.deleteDatabase(databaseName)

        val oldDatabase = helper.createDatabase(databaseName, 7)
        insertarUsuario(oldDatabase, "persona@dominio.cl")
        oldDatabase.close()

        val roomDatabase = Room.databaseBuilder(
            targetContext,
            AppDatabase::class.java,
            databaseName
        ).build()

        var failure: Throwable? = null
        try {
            roomDatabase.openHelper.writableDatabase
        } catch (error: Throwable) {
            failure = error
        } finally {
            roomDatabase.close()
        }

        assertNotNull("Room debía fallar cuando no se registra la migración 7→8", failure)
        val errorText = generateSequence(failure) { it.cause }
            .joinToString(" ") { it.message.orEmpty() }
        assertTrue(
            "El error debe identificar la ruta de migración ausente",
            errorText.contains("migration from 7 to 8", ignoreCase = true)
        )

        val rawDatabase = SQLiteDatabase.openDatabase(
            targetContext.getDatabasePath(databaseName).path,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
        try {
            assertEquals(
                7,
                rawDatabase.rawQuery("PRAGMA user_version", null).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getInt(0)
                }
            )
            assertEquals(
                1,
                rawDatabase.rawQuery("SELECT COUNT(*) FROM usuarios", null).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getInt(0)
                }
            )
            assertEquals(
                "hash-original",
                rawDatabase.rawQuery(
                    "SELECT password FROM usuarios WHERE email = 'persona@dominio.cl'",
                    null
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    cursor.getString(0)
                }
            )
            assertEquals(
                0,
                rawDatabase.rawQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'productos'",
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

    @Test
    fun colisionEnMigracion6a7RevierteSinEliminarRegistros() {
        val databaseName = "room-migration-v6-collision"
        targetContext.deleteDatabase(databaseName)

        val oldDatabase = helper.createDatabase(databaseName, 6)
        insertarUsuario(oldDatabase, "Persona@dominio.cl")
        insertarUsuario(oldDatabase, "persona@dominio.cl")
        oldDatabase.close()

        val roomDatabase = Room.databaseBuilder(
            targetContext,
            AppDatabase::class.java,
            databaseName
        )
            .addMigrations(AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8)
            .build()

        var failure: Throwable? = null
        try {
            roomDatabase.openHelper.writableDatabase
        } catch (error: Throwable) {
            failure = error
        } finally {
            roomDatabase.close()
        }

        assertNotNull("La colisión de correo debe abortar la migración", failure)
        val errorText = generateSequence(failure) { it.cause }
            .joinToString(" ") { it.message.orEmpty() }
        assertTrue(errorText.contains("collide under NOCASE"))
        assertFalse("El error no debe exponer el correo en conflicto", errorText.contains("Persona@dominio.cl"))

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

    private fun insertarUsuario(database: SupportSQLiteDatabase, email: String) {
        database.execSQL(
            """
            INSERT INTO usuarios (
                email, nombre, password, rut, telefono, fechaNacimiento, comuna, rol
            ) VALUES (?, 'Persona', 'hash-original', '12.345.678-9',
                      '+56912345678', '01/01/1990', 'Santiago', 'PACIENTE')
            """.trimIndent(),
            arrayOf(email)
        )
    }

    private fun insertarCita(database: SupportSQLiteDatabase, email: String) {
        database.execSQL(
            """
            INSERT INTO citas (
                id, usuarioEmail, especialidadId, especialidadNombre,
                especialistaId, especialistaNombre, modalidad, inicioMillis,
                finMillis, hora, pacienteNombre, beneficiarioId, estado, boxAsignado
            ) VALUES (1, ?, 1, 'Especialidad', 1, 'Profesional', 'ONLINE',
                      1000, 2000, '10:00', 'Paciente de prueba', NULL,
                      'CONFIRMADA', NULL)
            """.trimIndent(),
            arrayOf(email)
        )
    }

    private fun insertarBeneficiario(database: SupportSQLiteDatabase, email: String) {
        database.execSQL(
            """
            INSERT INTO beneficiarios (
                id, propietarioEmail, nombre, rut, fechaNacimiento, parentesco, activo
            ) VALUES (1, ?, 'Beneficiario de prueba', '98.765.432-1',
                      '01/01/2010', 'Hijo', 1)
            """.trimIndent(),
            arrayOf(email)
        )
    }

    private fun version(database: SupportSQLiteDatabase): Int =
        database.query("PRAGMA user_version").use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getInt(0)
        }

    private fun contar(database: SupportSQLiteDatabase, tabla: String): Int =
        database.query("SELECT COUNT(*) FROM `$tabla`").use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getInt(0)
        }

    private fun existeTabla(database: SupportSQLiteDatabase, tabla: String): Boolean =
        database.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(tabla)
        ).use { cursor -> cursor.moveToFirst() }
}
