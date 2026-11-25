# CONTRATO DE INTEGRACIÓN MODULAR Y TRABAJO PARALELO — SANAESENCIA

Este documento establece la normativa técnica y el procedimiento de coordinación para que Anaís, Mark, Valentina y Alejandra desarrollen sus módulos simultáneamente en sus respectivas ramas sin generar conflictos Git ni romper la arquitectura común.

---

## 1. Distribución Oficial de Responsabilidades y Propiedad de Archivos

### Anaís (`Anais` -> `develop`)
- **Responsabilidad:** Infraestructura, base de datos Room, seguridad de sesión, inyección de dependencias central y arquitectura modular de navegación y ViewModels.
- **Propiedad Exclusiva:**
  - `MyApplication.kt`
  - `AppContainer.kt`
  - `AppViewModelProvider.kt`
  - `UsuarioEntity.kt`, `UsuarioDao.kt`, `AppDatabase.kt` (y esquemas/migraciones)
  - `UsuarioRepository.kt`, `SesionRepository.kt`
  - `Seguridad.kt`
  - `build.gradle.kts`, `libs.versions.toml`, `.gitignore`
  - `navigation/Navigation.kt` (Raíz del NavHost)

### Mark (`Rama-Mark`)
- **Responsabilidad:** Autenticación de usuario, pantallas de Login y Registro, vista de administración, roles y cierre de sesión.
- **Propiedad Exclusiva:**
  - `viewmodel/AuthViewModel.kt`, `viewmodel/AuthUiState.kt`
  - `ui/views/LoginScreen.kt`, `ui/views/RegistroScreen.kt`
  - `ui/views/admin/HomeAdminScreen.kt`
  - `navigation/AuthGraph.kt` (Rutas del módulo de autenticación)

### Valentina (`vale`)
- **Responsabilidad:** Agenda, sistema de citas, selección de especialista/horarios, pasarela de pagos y vista de citas del paciente.
- **Propiedad Exclusiva:**
  - `data/room/CitaEntity.kt`, `data/room/CitaDao.kt`
  - `viewmodel/CitaViewModel.kt`, `viewmodel/CitaViewModelFactory.kt`
  - `ui/views/agenda/AgendarScreen.kt`, `ui/views/agenda/PagoScreen.kt`, `ui/views/agenda/ConfirmacionScreen.kt`
  - `ui/views/citas/MisCitasScreen.kt`
  - `navigation/AgendaGraph.kt` (Rutas de agendamiento y pagos)

### Alejandra (`Ale`)
- **Responsabilidad:** Inicio, catálogo de servicios, gestión de beneficiarios, portal del terapeuta, juegos cognitivos y documentación comercial.
- **Propiedad Exclusiva:**
  - `ui/views/HomeScreen.kt`, `ui/views/InicioScreen.kt`, `ui/views/SplashScreen.kt`
  - `ui/views/catalogo/DetalleServicioScreen.kt`, `ui/views/catalogo/SeleccionEspecialistaScreen.kt`
  - `ui/views/auth/FichaBeneficiarioScreen.kt`
  - `ui/views/terapeuta/*` (Vista y ViewModel de Terapeuta)
  - `ui/views/juegos/*` (Hub y minijuegos de bienestar)
  - `navigation/CatalogoGraph.kt`, `navigation/TerapeutaGraph.kt`, `navigation/JuegosGraph.kt`
  - `README.md`

---

## 2. Procedimiento para Registrar Repositorios en AppContainer

`AppContainer.kt` es de propiedad exclusiva de **Anaís**. Para agregar un nuevo repositorio o DAO al contenedor general:

1. **Desarrollo Local:** El integrante crea sus clases `Entity`, `Dao` y `Repository` en sus propios paquetes dentro de su rama.
2. **Solicitud a Anaís:** El integrante notifica a Anaís con los detalles:
   - Nombre de la clase Repositorio y su constructor.
   - DAOs o dependencias que requiere del `AppDatabase`.
   - ViewModels que consumirán el repositorio.
3. **Registro por Anaís:** Anaís incorpora el repositorio en `AppContainer.kt` en la rama `develop`.
4. **Verificación:** El integrante actualiza su rama desde `develop` y consume la propiedad pública expuesta en `AppContainer`.

---

## 3. Uso del Patrón AppViewModelModule

Para evitar que todos los integrantes editen el mismo archivo `AppViewModelProvider.kt`, se utiliza el contrato `AppViewModelModule`:

```kotlin
interface AppViewModelModule {
    fun supports(modelClass: Class<out ViewModel>): Boolean
    fun create(
        modelClass: Class<out ViewModel>,
        appContainer: AppContainer
    ): ViewModel
}
```

### Reglas para los integrantes:
1. Crear una implementación en su paquete propio (ejemplo: `AuthViewModelModule` en `viewmodel/`).
2. Responder `true` en `supports(...)` únicamente para sus ViewModels.
3. En `create(...)`, instanciar su ViewModel utilizando los repositorios provistos por `appContainer`.
4. Registrar su módulo pasando una instancia a `AppViewModelProvider`.

---

## 4. Incorporación de Rutas de Navegación con NavGraphBuilder

Para evitar conflictos en `Navigation.kt`, cada módulo declara sus rutas mediante **funciones de extensión** sobre `NavGraphBuilder`:

- `NavGraphBuilder.authGraph(navController, appState)` -> `navigation/AuthGraph.kt` (Mark)
- `NavGraphBuilder.agendaGraph(navController, appState, citaViewModel)` -> `navigation/AgendaGraph.kt` (Valentina)
- `NavGraphBuilder.catalogoGraph(navController, appState, citaViewModel)` -> `navigation/CatalogoGraph.kt` (Alejandra)
- `NavGraphBuilder.terapeutaGraph(navController, appState, terapeutaViewModel)` -> `navigation/TerapeutaGraph.kt` (Alejandra)
- `NavGraphBuilder.juegosGraph(navController)` -> `navigation/JuegosGraph.kt` (Alejandra)

**Norma de edición:** Cada integrante modifica **únicamente** su archivo de grafo de navegación (`AuthGraph.kt`, `AgendaGraph.kt`, etc.). `Navigation.kt` solo se edita para llamar al grafo correspondiente.

---

## 5. Reglas de Transición y Depreciación de AppState

1. `AppState.kt` es un estado temporal y monolítico.
2. **Queda prohibido eliminar `AppState.kt`** mientras Valentina y Alejandra conserven vistas que dependan de `appState.usuarioActual`, `appState.beneficiarios` o selecciones de catálogo.
3. Cuando todos los integrantes hayan migrado sus vistas a colectar `StateFlow` desde sus propios ViewModels y `SesionRepository`, Mark coordinará la eliminación definitiva de `AppState.kt`.

---

## 6. Flujo Git Obligatorio para Integración

1. **Fusión Únicamente en `develop`:** Ningún integrante hace merge directo con la rama de otro integrante. Todo se fusiona hacia `develop`.
2. **Merge Commits:** Usar `git merge --no-ff` para preservar el historial de características. Queda prohibido `git push --force`.
3. **Archivos Excluidos de Git:** Nunca incluir carpetas `.idea/`, archivos `.jks`, claves de firma ni archivos de configuración local en commits o PRs.
