package com.sana_esencia.usuarios_service.config;

import com.sana_esencia.usuarios_service.entity.Usuario;
import com.sana_esencia.usuarios_service.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Locale;

@Configuration
public class AdminBootstrapConfig {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapConfig.class);

    @Bean
    CommandLineRunner crearAdministradorInicial(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.bootstrap-enabled:true}") boolean enabled,
            @Value("${app.admin.email:admin@sanaesencia.cl}") String email,
            @Value("${app.admin.password:admin123}") String password
    ) {
        return args -> {
            if (!enabled) return;

            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            if (repository.existsById(normalizedEmail)) {
                log.info("La cuenta administrativa configurada ya existe: {}", normalizedEmail);
                return;
            }

            if (password == null || password.isBlank()) {
                throw new IllegalStateException("app.admin.password no puede estar vacío");
            }

            Usuario admin = new Usuario(
                    normalizedEmail,
                    "Administrador SanaEsencia",
                    passwordEncoder.encode(password),
                    "11.111.111-1",
                    "+56912345678",
                    LocalDate.of(1985, 1, 1),
                    "Santiago",
                    "ADMIN"
            );
            repository.save(admin);
            log.warn("Se creó el administrador inicial {}. Cambia la contraseña de demostración antes de producción.", normalizedEmail);
        };
    }
}
