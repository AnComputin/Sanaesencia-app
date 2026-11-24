package com.sana_esencia.tienda_service.controller;

import com.sana_esencia.tienda_service.entity.Producto;
import com.sana_esencia.tienda_service.entity.Venta;
import com.sana_esencia.tienda_service.service.TiendaService;
import jakarta.validation.Valid;
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

    @GetMapping("/productos/{id}")
    public ResponseEntity<?> obtenerProducto(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.obtenerProductoPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/productos")
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearProducto(producto));
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Integer id, @Valid @RequestBody Producto producto) {
        try {
            return ResponseEntity.ok(service.actualizarProducto(id, producto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/ventas")
    public ResponseEntity<?> registrarVenta(@Valid @RequestBody Venta venta) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarCompra(venta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/ventas")
    public ResponseEntity<List<Venta>> listarVentas(@RequestParam(required = false) String compradorEmail) {
        return ResponseEntity.ok(service.listarVentas(compradorEmail));
    }
}