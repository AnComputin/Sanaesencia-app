# Juegos y pausas de bienestar

- **Responsable:** Alejandra Núñez
- **Actualizado:** 22-11-2026
- **Versión:** 1.0.0
- **Estado:** Implementado

## Juegos

- **Respiración Guiada:** ciclo de inhalar/mantener/exhalar durante 60 segundos; pausa, reanudación y reinicio.
- **Pausa Consciente:** cuatro pasos guiados de atención al cuerpo, respiración, entorno y reconexión.
- **Memorice Zen:** ocho parejas de símbolos; cuenta intentos y tiempo.
- **Gato Armónico:** tres en raya, dos jugadores o CPU fácil/experta, resultados y puntaje.
- **Camino del Bienestar:** tablero de 25 casillas con avances, retrocesos, meditación y meta.

## Pantallas y navegación

`JuegosHubScreen` es el listado/hub. `JuegosGraph` conecta a `respiracion_guiada`, `pausa_consciente`, `memorice_zen`, `gato_armonico`, `camino_bienestar`. Inicio abre el hub mediante callback. Cada pantalla vuelve con `popBackStack()`; atrás del sistema emplea la pila común. Todas las rutas declaradas tienen destino único.

## Archivos relacionados

- `navigation/JuegosGraph.kt` y `ui/views/juegos/JuegosHubScreen.kt`, `RespiracionGuiadaScreen.kt`, `PausaConscienteScreen.kt`, `MemoriceScreen.kt`, `GatoScreen.kt`, `CaminoBienestarScreen.kt`.
- `viewmodel/RespiracionViewModel.kt`, `PausaConscienteViewModel.kt`, `MemoriceViewModel.kt`, `GatoViewModel.kt`, `CaminoViewModel.kt`.
- Pruebas correspondientes en `app/src/test/java/com/example/sanaesenciaapp/viewmodel/`.

## ViewModels y estado

ViewModels: `RespiracionViewModel`, `PausaConscienteViewModel`, `MemoriceViewModel`, `GatoViewModel`, `CaminoViewModel`. Estado reactivo por StateFlow. CPU y cronómetros usan coroutines cancelables; sin repositorios ni persistencia.

## Estados y reglas

Las pantallas exponen estado inicial, juego activo y completado. No tienen carga/error de datos porque operan localmente; no hay una fuente externa cuya lectura pueda fallar. Reinicio limpia la ronda según las reglas de cada ViewModel. Camino genera dado aleatorio 1–6 en producción.

## Validaciones y acceso

No hay formularios ni datos de usuario. El hub se enlaza desde Home. Las interacciones rechazan movimientos inválidos, ocupados o posteriores a finalizar; Gato bloquea entrada cuando CPU piensa.

## Pruebas

AL-05 ejecutó compile, 200 unit tests y assemble. Coberturas incluyen inicialización, movimientos, CPU, meta, parejas, victoria, finalización, pausa/reanudación, reinicio y ruta de acceso desde Home revisada en grafo/callbacks.

## Limitaciones

El estado vive en ViewModel del destino: se conserva mientras dicho destino esté en la pila/configuración, pero se pierde al volver al hub y abrir una sesión nueva o cerrar la app. No hay telemetría de resultados.
