# Portal Terapeuta

- **Responsable:** Alejandra Núñez
- **Actualizado:** 22-11-2026
- **Versión:** 1.0.0
- **Estado:** Parcial (acceso clínico bloqueado por dependencia de autorización)

## Objetivo

La interfaz plantea agenda diaria, pacientes asignados y continuidad; actualmente no consulta datos clínicos ni permite seguimiento.

## Pantallas y navegación

- `HomeTerapeutaScreen`: accesos de jornada/pacientes y logout.
- `AgendaDiaScreen`: presenta mensaje de agenda no disponible.
- `PacientesAsignadosScreen`: presenta mensaje de pacientes no disponibles.
- `DetalleContinuidadScreen`: cuando no hay selección, muestra aviso de bloqueo y no expone información personal; las notas aparecen deshabilitadas.
- `TerapeutaGraph`: home → agenda/pacientes → detalle solo desde una selección presente en el estado del ViewModel.

## Archivos relacionados

- `navigation/TerapeutaGraph.kt`, `viewmodel/TerapeutaViewModel.kt`, `viewmodel/TerapeutaViewModelModule.kt`.
- `ui/views/terapeuta/HomeTerapeutaScreen.kt`, `AgendaDiaScreen.kt`, `PacientesAsignadosScreen.kt`, `DetalleContinuidadScreen.kt`.
- `data/room/CitaEntity.kt` / `CitaDao` (dependencia de Agenda, propiedad de Valentina; consultados sin edición).

## ViewModel, modelos y persistencia

`TerapeutaViewModel` y modelos `Paciente`/`Cita` guardan únicamente estado en memoria. No hay repositorio del módulo ni fuente Room/servicio conectada para pacientes/seguimientos. La prueba confirma estado vacío y rechazo de selección/actualización de elementos no verificados.

Las citas Room incluyen `especialistaId`, pero `UsuarioEntity` y `SesionUsuario` no relacionan cuenta con especialista. Tampoco existe una asignación terapeuta-paciente. Por ello, no se pueden filtrar citas de forma autorizada por terapeuta. No se presenta dato de demostración como real.

## Estados, reglas y permisos

La pantalla muestra indisponibilidad, no una lista vacía de pacientes reales. No se habilitan pacientes, citas, historial, cambios de estado ni edición de notas. Las notas no tienen almacenamiento autorizado y no persisten. El rol TERAPEUTA aislado no basta para acceder a información de pacientes.

## Dependencias técnicas pendientes

- **Anaís:** definir relación persistida cuenta-terapeuta-`especialistaId` y asignaciones autorizadas de paciente/atención, restricciones, consultas aisladas y migración no destructiva.
- **Mark:** si la autorización es remota, publicar contrato autenticado de resolución de especialista, consulta de pacientes/citas y persistencia del seguimiento, incluidos permisos/campos/errores.
- **Valentina:** confirmar estados/campos de cita de Agenda que el módulo podrá consultar una vez autorizada la asociación.
- No existe contrato API publicado; no se documentan endpoints inventados.

## Pruebas y limitaciones

AL-04 ejecutó compile, 196 unit tests y assemble. Pruebas cubren estado vacío y operaciones rechazadas sobre datos no autorizados. No se pudo probar terapeuta con relación válida ni persistencia tras reinicio porque la relación/almacenamiento no existe. La tarea funcional queda pendiente de las dependencias indicadas.
