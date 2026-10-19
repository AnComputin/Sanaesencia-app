# Sanaesencia App

Aplicación móvil Android para el Centro Integrativo Sanaesencia (Los Vilos):
catálogo de especialidades, agendamiento de citas y seguimiento de atenciones.

Asignatura DSY1105 · Kotlin · Jetpack Compose · Material 3 · Room

## Docente 
Nancy Beatriz Bernal Sanchez

## Integrantes
- Mark Toro: núcleo, Room y navegación.
- Valentina Guerrero: autenticación y formularios.
- Alejandra Núñez: diseño visual y catálogo.
- Anaís Riquelme: agendamiento y gestión de citas.

## Funcionalidades
- Registro e inicio de sesión con validaciones por campo.
- Catálogo de especialidades y servicios.
- Agendamiento: modalidad, fecha, hora y datos del paciente.
- Pago simulado con temporizador de 10 minutos.
- Confirmación, Mis citas, historial y cancelación (regla de 24 h).

## Recursos nativos
| Recurso | Dónde se usa | Permiso |
|---|---|---|
| SQLite (Room) | Usuarios y datos de la app | No requiere |
| GPS / ubicación | Pantalla de ubicación del centro | Ubicación (se pide al usar el botón) |
| Calendario | Confirmación y Mis citas, botón "Agregar a mi calendario" | No requiere (Intent ACTION_INSERT) |

## Arquitectura
MVVM: `ui/views` (pantallas), `viewmodel` (estado), `data` (Room y AppState),
`navigation`, `utils`.


## Usuarios de prueba



