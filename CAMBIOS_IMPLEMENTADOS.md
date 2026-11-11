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

La base de datos está en versión 6. Las migraciones son no destructivas para usuarios y citas, normalizan el catálogo final de 7 especialidades y agregan beneficiarios persistentes.

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
- transforma las citas antiguas al esquema nuevo sin borrar el historial;
- conserva usuarios;
- normaliza el catálogo final de 7 especialidades.

### Prueba recomendada de beneficiarios

1. Iniciar sesión como PACIENTE.
2. Comenzar una reserva hasta llegar a Agendar.
3. Pulsar `Registrar menor o beneficiario a cargo`.
4. Completar nombre, RUT, fecha de nacimiento y parentesco.
5. Guardar: debe volver a Agendar con ese beneficiario seleccionado.
6. Completar la reserva y verificar que Confirmación/Mis citas muestre el nombre del beneficiario.
7. Cerrar sesión y entrar con otra cuenta: no debe ver los beneficiarios del primer usuario.
8. Volver a iniciar sesión con la primera cuenta: el beneficiario debe seguir disponible.
