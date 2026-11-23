# SanaEsencia - cambios completados

## Flujo de reserva

El flujo queda así:

Especialidad -> Especialista -> Modalidad -> Fecha -> Hora -> Paciente -> Pago -> Confirmación -> Mis citas.

## Catálogo

Se mantienen 7 especialidades y 30 especialistas:

- Psicología Infanto-Juvenil: 5
- Psicología Adulto: 5
- Terapia Ocupacional: 4
- Nutrición Integrativa: 4
- Terapia Integrativa: 4
- Acupuntura: 4
- Quiropraxia: 4

Los especialistas se guardan en Room mediante `EspecialistaEntity` y se relacionan con la especialidad mediante `especialidadId`.

## Citas y Room

Las citas ahora se guardan en la tabla `citas`. Una cuenta nueva no recibe citas ficticias y `Mis citas` filtra por el correo del usuario autenticado.

Cada cita guarda una copia del nombre de la especialidad y del especialista para conservar el historial aunque el catálogo cambie después.

## Disponibilidad y 8 boxes

Una cita dura 60 minutos.

Un horario se bloquea si:

1. El especialista ya tiene otra cita confirmada que se solapa con ese intervalo.
2. Para modalidad presencial, ya existen 8 citas presenciales superpuestas.

La condición de solapamiento utilizada es:

`inicioExistente < finNuevo && finExistente > inicioNuevo`

Al confirmar una cita presencial, Room busca automáticamente el primer box libre entre 1 y 8 dentro de una transacción. Las citas online no usan box.

## Panel administrador

Usuario de prueba:

- Correo: `admin@sanaesencia.cl`
- Contraseña: `admin123`

El panel permite cambiar modalidad y pausar/reactivar la agenda de una especialidad. Estos cambios ya no son sobrescritos al reiniciar la aplicación.

## Prueba recomendada

1. Crear un paciente nuevo.
2. Entrar a Mis citas: debe estar vacío.
3. Reservar una especialidad y seleccionar un especialista.
4. Confirmar una cita presencial.
5. Verificar en Confirmación/Mis citas que aparece `Box N`.
6. Cerrar y abrir la app: la cita debe seguir en Room.
7. Entrar con otra cuenta: no debe ver la cita anterior.
8. Desde ADMIN cambiar una especialidad de Ambas a Presencial y verificar que el flujo del paciente se adapte.
9. Pausar una agenda desde ADMIN y verificar que el paciente no pueda reservarla.

## Nota de migración

La base de datos llegó a la versión 7 con `MIGRATION_6_7`, que aplica unicidad de correo sin borrar usuarios ni citas. La tienda se agrega en la versión 8 mediante `MIGRATION_7_8`.

## Panel ADMIN: pestaña Boxes

Se agregó una pestaña **Boxes** al `HomeAdminScreen`.

- Permite elegir una de las mismas 7 fechas y horarios utilizados por el flujo de reserva.
- Muestra los 8 boxes físicos del centro en tiempo real desde Room.
- Un box aparece ocupado solamente si existe una cita `PRESENCIAL` con estado `CONFIRMADA` que se solapa con el intervalo seleccionado.
- Las citas `ONLINE` no consumen box.
- Se muestra el profesional, especialidad, paciente y hora de la cita que ocupa cada box.
- Incluye un contador `X / 8 boxes ocupados` y una alerta de capacidad completa cuando los ocho están ocupados.
- Si por corrupción de datos existieran dos citas superpuestas asignadas al mismo box, el panel muestra una advertencia de conflicto.

La pantalla observa `CitaDao.obtenerTodasFlow()`, por lo que los cambios se reflejan automáticamente cuando Room cambia.

## Beneficiarios / menores a cargo

Se reactivó y completó la función de `FichaBeneficiarioScreen` proveniente del trabajo del equipo.

