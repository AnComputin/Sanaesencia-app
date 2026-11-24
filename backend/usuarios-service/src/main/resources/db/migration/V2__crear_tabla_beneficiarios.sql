CREATE TABLE beneficiarios (
                               id INT NOT NULL AUTO_INCREMENT,
                               propietario_email VARCHAR(150) NOT NULL,
                               nombre VARCHAR(120) NOT NULL,
                               rut VARCHAR(12) NOT NULL,
                               fecha_nacimiento DATE NOT NULL,
                               parentesco VARCHAR(50) NOT NULL,
                               activo BOOLEAN NOT NULL DEFAULT TRUE,
                               PRIMARY KEY (id),
                               INDEX idx_beneficiarios_propietario (propietario_email),
                               UNIQUE KEY uk_beneficiarios_propietario_rut (propietario_email, rut),
                               CONSTRAINT fk_beneficiarios_usuario
                                   FOREIGN KEY (propietario_email) REFERENCES usuarios (email)
                                       ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;