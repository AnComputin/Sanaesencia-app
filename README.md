# Sanaesencia

Aplicación móvil Android para consultar especialidades, agendar y gestionar citas del Centro Integrativo Sanaesencia (Los Vilos).
Proyecto desarrollado para la asignatura DSY1105 · Duoc UC.

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

##  Funcionalidades Implementadas

- **Autenticación:** Registro e inicio de sesión con validación por campo (RUT, teléfono, contraseña y fecha de nacimiento).
- **Catálogo de Servicios:** Consulta de especialidades y detalle informativo de cada servicio.
- **Reserva y Agendamiento:** Selección de especialidad, especialista, modalidad (presencial u online), fecha disponible, hora y paciente/beneficiario.
- **Pago Simulado:** Pasarela de pago ficticia con temporizador regresivo de 10 minutos.
- **Confirmación de Citas:** Pantalla de resumen post-pago con confirmación animada.
- **Gestión de Citas (Mis Citas):** Pestañas de citas *Próximas* e *Historial*, con opción de cancelación respetando la regla de 24 horas de anticipación.
- **Ubicación y Sede:** Cálculo de distancia a la sede presencial mediante GPS, apertura nativa en Google Maps y contacto directo por WhatsApp.
- **Perfil Terapeuta:** Panel para profesionales con pacientes asignados, alerta de riesgo de deserción (más de 30 días sin atención), agenda del día y detalle de continuidad.
- **Beneficiarios:** Registro persistente de menores o personas a cargo asociados a la cuenta del paciente y selección directa al reservar.
- **Administración:** Panel ADMIN para modalidad/agenda de especialidades y visualización en tiempo real de ocupación de los 8 boxes.

---

##  Recursos Nativos Utilizados

| Recurso | Dónde se usa | Permiso |
| :--- | :--- | :--- |
| **SQLite / Room** | Almacenamiento local de usuarios y catálogo de especialidades | Sin permiso requerido |
| **GPS / Ubicación** | Pantalla de Ubicación para calcular la distancia a la sede | `ACCESS_FINE_LOCATION` (solicitado en tiempo de ejecución) |
| **Calendario del dispositivo** | Pantallas de Confirmación y Mis Citas para agregar eventos | Mediante `Intent` con `ACTION_INSERT` (sin permiso requerido) |

---

##  Arquitectura y Estructura del Proyecto

El proyecto implementa la arquitectura **MVVM (Model-View-ViewModel)**, separando la lógica de negocio y el manejo de estado de la interfaz gráfica. Esta estructura facilita la mantenibilidad, escalabilidad y pruebas unitarias.

**Descripción de carpetas (`app/src/main/java/com/example/sanaesenciaapp/`):**
- **`data`**: Contiene la definición del estado global de la aplicación (`AppState`) y la capa de base de datos Room (`room/`).
- **`navigation`**: Define el grafo de navegación y las rutas entre pantallas con Navigation Compose (`Navigation.kt`).
- **`viewmodel`**: Contiene los ViewModels que gestionan el estado y la lógica de cada flujo (`CitaViewModel`, `AuthViewModel`, `TerapeutaViewModel`, etc.).
- **`ui/theme`**: Define la paleta de colores, tipografía y tema de Material 3 (`Color.kt`, `Theme.kt`, `Type.kt`).
- **`ui/components`**: Componentes visuales reutilizables como botones y fondos decorativos.
- **`ui/views`**: Pantallas de la aplicación organizadas por módulo (`auth`, `agenda`, `citas`, `catalogo`, `terapeuta`).
- **`utils`**: Clases auxiliares para validación de campos, geolocalización por GPS, integración con calendario y hashing de contraseñas.
- **`test`**: Pruebas unitarias para validar funciones auxiliares de seguridad y validaciones de formularios.

---

##  Cómo Ejecutar el Proyecto

1. Abrir el proyecto en **Android Studio**.
2. Esperar a que **Gradle** finalice la sincronización de dependencias (`Sync Project with Gradle Files`).
3. Conectar un dispositivo físico o iniciar un emulador con **Android 7.0 (API 24)** o superior.
4. Presionar el botón **Run** (o `Shift + F10`) en Android Studio.

> **Notas de prueba:**
> - Para probar la función "Agregar a mi calendario" se requiere tener una aplicación de calendario instalada en el dispositivo/emulador.
> - Para el cálculo de distancia a la sede, asegúrese de tener activada la ubicación (GPS) del equipo.

---

##  Usuarios de Prueba

Para probar PACIENTE o TERAPEUTA, regístrese desde la pantalla de **Registro** indicando los datos requeridos y seleccionando el rol correspondiente. Además existe un ADMIN ficticio para demostración: `admin@sanaesencia.cl` / `admin123`.
- **Paciente**: Acceso al flujo de agendamiento, pago y gestión de citas.
- **Terapeuta**: Acceso al perfil profesional, pacientes asignados y agenda del día.

---

##  Manejo de Ramas Git

- **`main`**: Versión estable del proyecto; solo se fusiona mediante Pull Request con aprobación del equipo.
- **`develop`**: Rama de integración continua donde se consolidan las funcionalidades de los desarrolladores.
- **Ramas personales**: Ramas de trabajo de cada integrante (`Anais`, `Rama-Mark`, `Ale`, `vale`).

---

##  Limitaciones Conocidas

- Los datos simulados del módulo terapeuta siguen siendo de demostración; las citas de pacientes, especialistas y beneficiarios sí se persisten en Room.
- El proceso de pago es una simulación visual sin conexión a una pasarela real.
- El almacenamiento de contraseñas utiliza un hashing con sal educativo (SHA-256) en lugar de PBKDF2/bcrypt.
