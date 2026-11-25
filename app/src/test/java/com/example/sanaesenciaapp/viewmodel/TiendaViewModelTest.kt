package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModelStore
import com.example.sanaesenciaapp.data.repository.LineaCompra
import com.example.sanaesenciaapp.navigation.rutaDetalleProducto
import com.example.sanaesenciaapp.data.repository.ResultadoCompra
import com.example.sanaesenciaapp.data.repository.TiendaRepositoryContract
import com.example.sanaesenciaapp.data.room.ProductoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TiendaViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun catalogoEmiteContenidoVacioYResuelveDetallePorId() = runTest(dispatcher) {
        val producto = producto(7)
        val source = FakeTiendaRepository(listOf(producto))
        val store = ViewModelStore()
        val viewModel = TiendaViewModel(source).also { store.put("tienda", it) }
        try {
            assertEquals(EstadoCatalogoTienda.Cargando, viewModel.estadoCatalogo.value)
            backgroundScope.launch(dispatcher) { viewModel.estadoCatalogo.collect() }
            advanceUntilIdle()
            val contenido = viewModel.estadoCatalogo.value as EstadoCatalogoTienda.Contenido
            assertEquals(producto, contenido.producto(7))
            assertEquals(null, contenido.producto(99))

            source.activos.value = emptyList()
            advanceUntilIdle()
            assertEquals(EstadoCatalogoTienda.Vacio, viewModel.estadoCatalogo.value)
        } finally {
            store.clear()
        }
    }

    @Test
    fun errorDeCargaPermiteReintentarYRecuperarProductos() = runTest(dispatcher) {
        var lecturas = 0
        val producto = producto(2)
        val source = FakeTiendaRepository(flowActivos = {
            flow {
                lecturas++
                if (lecturas == 1) error("fallo de lectura")
                emit(listOf(producto))
            }
        })
        val store = ViewModelStore()
        val viewModel = TiendaViewModel(source).also { store.put("tienda", it) }
        try {
            backgroundScope.launch(dispatcher) { viewModel.estadoCatalogo.collect() }
            advanceUntilIdle()
            assertEquals(EstadoCatalogoTienda.Error, viewModel.estadoCatalogo.value)

            viewModel.reintentarCatalogo()
            advanceUntilIdle()
            val contenido = viewModel.estadoCatalogo.value as EstadoCatalogoTienda.Contenido
            assertEquals(listOf(producto), contenido.productos)
            assertEquals(2, lecturas)
        } finally {
            store.clear()
        }
    }

    @Test
    fun rutaDeDetalleConservaElIdentificadorSeleccionado() {
        assertEquals("producto_detalle/7", rutaDetalleProducto(7))
    }

    @Test
    fun agregarAlCarritoActualizaCantidadYRechazaProductoSinStock() = runTest(dispatcher) {
        val store = ViewModelStore()
        val viewModel = TiendaViewModel(FakeTiendaRepository()).also { store.put("tienda", it) }
        try {
            viewModel.agregarAlCarrito(producto(1))
            viewModel.agregarAlCarrito(producto(1))
            assertEquals(2, viewModel.carrito.value.single().cantidad)

            viewModel.agregarAlCarrito(producto(3, stock = 0))
            assertEquals(1, viewModel.carrito.value.size)
            assertFalse(viewModel.mensaje.value.isNullOrBlank())
        } finally {
            store.clear()
        }
    }

    @Test
    fun operacionesDeCarritoAdminYSimularPagoFuncionanCorrectamente() = runTest(dispatcher) {
        val repo = FakeTiendaRepository(listOf(producto(1, stock = 3), producto(2, stock = 2)))
        val store = ViewModelStore()
        val vm = TiendaViewModel(repo).also { store.put("tienda", it) }
        try {
            backgroundScope.launch(dispatcher) { vm.todosProductos.collect() }
            advanceUntilIdle()

            // Carrito: agregar, aumentar hasta stock, disminuir, quitar, limpiar
            vm.agregarAlCarrito(producto(1, stock = 3))
            vm.aumentarCantidad(1)
            vm.aumentarCantidad(1)
            vm.aumentarCantidad(1) // no supera stock 3
            assertEquals(3, vm.carrito.value.single().cantidad)

            vm.disminuirCantidad(1)
            assertEquals(2, vm.carrito.value.single().cantidad)

            vm.agregarAlCarrito(producto(2, stock = 2))
            vm.quitarDelCarrito(2)
            assertEquals(1, vm.carrito.value.size)

            vm.limpiarMensaje()
            assertEquals(null, vm.mensaje.value)

            // Admin: guardar, actualizarStock, eliminar
            vm.guardarProducto(producto(5, stock = 4))
            vm.actualizarStock(1, 5)
            vm.eliminarProducto(2)
            advanceUntilIdle()
            assertFalse(vm.mensaje.value.isNullOrBlank())

            // Pago con carrito vacío y con carrito cargado
            vm.limpiarCarrito()
            vm.simularPago("ana@cl.cl", "Ana", "Tarjeta")
            assertFalse(vm.estadoPago.value.resultado!!.exito)

            vm.limpiarResultadoPago()
            assertEquals(null, vm.estadoPago.value.resultado)

            vm.agregarAlCarrito(producto(1, stock = 5))
            vm.simularPago("ana@cl.cl", "Ana", "Tarjeta")
            advanceUntilIdle()
            assertEquals(false, vm.estadoPago.value.procesando)
        } finally {
            store.clear()
        }
    }

    private fun producto(id: Int, stock: Int = 4) = ProductoEntity(
        id = id,
        nombre = "Producto $id",
        descripcion = "Descripción disponible en catálogo",
        categoria = "Bienestar",
        precio = 2500,
        stock = stock,
        icono = "🌿"
    )

    private class FakeTiendaRepository(
        initial: List<ProductoEntity> = emptyList(),
        private val flowActivos: (() -> Flow<List<ProductoEntity>>)? = null
    ) : TiendaRepositoryContract {
        val activos = MutableStateFlow(initial)
        override val productos: Flow<List<ProductoEntity>>
            get() = flowActivos?.invoke() ?: activos
        override val todosLosProductos: Flow<List<ProductoEntity>> = activos

        override suspend fun guardarProducto(producto: ProductoEntity) {
            activos.value = activos.value.filterNot { it.id == producto.id } + producto
        }

        override suspend fun eliminarProducto(id: Int) {
            activos.value = activos.value.filterNot { it.id == id }
        }

        override suspend fun actualizarStock(id: Int, nuevoStock: Int) {
            activos.value = activos.value.map { if (it.id == id) it.copy(stock = nuevoStock) else it }
        }

        override suspend fun registrarCompra(
            email: String,
            nombre: String,
            metodoPago: String,
            lineas: List<LineaCompra>
        ): ResultadoCompra = ResultadoCompra(false, "No implementado en fake")
    }
}
