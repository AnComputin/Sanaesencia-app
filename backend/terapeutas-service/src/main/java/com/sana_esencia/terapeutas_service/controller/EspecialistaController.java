package com.sana_esencia.terapeutas_service.controller;

import com.sana_esencia.terapeutas_service.entity.Especialista;
import com.sana_esencia.terapeutas_service.service.EspecialistaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialistas")
@CrossOrigin(origins = "*")
public class EspecialistaController {

    private final EspecialistaService service;

    public EspecialistaController(EspecialistaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Especialista>> listar(
            @RequestParam(required = false) Integer especialidadId,
            @RequestParam(defaultValue = "true") boolean soloActivos
    ) {
        if (especialidadId != null) {
            return ResponseEntity.ok(service.listarPorEspecialidad(especialidadId, soloActivos));
        }
        return ResponseEntity.ok(service.listarTodos(soloActivos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.obtenerPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Especialista> crear(@Valid @RequestBody Especialista especialista) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(especialista));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @Valid @RequestBody Especialista datos) {
        try {
            return ResponseEntity.ok(service.actualizar(id, datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activo")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestParam boolean activo) {
        try {
            return ResponseEntity.ok(service.cambiarEstadoActivo(id, activo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}