- La ficha ahora guarda realmente el beneficiario en Room mediante `BeneficiarioEntity` y `BeneficiarioDao`.
- Cada beneficiario queda asociado al correo del titular que inició sesión.
- Room impide registrar dos veces el mismo RUT para una misma cuenta.
- Al volver a Agendar, el beneficiario recién creado queda seleccionado automáticamente.
- En Agendar se puede escoger `Para mí` o cualquiera de los beneficiarios registrados.
- La cita conserva `pacienteNombre` como snapshot histórico y además `beneficiarioId` cuando corresponde.
- Los beneficiarios sobreviven al cierre de la aplicación y solo aparecen en la cuenta que los registró.
- La base de datos se elevó a versión 6 mediante `MIGRATION_5_6`, sin borrar usuarios ni citas.

### Compatibilidad con develop

También se reforzó `MIGRATION_4_5` para reconocer la estructura de Room que tenía la rama `develop` (v4):

- crea la tabla de especialistas si esa versión no la tenía;
- si la tabla `citas` ya existía, conserva los datos y solo agrega `especialistaId` cuando no está presente.

### Prueba recomendada de beneficiarios

1. Iniciar sesión como PACIENTE.
2. Comenzar una reserva hasta llegar a Agendar.
3. Pulsar `Registrar menor o beneficiario a cargo`.
4. Completar nombre, RUT, fecha de nacimiento y parentesco.
5. Guardar: debe volver a Agendar con ese beneficiario seleccionado.
6. Completar la reserva y verificar que Confirmación/Mis citas muestre el nombre del beneficiario.
7. Cerrar sesión y entrar con otra cuenta: no debe ver los beneficiarios del primer usuario.
8. Volver a iniciar sesión con la primera cuenta: el beneficiario debe seguir disponible.

## Tienda virtual y compras simuladas

Se incorporó la tienda virtual sin reemplazar los flujos existentes de reservas, citas, beneficiarios, terapeuta ni administración.

- La pantalla principal incluye el acceso **Tienda de bienestar**.
- El catálogo inicial contiene aceites de aromaterapia y masaje, spray ambiental, vela aromática, sales de baño e infusión herbal.
- Cada producto muestra descripción, categoría, precio y stock disponible.
- El carrito permite agregar unidades, reducirlas, quitar productos y revisar el subtotal.
- El checkout permite elegir entre tarjeta de prueba y transferencia de prueba. No se solicitan ni almacenan datos bancarios reales y no se realiza ningún cobro.
- Al confirmar el pago simulado, se registra la venta y el detalle de los productos. El stock se descuenta dentro de una transacción de Room; si el inventario no alcanza, la compra no se confirma.
- Los detalles de las ventas guardan el nombre y precio del producto como registro histórico, por lo que quitar posteriormente un producto no modifica las ventas anteriores.

### Panel ADMIN: pestaña Productos

Se agregó la pestaña **Productos** junto a las pestañas existentes **Especialidades** y **Boxes**.

- Permite agregar productos con nombre, descripción, categoría, precio, stock inicial e ícono.
- Muestra el stock actual de cada producto.
- Permite aumentar o disminuir existencias desde el mismo panel.
- Permite quitar un producto de la tienda con confirmación.
- Los cambios se observan desde la tienda mediante flujos de Room.

### Persistencia y migración

La base de datos se elevó de la versión 7 a la 8. `MIGRATION_7_8` agrega las tablas `productos`, `ventas` y `detalle_ventas` sin modificar las tablas existentes. Para instalaciones nuevas, los mismos productos de ejemplo se insertan desde el callback de creación de Room. No se usa una migración destructiva.

### Contraseña con validación visual

En el registro, el borde del campo **Contraseña** se muestra rojo mientras la contraseña escrita no cumple las reglas existentes del proyecto: mínimo 8 caracteres, una mayúscula, un número y un carácter especial. El texto de ayuda refleja en tiempo real la condición que falta. No se modificaron los criterios de seguridad ya existentes.

### Prueba recomendada de la tienda

1. Iniciar sesión como paciente y entrar a **Tienda de bienestar**.
2. Agregar dos o más productos al carrito.
3. En el carrito, aumentar y reducir cantidades y quitar uno de los productos.
4. Continuar al pago y elegir un método de demostración.
5. Confirmar el pago simulado y revisar el número de pedido.
6. Entrar como administrador (`admin@sanaesencia.cl`, contraseña `admin123`) y abrir **Productos**.
7. Verificar que el stock se haya descontado; aumentar una unidad, quitar un producto o agregar uno nuevo.
8. Volver a la tienda y comprobar que los cambios aparecen sin reiniciar la aplicación.
9. Cerrar y volver a abrir la aplicación: los productos, inventario y ventas permanecen guardados en Room.

