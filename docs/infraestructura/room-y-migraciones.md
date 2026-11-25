# Room y migraciones

## Identificación

- **Proyecto:** SanaEsencia Android.
- **Responsable:** Anaís — infraestructura Android y persistencia.
- **Fecha de actualización:** 2026-11-23.
- **Versión del documento:** 1.0.
- **Estado:** validado mediante pruebas instrumentadas para las rutas v6→v8 y v7→v8. La compatibilidad histórica anterior a v6 no está demostrada.
- **Base de referencia:** `d57e88cee9b32d2a11921deb6cd79cd6d5ac1a57`, worktree aislado de AN-03.
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

El builder no configura fallback destructivo. Si no existe una ruta de migración para la versión instalada, Room falla al abrir la base en vez de reemplazarla mediante esa política. La prueba de migración ausente comprueba este comportamiento para v7→v8 y vuelve a consultar la base original.

## Migraciones implementadas

Las descripciones siguientes reflejan el SQL de `AppDatabase.kt`; la existencia de código de migración no demuestra por sí sola que un esquema histórico no exportado haya sido reproducido correctamente.

| Ruta | Efecto implementado |
|---|---|
| **2→3** | Agrega a `usuarios` las columnas `rut`, `telefono`, `fechaNacimiento`, `comuna` y `rol`, con los valores por defecto definidos en el SQL. |
| **3→4** | Crea `especialistas`, su índice por `especialidadId` y la tabla `citas` con las columnas declaradas en la migración. |
| **4→5** | Adapta posibles estructuras previas de `especialistas` y `citas`; cuando corresponde, copia filas a tablas nuevas y transforma los campos de citas. También normaliza el catálogo a siete especialidades mediante actualizaciones e inserciones. La conversión contiene mapeos explícitos y valores de respaldo; deben considerarse al evaluar datos históricos concretos. |
| **5→6** | Crea `beneficiarios`, sus índices —incluido el índice único por propietario y RUT— y agrega `beneficiarioId` nullable a `citas`. |
| **6→7** | Detecta grupos de correos que colisionarían sin distinguir mayúsculas. Si encuentra colisiones, lanza una excepción antes de reconstruir `usuarios`. Si no hay colisiones, recrea la tabla con `email COLLATE NOCASE`, copiando los valores existentes tal como están, incluidos los espacios periféricos. |
| **7→8** | Crea `productos`, `ventas` y `detalle_ventas`, con índice de `detalle_ventas.ventaId`, e inserta seis productos de ejemplo con `INSERT OR IGNORE`. No modifica las tablas preexistentes. |

Las migraciones siguen registradas en el builder de producción. No se agregó fallback destructivo ni se incorporaron migraciones históricas nuevas durante esta validación.

## Integridad y manejo de fallos

La migración 6→7 aplica unicidad de correo sin distinguir mayúsculas y conserva la grafía y espacios ya almacenados. Los registros que colisionan bajo `NOCASE` hacen que la migración aborte; Room ejecuta la migración en transacción. La prueba verifica que el usuario permanezca en versión 6, que sigan existiendo ambos registros y que no quede la tabla temporal `usuarios_nuevo`.

Las pruebas también verifican:

- Conservación de usuario, cita y beneficiario al migrar v6→v8 y v7→v8.
- Versión final 8, creación de tablas de venta y detalle, y seis productos de seed.
- Que un duplicado de correo con distinta capitalización no reemplace el registro original.
- Que el correo en conflicto no aparezca en el mensaje de error.
- Que una migración no registrada v7→v8 falle e identifique la ruta ausente sin alterar la versión ni los datos existentes ni crear `productos`.

Estas afirmaciones están limitadas a los datos y esquemas usados por las pruebas instrumentadas; no certifican bases históricas no disponibles.

## Pruebas y evidencia

Se ejecutó en el AVD `Medium_Phone_API_37.0`, reconocido como `emulator-5554`, con Android API 37 y `sys.boot_completed=1`.

Comando:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

La segunda ejecución terminó con **BUILD SUCCESSFUL**: 26 pruebas aprobadas, 0 fallos. El reporte XML registra el timestamp `2026-11-23T05:29:37` (el XML no indica zona horaria).

| Caso AN-03 | Resultado |
|---|---|
| `migracionesProductivasDesdeVersion6Hasta8ConservanDatosYTablas` | Aprobado |
| `migracionProductivaDeVersion7a8ConservaLosDatosExistentes` | Aprobado |
| `migracionNoDisponibleFallaYConservaLaBaseOriginal` | Aprobado |
| `colisionEnMigracion6a7RevierteSinEliminarRegistros` | Aprobado |

También aprobaron las pruebas instrumentadas de `BeneficiarioDaoRoomTest` (12) y `UsuarioRepositoryRoomTest` (9), además de `ExampleInstrumentedTest` (1).

Reportes de la ejecución:

- XML: `app/build/outputs/androidTest-results/connected/debug/TEST-Medium_Phone_API_37.0(AVD) - 17-_app-.xml`
- HTML: `app/build/reports/androidTests/connected/debug/index.html`

Las compilaciones `:app:compileDebugKotlin`, `:app:compileDebugAndroidTestKotlin`, las pruebas unitarias `:app:testDebugUnitTest` y `git diff --check` se habían aprobado antes de esta intervención documental. No se repitieron porque aquí no se modificaron código ni pruebas.

## Schemas y compatibilidad histórica

Los únicos schemas exportados versionados encontrados son:

- `app/schemas/com.example.sanaesenciaapp.data.room.AppDatabase/6.json`
- `app/schemas/com.example.sanaesenciaapp.data.room.AppDatabase/7.json`
- `app/schemas/com.example.sanaesenciaapp.data.room.AppDatabase/8.json`

El historial Git muestra declaraciones antiguas `@Database(version = 1)` en `d836888` y `@Database(version = 2)` en `9e24840`. No se encontró evidencia de una migración productiva 1→2. Tampoco hay schemas exportados v2–v5.

Por lo tanto:

- **v6→v7→v8 y v7→v8:** validadas instrumentalmente en API 37 con los casos descritos.
- **Migraciones 2→3 hasta 5→6:** presentes en el código, pero sus esquemas históricos y su recorrido real no están demostrados por schemas exportados ni por estas pruebas.
- **v1→v8:** no hay una ruta migratoria comprobada; no se afirma compatibilidad.
- La prueba antigua v2→v7 que construía manualmente un esquema y duplicaba migraciones dentro del test fue sustituida; no se usa como evidencia histórica.

Para certificar versiones anteriores se necesita evidencia auténtica de los esquemas que estuvieron en uso y una validación basada en esos esquemas. No se deben inferir columnas, índices ni valores de versiones sin esa evidencia.

## Dependencias, pruebas y pendientes

- **Dependencias Room declaradas:** 2.8.4 para runtime, KTX, compilador y testing.
- **Procesamiento:** KSP con exportación de schemas a `app/schemas`.
- **Validación ejecutada:** pruebas instrumentadas en Medium Phone API 37; 26 aprobadas, 0 fallos.
- **Pendiente histórico:** obtener o reconstruir con evidencia confiable los schemas v1–v5 y determinar si debe existir una ruta v1→2. No crear migraciones hasta confirmar el contrato histórico.
- **Publicación:** esta documentación y los cambios de AN-03 siguen locales al worktree aislado; este registro no implica commit, push ni integración en `develop`.
