CREATE TABLE especialidades (
                                id INT NOT NULL AUTO_INCREMENT,
                                nombre VARCHAR(120) NOT NULL,
                                descripcion VARCHAR(500) NOT NULL,
                                icono VARCHAR(80) NOT NULL,
                                profesional VARCHAR(120) NOT NULL,
                                modalidad VARCHAR(30) NOT NULL,
                                agenda_pausada BOOLEAN NOT NULL DEFAULT FALSE,
                                PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;