package com.sana_esencia.agenda_service.controller;

import com.sana_esencia.agenda_service.entity.Cita;
import com.sana_esencia.agenda_service.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "*")
public class CitaController {

    private final CitaService service;

    public CitaController(CitaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listar(
            @RequestParam(required = false) String usuarioEmail,
            @RequestParam(required = false) Integer especialistaId
    ) {
        if (usuarioEmail != null && !usuarioEmail.isBlank()) {
            return ResponseEntity.ok(service.listarPorUsuario(usuarioEmail));
        }
        if (especialistaId != null) {
            return ResponseEntity.ok(service.listarPorEspecialista(especialistaId));
        }
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.obtenerPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/retenciones")
    public ResponseEntity<?> retener(
            @Valid @RequestBody Cita cita,
            @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            if (jwt == null || !cita.getUsuarioEmail().equalsIgnoreCase(jwt.getSubject())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("El usuario de la cita no coincide con la sesión");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(service.retener(cita));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarRetencion(
            @PathVariable Integer id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            return ResponseEntity.ok(service.confirmarRetencion(id, jwt == null ? null : jwt.getSubject()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/retencion/liberar")
    public ResponseEntity<?> liberarRetencion(
            @PathVariable Integer id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            service.liberarRetencion(id, jwt == null ? null : jwt.getSubject());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> reservar(@Valid @RequestBody Cita cita) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.reservar(cita));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado,
            @RequestParam(required = false) Integer boxAsignado
    ) {
        try {
            return ResponseEntity.ok(service.actualizarEstado(id, estado, boxAsignado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}