CREATE TABLE especialistas (
                               id INT NOT NULL AUTO_INCREMENT,
                               nombre VARCHAR(120) NOT NULL,
                               especialidad_id INT NOT NULL,
                               modalidad VARCHAR(30) NOT NULL,
                               activo BOOLEAN NOT NULL DEFAULT TRUE,
                               PRIMARY KEY (id),
                               INDEX idx_especialistas_especialidad (especialidad_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;