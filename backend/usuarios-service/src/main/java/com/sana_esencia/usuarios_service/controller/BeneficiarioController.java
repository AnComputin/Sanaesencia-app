package com.sana_esencia.usuarios_service.controller;

import com.sana_esencia.usuarios_service.dto.BeneficiarioRequest;
import com.sana_esencia.usuarios_service.dto.BeneficiarioResponse;
import com.sana_esencia.usuarios_service.entity.Beneficiario;
import com.sana_esencia.usuarios_service.service.BeneficiarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiarios")
@CrossOrigin(origins = "*")
public class BeneficiarioController {

    private final BeneficiarioService beneficiarioService;

    public BeneficiarioController(BeneficiarioService beneficiarioService) {
        this.beneficiarioService = beneficiarioService;
    }

    @GetMapping
    public ResponseEntity<List<BeneficiarioResponse>> listarPorTitular(
            @RequestParam String propietarioEmail,
            @RequestParam(defaultValue = "true") boolean soloActivos
    ) {
        List<BeneficiarioResponse> lista = beneficiarioService
                .listarPorPropietario(propietarioEmail, soloActivos)
                .stream()
                .map(BeneficiarioResponse::new)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            Beneficiario beneficiario = beneficiarioService.obtenerPorId(id);
            return ResponseEntity.ok(new BeneficiarioResponse(beneficiario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody BeneficiarioRequest request) {
        try {
            Beneficiario guardado = beneficiarioService.registrarOReactivar(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new BeneficiarioResponse(guardado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody BeneficiarioRequest request
    ) {
        try {
            Beneficiario actualizado = beneficiarioService.actualizar(id, request);
            return ResponseEntity.ok(new BeneficiarioResponse(actualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(@PathVariable Integer id) {
        try {
            Beneficiario actualizado = beneficiarioService.cambiarEstadoActivo(id, false);
            return ResponseEntity.ok(new BeneficiarioResponse(actualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<?> reactivar(@PathVariable Integer id) {
        try {
            Beneficiario actualizado = beneficiarioService.cambiarEstadoActivo(id, true);
            return ResponseEntity.ok(new BeneficiarioResponse(actualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}