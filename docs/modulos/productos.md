# Tienda de bienestar y Productos

- **Responsable:** Alejandra Núñez
- **Actualizado:** 22-11-2026
- **Versión:** 1.0.0
- **Estado:** Implementado (catálogo y checkout simulado)

## Objetivo y distinción

Vender/mostrar productos de bienestar como aceites, vela, sales e infusión. Esta sección es independiente del catálogo de especialidades terapéuticas.

## Pantallas y navegación

- `TiendaScreen`: listado/categoría, disponibilidad, carrito.
- `DetalleProductoTiendaScreen`: detalle por ID.
- `CarritoTiendaScreen`: unidades, quitar líneas, subtotal.
- `PagoTiendaScreen`: selección de método de prueba, resumen y pago simulado.
- `CompraConfirmadaScreen`: resumen del registro local.
- `TiendaGraph`: `tienda` → `producto_detalle/{productoId}`, `carrito_tienda` → `pago_tienda` → `compra_confirmada/{ventaId}/{total}`. Regreso usa la pila Compose.

## Archivos relacionados

- `ui/views/tienda/TiendaScreen.kt`, `DetalleProductoTiendaScreen.kt`, `CarritoTiendaScreen.kt`, `PagoTiendaScreen.kt`.
- `navigation/TiendaGraph.kt`, `viewmodel/TiendaViewModel.kt`, `data/repository/TiendaRepository.kt`.
- `ProductoEntity`, `ProductoDao`, `VentaEntity`, `VentaDao`, `DetalleVentaEntity` y `AppDatabase` (propiedad de infraestructura, consultados sin modificación).
- `ui/views/admin/ProductosAdminContent.kt` mantiene el catálogo desde administración.

## Modelo, fuente y persistencia

`ProductoEntity`: nombre, descripción, categoría, precio entero, stock, icono emoji y activo. No contiene URL/bitmap de imagen; no hay imágenes fotográficas. `ProductoDao` entrega flujo de activos y consulta por ID. `TiendaRepository` y `TiendaViewModel` conectan el catálogo y checkout a Room.

La versión de Room es 8. `MIGRATION_7_8` y callback de creación insertan seis productos de ejemplo. Son datos demostrativos de prototipo, no inventario comercial confirmado. Administrador puede mantener productos y stock. No hay API/backend.

`VentaEntity` y `DetalleVentaEntity` guardan comprador, estado `PAGADO_SIMULADO`, método, total y snapshot de producto dentro de transacción junto con descuento de stock.

## Estados y validaciones

Catálogo diferencia Cargando, Vacio, Error con reintento y Contenido. Detalle resuelve ID contra el catálogo vivo; ID inexistente/desactivado muestra estado no disponible. Stock cero deshabilita agregar; carrito limita cantidades según stock. Compra rechaza sesión ausente, carrito vacío, productos inactivos o stock insuficiente; ajuste y venta se confirman en transacción. El método de pago es ficticio y no se solicitan datos bancarios reales.

## Acceso

Tienda puede consultarse desde Home. Pago exige usuario autenticado y usa identidad de `AppState.usuarioActual`; las ventas se etiquetan con comprador. La UI de demostración no realiza cobro.

## Pruebas

AL-06 ejecutó compile, 204 unit tests y assemble. `TiendaViewModelTest` cubre carga/contenido, vacío, error y reintento, detalle por ID, ID inexistente, selección de ruta y límite de stock/carrito.

## Limitaciones

Datos iniciales de demostración; iconos emoji en vez de imágenes; sin backend, fulfillment, inventario en servidor ni pasarela de pago real. Los datos locales no constituyen oferta comercial en línea.
