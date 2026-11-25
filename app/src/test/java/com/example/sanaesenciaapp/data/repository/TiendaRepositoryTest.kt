package com.example.sanaesenciaapp.data.repository

import com.example.sanaesenciaapp.data.remote.RetrofitClient
import com.example.sanaesenciaapp.data.remote.TokenManager
import com.example.sanaesenciaapp.data.room.DetalleVentaEntity
import com.example.sanaesenciaapp.data.room.ProductoDao
import com.example.sanaesenciaapp.data.room.ProductoEntity
import com.example.sanaesenciaapp.data.room.VentaDao
import com.example.sanaesenciaapp.data.room.VentaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class TiendaRepositoryTest {

    private class FakeProductoDao : ProductoDao {
        val productos = MutableStateFlow<List<ProductoEntity>>(emptyList())
        private var nextId = 1

        override fun obtenerTodosFlow(): Flow<List<ProductoEntity>> = productos
        override fun obtenerActivosFlow(): Flow<List<ProductoEntity>> =
            productos.map { lista -> lista.filter { it.activo } }
        override suspend fun obtenerPorId(id: Int): ProductoEntity? =
            productos.value.firstOrNull { it.id == id }
        override suspend fun guardar(producto: ProductoEntity): Long {
            val conId = if (producto.id > 0) producto else producto.copy(id = nextId++)
            productos.value = productos.value.filterNot { it.id == conId.id } + conId
            return conId.id.toLong()
        }
        override suspend fun guardarTodos(productos: List<ProductoEntity>) {
            this.productos.value = productos
        }
        override suspend fun eliminarTodos() {
            productos.value = emptyList()
        }
        override suspend fun eliminarPorId(id: Int): Int {
            val antes = productos.value.size
            productos.value = productos.value.filterNot { it.id == id }
            return if (productos.value.size < antes) 1 else 0
        }
        override suspend fun actualizarStock(id: Int, nuevoStock: Int): Int {
            productos.value = productos.value.map {
                if (it.id == id) it.copy(stock = nuevoStock) else it
            }
            return 1
        }
        override suspend fun descontarStock(id: Int, cantidad: Int): Int {
            var afectado = 0
            productos.value = productos.value.map {
                if (it.id == id && it.activo && it.stock >= cantidad) {
                    afectado = 1
                    it.copy(stock = it.stock - cantidad)
                } else it
            }
            return afectado
        }
    }

    private class FakeVentaDao : VentaDao {
        val ventas = mutableListOf<VentaEntity>()
        val detalles = mutableListOf<DetalleVentaEntity>()
        private var nextId = 100

        override suspend fun insertarVenta(venta: VentaEntity): Long {
            val guardada = venta.copy(id = nextId++)
            ventas.add(guardada)
            return guardada.id.toLong()
        }
        override suspend fun insertarDetalles(detalles: List<DetalleVentaEntity>) {
            this.detalles.addAll(detalles)
        }
    }

    private fun producto(id: Int = 1, stock: Int = 10, activo: Boolean = true) = ProductoEntity(
        id = id,
        nombre = "Aceite $id",
        descripcion = "Desc",
        categoria = "Aromaterapia",
        precio = 5000,
        stock = stock,
        icono = "🌿",
        activo = activo
    )

    private fun configurarRetrofit(interceptor: Interceptor) {
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        RetrofitClient.configurarParaPruebas("http://localhost/", client, TokenManager(null))
    }

    @Before
    fun setUp() {
        RetrofitClient.resetParaPruebas()
    }

    @After
    fun tearDown() {
        RetrofitClient.resetParaPruebas()
    }

    @Test
    fun `operaciones CRUD de productos funcionan en modo local sin Retrofit`() = runBlocking {
        val productoDao = FakeProductoDao()
        val repo = TiendaRepository(productoDao, FakeVentaDao(), null)

        repo.guardarProducto(producto(0, stock = 5))
        assertEquals(1, repo.todosLosProductos.first().size)
        assertEquals(1, repo.productos.first().size)

        repo.actualizarStock(1, -3)
        assertEquals(0, productoDao.obtenerPorId(1)?.stock)

        repo.eliminarProducto(1)
        assertFalse(productoDao.obtenerPorId(1)!!.activo)
        assertTrue(repo.productos.first().isEmpty())
    }

    @Test
    fun `sincronizacion y CRUD con Retrofit exitoso actualizan Room`() = runBlocking {
        configurarRetrofit { chain ->
            val path = chain.request().url.encodedPath
            val method = chain.request().method
            val json = when {
                path.endsWith("/api/productos") && method == "GET" ->
                    """[{"id":1,"nombre":"Lavanda","descripcion":"D","categoria":"Aroma","precio":8990,"stock":12,"icono":"💜","activo":true}]"""
                path.endsWith("/api/productos") && method == "POST" ->
                    """{"id":2,"nombre":"Vela","descripcion":"D","categoria":"Bienestar","precio":6490,"stock":8,"icono":"🕯️","activo":true}"""
                path.endsWith("/api/productos/1") && method == "PUT" ->
                    """{"id":1,"nombre":"Lavanda","descripcion":"D","categoria":"Aroma","precio":8990,"stock":20,"icono":"💜","activo":false}"""
                else -> "{}"
            }
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(json.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val productoDao = FakeProductoDao()
        val repo = TiendaRepository(productoDao, FakeVentaDao(), null)

        repo.sincronizarProductosDesdeServidor()
        assertEquals("Lavanda", productoDao.obtenerPorId(1)?.nombre)

        repo.guardarProducto(producto(0))
        assertEquals("Vela", productoDao.obtenerPorId(2)?.nombre)

        repo.actualizarStock(1, 20)
        assertEquals(20, productoDao.obtenerPorId(1)?.stock)

        repo.eliminarProducto(1)
        assertFalse(productoDao.obtenerPorId(1)!!.activo)
    }

    @Test
    fun `guardarProducto actualizarStock y eliminarProducto lanzan excepcion si HTTP falla`() {
        configurarRetrofit { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(403)
                .message("Forbidden")
                .body("Sin permiso".toResponseBody("text/plain".toMediaType()))
                .build()
        }

        val productoDao = FakeProductoDao().apply {
            runBlocking { guardar(producto(1)) }
        }
        val repo = TiendaRepository(productoDao, FakeVentaDao(), null)

        assertThrows(IllegalStateException::class.java) {
            runBlocking { repo.guardarProducto(producto(1)) }
        }
        assertThrows(IllegalStateException::class.java) {
            runBlocking { repo.actualizarStock(1, 5) }
        }
        assertThrows(IllegalStateException::class.java) {
            runBlocking { repo.eliminarProducto(1) }
        }
        assertThrows(IllegalStateException::class.java) {
            runBlocking { repo.eliminarProducto(999) }
        }
    }

    @Test
    fun `registrarCompra valida entradas y maneja HTTP 200 400 500 y excepcion`() = runBlocking {
        val productoDao = FakeProductoDao().apply { guardar(producto(1, stock = 10)) }
        val ventaDao = FakeVentaDao()
        val repo = TiendaRepository(productoDao, ventaDao, null)

        assertFalse(repo.registrarCompra("", "Ana", "Tarjeta", listOf(LineaCompra(1, 1))).exito)
        assertFalse(repo.registrarCompra("ana@cl.cl", "Ana", "Tarjeta", emptyList()).exito)
        assertFalse(repo.registrarCompra("ana@cl.cl", "Ana", "Tarjeta", listOf(LineaCompra(1, 0))).exito)

        // HTTP 200 con consolidación de líneas
        configurarRetrofit { chain ->
            val json = """
                {
                  "id": 77,
                  "compradorEmail": "ana@cl.cl",
                  "compradorNombre": "Ana",
                  "fechaMillis": 123456,
                  "total": 15000,
                  "estado": "PAGADA",
                  "metodoPago": "Tarjeta",
                  "detalles": [
                    {"id":1,"ventaId":77,"productoId":1,"nombreProducto":"Aceite 1","precioUnitario":5000,"cantidad":3,"subtotal":15000}
                  ]
                }
            """.trimIndent()
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(201)
                .message("Created")
                .body(json.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val ok = repo.registrarCompra(
            "ANA@CL.CL",
            "Ana",
            "Tarjeta",
            listOf(LineaCompra(1, 1), LineaCompra(1, 2))
        )
        assertTrue(ok.exito)
        assertEquals(77, ok.ventaId)
        assertEquals(15000, ok.total)
        assertEquals(7, productoDao.obtenerPorId(1)?.stock)
        assertEquals(1, ventaDao.ventas.size)
        assertEquals(1, ventaDao.detalles.size)

        // HTTP 400
        configurarRetrofit { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(400)
                .message("Bad Request")
                .body("Stock insuficiente".toResponseBody("text/plain".toMediaType()))
                .build()
        }
        val badReq = repo.registrarCompra("ana@cl.cl", "Ana", "Tarjeta", listOf(LineaCompra(1, 1)))
        assertFalse(badReq.exito)
        assertEquals("Stock insuficiente", badReq.mensaje)

        // HTTP 500
        configurarRetrofit { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Error")
                .body("fallo".toResponseBody("text/plain".toMediaType()))
                .build()
        }
        val err500 = repo.registrarCompra("ana@cl.cl", "Ana", "Tarjeta", listOf(LineaCompra(1, 1)))
        assertFalse(err500.exito)
        assertTrue(err500.mensaje.contains("500"))

        // Red
        configurarRetrofit { throw IOException("timeout") }
        val errRed = repo.registrarCompra("ana@cl.cl", "Ana", "Tarjeta", listOf(LineaCompra(1, 1)))
        assertFalse(errRed.exito)

        // Sincronizacion fallida (HTTP 500 y error de red) no borra datos existentes
        configurarRetrofit { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(500).message("Err").body("".toResponseBody("text/plain".toMediaType())).build()
        }
        repo.sincronizarProductosDesdeServidor()
        configurarRetrofit { throw IOException("sin red") }
        repo.sincronizarProductosDesdeServidor()
        assertEquals(1, productoDao.productos.value.size)

        // Actualizar producto existente via PUT (id > 0)
        configurarRetrofit { chain ->
            val json = """{"id":1,"nombre":"Aceite Renovado","descripcion":"D","categoria":"Aroma","precio":5500,"stock":9,"icono":"🌿","activo":true}"""
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK").body(json.toResponseBody("application/json".toMediaType())).build()
        }
        repo.guardarProducto(producto(1))
        assertEquals("Aceite Renovado", productoDao.obtenerPorId(1)?.nombre)
    }
}
