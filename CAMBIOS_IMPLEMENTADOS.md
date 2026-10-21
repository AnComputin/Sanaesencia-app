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

La base de datos se elevó a versión 5. La migración es no destructiva para usuarios y citas. También corrige la definición de la tabla `especialistas` y normaliza el catálogo final de 7 especialidades.

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
