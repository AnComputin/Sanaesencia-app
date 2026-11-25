# Diagnóstico de infraestructura Android para API de citas (VA-05)

- Responsable: Anaís.
- Fecha: 2026-11-24.
- Versión: 0.2 (comparación de ramas).
- Estado: Parcial; hay un cliente JWT en Rama-Mark, aún no integrado y con brechas técnicas.
- Worktree: C:/Users/ACRZ-/StudioProjects/Sanaesencia-app-Anais-sync-20261123.
- Rama/SHA Anais: Anais / 08bd3acf1d33b36dc944899661d89941c91408b3.
- Rama Mark revisada: origin/Rama-Mark / 4841bb493f8698250871febdf5a67d3f87a759cb.
- Develop remoto de comparación: origin/develop / a1aecc5bcbcbb327450c66e644d1c4e398234477.
- Estado Git inicial: rama Anais limpia y sincronizada. Estado final: `app/build.gradle.kts` y `gradle/libs.versions.toml` modificados; esta guía sigue sin seguimiento. Sin commit.
- Alcance: comparación estática; se centralizaron dependencias Retrofit/OkHttp para AN-01. No se portó ni ejecutó la rama Mark; el cliente, JWT en DI y pruebas HTTP siguen pendientes.

## Resultado

| Requisito | Estado | Evidencia |
|---|---|---|
| Cliente Retrofit/OkHttp | Parcial | En Anais se añadieron al catálogo y `app/build.gradle.kts` Retrofit/converter Gson 2.11.0 y OkHttp/logging-interceptor 4.12.0. La implementación cliente continúa solo en Rama-Mark (`data/remote/RetrofitClient.kt`, `MicrocerviciosApi.kt`). |
| JWT/interceptor | Parcial | Rama-Mark añade `AuthInterceptor` y `TokenManager`; no hay pruebas remotas y el logger BODY no redacta token ni contraseñas. |
| JWT de usuarios-service | Parcial | Rama-Mark llama `POST /api/usuarios/login`, recibe `token/tipo/expiraEnSegundos/usuario` y guarda token. No implementa expiración/401/refresh; `SesionRepository` conserva fallback local sin token. |
| Servicio remoto en DI | Pendiente | `AppContainer` no expone servicios; `RetrofitClient` es singleton estático y repositorios lo consultan directamente. |
| URL por entorno | Bloqueado | Rama-Mark fija `127.0.0.1` para todos los puertos; no es configurable y no apunta al host desde Android. |
| Acceso de red | Parcial | Rama-Mark agrega `INTERNET` y `usesCleartextTraffic=true` global; falta acotar HTTP a desarrollo. En Anais/develop no aparece `INTERNET`. |
| Despacho ViewModel | Implementado | AppViewModelProvider entrega AppContainer a módulos; sus pruebas existentes solo validan despacho. |
| Compilación | Validada | .\gradlew.bat :app:compileDebugKotlin — código 0, BUILD SUCCESSFUL. |

## Archivos inspeccionados

- app/build.gradle.kts; gradle/libs.versions.toml; app/src/main/AndroidManifest.xml.
- app/src/main/java/com/example/sanaesenciaapp/MyApplication.kt.
- app/src/main/java/com/example/sanaesenciaapp/di/AppContainer.kt.
- app/src/main/java/com/example/sanaesenciaapp/viewmodel/AppViewModelProvider.kt.
- app/src/main/java/com/example/sanaesenciaapp/data/repository/SesionRepository.kt.
- app/src/main/java/com/example/sanaesenciaapp/viewmodel/AuthViewModel.kt.
- app/src/test/java/com/example/sanaesenciaapp/data/repository/SesionRepositoryTest.kt.
- app/src/test/java/com/example/sanaesenciaapp/viewmodel/AppViewModelProviderTest.kt.
- docs/infraestructura/room-y-migraciones.md.

## Hallazgos en la rama de Mark y brechas para el portado

