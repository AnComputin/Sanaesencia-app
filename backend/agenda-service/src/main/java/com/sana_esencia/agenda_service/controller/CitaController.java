package com.sana_esencia.agenda_service.controller;

import com.sana_esencia.agenda_service.entity.Cita;
import com.sana_esencia.agenda_service.service.CitaService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    /**
     * Los pacientes solo pueden consultar sus propias citas. Los administradores
     * pueden consultar todas o aplicar filtros. Los terapeutas quedan bloqueados
     * hasta que exista una asociación verificada entre su usuario y especialistaId.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PACIENTE')")
    public ResponseEntity<List<Cita>> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String usuarioEmail,
            @RequestParam(required = false) Integer especialistaId
    ) {
        String rol = rol(jwt);
        if ("PACIENTE".equals(rol)) {
            if (especialistaId != null) {
                throw new AccessDeniedException("Los pacientes no pueden consultar citas por especialista");
            }
            // No confiar en usuarioEmail enviado por el cliente: la identidad viene del JWT.
            return ResponseEntity.ok(service.listarPorUsuario(jwt.getSubject()));
        }

        if (usuarioEmail != null && !usuarioEmail.isBlank()) {
            return ResponseEntity.ok(service.listarPorUsuario(usuarioEmail));
        }
        if (especialistaId != null) {
            return ResponseEntity.ok(service.listarPorEspecialista(especialistaId));
        }
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PACIENTE')")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            Cita cita = service.obtenerPorId(id);
            if ("PACIENTE".equals(rol(jwt)) && !esPropietario(cita, jwt)) {
                throw new AccessDeniedException("No tienes permiso para consultar esta cita");
            }
            return ResponseEntity.ok(cita);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<?> reservar(
            @Valid @RequestBody Cita cita,
            @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            // El correo del propietario siempre se obtiene del token, nunca del body.
            cita.setUsuarioEmail(jwt.getSubject());
            // El paciente solo puede crear una reserva nueva y confirmada.
            cita.setEstado("CONFIRMADA");
            cita.setId(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(service.reservar(cita));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
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

    private String rol(Jwt jwt) {
        String rol = jwt.getClaimAsString("rol");
        return rol == null ? "" : rol.trim().toUpperCase();
    }

    private boolean esPropietario(Cita cita, Jwt jwt) {
        String subject = jwt.getSubject();
        return subject != null && cita.getUsuarioEmail() != null
                && cita.getUsuarioEmail().equalsIgnoreCase(subject);
    }
}