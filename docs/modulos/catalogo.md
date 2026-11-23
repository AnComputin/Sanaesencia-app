# Catálogo terapéutico

- **Responsable:** Alejandra Núñez
- **Actualizado:** 22-11-2026
- **Versión:** 1.0.0
- **Estado:** Implementado

## Objetivo y catálogo

Mostrar especialidades terapéuticas, su descripción/modalidad/estado de agenda, especialistas asociados y detalle para continuar a una reserva. No comparte contenido con Tienda de bienestar.

Room contiene siete especialidades y 30 especialistas iniciales de demostración; administración puede cambiar modalidad y pausar agenda. Los especialistas se filtran por `especialidadId` y modalidad compatible. La fuente de lectura es `CatalogoRepository` sobre los DAO de Room.

## Pantallas y navegación

- `SeleccionEspecialidadScreen`: lista y selección.
- `DetalleServicioScreen`: nombre/descripción y CTA de reserva.
- `SeleccionEspecialistaScreen`: datos visibles del profesional, modalidad compatible y selección.
- `CatalogoGraph`: `seleccionar_especialidad` → `detalle_servicio/{especialidadId}` o `seleccionar_especialista/{especialidadId}` → `agendar`.
- La selección envía especialidad y especialista a `CitaViewModel`; el ID de especialidad se recupera con `SavedStateHandle`. El regreso usa la pila de Navigation Compose.

## Archivos relacionados

- `data/repository/CatalogoRepository.kt`, `viewmodel/CatalogoViewModel.kt`.
- `ui/views/SeleccionEspecialidadScreen.kt`, `ui/views/catalogo/DetalleServicioScreen.kt`, `ui/views/catalogo/SeleccionEspecialistaScreen.kt`.
- `navigation/CatalogoGraph.kt`; consumidores: `CitaViewModel` y `AgendaGraph` (Valentina).

## ViewModels y repositorios

`CatalogoViewModel` observa especialidades/especialistas con StateFlow, selecciona la especialidad por ID y expone reintentos. `CatalogoRepository` adapta los flujos de Room. Agenda consume la selección mediante el contrato existente de `CitaViewModel` y `AppState`.

## Estados y validaciones

La lista y especialistas distinguen carga, contenido, vacío y error con acción de reintento. Especialidad inexistente no se presenta como contenido válido. Una especialidad sin especialistas muestra estado vacío; una modalidad incompatible y agenda pausada se informan aparte. No se crean productos ni datos alternativos en las pantallas.

## Reglas de acceso

Consulta del catálogo disponible para usuarios de la app. Reservar requiere sesión autenticada; la agenda pausada no continúa a selección/reserva. Las reglas de disponibilidad pertenecen a Agenda.

## Dependencias y pruebas

Dependencias: Room común (solo lectura aquí), `CatalogoRepository`, `CatalogoViewModel`, `CatalogoGraph`, `CitaViewModel`, `SavedStateHandle`, `AppState` y Agenda. Pruebas en `CatalogoViewModelModuleTest` cubren ID, filtrado, error/reintento, vacío y estados. En AL-02: compile, pruebas (195 aprobadas) y assemble reportados exitosos.

## Limitaciones

Los datos iniciales de especialistas/especialidades son de demostración. La edición de catálogo es administración; no hay fuente remota.
