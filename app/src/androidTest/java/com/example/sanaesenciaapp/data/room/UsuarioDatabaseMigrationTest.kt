package com.example.sanaesenciaapp.data.room

import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
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
