# Plan de Implementación: Espacio de Bienestar (Juegos Mentales y Pausas de Bienestar)

Ampliación del módulo de bienestar para consolidar el **"Espacio de Bienestar"**, organizándolo en dos secciones principales: **Juegos Mentales** (Memorice Zen, Gato Armónico, Camino del Bienestar) y **Pausas de Bienestar** (Respiración Guiada y Pausa Consciente).

## User Review Required

> [!IMPORTANT]
> - **Rediseño del Catálogo (`BienestarHubScreen`)**: Se actualizará la pantalla del hub/catálogo para renombrarse a **"Espacio de Bienestar"** e incluir las dos categorías organizadas:
>   1. **Juegos Mentales**: Memorice Zen, Gato Armónico, Camino del Bienestar.
>   2. **Pausas de Bienestar**: Respiración Guiada (1 min), Pausa Consciente (1-3 min).
> - **Acceso desde Home**: El botón en `HomeScreen` se actualizará a **"🌿 Espacio de Bienestar"**.
> - **Controles de Ciclo de Vida**: Las animaciones de respiración y temporizadores de pausas se gestionarán limpiamente mediante `coroutineScope` / `DisposableEffect` / `onCleared` para evitar fugas o tareas activas al salir.

## Proposed Changes

### Catálogo y Navegación

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/ui/views/HomeScreen.kt)
- Renombrar y estilizar el botón de acceso a **"🌿 Espacio de Bienestar"**.

#### [MODIFY] [JuegosHubScreen.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/ui/views/juegos/JuegosHubScreen.kt) (o `BienestarHubScreen.kt`)
- Actualizar el catálogo con título **"Espacio de Bienestar"**, texto de bienvenida profesional y dos secciones clarificadoras:
  - **Juegos mentales**: Memorice Zen, Gato Armónico, Camino del Bienestar.
  - **Pausas de bienestar**: Respiración Guiada, Pausa Consciente.

#### [MODIFY] [Navigation.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/app/src/main/java/com/example/sanaesenciaapp/navigation/Navigation.kt)
- Registrar nuevas rutas: `"respiracion_guiada"` y `"pausa_consciente"`.

---

### Módulo de Pausas de Bienestar

#### [NEW] [RespiracionViewModel.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/viewmodel/RespiracionViewModel.kt)
- Estado y lógica de la sesión de respiración guiada de 1 minuto (60 segs).
- Fases de respiración: Inhalar (4s), Mantener (4s), Exhalar (4s).
- Controles de Iniciar, Pausar, Reanudar, Reiniciar y Finalizar.

#### [NEW] [RespiracionGuiadaScreen.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/ui/views/juegos/RespiracionGuiadaScreen.kt)
- Interfaz interactiva con círculo animado que se expande y contrae sincronizado con la fase de respiración, temporizador regresivo, contador de tiempo y mensaje de cierre sereno.

#### [NEW] [PausaConscienteViewModel.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/viewmodel/PausaConscienteViewModel.kt)
- Lógica de la actividad guiada por pasos secuenciales (Detenerse, Observar la respiración, Reconocer el entorno, Volver a la rutina).
- Indicador de progreso (paso actual / total de pasos, tiempo), controles de avanzar, pausar y reiniciar.

#### [NEW] [PausaConscienteScreen.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/ui/views/juegos/PausaConscienteScreen.kt)
- Interfaz amable, tarjeta explicativa paso a paso con barra de progreso, controles interactivos y pantalla de finalización.

---

### Pruebas Unitarias

#### [NEW] [PausasViewModelTest.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/test/java/com/example/sanaesenciaapp/viewmodel/PausasViewModelTest.kt)
- Tests unitarios para las transiciones de estado de `RespiracionViewModel` y `PausaConscienteViewModel` (iniciar, pausar, avanzar paso, completar).

## Verification Plan

### Automated Tests
- Ejecutar pruebas unitarias Gradle: `./gradlew testDebugUnitTest`

### Manual Verification
- Navegar desde el Home al **Espacio de Bienestar**.
- Probar las dos secciones ("Juegos mentales" y "Pausas de bienestar").
- Iniciar una sesión de **Respiración Guiada**, verificar la animación del círculo expandiendo/contrayendo, pausar y reanudar, y comprobar el mensaje final.
- Iniciar la **Pausa Consciente**, avanzar paso a paso, verificar el progreso y finalizar la actividad.
