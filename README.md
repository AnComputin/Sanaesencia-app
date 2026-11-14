# Sanaesencia

Aplicación móvil Android del Centro Integrativo Sanaesencia, ubicado en Los Vilos. Permite consultar especialidades, elegir un especialista, agendar una cita y gestionarla. Proyecto de la asignatura DSY1105, Duoc UC.

---

## Integrantes y Docente

**Equipo de Desarrollo:**
- Mark Toro
- Valentina Guerrero
- Alejandra Núñez
- Anaís Riquelme

**Docente:**
- Nancy Beatriz Bernal Sanchez

---

##  Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **Interfaz de Usuario:** Jetpack Compose con Material 3
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Persistencia Local:** Room (SQLite)
- **Navegación:** Navigation Compose
- **Asincronía y Estado:** Kotlin Coroutines y StateFlow

*Planificado para la entrega final: Retrofit y backend.*

---

## Qué hace la aplicación

La aplicación tiene tres tipos de usuario: paciente, terapeuta y administrador.

*Paciente*
- Se registra e inicia sesión. Cada campo se valida (RUT, teléfono, contraseña y fecha de nacimiento).
- Revisa las especialidades y el detalle de cada una.
- Elige un especialista y reserva con modalidad, fecha y hora. Puede reservar para sí mismo o para un menor o persona a cargo.
- Paga en una pasarela simulada con un temporizador de 10 minutos.
- Ve la confirmación con el box asignado y puede agregar la cita al calendario del teléfono.
- En "Mis citas" ve sus citas próximas y su historial. Puede cancelar con 24 horas o más de anticipación.
- Ve la distancia a la sede con GPS, abre la dirección en el mapa y escribe por WhatsApp.

*Terapeuta (bloqueado)*
- La agenda y la lista de pacientes no muestran datos hasta contar con una asociación persistida y verificable entre la cuenta autenticada y un especialista.
- El módulo no consulta ni actualiza citas reales. Las notas clínicas están deshabilitadas porque no existe almacenamiento autorizado.

*Administrador*
- Cambia la modalidad de cada especialidad (presencial, online o ambas).
- Pausa o reactiva la agenda de una especialidad.
- Ve en tiempo real cuáles de los 8 boxes del centro están ocupados.

## Cómo se reservan las citas

- Cada cita dura 60 minutos.
- Un horario no se ofrece si el especialista ya tiene otra cita en ese momento.
- Una cita presencial usa uno de los 8 boxes y el sistema asigna el primero libre. Si los 8 están ocupados, ese horario se bloquea.
- Las citas online no usan box.
- La reserva se confirma de una sola vez en la base de datos. Así dos personas no pueden tomar la misma hora.

## Recursos nativos

| Recurso | Dónde se usa | Permiso |
| --- | --- | --- |
| SQLite (Room) | Usuarios, especialidades, especialistas, citas y beneficiarios | No necesita |
| GPS | Pantalla de ubicación, para calcular la distancia a la sede | Ubicación, que se pide al tocar el botón |
| Calendario del teléfono | Confirmación y Mis citas, botón "Agregar a mi calendario" | No necesita (usa un Intent) |

## Arquitectura y estado de implementación

La interfaz usa Jetpack Compose y Material 3 con arquitectura MVVM. `AppContainer` expone los componentes compartidos y `AppViewModelProvider` registra módulos por funcionalidad mediante `AppViewModelModule`. La sesión y algunos datos compartidos de agenda aún usan `AppState`; las pantallas propias de catálogo y ficha de beneficiarios usan ViewModels. Los grafos conservan la navegación y las pantallas reciben eventos mediante callbacks.

Carpetas dentro de `app/src/main/java/com/example/sanaesenciaapp/`:

