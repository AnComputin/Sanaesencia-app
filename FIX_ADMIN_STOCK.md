# Corrección de administración de productos

## Causa
El usuario `admin@sanaesencia.cl` se sembraba únicamente en Room (Android). Las operaciones POST/PUT del `tienda-service` requieren JWT con claim `rol=ADMIN`, por lo que la sesión local no tenía autorización para modificar productos.

## Cambios
- `usuarios-service`: crea el administrador inicial en MySQL si no existe, usando el mismo `PasswordEncoder` del login.
- `SesionRepository`: elimina un JWT anterior cuando falla la autenticación remota y no permite iniciar como ADMIN con la cuenta local sin JWT.
- `TiendaRepository` / `TiendaViewModel`: conservan y muestran el código HTTP y detalle de errores de las modificaciones.
- `ProductosAdminContent`: muestra el resultado de la operación en el panel.

## Credenciales iniciales de desarrollo
- Correo: `admin@sanaesencia.cl`
- Contraseña: `admin123`

El administrador se crea en la base `sana_usuarios_db_app` al iniciar `usuarios-service`, si no existe. Puedes cambiarlo con `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD` y desactivar la creación automática con `APP_ADMIN_BOOTSTRAP_ENABLED=false`. Cambia la contraseña antes de cualquier despliegue real.

## Cómo probar
1. Verifica que MySQL esté activo y que `usuarios-service` apunte a `sana_usuarios_db_app`.
2. Reinicia `usuarios-service` para ejecutar el bootstrap.
3. En MySQL Workbench ejecuta: `SELECT email, rol, activo FROM usuarios WHERE email='admin@sanaesencia.cl';`
4. Reinicia los demás microservicios, especialmente `tienda-service` (puerto 8085).
5. Cierra sesión en la app y vuelve a entrar como administrador para obtener un JWT nuevo.
6. Prueba aumentar stock y editar nombre/descripción/precio. Comprueba `SELECT id,nombre,descripcion,precio,stock,activo FROM sana_tienda_db_app.productos;`.

La lógica de compra y descuento de stock no se modificó.
