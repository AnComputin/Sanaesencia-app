-- Agrega la categoría que utiliza la aplicación Android.
ALTER TABLE productos
    ADD COLUMN categoria VARCHAR(80) NOT NULL DEFAULT 'Bienestar';

UPDATE productos SET categoria = CASE id
    WHEN 1 THEN 'Aromaterapia'
    WHEN 2 THEN 'Masajes'
    WHEN 3 THEN 'Aromaterapia'
    WHEN 4 THEN 'Aromaterapia'
    WHEN 5 THEN 'Cuidado personal'
    WHEN 6 THEN 'Infusiones'
    ELSE 'Bienestar'
END
WHERE id BETWEEN 1 AND 6;

-- Los primeros seis productos ya existen y conservan sus datos y stock actuales.
-- INSERT IGNORE agrega solamente los productos nuevos que todavía no estén registrados.
INSERT IGNORE INTO productos (id, nombre, descripcion, categoria, precio, stock, icono, activo) VALUES
(7, 'Set de aromaterapia', 'Conjunto de esencias para crear un ambiente de calma y bienestar en el hogar.', 'Aromaterapia', 12990, 8, '🌸', TRUE),
(8, 'Crema corporal hidratante natural', 'Crema de uso diario para nutrir la piel y complementar tu rutina de autocuidado.', 'Cuidado personal', 9990, 10, '🧴', TRUE),
(9, 'Almohadilla térmica relajante', 'Almohadilla reutilizable para aportar una sensación reconfortante después de un día intenso.', 'Relajación', 8490, 7, '🌿', TRUE),
(10, 'Kit de autocuidado y relajación', 'Selección de productos para disfrutar una pausa de descanso y bienestar personal.', 'Bienestar', 15990, 6, '🎁', TRUE);
