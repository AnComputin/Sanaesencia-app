# Walkthrough - Módulo Espacio de Bienestar (SanaEsencia)

Se ha integrado completamente el **Espacio de Bienestar** en la aplicación SanaEsencia, organizando actividades de estimulación cognitiva y pausas guiadas de autocuidado, respetando la arquitectura del proyecto, Jetpack Compose, Material 3, corrutinas con gestión limpia del ciclo de vida y pruebas unitarias automatizadas.

## Cambios Realizados

### 1. Acceso y Catálogo Central (`Espacio de Bienestar`)
- **[JuegosHubScreen.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/ui/views/juegos/JuegosHubScreen.kt)**: Rediseñado como catálogo **Espacio de Bienestar** dividido en dos secciones:
  - **🧠 Juegos mentales**: Memorice Zen, Gato Armónico, Camino del Bienestar.
  - **🧘 Pausas de bienestar**: Respiración Guiada, Pausa Consciente.
- **[HomeScreen.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/ui/views/HomeScreen.kt)**: Botón de acceso destacado **"🌿 Espacio de Bienestar"**.
- **[Navigation.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/navigation/Navigation.kt)**: Registro de las nuevas rutas de navegación (`respiracion_guiada` y `pausa_consciente`).

---

### 2. Pausas de Bienestar Guiadas
- **Respiración Guiada**:
  - **[RespiracionViewModel.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/viewmodel/RespiracionViewModel.kt)**: Lógica para la sesión de 1 minuto con fases de Inhalar (4s), Mantener (4s) y Exhalar (4s).
  - **[RespiracionGuiadaScreen.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/ui/views/juegos/RespiracionGuiadaScreen.kt)**: Interfaz con círculo expansivo/contractivo animado, temporizador, controles (Iniciar, Pausar, Reanudar, Reiniciar) y mensaje final sereno.
- **Pausa Consciente**:
  - **[PausaConscienteViewModel.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/viewmodel/PausaConscienteViewModel.kt)**: Lógica guiada paso a paso (Detenerse, Observar la respiración, Reconocer el entorno, Reconectar).
  - **[PausaConscienteScreen.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/ui/views/juegos/PausaConscienteScreen.kt)**: Tarjeta con guía, indicador de progreso (Paso X de 4), navegación secuencial y mensaje de cierre.

---

### 3. Juegos Mentales Integrados
- **Memorice Zen** ([MemoriceViewModel.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/viewmodel/MemoriceViewModel.kt), [MemoriceScreen.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/ui/views/juegos/MemoriceScreen.kt)).
- **Gato Armónico** ([GatoViewModel.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/viewmodel/GatoViewModel.kt), [GatoScreen.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/ui/views/juegos/GatoScreen.kt)).
- **Camino del Bienestar** ([CaminoViewModel.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/viewmodel/CaminoViewModel.kt), [CaminoBienestarScreen.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/main/java/com/example/sanaesenciaapp/ui/views/juegos/CaminoBienestarScreen.kt)).

---

### 4. Pruebas Unitarias
- **[PausasViewModelTest.kt](file:///C:/Users/VI-Alumno/StudioProjects/Sanaesencia-app/app/src/test/java/com/example/sanaesenciaapp/viewmodel/PausasViewModelTest.kt)**: Pruebas unitarias para las sesiones de Respiración Guiada y Pausa Consciente.

## Resultados de Verificación

### Pruebas Automatizadas
- Ejecutado `app:testDebugUnitTest`.
- **Resultado**: 53 pruebas ejecutadas con éxito (0 fallos).

```text
BUILD SUCCESSFUL in 2s
53 tests completed, 0 failed
```
