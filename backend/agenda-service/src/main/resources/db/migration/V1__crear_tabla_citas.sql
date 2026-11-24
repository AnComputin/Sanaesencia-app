CREATE TABLE citas (
                       id INT NOT NULL AUTO_INCREMENT,
                       usuario_email VARCHAR(150) NOT NULL,
                       especialidad_id INT NOT NULL,
                       especialidad_nombre VARCHAR(120) NOT NULL,
                       especialista_id INT NOT NULL,
                       especialista_nombre VARCHAR(120) NOT NULL,
                       modalidad VARCHAR(30) NOT NULL,
                       inicio_millis BIGINT NOT NULL,
                       fin_millis BIGINT NOT NULL,
                       hora VARCHAR(20) NOT NULL,
                       paciente_nombre VARCHAR(120) NOT NULL,
                       beneficiario_id INT NULL,
                       estado VARCHAR(30) NOT NULL,
                       box_asignado INT NULL,
                       PRIMARY KEY (id),
                       INDEX idx_citas_usuario (usuario_email),
                       INDEX idx_citas_especialista_horario (especialista_id, inicio_millis, fin_millis)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;