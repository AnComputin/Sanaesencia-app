# Estado de implementación de SanaEsencia

**Rama revisada:** `Rama-Mark`  
**Commit revisado:** `5df11b3fed403dc9f988870acbe9e824c3def2b7` (`Se agregan funciones del backend relacionado a la agenda`, 2026-11-24).  
**Alcance:** inventario estático del código presente en la rama; no implica que los servicios estén desplegados ni que todas las operaciones se hayan validado contra un servidor.

## Resumen

El repositorio contiene una aplicación Android Compose con persistencia local Room y cinco microservicios Spring Boot con MySQL/Flyway. También contiene contratos Retrofit para esos servicios. Sin embargo, el contenedor de dependencias Android sigue proporcionando repositorios locales basados en Room; la inicialización del cliente Retrofit no demuestra que las pantallas estén usando el backend. La integración Android-backend debe considerarse **parcial/no comprobada**.

## Aplicación Android

### Arquitectura y plataforma

- Kotlin, Jetpack Compose, Material 3, MVVM, Coroutines y StateFlow.
- Navigation Compose con un NavHost raíz y grafos para autenticación, catálogo, agenda, terapeuta, juegos y tienda.
- Room/SQLite local, base de datos versión 8, entidades y migraciones en `data/room`.
- `AppContainer` construye DAOs y repositorios; `AppViewModelProvider` distribuye módulos de ViewModel.
- Android SDK declarado: compile/target 37 y min SDK 24. Las versiones concretas se mantienen en Gradle y el catálogo de versiones.

### Flujos presentes

- **Autenticación:** registro, validación de campos/RUT, inicio de sesión y estado de sesión en memoria mediante `SesionRepository`. La sesión no se restaura al reiniciar.
- **Catálogo:** especialidades y especialistas locales, detalle de servicio y navegación hacia agenda.
- **Beneficiarios:** alta, consulta, edición, desactivación/reactivación y estado ligado a sesión. El propietario se deriva de la sesión y el DAO limita consultas y cambios por propietario.
- **Agenda:** disponibilidad, reglas de solapamiento, selección de box, reserva, pago de cita simulado, confirmación y Mis citas, persistidos localmente.
- **Tienda:** productos y ventas locales, carrito y flujo de compra simulado; no hay pasarela ni procesamiento de pagos real.
- **Administración:** gestión de catálogo/agenda y productos/stock dentro de los flujos existentes.
- **Terapeuta:** pantallas presentes, pero la consulta de pacientes/citas y escritura clínica no están habilitadas sin asociación persistida y autorizada entre cuenta y especialista/pacientes.
- **Juegos:** Respiración Guiada, Pausa Consciente, Memorice Zen, Gato Armónico y Camino del Bienestar. Su estado es de la sesión/pantalla y puede reiniciarse al salir.

## Backend

Hay cinco aplicaciones Spring Boot independientes en `backend/`:

| Servicio | Puerto declarado | Responsabilidad |
| --- | ---: | --- |
| usuarios-service | 8081 | Registro/login, usuarios y beneficiarios |
| catalogo-service | 8082 | Especialidades |
| terapeutas-service | 8083 | Especialistas |
| agenda-service | 8084 | Citas |
| tienda-service | 8085 | Productos y ventas |

Cada servicio tiene su propio datasource MySQL, migraciones Flyway, entidades JPA, repositorios y controlador REST. Spring Security usa sesiones stateless y validación JWT en los servicios protegidos. Usuarios emite tokens con sujeto, rol y expiración; los servicios están configurados para validar tokens con una clave compartida. Las propiedades del repositorio incluyen valores de desarrollo; no son configuración apropiada para producción y este documento no reproduce secretos.

### Cliente Android de servicios

`data/remote` contiene RetrofitClient, interfaces de API, AuthInterceptor y TokenManager. `MainActivity` inicializa RetrofitClient. En cambio, `AppContainer` expone repositorios que usan Room, y los ViewModels actuales dependen principalmente de esos repositorios. Por tanto, la presencia de endpoints/DTO y la inicialización Retrofit **no prueban consumo efectivo del backend**.

El cliente apunta a loopback HTTP configurado para desarrollo y el manifiesto permite cleartext. Para usar un emulador, dispositivo físico o despliegue remoto se necesita configurar una URL alcanzable y HTTPS según entorno.

## Seguridad y limitaciones conocidas

- Las rutas protegidas requieren JWT, pero la autorización de negocio debe comprobar que propietario/usuario de cada recurso coincide con la identidad autenticada, no confiar solo en correos o IDs enviados por el cliente. La implementación actual debe recibir una auditoría y pruebas específicas de autorización por propietario para citas y beneficiarios.
- Las reglas de rol deben proteger las operaciones administrativas en backend, no solamente ocultar botones en Android.
- Los secretos, contraseñas de base de datos y credenciales de bootstrap presentes en propiedades/código son valores de desarrollo y deben externalizarse/rotarse antes de desplegar.
- HTTP, `127.0.0.1` y logging HTTP detallado corresponden a configuración de desarrollo; no deben usarse para producción.
- No se encontró evidencia de que los cinco servicios estén orquestados/desplegados juntos ni de que la aplicación Android consuma sus endpoints durante los flujos normales.
- Pagos son simulados; no hay pasarela, cobros ni datos bancarios.
- La cuenta de Terapeuta no está vinculada de forma persistida a especialista y pacientes; el portal clínico queda bloqueado.

## Verificación registrada

En la revisión de 2026-11-24:

- Rama remota revisada: `origin/Rama-Mark`, SHA `5df11b3fed403dc9f988870acbe9e824c3def2b7`.
- La ejecución Maven con JDK del sistema falló porque el código requiere Java 21 y el entorno disponía de Java 17.
- Se ejecutó `mvnw.cmd "-Djava.version=17" test` en los cinco servicios. Cada servicio terminó correctamente con una prueba de contexto (1 test, 0 fallos). Esta ejecución temporal con nivel de bytecode 17 no sustituye validación con Java 21.
- Maven descargó dependencias desde Maven Central; aparecieron avisos 401 al consultar metadatos de repositorios secundarios, pero no bloquearon esas cinco ejecuciones.
- No se ejecutaron pruebas de endpoint con JWT real, autorización por rol/propietario, despliegue de los cinco servicios ni pruebas Android contra el backend.
- Estas pruebas de humo confirman que los contextos de Spring arrancan bajo la configuración de prueba usada; no validan las reglas de seguridad ni el comportamiento integral de la API.

## Comandos Maven

Desde cada directorio `backend/<servicio>`, con JDK 21 disponible:

```powershell
.\\mvnw.cmd test
```

Servicios: `usuarios-service`, `catalogo-service`, `terapeutas-service`, `agenda-service` y `tienda-service`.

## Pendientes para considerar integrada la solución

1. Configurar perfiles por entorno y secretos fuera del repositorio; establecer HTTPS y URLs para emulador/dispositivo.
2. Conectar repositorios Android a Retrofit y definir estrategia de persistencia/cache/error, sin mezclar silenciosamente Room y remoto.
3. Derivar identidad/rol del JWT en backend y agregar pruebas de autorización por rol y por propietario.
4. Agregar pruebas de integración para login, emisión/validación JWT y operaciones protegidas.
5. Ejecutar build/tests con JDK 21 y probar los servicios contra una configuración de base de datos reproducible.
6. Definir despliegue/orquestación y asociación autorizada terapeuta-especialista-paciente antes de habilitar datos clínicos.
