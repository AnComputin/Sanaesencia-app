# SanaEsencia

Aplicación Android del Centro Integrativo SanaEsencia, ubicado en Los Vilos. El proyecto académico permite conocer servicios terapéuticos, reservar atenciones, gestionar beneficiarios, usar actividades de bienestar y consultar una tienda local de productos de demostración.

## Objetivo

Reunir en una aplicación los flujos de atención del centro y herramientas de bienestar, respetando la sesión autenticada, la disponibilidad de agenda y el aislamiento de datos por cuenta.

## Tecnologías

### Aplicación Android
- Kotlin, Jetpack Compose y Material 3.
- MVVM con ViewModels y StateFlow.
- Kotlin Coroutines.
- Navigation Compose mediante grafos por módulo.
- Room/SQLite (versión 8) para caché local y respaldo offline.
- Retrofit 2.11.0 + OkHttp 4.12.0 + Gson para consumo REST de microservicios y API externa.
- Google Play Services Location + Android Geocoder en módulo de Sede/Ubicación.
- JaCoCo (`jacocoLogicaReport`) para medición de cobertura unitaria sobre lógica de negocio.
- Android SDK: compile/target 37; min SDK 24.
- AGP 9.3.3, Kotlin 2.2.10, Room 2.8.4 (versiones declaradas en el proyecto).

### Backend (Microservicios)
- Java 21 (compatible con ejecución local en JDK 17 mediante `-Djava.version=17`) + Spring Boot 4.2.0.
- 5 microservicios independientes con Spring Data JPA (`ddl-auto=validate`), MySQL 8.0, Flyway y seguridad JWT stateless (`JwtEncoder`/`JwtDecoder` HS256):
  - `usuarios-service` (`:8081`): registro, login, emisión JWT, bootstrap de admin y CRUD de beneficiarios.
  - `catalogo-service` (`:8082`): CRUD de especialidades y pausa de agenda.
  - `terapeutas-service` (`:8083`): CRUD de especialistas.
  - `agenda-service` (`:8084`): reserva y estado de citas con control de solapamiento por especialista y asignación de boxes 1–8.
  - `tienda-service` (`:8085`): CRUD de productos y registro transaccional de ventas con descuento de stock.
- Orquestación con Docker Compose (`backend/docker-compose.yml`), `backend/Dockerfile` y reverse proxy Nginx (`backend/nginx/default.conf`).
- JaCoCo (`jacoco-maven-plugin`) integrado en los 5 servicios.

> **Nota sobre pagos simulados:** Las operaciones de pago de citas y tienda registran la confirmación/venta en los microservicios y Room local, sin pasarela bancaria real.

## Consumo de API Externa

- En `app/src/main/java/com/example/sanaesenciaapp/data/remote/ClimaService.kt` (`OpenMeteoApi` + `ClimaRepository`), la app consume la API REST pública de **Open-Meteo** (`https://api.open-meteo.com/v1/forecast`) para mostrar el clima actual de la sede en Los Vilos (`UbicacionScreen.kt`), con estados de carga, error, reintento y recomendación de bienestar según código WMO.

## Arquitectura

El código de aplicación está bajo `app/src/main/java/com/example/sanaesenciaapp/`:

- `data/room`: entidades, DAO, base de datos y migraciones.
- `data/repository`: acceso a datos y reglas de módulo.
- `viewmodel`: estado y lógica de presentación.
- `ui/views`: pantallas Compose agrupadas por flujo.
- `navigation`: `Navigation.kt` como NavHost raíz y grafos por módulo.
- `di`: contenedor y fábrica modular de ViewModels.

Room está en **versión 8**. `AppState` todavía comparte sesión y algunos datos de navegación; los ViewModels de Catálogo, Beneficiarios, Juegos y Tienda manejan estado propio en sus respectivos alcances. Las citas siguen el flujo de Agenda de Valentina.

## Equipo y responsabilidades

Según el contrato de integración del proyecto:

- **Anaís Riquelme:** infraestructura, Room, sesión e inyección de dependencias.
- **Mark Toro:** autenticación, administración, roles y cierre de sesión.
- **Valentina Guerrero:** Agenda, reservas, pagos de citas y Mis citas.
- **Alejandra Núñez:** Inicio, Catálogo, Beneficiarios, portal Terapeuta, Juegos y documentación de módulos.

## Funcionalidades por usuario

### Paciente

- Registro e inicio de sesión.
- Consultar especialidades y especialistas terapéuticos.
- Reservar una cita con modalidad, fecha y horario disponibles.
- Reservar para sí mismo o seleccionar un beneficiario asociado a su cuenta.
- Consultar y cancelar citas conforme a las reglas de Agenda.
- Usar los cinco juegos/pausas de bienestar.
- Consultar productos locales, mantener un carrito y completar un pago simulado.

### Administrador

- Gestionar modalidad y pausa de agendas de especialidades.
- Consultar ocupación de los ocho boxes.
- Crear productos de tienda, ajustar stock o retirarlos.

### Terapeuta

El portal conserva sus pantallas, pero los datos clínicos están **bloqueados**. No existe una relación persistida y autorizada entre la cuenta del terapeuta y `especialistaId`, ni una asignación persistida de pacientes. No se consultan citas reales ni se guardan notas clínicas.

## Módulos

- [Inicio](docs/modulos/inicio.md): bienvenida, acceso, Inicio y accesos a módulos.
- [Catálogo terapéutico](docs/modulos/catalogo.md): especialidades, especialistas, detalle y continuidad con Agenda.
- [Beneficiarios](docs/modulos/beneficiarios.md): alta, consulta, edición y aislamiento por titular.
- [Terapeuta](docs/modulos/terapeuta.md): alcance actual, bloqueo clínico y dependencias pendientes.
- [Juegos](docs/modulos/juegos.md): Respiración Guiada, Pausa Consciente, Memorice Zen, Gato Armónico y Camino del Bienestar.
- [Productos](docs/modulos/productos.md): catálogo separado, detalle, carrito y compra simulada.

