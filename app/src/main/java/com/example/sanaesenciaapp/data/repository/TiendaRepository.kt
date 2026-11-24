package com.example.sanaesenciaapp.data.repository

import androidx.room.withTransaction
import com.example.sanaesenciaapp.data.remote.CrearVentaRequest
import com.example.sanaesenciaapp.data.remote.DetalleVentaItemRequest
import com.example.sanaesenciaapp.data.remote.RetrofitClient
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.DetalleVentaEntity
import com.example.sanaesenciaapp.data.room.ProductoEntity
import com.example.sanaesenciaapp.data.room.VentaEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class LineaCompra(
    val productoId: Int,
    val cantidad: Int
)

data class ResultadoCompra(
    val exito: Boolean,
    val mensaje: String,
    val ventaId: Int? = null,
    val total: Int = 0
)

interface TiendaRepositoryContract {
    val productos: kotlinx.coroutines.flow.Flow<List<ProductoEntity>>
    val todosLosProductos: kotlinx.coroutines.flow.Flow<List<ProductoEntity>>
    suspend fun guardarProducto(producto: ProductoEntity)
    suspend fun eliminarProducto(id: Int)
    suspend fun actualizarStock(id: Int, nuevoStock: Int)
    suspend fun registrarCompra(
        email: String,
        nombre: String,
        metodoPago: String,
        lineas: List<LineaCompra>
    ): ResultadoCompra
}

class TiendaRepository(private val database: AppDatabase) : TiendaRepositoryContract {

    private val productoDao = database.productoDao()
    private val ventaDao = database.ventaDao()
    private val sincronizacionMutex = Mutex()

    override val productos = productoDao.obtenerActivosFlow()
        .onStart { sincronizarProductosDesdeServidor() }

    override val todosLosProductos = productoDao.obtenerTodosFlow()
        .onStart { sincronizarProductosDesdeServidor() }

    suspend fun sincronizarProductosDesdeServidor() = sincronizacionMutex.withLock {
        try {
            val response = RetrofitClient.tiendaApi.listarProductos(soloActivos = false)
            if (response.isSuccessful) {
                val catalogoRemoto = response.body().orEmpty().distinctBy { it.id }
                database.withTransaction {
                    productoDao.eliminarTodos()
                    if (catalogoRemoto.isNotEmpty()) productoDao.guardarTodos(catalogoRemoto)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Si no hay conexión, Room conserva la última copia sincronizada.
        }
    }

    override suspend fun guardarProducto(producto: ProductoEntity) {
        val response = if (producto.id > 0) {
            RetrofitClient.tiendaApi.actualizarProducto(producto.id, producto)
        } else {
            RetrofitClient.tiendaApi.crearProducto(producto.copy(id = 0))
        }
        if (!response.isSuccessful) {
            val detalle = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
            throw IllegalStateException("No se pudo guardar el producto (HTTP ${response.code()}): ${detalle ?: response.message()}")
        }
        val guardadoRemoto = response.body()
            ?: throw IllegalStateException("El servidor no devolvió el producto guardado.")
        productoDao.guardar(guardadoRemoto)
    }

    override suspend fun eliminarProducto(id: Int) {
        val actual = productoDao.obtenerPorId(id)
            ?: throw IllegalStateException("No se encontró el producto que deseas quitar.")
        val response = RetrofitClient.tiendaApi.actualizarProducto(id, actual.copy(activo = false))
        if (!response.isSuccessful) {
            val detalle = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
            throw IllegalStateException("No se pudo actualizar el producto (HTTP ${response.code()}): ${detalle ?: response.message()}")
        }
        val actualizado = response.body() ?: actual.copy(activo = false)
        productoDao.guardar(actualizado)
    }

    override suspend fun actualizarStock(id: Int, nuevoStock: Int) {
        val actual = productoDao.obtenerPorId(id)
            ?: throw IllegalStateException("No se encontró el producto.")
        val response = RetrofitClient.tiendaApi.actualizarProducto(id, actual.copy(stock = nuevoStock.coerceAtLeast(0)))
        if (!response.isSuccessful) {
            val detalle = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
            throw IllegalStateException("No se pudo actualizar el stock (HTTP ${response.code()}): ${detalle ?: response.message()}")
        }
        productoDao.guardar(response.body() ?: actual.copy(stock = nuevoStock.coerceAtLeast(0)))
    }

    override suspend fun registrarCompra(
        email: String,
        nombre: String,
        metodoPago: String,
        lineas: List<LineaCompra>
    ): ResultadoCompra {
        if (email.isBlank()) {
            return ResultadoCompra(false, "Debes iniciar sesión para comprar")
        }
        if (lineas.isEmpty() || lineas.any { it.cantidad <= 0 }) {
            return ResultadoCompra(false, "El carrito está vacío")
        }

        val lineasConsolidadas = lineas.groupBy { it.productoId }
            .map { (id, items) -> LineaCompra(id, items.sumOf { it.cantidad }) }
        val request = CrearVentaRequest(
            compradorEmail = email.trim().lowercase(),
            compradorNombre = nombre.trim(),
            metodoPago = metodoPago,
            detalles = lineasConsolidadas.map { DetalleVentaItemRequest(it.productoId, it.cantidad) }
        )

        return try {
            val response = RetrofitClient.tiendaApi.registrarVenta(request)

            when {
                response.isSuccessful -> {
                    val ventaRemota = response.body()
                        ?: return ResultadoCompra(false, "Respuesta vacía del servidor")

                    database.withTransaction {
                        for (det in ventaRemota.detalles) {
                            productoDao.descontarStock(det.productoId, det.cantidad)
                        }

                        val ventaIdLocal = ventaDao.insertarVenta(
                            VentaEntity(
                                compradorEmail = ventaRemota.compradorEmail,
                                compradorNombre = ventaRemota.compradorNombre,
                                fechaMillis = ventaRemota.fechaMillis,
                                total = ventaRemota.total,
                                estado = ventaRemota.estado,
                                metodoPago = ventaRemota.metodoPago
                            )
                        ).toInt()

                        ventaDao.insertarDetalles(
                            ventaRemota.detalles.map { det ->
                                DetalleVentaEntity(
                                    ventaId = ventaIdLocal,
                                    productoId = det.productoId,
                                    nombreProducto = det.nombreProducto,
                                    precioUnitario = det.precioUnitario,
                                    cantidad = det.cantidad,
                                    subtotal = det.subtotal
                                )
                            }
                        )
                    }

                    ResultadoCompra(
                        exito = true,
                        mensaje = "¡Compra realizada correctamente!",
                        ventaId = ventaRemota.id,
                        total = ventaRemota.total
                    )
                }

                response.code() == 400 -> {
                    val errorMsg = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                        ?: "No se pudo validar el stock de los productos"
                    ResultadoCompra(false, errorMsg)
                }

                else -> ResultadoCompra(false, "No se pudo registrar la compra en el servidor (HTTP ${response.code()}).")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoCompra(false, e.message ?: "No hay conexión con el servidor de la tienda. Intenta nuevamente.")
        }
    }

}
