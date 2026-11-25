package com.sana_esencia.usuarios_service.controller;

import com.sana_esencia.usuarios_service.dto.AuthResponse;
import com.sana_esencia.usuarios_service.dto.LoginRequest;
import com.sana_esencia.usuarios_service.dto.RegistroUsuarioRequest;
import com.sana_esencia.usuarios_service.dto.UsuarioResponse;
import com.sana_esencia.usuarios_service.entity.Usuario;
import com.sana_esencia.usuarios_service.service.JwtService;
import com.sana_esencia.usuarios_service.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public UsuarioController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroUsuarioRequest request) {
        try {
            Usuario usuario = usuarioService.registrarUsuario(request);
            String token = jwtService.generarToken(usuario);
            AuthResponse respuesta = new AuthResponse(
                    token,
                    jwtService.getExpirationSeconds(),
                    new UsuarioResponse(usuario)
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Usuario usuario = usuarioService.autenticarUsuario(request);
            String token = jwtService.generarToken(usuario);
            AuthResponse respuesta = new AuthResponse(
                    token,
                    jwtService.getExpirationSeconds(),
                    new UsuarioResponse(usuario)
            );
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> obtenerPorEmail(
            @PathVariable String email,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String rol = jwt.getClaimAsString("rol");
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol);
        if (!esAdmin && !email.trim().equalsIgnoreCase(jwt.getSubject())) {
            throw new AccessDeniedException("No tienes permiso para consultar otro usuario");
        }
        try {
            Usuario usuario = usuarioService.obtenerPorEmail(email);
            return ResponseEntity.ok(new UsuarioResponse(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}