La punta `origin/Rama-Mark` define login en `POST /api/usuarios/login`; request `{email,password}` y respuesta con `token`, `tipo`, `expiraEnSegundos` y `usuario`. `UsuarioRepository.login()` guarda el JWT en `TokenManager`, y el interceptor lo adjunta como Bearer. No se observó endpoint de refresh ni manejo de expiración/401.

Antes de portar, resolver con Mark/Anaís: (1) retirar logging BODY o redactar `Authorization` y cuerpos de login/registro; (2) decidir persistencia segura del JWT, pues TokenManager usa SharedPreferences sin cifrado; (3) eliminar el fallback que permite login local sin token cuando el login remoto falla; (4) centralizar Retrofit en AppContainer y usar URL por variante; (5) acotar cleartext solo a desarrollo; (6) mantener DTO Android separados de entidades Room; (7) añadir pruebas de auth/interceptor/errores.

No hay que inventar login: el contrato ya está en la rama Mark. Falta acordar y portar su integración dentro de archivos Android comunes, preservando ownership.

## Guía para Valentina

La interfaz real encontrada en Rama-Mark es `AgendaApi` dentro de `data/remote/MicrocerviciosApi.kt`, con listado, reserva y PATCH de estado. No está registrada en AppContainer y no está en `Anais`/`develop`, así que Valentina aún no debe acoplarse a ella ni crear un cliente propio. Al portar, registrar la API compartida en AppContainer y después proveerle un método de acceso estable.

Brechas de contrato: falta `GET /api/citas/{id}` y el parámetro de filtro `especialistaId`; el cliente usa `CitaEntity` en respuestas y debe sustituirse por modelos de red propios. `CitaRepository.cancelarCita()` llama PATCH ADMIN y luego actualiza Room incluso si falla la llamada. También acepta 409 pese a que Mark documenta 400. Las excepciones/otros errores pueden acabar en una reserva local, ocultando la falta de integración.

El contrato comunicado da http://localhost:8084. En Android localhost es el propio dispositivo, no el computador. Para el emulador Android estándar, el alias del host es 10.0.2.2, por lo que el backend local sería http://10.0.2.2:8084. Para un dispositivo físico se necesita la IP/DNS accesible que proporcione el entorno; esa dirección no se conoce y no se inventa. HTTP/cleartext solo debe habilitarse en configuración de desarrollo acotada; no se habilitó aquí.

## Evidencia y límites

- Compilación: .\gradlew.bat :app:compileDebugKotlin, exit code 0.
- Pruebas unitarias existentes de sesión y despacho DI: `.\gradlew.bat :app:testDebugUnitTest --tests com.example.sanaesenciaapp.data.repository.SesionRepositoryTest --tests com.example.sanaesenciaapp.viewmodel.AppViewModelProviderTest --console=plain` — exit code 0, BUILD SUCCESSFUL (25 tareas; suites ejecutadas, sin fallos reportados). Cubren sesión local y despacho, no JWT/red.
- Resolución y compilación: ` .\\gradlew.bat :app:dependencyInsight --dependency retrofit --configuration debugRuntimeClasspath :app:dependencyInsight --dependency okhttp --configuration debugRuntimeClasspath :app:compileDebugKotlin --console=plain` — exit code 0, BUILD SUCCESSFUL en 4m33s. Resolvió Retrofit 2.11.0 y OkHttp/logging-interceptor 4.12.0.
- Pruebas de interceptor, URL, DI remota y HTTP real: pendientes. Rama-Mark incluye Retrofit, pero no se probó su cliente en esta revisión ni se verificó una solicitud HTTP real.
- La guía `docs/estado-implementacion.md` en Rama-Mark refiere SHA 5df11b3, no la punta 4841bb4; no tratarla como descripción del estado final sin revalidarla.
- No se expuso ningún token/credencial.
- No se modificaron AppState.kt, UsuarioRepository.kt, UsuarioEntity.kt, UsuarioRepositoryTest.kt, archivos de Valentina, backend, README ni documentación funcional de Alejandra.