---

## Tarea AL-01 – Auditoría y optimización de Inicio y accesos a módulos

- **ID y Nombre:** AL-01 – Completar Inicio y accesos a módulos
- **Responsable:** Alejandra Núñez
- **Fecha:** 22-11-2026
- **Versión:** 1.0.0
- **Estado real:** Completado y validado
- **Archivos modificados:**
  - `app/src/main/java/com/example/sanaesenciaapp/ui/views/InicioScreen.kt`: incorporación de `@Preview` oficial sin alterar la navegación por callbacks.
  - `app/src/main/java/com/example/sanaesenciaapp/ui/views/HomeScreen.kt`: localización del botón de tienda mediante `stringResource(R.string.inicio_btn_tienda)` e incorporación de `@Preview` para `EspecialidadCard`.
  - `app/src/main/res/values/strings_inicio.xml`: adición del recurso `inicio_btn_tienda` para consistencia y localización de textos en Inicio/Home.
  - `app/src/test/java/com/example/sanaesenciaapp/viewmodel/InicioNavigationTest.kt`: suite de pruebas unitarias cubriendo redirección según rol post-login (ADMIN, TERAPEUTA, PACIENTE, anónimo), visibilidad de botones condicionales (Mis citas, Cerrar sesión, Reservar) y resolución de rutas de especialidad (activa vs pausada).
- **Dependencias:**
  - `Navigation.kt` raíz (intacto, respetado según reglas obligatorias).
  - `AppState.kt` (intacto, lectura de `usuarioActual`).
  - Módulos de navegación: `AuthGraph`, `CatalogoGraph`, `AgendaGraph`, `TerapeutaGraph`, `JuegosGraph`, `TiendaGraph`.
- **Pruebas ejecutadas y resultados:**
  - `./gradlew.bat :app:compileDebugKotlin` -> EXITOSO
  - `./gradlew.bat :app:testDebugUnitTest` -> 191 aprobadas, 0 fallidas, 0 omitidas
  - `./gradlew.bat :app:assembleDebug` -> EXITOSO
- **Limitaciones conocidas:**
  - El portal terapeuta permanece sin persistencia clínica real vinculada a la cuenta autenticada según la definición del proyecto.
  - La sesión permanece en memoria a través de `SesionRepository` y `AppState`, reseteándose al cerrar la aplicación.

---

## Tarea AL-02 – Catálogo de Especialidades y Especialistas

- **ID y Nombre:** AL-02 – Catálogo de especialidades y especialistas
- **Responsable:** Alejandra Núñez
- **Fecha:** 22-11-2026
- **Versión:** 1.0.0
- **Estado real:** Completado y validado
- **Archivos modificados:**
  - `app/src/main/java/com/example/sanaesenciaapp/viewmodel/CatalogoViewModel.kt`: adición de estado reactivo `cargandoEspecialistas`, flujos de reintento (`reintentarEspecialidades()`, `reintentarEspecialistas()`) y manejo robusto de excepciones sin alterar `SavedStateHandle`.
  - `app/src/main/java/com/example/sanaesenciaapp/ui/views/SeleccionEspecialidadScreen.kt`: implementación de tarjetas de error con botón de reintento, indicador visual central de progreso para carga, tarjeta de estado vacío (`catalogo_vacio`) y `@Preview` oficial.
  - `app/src/main/java/com/example/sanaesenciaapp/ui/views/catalogo/SeleccionEspecialistaScreen.kt`: distinción clara entre carga, lista vacía (`especialista_vacio`), sin compatibilidad de modalidad (`especialista_error_incompatibles`), agenda pausada y error con botón de reintento. Adición de `@Preview`.
  - `app/src/main/java/com/example/sanaesenciaapp/navigation/CatalogoGraph.kt`: conexión del callback `onReintentarClick` en `seleccionar_especialidad` y presentación visual amigable con botón de reintento en la ruta `detalle_servicio/{especialidadId}` ante errores de carga o ID inexistente.
  - `app/src/main/res/values/strings_catalogo.xml`: adición de cadenas localizadas `especialista_vacio`, `especialista_error_carga` y `catalogo_btn_reintentar`.
  - `app/src/test/java/com/example/sanaesenciaapp/viewmodel/CatalogoViewModelModuleTest.kt`: adición de pruebas unitarias para error/reintento de especialidades, error/reintento de especialistas, ID ausente y especialidad sin especialistas registrados.
