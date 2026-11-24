package com.example.sanaesenciaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sanaesenciaapp.data.repository.LineaCompra
import com.example.sanaesenciaapp.data.repository.ResultadoCompra
import com.example.sanaesenciaapp.data.repository.TiendaRepository
import com.example.sanaesenciaapp.data.repository.TiendaRepositoryContract
import com.example.sanaesenciaapp.data.room.AppDatabase
import com.example.sanaesenciaapp.data.room.ProductoEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LineaCarrito(
    val producto: ProductoEntity,
    val cantidad: Int
)

data class EstadoPagoTienda(
    val procesando: Boolean = false,
    val resultado: ResultadoCompra? = null
)

sealed interface EstadoCatalogoTienda {
    data object Cargando : EstadoCatalogoTienda
    data object Vacio : EstadoCatalogoTienda
    data object Error : EstadoCatalogoTienda
    data class Contenido(val productos: List<ProductoEntity>) : EstadoCatalogoTienda {
        fun producto(id: Int): ProductoEntity? = productos.firstOrNull { it.id == id }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TiendaViewModel(
    private val repository: TiendaRepositoryContract
) : ViewModel() {
    constructor(database: AppDatabase) : this(TiendaRepository(database))

    private val reintentosCatalogo = MutableStateFlow(0)

    val estadoCatalogo: StateFlow<EstadoCatalogoTienda> =
        reintentosCatalogo
            .flatMapLatest {
                repository.productos
                    .map<List<ProductoEntity>, EstadoCatalogoTienda> { productos ->
                        if (productos.isEmpty()) EstadoCatalogoTienda.Vacio
                        else EstadoCatalogoTienda.Contenido(productos)
                    }
                    .onStart { emit(EstadoCatalogoTienda.Cargando) }
                    .catch { error ->
                        if (error is CancellationException) throw error
                        emit(EstadoCatalogoTienda.Error)
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoCatalogoTienda.Cargando)

    val todosProductos: StateFlow<List<ProductoEntity>> =
        repository.todosLosProductos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val productos: StateFlow<List<ProductoEntity>> =
        repository.productos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _carrito = MutableStateFlow<List<LineaCarrito>>(emptyList())
    val carrito: StateFlow<List<LineaCarrito>> = _carrito.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _estadoPago = MutableStateFlow(EstadoPagoTienda())
    val estadoPago: StateFlow<EstadoPagoTienda> = _estadoPago.asStateFlow()

    init {
        viewModelScope.launch {
            todosProductos.collect { lista ->
                val productosDisponibles = lista.filter { it.activo && it.stock > 0 }.associateBy { it.id }
                _carrito.update { lineas ->
                    lineas.mapNotNull { linea ->
                        val productoActual = productosDisponibles[linea.producto.id]
                            ?: return@mapNotNull null
                        val cantidadAjustada = linea.cantidad.coerceAtMost(productoActual.stock)
                        if (cantidadAjustada <= 0) null
                        else LineaCarrito(productoActual, cantidadAjustada)
                    }
                }
            }
        }
    }

    fun agregarAlCarrito(producto: ProductoEntity) {
        if (!producto.activo || producto.stock <= 0) {
            _mensaje.value = "Este producto no tiene stock disponible."
            return
        }
        val existente = _carrito.value.firstOrNull { it.producto.id == producto.id }
        if (existente != null && existente.cantidad >= producto.stock) {
            _mensaje.value = "No quedan más unidades disponibles de este producto."
            return
        }
        _carrito.update { lineas ->
            if (existente == null) {
                lineas + LineaCarrito(producto, 1)
            } else {
                lineas.map {
                    if (it.producto.id == producto.id) it.copy(cantidad = it.cantidad + 1)
                    else it
                }
            }
        }
        _mensaje.value = "${producto.nombre} agregado al carrito."
    }

    fun aumentarCantidad(productoId: Int) {
        _carrito.update { lineas ->
            lineas.map { linea ->
                if (linea.producto.id == productoId && linea.cantidad < linea.producto.stock) {
                    linea.copy(cantidad = linea.cantidad + 1)
                } else linea
            }
        }
    }

    fun disminuirCantidad(productoId: Int) {
        _carrito.update { lineas ->
            lineas.mapNotNull { linea ->
                if (linea.producto.id != productoId) linea
                else if (linea.cantidad <= 1) null
                else linea.copy(cantidad = linea.cantidad - 1)
            }
        }
    }

    fun quitarDelCarrito(productoId: Int) {
        _carrito.update { it.filterNot { linea -> linea.producto.id == productoId } }
    }

    fun reintentarCatalogo() {
        reintentosCatalogo.update { it + 1 }
    }

    fun limpiarCarrito() {
        _carrito.value = emptyList()
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun guardarProducto(producto: ProductoEntity) {
        viewModelScope.launch {
            runCatching { repository.guardarProducto(producto) }
                .onFailure { _mensaje.value = "No se pudo guardar el producto." }
                .onSuccess { _mensaje.value = "Producto guardado correctamente." }
        }
    }

    fun eliminarProducto(productoId: Int) {
        viewModelScope.launch {
            runCatching { repository.eliminarProducto(productoId) }
                .onFailure { _mensaje.value = "No se pudo quitar el producto." }
                .onSuccess { _mensaje.value = "Producto quitado de la tienda." }
        }
    }

    fun actualizarStock(productoId: Int, nuevoStock: Int) {
        viewModelScope.launch {
            runCatching { repository.actualizarStock(productoId, nuevoStock) }
                .onFailure { _mensaje.value = "No se pudo actualizar el stock." }
        }
    }

    fun simularPago(email: String, nombre: String, metodoPago: String) {
        if (_estadoPago.value.procesando) return
        val lineasActuales = _carrito.value.toList()
        if (lineasActuales.isEmpty()) {
            _estadoPago.value = EstadoPagoTienda(
                resultado = ResultadoCompra(false, "El carrito está vacío")
            )
            return
        }

        viewModelScope.launch {
            _estadoPago.value = EstadoPagoTienda(procesando = true)
            val resultado = runCatching {
                repository.registrarCompra(
                    email = email,
                    nombre = nombre,
                    metodoPago = metodoPago,
                    lineas = lineasActuales.map { LineaCompra(it.producto.id, it.cantidad) }
                )
            }.getOrElse {
                ResultadoCompra(false, it.message ?: "Ocurrió un error al procesar la compra.")
            }

            if (resultado.exito) {
                _carrito.value = emptyList()
            }
            _estadoPago.value = EstadoPagoTienda(
                procesando = false,
                resultado = resultado
            )
        }
    }

    fun limpiarResultadoPago() {
        _estadoPago.value = EstadoPagoTienda()
    }
}

class TiendaViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TiendaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TiendaViewModel(database) as T
        }
        throw IllegalArgumentException("ViewModel de clase no reconocida: ${modelClass.name}")
    }
}
