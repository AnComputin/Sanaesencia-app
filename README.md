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

*Terapeuta*
- Ve sus pacientes asignados y una alerta si alguno lleva más de 30 días sin atención.
- Ve su agenda del día, cambia el estado de cada cita y revisa la continuidad del tratamiento.

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

## Estructura del proyecto

La arquitectura es MVVM. Las pantallas solo dibujan, los ViewModels manejan el estado y las reglas, y Room guarda los datos.

Carpetas dentro de app/src/main/java/com/example/sanaesenciaapp/:

- data: el estado global (AppState) y la base de datos Room, con sus tablas y consultas.
- navigation: el mapa de pantallas y rutas.
- viewmodel: los ViewModels de cada flujo (autenticación, citas, terapeuta y administrador).
- ui/theme: colores, tipografía y tema.
- ui/components: botones, campos y fondos reutilizables.
- ui/views: las pantallas, ordenadas por módulo (auth, catalogo, agenda, citas, terapeuta y admin).
- utils: validaciones, cifrado de contraseñas, calendario y GPS.

Las pruebas unitarias están en app/src/test y revisan las validaciones y el cifrado.

## Cómo ejecutar

1. Abrir el proyecto con Android Studio.
2. Esperar a que Gradle termine de sincronizar.
3. Conectar un celular o iniciar un emulador con Android 7.0 (API 24) o superior.
4. Si la app ya estaba instalada de antes, desinstalarla o borrar sus datos.
5. Presionar el botón Run.

Para probar el calendario se necesita una app de calendario instalada. Para el GPS hay que activar la ubicación.

## Usuarios de prueba

- *Paciente y terapeuta:* se crean desde la pantalla de Registro, eligiendo el rol.
- *Administrador:* admin@sanaesencia.cl con contraseña admin123. Es una cuenta de demostración.

## Ramas de Git

- main: versión estable. Solo se actualiza con un Pull Request aprobado por el equipo.
- develop: rama de integración, donde se juntan los avances.
- Ramas personales: Anais, Rama-Mark, Ale y vale.

## Limitaciones conocidas

- Los datos del módulo terapeuta son de demostración. Las citas, los especialistas y los beneficiarios de los pacientes sí se guardan en Room.
- El pago es una simulación, sin conexión a una pasarela real.
- Las contraseñas se guardan con un cifrado educativo (SHA-256 con sal). En un sistema real se usaría PBKDF2, bcrypt o Argon2.
- La sesión no se conserva al cerrar la aplicación.