- **Dependencias:**
  - `CatalogoRepository`: interfaz limpia de consulta Room observada mediante StateFlow.
  - `CitaViewModel`: continuidad funcional intacta hacia la agenda tras seleccionar el especialista (`seleccionarServicio`).
  - `SavedStateHandle`: recuperación de `especialidadId` como argumento de navegación.
- **Pruebas ejecutadas y resultados:**
  - `./gradlew.bat :app:compileDebugKotlin` -> EXITOSO
  - `./gradlew.bat :app:testDebugUnitTest` -> 195 aprobadas, 0 fallidas, 0 omitidas
  - `./gradlew.bat :app:assembleDebug` -> EXITOSO
- **Limitaciones conocidas:**
  - El catálogo de especialidades y especialistas es de lectura en la capa pública; las modificaciones de modalidad y pausa de agenda se realizan desde el panel de administración (`HomeAdminScreen`).

---

## Tarea AL-03 – Validar módulo Beneficiarios

- **ID y Nombre:** AL-03 – Validar módulo Beneficiarios
- **Responsable:** Alejandra Núñez
- **Fecha:** 22-11-2026
- **Versión:** 1.0.0
- **Estado real:** Completado y validado
- **Archivos modificados:**
  - `app/src/main/java/com/example/sanaesenciaapp/viewmodel/BeneficiarioViewModel.kt`: reintento de la observación cuando falla la lectura, conservando la limpieza de datos al cambiar/cerrar sesión.
  - `app/src/main/java/com/example/sanaesenciaapp/ui/views/auth/FichaBeneficiarioScreen.kt`: acción accesible para reintentar una consulta fallida.
  - `app/src/main/res/values/strings_beneficiario.xml`: mensaje comprensible de error de carga/guardado y texto de reintento.
  - `app/src/test/java/com/example/sanaesenciaapp/viewmodel/BeneficiarioViewModelModuleTest.kt`: prueba de error de lectura seguido de reintento exitoso.
- **Datos y validaciones:** nombre completo, RUT, fecha de nacimiento y parentesco. La pantalla valida reactivamente mediante `validarNombre`, `validarRut`, `validarFechaNacimiento` y `validarParentesco`; el repositorio vuelve a validar datos obligatorios y normaliza RUT y correo.
- **Propietario y acceso:** el propietario se obtiene de `SesionRepository.sesionActual`, nunca de argumentos de interfaz. Listado, detalle por ID, edición, desactivación y reactivación están limitados por ID y correo propietario en `BeneficiarioDao`. Cambiar de cuenta vacía el estado mientras carga la nueva cuenta; cerrar sesión elimina el listado del estado visible. Las acciones en curso de una sesión anterior no actualizan la UI nueva.
- **Operaciones disponibles:** alta, consulta reactiva, edición de campos personales, desactivación lógica y reactivación al volver a registrar un RUT inactivo. El RUT activo duplicado se rechaza por propietario.
- **Relación con Agenda:** Agenda consume la lista de beneficiarios de la cuenta vigente desde `AppState`; la selección conserva `beneficiarioId` y el nombre del paciente en la cita. La asociación de cita no se modificó en esta tarea.
- **Dependencias:** `BeneficiarioRepository`, `BeneficiarioViewModel`, `SesionRepository`, consultas existentes de `BeneficiarioDao`, `AppState` y flujo de Agenda. No se modificaron sesión, entidades, DAO ni Room común.
- **Pruebas ejecutadas y resultados:**
  - `:app:compileDebugKotlin` -> EXITOSO
  - `:app:testDebugUnitTest` -> 196 aprobadas, 0 fallidas, 0 omitidas
  - `:app:assembleDebug` -> EXITOSO
