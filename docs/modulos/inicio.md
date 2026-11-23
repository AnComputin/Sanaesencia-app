# Inicio

- **Responsable:** Alejandra Núñez
- **Actualizado:** 22-11-2026
- **Versión:** 1.0.0
- **Estado:** Implementado

## Objetivo

Presentar la aplicación, permitir entrar/registrarse y ofrecer accesos a los flujos según sesión y rol.

## Pantallas

- `SplashScreen`: bienvenida inicial.
- `InicioScreen`: acceso a Login y Registro.
- `HomeScreen`: pantalla principal con catálogo destacado y accesos a reserva, citas, ubicación, juegos y tienda; muestra controles según la sesión actual.
- Paneles de administración y terapeuta se abren después del flujo de autenticación de su rol.

## Navegación

`Navigation.kt` inicia en `splash`; al continuar navega a `inicio`. Desde Inicio se navega a `login` o `registro`. Después de autenticar, `AuthGraph` resuelve el destino por rol: ADMIN → `home_admin`, TERAPEUTA → `home_terapeuta`, otros → `home`. Home conecta con especialidades, ubicación, Juegos y Tienda mediante callbacks y grafos del módulo.

## Archivos relacionados

- `ui/views/InicioScreen.kt`, `ui/views/HomeScreen.kt`, `navigation/Navigation.kt`, `navigation/AuthGraph.kt`.
- `viewmodel/CatalogoViewModel.kt` para el catálogo destacado.

## ViewModels y repositorios

Home recolecta catálogo desde `CatalogoViewModel`; sesión se lee desde `AppState.usuarioActual`, sincronizado con `SesionRepository`. No hay un repositorio propio de Inicio.

## Estados y validaciones

Inicio y Home son pantallas sin lectura remota propia. Home presenta estados de carga/error/vacío del catálogo destacado. Formularios Login/Registro tienen sus propias validaciones, gestionadas en sus módulos.

## Reglas de acceso

La autenticación y rol pertenecen al flujo de Auth. Los accesos de Home dependen de sesión; reserva y operaciones protegidas verifican la sesión conforme a sus grafos. La pantalla de Terapeuta por sí sola no concede acceso clínico: su contenido se mantiene bloqueado mientras falta asociación autorizada.

## Dependencias

- Navigation Compose y callbacks.
- `AuthGraph`, `CatalogoGraph`, `AgendaGraph`, `JuegosGraph`, `TiendaGraph`.
- `AppState` / `SesionRepository` (propiedad de infraestructura compartida).
- `CatalogoViewModel` para especialidades destacadas.

## Pruebas

- `InicioNavigationTest`: accesos/decisión de destino según sesión y rol, reserva y rutas de especialidad.
- En validación de Inicio (AL-01): compile y assemble correctos; suite con 191 aprobadas. En la validación posterior AL-06, la suite completa alcanzó 204 aprobadas, 0 fallidas, 0 omitidas.

## Limitaciones

- La sesión permanece en memoria.
- El panel de Terapeuta permanece bloqueado clínicamente por falta de relación persistida.
