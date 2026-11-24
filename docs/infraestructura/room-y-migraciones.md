# Room y migraciones

## Identificación

- **Proyecto:** SanaEsencia Android.
- **Responsable:** Anaís — infraestructura Android y persistencia Room.
- **Fecha de actualización:** 2026-11-24.
- **Versión del documento:** 2.0.
- **Estado:** Validado mediante compilación y pruebas instrumentadas en emulador para las rutas v6→v8 y v7→v8. La compatibilidad histórica anterior a v6 no está demostrada por falta de schemas JSON v1–v5 exportados.
- **SHA de Origen:** `99b273994299ea3d6b58084e8ffbb11123125f60`
- **SHA Final:** `a1aecc5bcbcbb327450c66e644d1c4e398234477` (Sincronizado fast-forward con `origin/develop`).
- **Archivos relacionados:**
  - `app/src/main/java/com/example/sanaesenciaapp/data/room/AppDatabase.kt`
  - `app/src/androidTest/java/com/example/sanaesenciaapp/data/room/UsuarioDatabaseMigrationTest.kt`
  - `app/schemas/com.example.sanaesenciaapp.data.room.AppDatabase/6.json`
  - `app/schemas/com.example.sanaesenciaapp.data.room.AppDatabase/7.json`
  - `app/schemas/com.example.sanaesenciaapp.data.room.AppDatabase/8.json`
  - `gradle/libs.versions.toml`
  - `app/build.gradle.kts`

## Arquitectura y configuración

El catálogo de versiones Gradle declara Room **2.8.4**. El proyecto referencia `room-runtime`, `room-ktx`, `room-compiler` y `room-testing` desde el catálogo; KSP ejecuta el compilador de Room.

La configuración de KSP en `app/build.gradle.kts` asigna `room.schemaLocation` a `$projectDir/schemas`. La base `AppDatabase` está declarada con `version = 8` y `exportSchema = true`. Su proveedor registra en orden las migraciones 2→3, 3→4, 4→5, 5→6, 6→7 y 7→8.

**Estrategia No Destructiva:**
El builder en `AppDatabase.getInstance()` **NO** utiliza ni habilita fallback destructivo (`.fallbackToDestructiveMigration(false)` configurado en `develop`). Si no existe una ruta de migración para la versión instalada, Room falla al abrir la base en vez de borrar datos de producción. La prueba de migración ausente comprueba este comportamiento para v7→v8 y vuelve a consultar la base original.

## Migraciones implementadas

| Ruta | Efecto implementado |
|---|---|
| **2→3** | Agrega a `usuarios` las columnas `rut`, `telefono`, `fechaNacimiento`, `comuna` y `rol`, con los valores por defecto definidos en el SQL. |
| **3→4** | Crea `especialistas`, su índice por `especialidadId` y la tabla `citas` con las columnas declaradas en la migración. |
| **4→5** | Adapta posibles estructuras previas de `especialistas` y `citas`; cuando corresponde, copia filas a tablas nuevas y transforma los campos de citas. También normaliza el catálogo a siete especialidades mediante actualizaciones e inserciones. |
| **5→6** | Crea `beneficiarios`, sus índices —incluido el índice único por propietario y RUT— y agrega `beneficiarioId` nullable a `citas`. |
| **6→7** | Detecta grupos de correos que colisionarían sin distinguir mayúsculas. Si encuentra colisiones, lanza una excepción antes de reconstruir `usuarios`. Si no hay colisiones, recrea la tabla con `email COLLATE NOCASE`, copiando los valores existentes tal como están, incluidos los espacios periféricos. |
| **7→8** | Crea `productos`, `ventas` y `detalle_ventas`, con índice de `detalle_ventas.ventaId`, e inserta seis productos de ejemplo con `INSERT OR IGNORE`. No modifica las tablas preexistentes. |

## Integridad y manejo de fallos

La migración 6→7 aplica unicidad de correo sin distinguir mayúsculas y conserva la grafía y espacios ya almacenados. Los registros que colisionan bajo `NOCASE` hacen que la migración aborte; Room ejecuta la migración en transacción. La prueba verifica que el usuario permanezca en versión 6, que sigan existiendo ambos registros y que no quede la tabla temporal `usuarios_nuevo`.