- **Limitaciones conocidas:** la ficha muestra un resumen del beneficiario en el listado y carga ese registro en el formulario para editar; no hay una pantalla de detalle separada. La desactivación es lógica para conservar referencias históricas.

---

## Tarea AL-04 – Completar módulo Terapeuta

- **ID y Nombre:** AL-04 – Completar módulo Terapeuta
- **Responsable:** Alejandra Núñez
- **Fecha:** 22-11-2026
- **Versión:** 1.0.0
- **Estado real:** Módulo clínico bloqueado de forma segura; no se habilita hasta recibir una asociación verificable y autorización persistida.
- **Auditoría de pantallas y flujos:** `HomeTerapeutaScreen`, `AgendaDiaScreen`, `PacientesAsignadosScreen` y `DetalleContinuidadScreen` conservan su interfaz. Agenda y pacientes presentan mensajes de indisponibilidad; el detalle no muestra PII cuando no hay paciente seleccionado. Las notas clínicas permanecen deshabilitadas porque no se guardan en almacenamiento autorizado.
- **Relaciones verificadas:** `SesionUsuario` contiene email y rol, pero no `especialistaId`. `UsuarioEntity` tampoco tiene un identificador profesional. `CitaEntity` relaciona citas con `especialistaId`, pero no vincula ese especialista con la cuenta terapeuta. No existe un modelo persistido de asignación terapeuta-paciente ni autorización de seguimiento. El acceso a una cita como paciente/propietario no acredita autorización para una cuenta terapeuta.
- **Datos reales, temporales y demostrativos:** las citas de agenda están persistidas en Room, pero no se pueden asociar a un terapeuta autenticado con la información disponible. Los modelos `Paciente` y `Cita` del módulo son modelos de UI y no tienen una fuente autorizada conectada; los StateFlows permiten mutaciones temporales, no persistencia. No se presentan registros de ejemplo como pacientes reales.
- **Permisos y estados:** el módulo permanece vacío y bloqueado mientras no haya asociación autorizada. No se habilita lectura de citas, consulta de pacientes, historial ni escritura de estados clínicos. Las notas no se conservan al reiniciar porque no hay almacenamiento definido. La prueba verifica que el estado comienza vacío y rechaza selecciones/actualizaciones sobre elementos no autorizados.
- **Solicitud técnica para Anaís (persistencia):** definir y aprobar un modelo persistido que vincule inequívocamente cuenta terapeuta con `especialistaId`, además de la asignación autorizada de pacientes/atenciones. La propuesta deberá incluir entidades, restricciones, consultas con aislamiento por propietario, migración no destructiva, política de desasignación y pruebas Room. No se modificaron entidades, DAO ni migraciones en AL-04.
- **Solicitud técnica para Mark (contrato y autorización):** confirmar si la relación y autorización serán provistas por un backend. Si es así, entregar endpoints/contratos autenticados para resolver especialista desde la sesión, listar pacientes/citas asignados y registrar seguimiento; incluir campos permitidos, reglas de acceso, errores y política de persistencia. No se inventaron endpoints ni permisos locales.
- **Archivos modificados:** `app/src/test/java/com/example/sanaesenciaapp/viewmodel/TerapeutaViewModelTest.kt` (cobertura explícita del estado sin pacientes, citas, historial ni indicadores de riesgo); este documento.
- **Dependencias:** contrato persistido y migración aprobada por Anaís, o contrato autenticado de Mark; después, implementación y pruebas integradas del módulo.
- **Pruebas ejecutadas y resultados:**
  - `:app:compileDebugKotlin` -> EXITOSO
  - `:app:testDebugUnitTest` -> 196 aprobadas, 0 fallidas, 0 omitidas
  - `:app:assembleDebug` -> EXITOSO
- **Limitaciones conocidas:** el criterio de pacientes vinculados y seguimiento persistente no puede alcanzarse de manera segura hasta recibir una de las dependencias técnicas anteriores.

---

## Tarea AL-05 – Validar módulo Juegos

