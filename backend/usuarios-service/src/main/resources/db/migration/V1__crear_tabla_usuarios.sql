
CREATE TABLE usuarios (
                          id BIGINT NOT NULL AUTO_INCREMENT,

                          nombre VARCHAR(120) NOT NULL,
                          email VARCHAR(150) NOT NULL,
                          password_hash VARCHAR(255) NOT NULL,

                          rut VARCHAR(12) NOT NULL,
                          telefono VARCHAR(20) NOT NULL,
                          fecha_nacimiento DATE NOT NULL,
                          comuna VARCHAR(100) NOT NULL,

                          rol VARCHAR(20) NOT NULL DEFAULT 'PACIENTE',
                          activo BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,

                          PRIMARY KEY (id),
                          UNIQUE KEY uk_usuarios_email (email),
                          UNIQUE KEY uk_usuarios_rut (rut),
                          INDEX idx_usuarios_rol (rol)
);