## Navegación

`Navigation.kt` inicia en Splash y declara Inicio/Home, y conecta grafos independientes para autenticación, catálogo, Agenda, Terapeuta, Juegos y Tienda. Las pantallas de módulo envían acciones por callbacks y los grafos controlan las rutas. El detalle de producto usa `producto_detalle/{productoId}`; el detalle del catálogo terapéutico usa `detalle_servicio/{especialidadId}`.

## Persistencia y fuentes de datos

- Los 5 microservicios Spring Boot + MySQL 8.0 son la fuente remota principal autenticada mediante JWT (`RetrofitClient`, `AuthInterceptor`, `TokenManager`).
- Room/SQLite local (versión 8) actúa como caché reactivo sincronizado con el servidor y respaldo de consulta.
- Beneficiarios se consultan y mutan limitados por el correo titular derivado de la sesión (tanto en `BeneficiarioController` mediante el `subject` del JWT como en las consultas locales del DAO).
- Las ventas de tienda se registran transaccionalmente en `tienda-service` descontando stock en MySQL y actualizando Room; el pago es simulado sin pasarela bancaria.
- Las citas se validan contra solapamiento de especialista y capacidad de 8 boxes físicos en `agenda-service` y Room.
- Las notas clínicas de Terapeuta no se persisten.

## Autenticación y seguridad

`SesionRepository` autentica contra `UsuarioRepository` y expone identidad sin entregar hashes a los módulos. La sesión es volátil. Los datos de beneficiarios se limitan al titular autenticado. El rol por sí solo no habilita acceso a datos clínicos: falta una asignación verificable cuenta-terapeuta-especialista-paciente. No se publican credenciales de prueba, tokens ni claves en este README.

## Estado de implementación

- **Implementados:** Inicio, Sede/Ubicación (GPS + Geocoder + Open-Meteo REST), catálogo terapéutico, Beneficiarios, Agenda con 8 boxes, Juegos, Tienda con ventas transaccionales, Panel Admin y 5 microservicios Spring Boot dockerizados detrás de Nginx.
- **Parcial / bloqueado por dependencia:** portal Terapeuta. Sus pantallas están presentes, pero la lectura clínica y edición permanecen deshabilitadas hasta tener relación y autorización persistidas.
- **Pagos e imágenes:** pasarela de pago simulada y catálogo visual con iconografía emoji.

## Instalación y ejecución

1. Abrir el proyecto en Android Studio y esperar la sincronización de Gradle.
2. Usar JDK compatible con el Gradle Wrapper del proyecto y Android SDK 37.
3. Ejecutar la app desde la configuración Android `app` en un emulador o dispositivo API 24+.

Desde PowerShell en la raíz:

```powershell
.\gradlew.bat :app:assembleDebug
```

El APK debug queda en `app/build/outputs/apk/debug/`.

## Pruebas y cobertura

Comandos disponibles (Android):

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:jacocoLogicaReport
.\gradlew.bat :app:assembleDebug
```

El reporte de cobertura JaCoCo de la capa lógica queda en `app/build/reports/jacoco/jacocoLogicaReport/html/index.html`.

Comandos disponibles (Backend, en cada carpeta `backend/<servicio>` con MySQL 8.0 activo):

```powershell
.\mvnw.cmd "-Djava.version=17" test
```

El reporte JaCoCo de cada microservicio queda en `backend/<servicio>/target/site/jacoco/index.html`.

## Contratos e integración

El documento [Contrato de integración modular](docs/CONTRATO-INTEGRACION-MODULOS.md) describe propiedad de archivos y coordinación del equipo. No se encontró un contrato API publicado. Para desbloquear Terapeuta se requiere:
- De Anaís: modelo persistido y migración no destructiva para asociación terapeuta-especialista y asignación de pacientes, con consultas aisladas.
- De Mark: si la autorización será remota, contrato autenticado de consulta de especialista, pacientes/citas y escritura de seguimiento.
- De Valentina: conservar el contrato de Agenda y confirmar los campos/estados que puede consumir Terapeuta cuando exista autorización.

No se inventan endpoints, DTO, permisos ni datos clínicos.

## Limitaciones conocidas

- La sesión no sobrevive al cierre de la app.
- Terapeuta no puede ver pacientes/citas ni guardar seguimiento hasta que exista relación y autorización persistidas.
- La tienda usa productos de ejemplo, imágenes emoji y pagos simulados; no hay servicios de fulfillment ni pasarela real.
- Los juegos guardan su estado en memoria del destino; al salir al hub, una nueva entrada inicia otra sesión.
- Los recursos de texto están en español; no se declaran traducciones a otros idiomas.

## Registro documental AL-07

- **Responsable:** Alejandra Núñez
- **Fecha:** 22-11-2026
- **Versión:** 1.0.0
- **Estado:** Documentación actualizada contra el código de la rama Ale.
- **Archivos:** este README y `docs/modulos/{inicio,catalogo,beneficiarios,terapeuta,juegos,productos}.md`. El contrato de integración preexistente se consultó y se dejó intacto.
- **Pruebas:** AL-07 no modificó código y no volvió a ejecutar Gradle. La suite completa reportada aquí (204 aprobadas) corresponde a la validación AL-06 ejecutada con compile y assemble; no es una ejecución de AL-07.
- **Revisión:** documentación de Room 8, datos demostrativos, ausencia de backend, pagos simulados, restricciones de Terapeuta y dependencias de integración comprobada contra los archivos actuales.