- **ID y Nombre:** AL-05 – Validar módulo Juegos
- **Responsable:** Alejandra Núñez
- **Fecha:** 22-11-2026
- **Versión:** 1.0.0
- **Estado real:** Completado y validado
- **Juegos y objetivo:**
  - **Respiración Guiada:** ciclo de inhalar, mantener y exhalar durante 60 segundos, con pausa y reanudación.
  - **Pausa Consciente:** recorrido guiado de cuatro pasos para detenerse, observar respiración/entorno y reconectar.
  - **Memorice Zen:** encontrar ocho parejas de símbolos de bienestar, contando intentos y tiempo.
  - **Gato Armónico:** tres en raya para dos jugadores o contra CPU fácil/experta, con puntajes y empates.
  - **Camino del Bienestar:** tablero de 25 casillas con avance, retroceso y meditación; objetivo llegar a la meta.
- **Pantallas y navegación:** Inicio abre `juegos_hub` mediante callback. El hub enlaza una vez a cada ruta: `respiracion_guiada`, `pausa_consciente`, `memorice_zen`, `gato_armonico` y `camino_bienestar`. Cada pantalla tiene callback de regreso que ejecuta `popBackStack()`; el botón atrás del sistema usa la pila normal de Navigation Compose. No se encontraron rutas duplicadas ni juegos declarados sin destino.
- **ViewModels y estado:** `RespiracionViewModel`, `PausaConscienteViewModel`, `MemoriceViewModel`, `GatoViewModel` y `CaminoViewModel` mantienen el estado en StateFlow. Los temporizadores y el turno de CPU son tareas coroutine con cancelación. El estado vive en el ViewModel de cada destino: sobrevive recomposición/configuración mientras la entrada permanece en la pila; al volver al hub, el destino se elimina y al entrar de nuevo inicia una sesión nueva. Los juegos son locales y no tienen repositorios ni persistencia.
- **Mecánicas revisadas:** turnos y victoria por línea/empate en Gato; parejas, bloqueo durante ocultado y finalización en Memorice; efectos de casillas y meta en Camino; transiciones de fase, pausa y final de Respiración; pasos, avance y finalización de Pausa Consciente. Camino recibe un generador de dado con valor aleatorio 1–6 por defecto para permitir pruebas reproducibles sin cambiar la mecánica.
- **Errores y estados:** los juegos no dependen de lecturas remotas ni almacenamiento, por lo que no tienen estados de carga/error de datos; muestran estado inicial, ejecución y finalización dentro de cada UI. No se agregaron datos ficticios o resultados persistidos.
- **Archivos modificados:**
  - `app/src/main/java/com/example/sanaesenciaapp/viewmodel/CaminoViewModel.kt`: inyección del generador de dado para reproducibilidad en tests; el valor de producción sigue siendo aleatorio entre 1 y 6.
  - `app/src/test/java/com/example/sanaesenciaapp/viewmodel/CaminoViewModelTest.kt`: finalización en meta, bloqueo de tiradas posteriores y reinicio.
  - `app/src/test/java/com/example/sanaesenciaapp/viewmodel/GatoViewModelTest.kt`: victoria de X, bloqueo tras finalizar y reinicio conservando puntaje.
  - `app/src/test/java/com/example/sanaesenciaapp/viewmodel/MemoriceViewModelTest.kt`: completar ocho parejas y comenzar una nueva ronda.
  - `app/src/test/java/com/example/sanaesenciaapp/viewmodel/PausasViewModelTest.kt`: fin y reinicio de Pausa Consciente; cambios de fase, pausa/reanudación, final y reinicio de Respiración Guiada.
  - Este documento.