- `data`: `AppState`, repositorios y base de datos Room.
- `di`: `AppContainer` crea y expone la base de datos, DAO, `UsuarioRepository` y `SesionRepository`.
- `data/room`: entidades, DAO, migraciones y datos iniciales. El esquema actual de Room es versión 7.
- `navigation`: `Navigation.kt` define el `NavHost`; los módulos Catálogo, Juegos y Terapeuta usan grafos separados. Juegos comunica navegación a sus pantallas mediante callbacks.
- `viewmodel`: ViewModels de autenticación, citas, catálogo, administración, terapeuta y juegos.
- `viewmodel/AppViewModelProvider.kt`: fábrica modular con soporte para `CreationExtras` y `SavedStateHandle`; Navigation registra los módulos de Agenda, Terapeuta, Catálogo y Beneficiarios.
- `data/repository/CatalogoRepository.kt` y `CatalogoViewModel.kt`: observan especialidades y especialistas desde Room con StateFlow. El seed incluye 7 especialidades y 30 especialistas.
- `data/repository/BeneficiarioRepository.kt` y `BeneficiarioViewModel.kt`: realizan operaciones de beneficiarios vinculadas a la sesión, con aislamiento de propietario en DAO/repositorio.
- `ui/theme` y `ui/components`: tema y componentes compartidos.
- `ui/views`: pantallas agrupadas por flujo (`auth`, `catalogo`, `agenda`, `citas`, `terapeuta`, `juegos` y `admin`).
- `utils`: validaciones, hashing de contraseñas, calendario y ubicación.

Estado de los flujos según el código:

- Catálogo: especialidades y especialistas se observan desde Room mediante `CatalogoRepository` y `CatalogoViewModel`. La selección que comparte con la agenda todavía usa estado de `AppState`.
- Beneficiarios: la pantalla de ficha usa `BeneficiarioViewModel` y `BeneficiarioRepository`. El propietario se determina desde la sesión; las operaciones están restringidas por propietario. La agenda aún consume el estado compartido de beneficiarios de `AppState`.
- Terapeuta: el módulo está registrado, pero permanece bloqueado y sin datos porque no existe asociación persistida cuenta-terapeuta a especialista. Las notas no se guardan y están deshabilitadas.
- Juegos: los juegos usan ViewModels propios, estado en memoria, navegación mediante callbacks y pruebas unitarias.
- Sesión: `SesionRepository` mantiene la sesión en memoria; no se restaura al cerrar y volver a abrir la aplicación.
- Servicios remotos: Retrofit y un backend no están configurados en las dependencias actuales.

Las pruebas unitarias están en `app/src/test` y cubren utilidades, repositorios y ViewModels, incluidos módulos de catálogo, beneficiarios y juegos. Las pruebas Room instrumentadas están en `app/src/androidTest`. Validación local del 14 de noviembre de 2026: `compileDebugKotlin`, `testDebugUnitTest` (109 aprobadas, 0 omitidas, 0 fallidas), `compileDebugAndroidTestKotlin` y `assembleDebug` finalizaron correctamente. `connectedDebugAndroidTest` no se ejecutó: `adb devices -l` no mostró dispositivos conectados.

## Requisitos y ejecución

- Android Studio con el Android SDK requerido por el proyecto (compile SDK 37).
- JDK compatible con la versión de Gradle Wrapper configurada (Gradle 9.5.0).
- La aplicación admite Android 7.0 (API 24) o superior.

Abre el proyecto en Android Studio y espera a que termine la sincronización de Gradle. Desde PowerShell en la raíz del proyecto puedes ejecutar:

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug
```

Las pruebas instrumentadas requieren un dispositivo o emulador conectado y pueden ejecutarse con `.\gradlew.bat :app:connectedDebugAndroidTest`. No hay configuración de firma release en `app/build.gradle.kts`; el artefacto release, si se genera, no está firmado para distribución.

Para probar el calendario se necesita una aplicación de calendario instalada. La ubicación se solicita al tocar el botón correspondiente.

## Datos de demostración

La carga inicial incluye datos de demostración para el prototipo. No se publican credenciales de acceso en este README.

## Ramas de Git

- main: versión estable. Solo se actualiza con un Pull Request aprobado por el equipo.
- develop: rama de integración, donde se juntan los avances.
- Ramas personales: Anais, Rama-Mark, Ale y vale.

## Limitaciones conocidas

- El portal terapeuta no tiene acceso a citas reales hasta que exista una asociación persistida y verificable entre la cuenta autenticada y `especialistaId`.
- Las notas clínicas no se persisten y la edición está deshabilitada.
- El pago es una simulación, sin conexión a una pasarela real.
- La autenticación migra credenciales históricas al formato PBKDF2 implementado por `Seguridad`; la sesión permanece solo en memoria y no se restaura al reiniciar la aplicación.
- Los recursos de texto se encuentran en `res/values`; no se declaran traducciones a otros idiomas.
