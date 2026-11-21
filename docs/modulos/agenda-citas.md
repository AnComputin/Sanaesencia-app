# Manual del Módulo Agenda y Citas

Responsable: Valentina Guerrero (`vale`)

Este manual documenta el flujo de reserva de citas de SanaEsenciaApp, sus reglas de negocio, el comportamiento del pago simulado y las pruebas que lo respaldan.

---

## 1. Flujo completo de reserva

| Paso | Pantalla | Archivo |
|---|---|---|
| 1. Selección de especialidad | `SeleccionEspecialidadScreen` / `DetalleServicioScreen` | `ui/views/` (Alejandra) |
| 2. Selección de especialista | `SeleccionEspecialistaScreen` | `ui/views/catalogo/` (Alejandra) |
| 3. Modalidad, fecha, hora y paciente | `AgendarScreen` | `ui/views/agenda/AgendarScreen.kt` |
| 4. Resumen y pago simulado | `PagoScreen` | `ui/views/agenda/PagoScreen.kt` |
| 5. Confirmación con resumen y calendario | `ConfirmacionScreen` | `ui/views/agenda/ConfirmacionScreen.kt` |
| 6. Próximas citas, historial y cancelación | `MisCitasScreen` | `ui/views/citas/MisCitasScreen.kt` |

Rutas (`navigation/AgendaGraph.kt`): `agendar` → `pago` → `confirmacion` → `mis_citas`.

**Guardia de sesión:** las rutas `agendar`, `pago` y `mis_citas` redirigen a `login` si no hay sesión iniciada.

**Selección del paciente:** en `AgendarScreen` se puede reservar para el titular o para un beneficiario registrado. El nombre del paciente debe tener al menos 3 caracteres.

---

## 2. Reglas de negocio (fuente única: `ReglasAgenda`)

Todas las reglas viven en `data/repository/ReglasAgenda.kt`. `CitaViewModel` y `CitaDao.reservarSiDisponible` las consumen; no deben duplicarse constantes en otros archivos.

| Regla | Valor |
|---|---|
| Duración de la cita | 60 minutos |
| Boxes presenciales | 8 (numerados 1 a 8) |
| Modalidades | `PRESENCIAL`, `ONLINE` |
| Estados de la cita | `CONFIRMADA`, `COMPLETADA`, `CANCELADA` |
| Anticipación mínima para cancelar | 24 horas |

### Disponibilidad y solapamientos

- Una cita dura exactamente 60 minutos (`ReglasAgenda.calcularFin`).
- Un especialista no puede tener dos citas superpuestas (`haySolapamiento`); el intervalo es `[inicio, fin)`, por lo que dos citas contiguas (una termina cuando la otra empieza) sí están permitidas.
- Al seleccionar fecha y hora, los horarios ocupados aparecen deshabilitados.

### Boxes (solo modalidad presencial)

- Cada cita presencial recibe un box libre entre 1 y 8 al momento de confirmarse.
- La novena cita presencial superpuesta se rechaza: no hay boxes disponibles.
- Una cita `ONLINE` no consume box y puede coexistir con las 8 presenciales del mismo horario.
- La asignación es atómica (`CitaDao.reservarSiDisponible`, dentro de una transacción Room): dos reservas simultáneas no pueden quedarse con el mismo box.

### Cancelación

- Solo las citas `CONFIRMADA` con más de 24 horas de anticipación pueden cancelarse.
- Al cancelar, el estado cambia a `CANCELADA` y la cita pasa al historial.

---

## 3. Pago simulado y temporizador

- El pago es una **simulación** para el prototipo: no se procesa ningún medio de pago real.
- El botón de pago está habilitado solo con sesión iniciada y todos los datos completos.
- La reserva **no se confirma ni se persiste** hasta pulsar "Pagar": el horario y el box se asignan recién en ese momento. Hasta entonces, el horario puede ser tomado por otra reserva.
- Existe un temporizador de 10 minutos (`duracionPagoSegundos = 600`) que actúa como límite de la sesión de pago. Al expirar, se avisa al usuario y debe intentarlo nuevamente.
- **Pendiente de aprobación del equipo:** definir si el temporizador debe liberar el cupo, retenerlo durante el pago o mantenerse solo como recordatorio visual. Hasta que se acuerde, la interfaz **no afirma retención de cupo**.

---

## 4. Calendario

- Desde `ConfirmacionScreen` y `MisCitasScreen` se puede agregar la cita al calendario del dispositivo.
- El evento se crea con la fecha y hora de inicio y **duración de 60 minutos** (`ReglasAgenda.DURACION_CITA_MINUTOS`).
- Incluye título (especialidad), descripción (profesional y paciente) y ubicación (modalidad y box si corresponde).
- Si no hay aplicación de calendario disponible, se muestra un aviso.

---

## 5. Persistencia

- Entidad: `data/room/CitaEntity.kt` (tabla `citas`).
- DAO: `data/room/CitaDao.kt` — la reserva es atómica vía `reservarSiDisponible` (transacción Room con verificación de solapamiento, boxes y estado `CONFIRMADA`).
- Repositorio: `data/repository/CitaRepository.kt`.
- Con DAO real, Room es la fuente de verdad: las citas sobreviven al cierre de la app. Sin DAO (previews), el ViewModel usa una lista en memoria.

---

## 6. Pruebas

`app/src/test/java/com/example/sanaesenciaapp/viewmodel/CitaViewModelTest.kt` cubre el flujo vivo con un `FakeCitaDao`:

- Estado inicial y selección de servicio, modalidad, fecha, hora y paciente.
- Validaciones de nombre y `puedeContinuar`.
- `confirmarCita` presencial asigna box; online no asigna box.
- Reserva solapada para el mismo especialista es rechazada.
- La novena reserva presencial es rechazada con los 8 boxes ocupados.
- Online no consume box y permite presencial simultánea.
- `puedeCancelar` (24 h) y `cancelarCita` con persistencia.

También: `ReglasAgendaTest.kt` (reglas puras) y `CitaRepositoryTest.kt` (repositorio con DAO falso).

---

## 7. Archivos del módulo (propiedad `vale`)

- `data/room/CitaEntity.kt`, `data/room/CitaDao.kt`
- `data/repository/ReglasAgenda.kt`, `data/repository/CitaRepository.kt`
- `viewmodel/CitaViewModel.kt`, `viewmodel/AgendaViewModelModule.kt`
- `ui/views/agenda/AgendarScreen.kt`, `PagoScreen.kt`, `ConfirmacionScreen.kt`
- `ui/views/citas/MisCitasScreen.kt`
- `navigation/AgendaGraph.kt`
- `utils/CalendarioHelper.kt` (uso desde este módulo)

---

## 8. Limitaciones conocidas

- El pago es simulado; no integra pasarela real (pendiente contrato backend).
- La regla de expiración/retén de cupo del temporizador está pendiente de aprobación del equipo.
- La integración con microservicios (VA-05) aún no está implementada; hoy la persistencia es local con Room.