Las pruebas también verifican:
- Conservación de usuario, cita y beneficiario al migrar v6→v8 y v7→v8.
- Versión final 8, creación de tablas de venta y detalle, y seis productos de seed.
- Que un duplicado de correo con distinta capitalización no reemplace el registro original.
- Que el correo en conflicto no aparezca en el mensaje de error.
- Que una migración no registrada v7→v8 falle e identifique la ruta ausente sin alterar la versión ni los datos existentes ni crear `productos`.

## Pruebas y evidencia de ejecución

### 1. Pruebas Instrumentadas (`AndroidTest`)
Ejecutadas en el emulador AVD `Argon2_API_24` (`emulator-5554`), Android 7.0 (API Level 24), con `sys.boot_completed=1`.

Comando:
```powershell
.\gradlew.bat :app:connectedDebugAndroidTest --console=plain
```
**Resultado:** **`BUILD SUCCESSFUL`** — **26 pruebas ejecutadas, 26 aprobadas, 0 fallos, 0 errores (100% PASS)**.

| Caso de Prueba Instrumentada | Resultado |
|---|---|
| `UsuarioDatabaseMigrationTest.migracionesProductivasDesdeVersion6Hasta8ConservanDatosYTablas` | Aprobado |
| `UsuarioDatabaseMigrationTest.migracionProductivaDeVersion7a8ConservaLosDatosExistentes` | Aprobado |
| `UsuarioDatabaseMigrationTest.migracionNoDisponibleFallaYConservaLaBaseOriginal` | Aprobado |
| `UsuarioDatabaseMigrationTest.colisionEnMigracion6a7RevierteSinEliminarRegistros` | Aprobado |
| `BeneficiarioDaoRoomTest` (12 pruebas de persistencia beneficiarios) | Aprobado |
| `UsuarioRepositoryRoomTest` (9 pruebas de persistencia repositorios) | Aprobado |
| `ExampleInstrumentedTest` (1 prueba) | Aprobado |

Reporte XML: `app/build/outputs/androidTest-results/connected/debug/`

### 2. Pruebas Unitarias (`UnitTest`)
Comando:
```powershell
.\gradlew.bat :app:testDebugUnitTest --console=plain
```
**Resultado:** **`BUILD SUCCESSFUL`** — **204 pruebas unitarias ejecutadas, 204 aprobadas, 0 fallos (100% PASS)**.

### 3. Compilaciones de Verificación
- `.\gradlew.bat :app:compileDebugKotlin` -> **`BUILD SUCCESSFUL`**
- `.\gradlew.bat :app:compileDebugAndroidTestKotlin` -> **`BUILD SUCCESSFUL`**
- `.\gradlew.bat :app:assembleDebug` -> **`BUILD SUCCESSFUL`**
- `git diff --check` -> **Limpio** (sin errores de formato/espacios).

## Schemas y compatibilidad histórica

Los únicos schemas exportados versionados encontrados son:
- `app/schemas/com.example.sanaesenciaapp.data.room.AppDatabase/6.json`
- `app/schemas/com.example.sanaesenciaapp.data.room.AppDatabase/7.json`
- `app/schemas/com.example.sanaesenciaapp.data.room.AppDatabase/8.json`

Compatibilidad demostrada:
- **v6→v7→v8 y v7→v8:** Validadas instrumentalmente en AVD API 24 con los casos descritos.
- **Migraciones 2→3 hasta 5→6:** Presentes en el código, pero sus esquemas históricos anteriores a v6 no están demostrados por schemas exportados.
- **v1→v8:** No hay una ruta migratoria comprobada de v1; no se afirma compatibilidad anterior a v6.

## Estado de Git y Publicación

- **Working Tree:** `working tree clean` (0 cambios pendientes).
- **Publicación:** Cambios mantenidos localmente en la rama `Anais`. No se ha realizado commit nuevo, push ni merge adicional.
