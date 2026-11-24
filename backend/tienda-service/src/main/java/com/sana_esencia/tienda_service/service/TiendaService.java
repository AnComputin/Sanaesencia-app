// service/TiendaService.java
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
        return soloActivos ? productoRepository.findByActivoTrue() : productoRepository.findAll();
    }

    @Transactional
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    @Transactional
    public Venta registrarCompra(Venta ventaRequest) {
        if (ventaRequest.getDetalles() == null || ventaRequest.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La compra debe incluir al menos un producto");
        }

        Venta venta = new Venta();
        venta.setCompradorEmail(ventaRequest.getCompradorEmail().trim().toLowerCase());
        venta.setCompradorNombre(ventaRequest.getCompradorNombre().trim());
        venta.setFechaMillis(ventaRequest.getFechaMillis() != null ? ventaRequest.getFechaMillis() : System.currentTimeMillis());
        venta.setMetodoPago(ventaRequest.getMetodoPago());
        venta.setEstado("PAGADA");

        int totalCalculado = 0;

        for (DetalleVenta item : ventaRequest.getDetalles()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.getProductoId()));

            if (!producto.isActivo() || producto.getStock() < item.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para: " + producto.getNombre());
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
    public List<Venta> listarComprasPorUsuario(String email) {
        return ventaRepository.findByCompradorEmailOrderByFechaMillisDesc(email.trim().toLowerCase());
    }
}
