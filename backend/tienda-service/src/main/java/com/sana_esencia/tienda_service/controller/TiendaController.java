// controller/TiendaController.java
package com.sana_esencia.tienda_service.controller;

import com.sana_esencia.tienda_service.entity.Producto;
import com.sana_esencia.tienda_service.entity.Venta;
import com.sana_esencia.tienda_service.service.TiendaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TiendaController {

    private final TiendaService service;

    public TiendaController(TiendaService service) {
        this.service = service;
    }

    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> listarProductos(@RequestParam(defaultValue = "true") boolean soloActivos) {
        return ResponseEntity.ok(service.listarProductos(soloActivos));
    }

    @PostMapping("/productos")
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        producto.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarProducto(producto));
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Integer id, @RequestBody Producto producto) {
        producto.setId(id);
        return ResponseEntity.ok(service.guardarProducto(producto));
    }

    @PostMapping("/ventas")
    public ResponseEntity<?> registrarVenta(@RequestBody Venta venta) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarCompra(venta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/ventas")
    public ResponseEntity<List<Venta>> listarVentas(@RequestParam String compradorEmail) {
        return ResponseEntity.ok(service.listarComprasPorUsuario(compradorEmail));
    }
}