CREATE TABLE productos (
                           id INT NOT NULL AUTO_INCREMENT,
                           nombre VARCHAR(120) NOT NULL,
                           descripcion VARCHAR(500) NOT NULL,
                           precio INT NOT NULL,
                           stock INT NOT NULL DEFAULT 0,
                           icono VARCHAR(80) NOT NULL,
                           activo BOOLEAN NOT NULL DEFAULT TRUE,
                           PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ventas (
                        id INT NOT NULL AUTO_INCREMENT,
                        comprador_email VARCHAR(150) NOT NULL,
                        comprador_nombre VARCHAR(120) NOT NULL,
                        fecha_millis BIGINT NOT NULL,
                        total INT NOT NULL,
                        estado VARCHAR(30) NOT NULL,
                        metodo_pago VARCHAR(50) NOT NULL,
                        PRIMARY KEY (id),
                        INDEX idx_ventas_comprador (comprador_email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE detalle_ventas (
                                id INT NOT NULL AUTO_INCREMENT,
                                venta_id INT NOT NULL,
                                producto_id INT NOT NULL,
                                nombre_producto VARCHAR(120) NOT NULL,
                                precio_unitario INT NOT NULL,
                                cantidad INT NOT NULL,
                                subtotal INT NOT NULL,
                                PRIMARY KEY (id),
                                INDEX idx_detalle_venta_id (venta_id),
                                CONSTRAINT fk_detalle_venta FOREIGN KEY (venta_id) REFERENCES ventas (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;