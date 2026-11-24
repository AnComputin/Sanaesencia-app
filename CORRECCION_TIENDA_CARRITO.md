# Corrección de tienda y carrito – SanaEsencia

## Cambios incluidos

- Se agrega `categoria` a la entidad Java `Producto`, alineándola con `ProductoEntity` de Android.
- Flyway ejecutará `V2__catalogo_productos_y_carga_inicial.sql` al iniciar `tienda-service`.
- La migración conserva los datos actuales de los productos 1 al 6, agrega sus categorías y crea los productos 7 al 10 solamente si todavía no existen.
- Android reemplaza el catálogo local por la respuesta completa del servidor cuando la sincronización es exitosa. Así se eliminan IDs locales obsoletos o duplicados.
- Se evita confirmar compras localmente cuando el backend no responde. El stock y la venta deben quedar registrados en `tienda-service`/MySQL.
- Se consolidan líneas repetidas del mismo ID antes de enviar la compra; el backend también consolida cantidades y calcula el total usando los precios almacenados en MySQL.
- El administrador puede crear, editar, desactivar productos y ajustar stock. Los endpoints de creación y modificación requieren el rol `ADMIN`.

## Aplicación

1. Haz una copia de seguridad de `sana_tienda_db_app`.
2. Reemplaza tu proyecto por esta versión corregida o copia los archivos modificados conservando tus configuraciones locales.
3. Confirma que `backend/tienda-service/src/main/resources/application.properties` apunta a tu base `sana_tienda_db_app` y que las credenciales son las tuyas.
4. Inicia `tienda-service`. Flyway aplicará V2 automáticamente; no ejecutes manualmente V2 si Flyway ya la registró.
5. En MySQL Workbench ejecuta:

```sql
USE sana_tienda_db_app;

SELECT id, nombre, categoria, precio, stock, icono, activo
FROM productos
ORDER BY id;

SELECT COUNT(*) AS total_productos FROM productos;
```

Deben aparecer 10 IDs únicos, del 1 al 10. Los productos 1 al 6 conservan su precio y stock que ya estaban guardados; los productos 7 al 10 se insertan con los valores iniciales de la migración.

6. Inicia los otros servicios como antes y ejecuta la app Android. Al abrir la tienda, Android descargará el catálogo oficial del backend.

## Prueba funcional recomendada

- Agrega al carrito un producto ID 1 y otro ID 2. Deben aparecer dos líneas independientes.
- Verifica que el subtotal sea `8990 + 10990 = 19980`.
- Aumenta una unidad del producto 1: el total debe subir exactamente $8.990.
- Finaliza una compra de prueba. Comprueba que se registre una sola venta, con dos detalles, total correcto y stock descontado en MySQL.
- Entra como administrador, edita precio/stock/nombre de un producto y comprueba que el cambio persista después de cerrar y volver a abrir la app.

## Nota de validación

En este entorno no fue posible ejecutar la compilación Android ni las pruebas Maven: los wrappers necesitan descargar Gradle/Maven desde Internet y el entorno de ejecución no tiene acceso DNS a esos repositorios. Por eso, compila y ejecuta las pruebas desde Android Studio/IntelliJ en tu equipo antes de entregar.
