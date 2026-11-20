package com.example.sanaesenciaapp.data.repository

import androidx.room.withTransaction
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.DetalleVentaEntity
import com.example.sanaesenciaapp.data.room.ProductoEntity
import com.example.sanaesenciaapp.data.room.VentaEntity

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

class TiendaRepository(private val database: AppDatabase) {

    private val productoDao = database.productoDao()
    private val ventaDao = database.ventaDao()

    val productos = productoDao.obtenerActivosFlow()
    val todosLosProductos = productoDao.obtenerTodosFlow()

    suspend fun guardarProducto(producto: ProductoEntity) {
        productoDao.guardar(producto)
    }

    suspend fun eliminarProducto(id: Int) {
        productoDao.eliminarPorId(id)
    }

    suspend fun actualizarStock(id: Int, nuevoStock: Int) {
        productoDao.actualizarStock(id, nuevoStock.coerceAtLeast(0))
    }

    suspend fun registrarCompra(
        email: String,
        nombre: String,
        metodoPago: String,
        lineas: List<LineaCompra>
    ): ResultadoCompra = database.withTransaction {
        if (email.isBlank()) {
            return@withTransaction ResultadoCompra(false, "Debes iniciar sesión para comprar")
        }
        if (lineas.isEmpty() || lineas.any { it.cantidad <= 0 }) {
            return@withTransaction ResultadoCompra(false, "El carrito está vacío")
        }

        val productosActuales = mutableListOf<Pair<ProductoEntity, Int>>()
        lineas.forEach { linea ->
            val producto = productoDao.obtenerPorId(linea.productoId)
                ?: return@withTransaction ResultadoCompra(
                    false,
                    "Uno de los productos ya no está disponible. Actualiza el carrito."
                )

            if (!producto.activo) {
                return@withTransaction ResultadoCompra(
                    false,
                    "${producto.nombre} ya no está disponible. Actualiza el carrito."
                )
            }
            if (producto.stock < linea.cantidad) {
                return@withTransaction ResultadoCompra(
                    false,
                    "Stock insuficiente para ${producto.nombre}. Disponible: ${producto.stock}."
                )
            }
            productosActuales += producto to linea.cantidad
        }

        val totalLong = productosActuales.sumOf { (producto, cantidad) ->
            producto.precio.toLong() * cantidad
        }
        if (totalLong <= 0L || totalLong > Int.MAX_VALUE) {
            return@withTransaction ResultadoCompra(false, "No se pudo calcular el total de la compra")
        }

        // Se descuenta el stock dentro de la misma transacción que guarda la venta.
        // Si alguna operación falla, Room revierte todas las modificaciones.
        for ((producto, cantidad) in productosActuales) {
            val filasActualizadas = productoDao.descontarStock(producto.id, cantidad)
            if (filasActualizadas != 1) {
                error("No fue posible reservar el stock de ${producto.nombre}")
            }
        }

        val ventaId = ventaDao.insertarVenta(
            VentaEntity(
                compradorEmail = email.trim(),
                compradorNombre = nombre.trim(),
                fechaMillis = System.currentTimeMillis(),
                total = totalLong.toInt(),
                estado = "PAGADO_SIMULADO",
                metodoPago = metodoPago
            )
        ).toInt()

        ventaDao.insertarDetalles(
            productosActuales.map { (producto, cantidad) ->
                DetalleVentaEntity(
                    ventaId = ventaId,
                    productoId = producto.id,
                    nombreProducto = producto.nombre,
                    precioUnitario = producto.precio,
                    cantidad = cantidad,
                    subtotal = producto.precio * cantidad
                )
            }
        )

        ResultadoCompra(
            exito = true,
            mensaje = "¡Compra simulada realizada correctamente!",
            ventaId = ventaId,
            total = totalLong.toInt()
        )
    }
}
