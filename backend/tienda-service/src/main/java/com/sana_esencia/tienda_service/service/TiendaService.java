package com.sana_esencia.tienda_service.service;

import com.sana_esencia.tienda_service.entity.DetalleVenta;
import com.sana_esencia.tienda_service.entity.Producto;
import com.sana_esencia.tienda_service.entity.Venta;
import com.sana_esencia.tienda_service.repository.ProductoRepository;
import com.sana_esencia.tienda_service.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TiendaService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    public TiendaService(ProductoRepository productoRepository, VentaRepository ventaRepository) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductos(boolean soloActivos) {
        return soloActivos
                ? productoRepository.findByActivoTrueOrderByNombreAsc()
                : productoRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Producto obtenerProductoPorId(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));
    }

    @Transactional
    public Producto crearProducto(Producto producto) {
        producto.setId(null);
        producto.setNombre(producto.getNombre().trim());
        producto.setDescripcion(producto.getDescripcion().trim());
        producto.setIcono(producto.getIcono().trim());
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizarProducto(Integer id, Producto datos) {
        Producto actual = obtenerProductoPorId(id);
        actual.setNombre(datos.getNombre().trim());
        actual.setDescripcion(datos.getDescripcion().trim());
        actual.setPrecio(datos.getPrecio());
        actual.setStock(datos.getStock());
        actual.setIcono(datos.getIcono().trim());
        actual.setActivo(datos.isActivo());
        return productoRepository.save(actual);
    }

    @Transactional
    public Venta registrarCompra(Venta ventaRequest) {
        if (ventaRequest.getDetalles() == null || ventaRequest.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La compra debe incluir al menos un producto");
        }

        Venta venta = new Venta();
        venta.setCompradorEmail(ventaRequest.getCompradorEmail().trim().toLowerCase());
        venta.setCompradorNombre(ventaRequest.getCompradorNombre().trim());
        venta.setFechaMillis(
                ventaRequest.getFechaMillis() != null ? ventaRequest.getFechaMillis() : System.currentTimeMillis()
        );
        venta.setMetodoPago(ventaRequest.getMetodoPago().trim());
        venta.setEstado("PAGADA");

        int totalCalculado = 0;

        for (DetalleVenta item : ventaRequest.getDetalles()) {
            if (item.getProductoId() == null || item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada detalle debe indicar productoId y una cantidad mayor a 0");
            }

            Producto producto = obtenerProductoPorId(item.getProductoId());

            if (!producto.isActivo()) {
                throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' no está disponible");
            }

            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para '" + producto.getNombre() + "'. Disponible: " + producto.getStock()
                );
            }

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            int subtotal = producto.getPrecio() * item.getCantidad();
            totalCalculado += subtotal;

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProductoId(producto.getId());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setCantidad(item.getCantidad());
            detalle.setSubtotal(subtotal);

            venta.agregarDetalle(detalle);
        }

        venta.setTotal(totalCalculado);
        return ventaRepository.save(venta);
    }

    @Transactional(readOnly = true)
    public List<Venta> listarVentas(String compradorEmail) {
        if (compradorEmail != null && !compradorEmail.isBlank()) {
            return ventaRepository.findByCompradorEmailOrderByFechaMillisDesc(compradorEmail.trim().toLowerCase());
        }
        return ventaRepository.findAllByOrderByFechaMillisDesc();
    }
}