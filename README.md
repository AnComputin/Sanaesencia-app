# SanaEsencia

Aplicación Android del Centro Integrativo SanaEsencia, ubicado en Los Vilos. El proyecto académico permite conocer servicios terapéuticos, reservar atenciones, gestionar beneficiarios, usar actividades de bienestar y consultar una tienda local de productos de demostración.

## Objetivo

Reunir en una aplicación los flujos de atención del centro y herramientas de bienestar, respetando la sesión autenticada, la disponibilidad de agenda y el aislamiento de datos por cuenta.

## Tecnologías

- Kotlin, Jetpack Compose y Material 3.
- MVVM con ViewModels y StateFlow.
- Kotlin Coroutines.
- Navigation Compose mediante grafos por módulo.
- Room/SQLite para persistencia local.
- Android SDK: compile/target 37; min SDK 24.
- AGP 9.3.3, Kotlin 2.2.10, Room 2.8.4 (versiones declaradas en el proyecto).

No hay Retrofit ni backend configurados. Las operaciones descritas como simuladas no cobran dinero ni llaman a una pasarela.

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

- Room/SQLite local es la fuente de especialidades, especialistas, citas, beneficiarios, productos y ventas.
- La sesión vive en memoria y no se restaura al reiniciar la aplicación.
- Beneficiarios se consultan y mutan limitados por el correo titular derivado de la sesión; las consultas del DAO también restringen por propietario.
- Los productos iniciales se insertan como **datos de demostración** al crear/actualizar la base. No representan una oferta comercial real. No hay imágenes fotográficas ni backend de productos.
- Las ventas de tienda se guardan localmente; el pago tiene estado simulado. No se almacenan datos bancarios.
- Las citas se persisten y aplican reglas de solapamiento y boxes dentro de la capa de Agenda.
- Las notas clínicas de Terapeuta no se persisten.

## Autenticación y seguridad

`SesionRepository` autentica contra `UsuarioRepository` y expone identidad sin entregar hashes a los módulos. La sesión es volátil. Los datos de beneficiarios se limitan al titular autenticado. El rol por sí solo no habilita acceso a datos clínicos: falta una asignación verificable cuenta-terapeuta-especialista-paciente. No se publican credenciales de prueba, tokens ni claves en este README.

## Estado de implementación

- **Implementados:** Inicio, catálogo terapéutico, Beneficiarios, Juegos y sección de Productos con compra simulada.
- **Parcial / bloqueado por dependencia:** portal Terapeuta. Sus pantallas están presentes, pero la lectura clínica y edición permanecen deshabilitadas hasta tener relación y autorización persistidas.
- **No integrado:** backend, API comercial, pasarela de pago real e imágenes remotas.

## Instalación y ejecución

1. Abrir el proyecto en Android Studio y esperar la sincronización de Gradle.
2. Usar JDK compatible con el Gradle Wrapper del proyecto y Android SDK 37.
3. Ejecutar la app desde la configuración Android `app` en un emulador o dispositivo API 24+.

Desde PowerShell en la raíz:

```powershell
.\gradlew.bat :app:assembleDebug
```

El APK debug queda en `app/build/outputs/apk/debug/`.

## Pruebas

Comandos disponibles:

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

En la validación AL-06 de la rama Ale se ejecutaron compile, suite unitaria y assemble: **204 pruebas unitarias aprobadas, 0 fallidas y 0 omitidas**. Esa cifra corresponde a la ejecución registrada; no implica que cada flujo se haya probado en un dispositivo. Las pruebas instrumentadas requieren emulador/dispositivo conectado.

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
