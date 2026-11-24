
package com.sana_esencia.usuarios_service.service;

import com.sana_esencia.usuarios_service.dto.RegistroUsuarioRequest;
import com.sana_esencia.usuarios_service.entity.Usuario;
import com.sana_esencia.usuarios_service.repository.UsuarioRepository;
import com.sana_esencia.usuarios_service.validation.RutValidator;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrarUsuario(RegistroUsuarioRequest request) {

        // Validar RUT
        if (!RutValidator.esValido(request.getRut())) {
            throw new IllegalArgumentException("El RUT ingresado no es válido");
        }

        String rutNormalizado = RutValidator.normalizar(request.getRut());

        // Validar correo duplicado
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese correo"
            );
        }

        // Validar RUT duplicado
        if (usuarioRepository.existsByRut(rutNormalizado)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese RUT"
            );
        }

        // Validar rol
        String rol = request.getRol().trim().toUpperCase();

        if (!rol.equals("PACIENTE") && !rol.equals("TERAPEUTA")) {
            throw new IllegalArgumentException("El rol seleccionado no es válido");
        }

        // Codificar contraseña
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // Crear usuario
        Usuario usuario = new Usuario(
                request.getEmail().trim().toLowerCase(),
                request.getNombre().trim(),
                passwordHash,
                rutNormalizado,
                request.getTelefono().trim(),
                request.getFechaNacimiento(),
                request.getComuna().trim(),
                rol
        );

        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario autenticarUsuario(com.sana_esencia.usuarios_service.dto.LoginRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findById(emailNormalizado)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!usuario.isActivo()) {
            throw new IllegalArgumentException("La cuenta se encuentra desactivada");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorEmail(String email) {
        String emailNormalizado = email.trim().toLowerCase();
        return usuarioRepository.findById(emailNormalizado)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }
}
