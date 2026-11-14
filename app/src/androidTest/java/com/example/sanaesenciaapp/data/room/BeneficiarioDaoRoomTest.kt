package com.example.sanaesenciaapp.data.room

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BeneficiarioDaoRoomTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: BeneficiarioDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.beneficiarioDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun consultaFiltraPorPropietarioActivosCorreoYOrdenaSinDistinguirMayusculas() = runBlocking {
        assertTrue(dao.insertar(beneficiario("Ana@dominio.cl", "zeta", rut = "11.111.111-1")) > 0L)
        assertTrue(dao.insertar(beneficiario("otro@dominio.cl", "Ajeno", rut = "22.222.222-2")) > 0L)
        assertTrue(dao.insertar(beneficiario("ana@dominio.cl", "Beta", rut = "33.333.333-3")) > 0L)
        assertTrue(dao.insertar(beneficiario("ANA@DOMINIO.CL", "Inactivo", rut = "44.444.444-4", activo = false)) > 0L)

        val encontrados = dao.obtenerPorPropietario("  aNa@DOMINIO.CL ")
        assertEquals(listOf("Beta", "zeta"), encontrados.map { it.nombre })
        assertEquals(listOf("Beta", "zeta"), dao.obtenerPorPropietarioFlow("ANA@dominio.cl").first().map { it.nombre })
    }

    @Test
    fun consultarPorIdRequierePropietarioYRegistroActivo() = runBlocking {
        val id = dao.insertar(beneficiario("ana@dominio.cl", "Propio")).toInt()
        val inactivoId = dao.insertar(beneficiario("ana@dominio.cl", "Inactivo", activo = false)).toInt()

        assertNotNull(dao.obtenerPorIdYPropietario(id, "ANA@DOMINIO.CL"))
        assertNull(dao.obtenerPorIdYPropietario(id, "otro@dominio.cl"))
        assertNull(dao.obtenerPorIdYPropietario(inactivoId, "ana@dominio.cl"))
    }

    @Test
    fun buscarRutIncluyeInactivosYReconoceFormatoHistoricoAlmacenado() = runBlocking {
        insertarHistoricoDirecto("ana@dominio.cl", "Histórico", "12.345.678-k", activo = false)
        insertarHistoricoDirecto("otro@dominio.cl", "Ajeno", "12.345.678-K", activo = true)

        val filaHistorica = filasPropietario("ana@dominio.cl").single()
        assertEquals("12.345.678-k", filaHistorica.rut)
        assertTrue(!filaHistorica.activo)

        val encontrados = dao.buscarPorRutYPropietario("ANA@DOMINIO.CL", " 12.345.678-K ")
        assertEquals(1, encontrados.size)
        assertEquals("Histórico", encontrados.single().nombre)
    }

    @Test
    fun registrarInsertaYNoDuplicaActivo() = runBlocking {
        val entrada = beneficiario(" ANA@DOMINIO.CL ", "Original", rut = "12.345.678-k")
        assertEquals(ResultadoRegistroBeneficiario.INSERTADO, dao.registrarOReactivar(entrada))
        assertEquals(ResultadoRegistroBeneficiario.YA_EXISTE_ACTIVO, dao.registrarOReactivar(entrada.copy(nombre = "No reemplazar")))

        val registros = filasPropietario("ana@dominio.cl")
        assertEquals(1, registros.size)
        assertEquals("Original", registros.single().nombre)
        assertEquals("ana@dominio.cl", registros.single().propietarioEmail)
        assertEquals("12345678-K", registros.single().rut)
    }

    @Test
    fun reactivarConservaIdYDatosDelRegistro() = runBlocking {
        val solicitudInicial = beneficiario("ana@dominio.cl", "Datos originales", rut = "12.345.678-K")
        val id = dao.insertar(solicitudInicial)
        assertTrue(id > 0L)

        val almacenadoAntes = filasPropietario("ana@dominio.cl").single()
        assertEquals(id.toInt(), almacenadoAntes.id)
        assertEquals("12345678-K", almacenadoAntes.rut)
        assertTrue(almacenadoAntes.activo)
        assertEquals(1, dao.desactivar(id.toInt(), "ana@dominio.cl"))

        val desactivado = dao.buscarPorRutYPropietario("ana@dominio.cl", solicitudInicial.rut).single()
        assertEquals(almacenadoAntes.copy(activo = false), desactivado)

        val resultado = dao.registrarOReactivar(
            solicitudInicial.copy(
                nombre = "No sobrescribir",
                rut = "12345678-K",
                fechaNacimiento = "1999-12-31",
                parentesco = "Otro parentesco"
            )
        )

        assertEquals(ResultadoRegistroBeneficiario.REACTIVADO, resultado)
        val restaurado = dao.buscarPorRutYPropietario("ana@dominio.cl", solicitudInicial.rut).single()
        assertEquals(id.toInt(), restaurado.id)
        assertEquals(almacenadoAntes.copy(activo = true), restaurado)
        assertEquals(1, filasPropietario("ana@dominio.cl").size)
    }

    @Test
    fun multiplesCoincidenciasHistoricasNoSeModificanNiSeEligeUna() = runBlocking {
        insertarHistoricoDirecto("ana@dominio.cl", "Uno", "12.345.678-k", activo = true)
        insertarHistoricoDirecto("ANA@DOMINIO.CL", "Dos", "12345678-K", activo = false)

        val antes = filasPropietario("ana@dominio.cl").associateBy { it.id }
        assertEquals(2, antes.size)
        assertEquals(setOf("12.345.678-k", "12345678-K"), antes.values.map { it.rut }.toSet())

        val coincidencias = dao.buscarPorRutYPropietario("Ana@dominio.cl", " 12.345.678-K ")
        assertEquals(2, coincidencias.size)
        assertEquals(setOf("Uno", "Dos"), coincidencias.map { it.nombre }.toSet())

        val resultado = dao.registrarOReactivar(
            beneficiario("ana@dominio.cl", "Tercero", rut = "12.345.678-K")
        )
        assertEquals(ResultadoRegistroBeneficiario.INCONSISTENCIA_DATOS, resultado)

        val despues = filasPropietario("ana@dominio.cl").associateBy { it.id }
        assertEquals(antes, despues)
        assertEquals(2, despues.size)
    }

    @Test
    fun actualizarDesactivarYReactivarExigenPropietario() = runBlocking {
        val id = dao.insertar(beneficiario("ana@dominio.cl", "Original")).toInt()

        assertEquals(0, dao.desactivar(id, "otro@dominio.cl"))
        assertEquals(1, dao.desactivar(id, "ANA@dominio.cl"))
        assertEquals(0, dao.desactivar(id, "ana@dominio.cl"))
        assertEquals(0, dao.reactivar(id, "otro@dominio.cl"))
        assertEquals(1, dao.reactivar(id, "ana@dominio.cl"))
        assertEquals(1, dao.actualizar(id, "ana@dominio.cl", "Actualizado", "12345678-K", "2000-01-01", "Hija"))
        assertEquals(0, dao.actualizar(id, "otro@dominio.cl", "Ajeno", "98765432-1", "2000-01-01", "Otro"))

        val actual = dao.obtenerPorIdYPropietario(id, "ana@dominio.cl")
        assertEquals("Actualizado", actual?.nombre)
        assertEquals("ana@dominio.cl", actual?.propietarioEmail)
        assertEquals("Hija", actual?.parentesco)
    }

    @Test
    fun actualizarRechazaRutsEquivalentesDeOtroRegistro() = runBlocking {
        val originalId = dao.insertar(beneficiario("ana@dominio.cl", "Original")).toInt()
        dao.insertar(beneficiario("ana@dominio.cl", "Existente", rut = "22.222.222-2"))

        assertEquals(
            0,
            dao.actualizar(
                originalId,
                "ana@dominio.cl",
                "No actualizar",
                "22.222.222-2",
                "2000-01-01",
                "Familiar"
            )
        )
        assertEquals("Original", dao.obtenerPorIdYPropietario(originalId, "ana@dominio.cl")?.nombre)
    }

    @Test
    fun insercionIgnoradaSeReportaComoConflicto() = runBlocking {
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER ignorar_beneficiario BEFORE INSERT ON beneficiarios BEGIN SELECT RAISE(IGNORE); END"
        )

        assertEquals(
            ResultadoRegistroBeneficiario.CONFLICTO_INSERCION,
            dao.registrarOReactivar(beneficiario("ana@dominio.cl", "Nuevo"))
        )
        assertTrue(filasPropietario("ana@dominio.cl").isEmpty())
    }

    @Test
    fun entradasVaciasNoAccedenNiCreanPropietariosORutsVacios() = runBlocking {
        val baseline = beneficiario("ana@dominio.cl", "Registro válido", rut = "55.555.555-5")
        val baselineId = dao.insertar(baseline)
        assertTrue("La fila válida de referencia debe insertarse", baselineId > 0L)
        val datosIniciales = filasPropietario("ana@dominio.cl").single()

        val blankOwnerId = database.openHelper.writableDatabase.insert(
            "beneficiarios",
            0,
            android.content.ContentValues().apply {
                put("propietarioEmail", "")
                put("nombre", "Histórico vacío")
                put("rut", "12-3")
                put("fechaNacimiento", "2000-01-01")
                put("parentesco", "Familiar")
                put("activo", 1)
            }
        ).toInt()

        assertTrue(dao.obtenerPorPropietario("  ").isEmpty())
        assertTrue(dao.obtenerPorPropietarioFlow("").first().isEmpty())
        assertTrue(dao.buscarPorRutYPropietario("", "12-3").isEmpty())
        assertNull(dao.obtenerPorIdYPropietario(blankOwnerId, " "))
        assertEquals(0, dao.desactivar(blankOwnerId, ""))
        assertEquals(0, dao.reactivar(blankOwnerId, " "))
        assertEquals(
            0,
            dao.actualizar(blankOwnerId, "", "No tocar", "12-3", "2000-01-01", "Familiar")
        )
        assertEquals(
            ResultadoRegistroBeneficiario.DATOS_INVALIDOS,
            dao.registrarOReactivar(beneficiario("", "Inválido"))
        )
        assertEquals(
            ResultadoRegistroBeneficiario.DATOS_INVALIDOS,
            dao.registrarOReactivar(beneficiario("ana@dominio.cl", "RUT vacío", rut = "...---"))
        )
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { dao.insertar(beneficiario("", "Inválido")) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { dao.insertar(beneficiario("ana@dominio.cl", "RUT vacío", rut = "---...")) }
        }
        assertEquals(listOf(datosIniciales), filasPropietario("ana@dominio.cl"))
        assertEquals(baselineId.toInt(), datosIniciales.id)
        assertEquals(baseline.nombre, datosIniciales.nombre)
        assertEquals("55555555-5", datosIniciales.rut)
    }

    @Test
    fun errorSqliteSePropagaParaQueRepositoryConserveLaCausa() = runBlocking {
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER abortar_beneficiario BEFORE INSERT ON beneficiarios BEGIN SELECT RAISE(ABORT, 'fallo'); END"
        )

        assertThrows(android.database.SQLException::class.java) {
            runBlocking {
                dao.registrarOReactivar(beneficiario("ana@dominio.cl", "Ficticio"))
            }
        }
        assertTrue(filasPropietario("ana@dominio.cl").isEmpty())
    }

    @Test
    fun registroConcurrenteEsAtomico() = runBlocking {
        val entrada = beneficiario("ana@dominio.cl", "Concurrente")
        val resultados = withContext(Dispatchers.IO) {
            listOf(
                async { dao.registrarOReactivar(entrada) },
                async { dao.registrarOReactivar(entrada.copy(nombre = "Otra solicitud")) }
            ).awaitAll()
        }

        assertEquals(1, resultados.count { it == ResultadoRegistroBeneficiario.INSERTADO })
        assertEquals(1, resultados.count { it == ResultadoRegistroBeneficiario.YA_EXISTE_ACTIVO })
        val finales = filasPropietario("ana@dominio.cl")
        assertEquals(1, finales.size)
        assertEquals("ana@dominio.cl", finales.single().propietarioEmail)
        assertTrue(finales.single().activo)
        assertEquals(
            1,
            dao.buscarPorRutYPropietario("ANA@DOMINIO.CL", entrada.rut).size
        )
    }

    private fun filasPropietario(email: String): List<BeneficiarioEntity> =
        database.openHelper.readableDatabase.query(
            """
            SELECT id, propietarioEmail, nombre, rut, fechaNacimiento, parentesco, activo
            FROM beneficiarios
            WHERE TRIM(?) != '' AND LOWER(TRIM(propietarioEmail)) = LOWER(TRIM(?))
            """.trimIndent(),
            arrayOf<Any>(email, email)
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        BeneficiarioEntity(
                            id = cursor.getInt(0),
                            propietarioEmail = cursor.getString(1),
                            nombre = cursor.getString(2),
                            rut = cursor.getString(3),
                            fechaNacimiento = cursor.getString(4),
                            parentesco = cursor.getString(5),
                            activo = cursor.getInt(6) != 0
                        )
                    )
                }
            }
        }

    private fun insertarHistoricoDirecto(
        propietarioEmail: String,
        nombre: String,
        rut: String,
        activo: Boolean
    ) {
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO beneficiarios
                (propietarioEmail, nombre, rut, fechaNacimiento, parentesco, activo)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any>(
                propietarioEmail,
                nombre,
                rut,
                "2000-01-01",
                "Familiar",
                if (activo) 1 else 0
            )
        )
    }

    private fun beneficiario(
        email: String,
        nombre: String,
        rut: String = "11.111.111-1",
        activo: Boolean = true
    ) = BeneficiarioEntity(
        propietarioEmail = email,
        nombre = nombre,
        rut = rut,
        fechaNacimiento = "2000-01-01",
        parentesco = "Familiar",
        activo = activo
    )
}
