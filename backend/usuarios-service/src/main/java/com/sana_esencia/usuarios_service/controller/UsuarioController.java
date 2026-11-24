
package com.sana_esencia.usuarios_service.controller;

import com.sana_esencia.usuarios_service.dto.RegistroUsuarioRequest;
import com.sana_esencia.usuarios_service.dto.UsuarioResponse;
import com.sana_esencia.usuarios_service.entity.Usuario;
import com.sana_esencia.usuarios_service.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(
            @Valid @RequestBody RegistroUsuarioRequest request
    ) {
        try {
            Usuario usuario = usuarioService.registrarUsuario(request);

            UsuarioResponse respuesta = new UsuarioResponse(usuario);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(respuesta);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody com.sana_esencia.usuarios_service.dto.LoginRequest request
    ) {
        try {
            Usuario usuario = usuarioService.autenticarUsuario(request);
            return ResponseEntity.ok(new UsuarioResponse(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> obtenerPorEmail(@PathVariable String email) {
        try {
            Usuario usuario = usuarioService.obtenerPorEmail(email);
            return ResponseEntity.ok(new UsuarioResponse(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