- **Dependencias:** Navigation Compose, callbacks de `JuegosGraph`, ViewModels de juegos y recursos `strings_juegos.xml). No se modificó `Navigation.kt` raíz ni archivos comunes.
- **Pruebas ejecutadas y resultados:**
  - `:app:compileDebugKotlin` -> EXITOSO
  - `:app:testDebugUnitTest` -> 200 aprobadas, 0 fallidas, 0 omitidas
  - `:app:assembleDebug` -> EXITOSO
- **Limitaciones conocidas:** la partida no se conserva después de volver al hub o cerrar la app; los ViewModels no guardan estado en Room ni en SavedStateHandle. No hay un flujo asíncrono de errores que requiera estado de error.

---

## Tarea AL-06 – Implementar y validar sección de Productos

- **ID y Nombre:** AL-06 – Implementar y validar sección de Productos
- **Responsable:** Alejandra Núñez
- **Fecha:** 22-11-2026
- **Versión:** 1.0.0
- **Estado real:** Completado y validado
- **Objetivo y separación:** Tienda de bienestar es una sección independiente del catálogo de especialidades terapéuticas. Tiene listado, carrito, pago simulado y confirmación propios.
- **Catálogo y campos:** cada `ProductoEntity` contiene nombre, descripción, categoría, precio, stock, icono emoji y estado activo. El icono es una representación visual; no hay imágenes fotográficas. El catálogo muestra disponibilidad y bloquea agregar unidades cuando el stock es cero.
- **Detalle:** seleccionar una tarjeta abre `producto_detalle/{productoId}`, que recupera el producto del catálogo vigente y presenta descripción, categoría, precio, stock y acción para agregar al carrito. IDs inexistentes o productos desactivados muestran un mensaje y permiten volver. Error de lectura muestra opción de reintento.
- **Fuente y persistencia:** el listado público observa `ProductoDao.obtenerActivosFlow()` mediante `TiendaRepository`; administración observa todos los productos. Room es la única fuente, sin backend. La migración 7→8 y el callback de creación insertan seis productos de ejemplo. Son datos de demostración, no inventario/oferta de un comercio real; administración puede mantener productos y stock en Room.
- **Categorías de ejemplo:** Aromaterapia, Masajes, Bienestar, Cuidado personal e Infusiones.
- **Ventas:** carrito y pago son locales; confirmar crea venta y detalle en Room y descuenta existencias de forma transaccional. El pago es explícitamente simulado, sin cobro ni datos bancarios reales.
- **Estados y selección:** el ViewModel distingue Cargando, Vacio, Error y Contenido. El retry vuelve a suscribirse a Room. El identificador seleccionado viaja en la ruta de detalle y se resuelve contra el estado actual del catálogo.
- **Navegación:** desde Home se abre `tienda`; desde listado se abre detalle, carrito o regreso a Home; detalle vuelve al listado; el carrito continúa a pago y confirmación. El botón atrás usa la pila de Navigation Compose. El detalle es una ruta nueva y única en `TiendaGraph`.
- **Archivos modificados:**
  - `app/src/main/java/com/example/sanaesenciaapp/data/repository/TiendaRepository.kt`: contrato de repositorio para aislar el ViewModel y habilitar pruebas con fuente controlada.
  - `app/src/main/java/com/example/sanaesenciaapp/viewmodel/TiendaViewModel.kt`: estado explícito del catálogo y reintento.
  - `app/src/main/java/com/example/sanaesenciaapp/ui/views/tienda/TiendaScreen.kt`: distingue carga/vacío/error/contenido y permite abrir detalle.
  - `app/src/main/java/com/example/sanaesenciaapp/ui/views/tienda/DetalleProductoTiendaScreen.kt`: detalle con carga/error/reintento y gestión de ID inexistente o producto inactivo.
  - `app/src/main/java/com/example/sanaesenciaapp/navigation/TiendaGraph.kt`: ruta del detalle con argumento entero.
  - `app/src/test/java/com/example/sanaesenciaapp/viewmodel/TiendaViewModelTest.kt`: estados, selección/detalle por ID, ID inexistente, catálogo vacío, error/reintento, carrito y ruta de navegación.
  - Este documento.
- **Dependencias:** `ProductoEntity`, `ProductoDao`, `AppDatabase` y migración 7→8 (consultados; sin modificaciones); repositorio, ViewModel, `TiendaGraph`, acceso desde Home, carrito y checkout existentes.
- **Pruebas ejecutadas y resultados:**
  - `:app:compileDebugKotlin` -> EXITOSO
  - `:app:testDebugUnitTest` -> 204 aprobadas, 0 fallidas, 0 omitidas
  - `:app:assembleDebug` -> EXITOSO
- **Limitaciones conocidas:** no existe integración con backend ni imágenes de producto. El inventario inicial es demostrativo; pagos de prueba no representan transacciones comerciales reales.
