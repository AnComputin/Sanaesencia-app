package com.sana_esencia.terapeutas_service.controller;

import com.sana_esencia.terapeutas_service.entity.Especialista;
import com.sana_esencia.terapeutas_service.service.EspecialistaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        if (especialidadId != null) {
            return ResponseEntity.ok(service.listarPorEspecialidad(especialidadId));
        }
        return ResponseEntity.ok(service.listarTodos(soloActivos));
    }

    @PostMapping
    public ResponseEntity<Especialista> crear(@RequestBody Especialista especialista) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(especialista));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Especialista datos) {
        try {
            return ResponseEntity.ok(service.actualizar(id, datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}