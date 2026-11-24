# Autorización de agenda-service

`agenda-service` utiliza el mismo JWT emitido por `usuarios-service`.

## Validación del token

- Encabezado: `Authorization: Bearer <JWT>`.
- Firma: HS256 con `jwt.secret` compartido.
- Emisor obligatorio: `sana-esencia-usuarios`.
- Se validan firma, `exp` y `iss`.
- El claim `rol` se convierte en `ROLE_<ROL>` para Spring Security.

## Permisos implementados

| Endpoint | PACIENTE | TERAPEUTA | ADMIN |
|---|---|---|---|
| `GET /api/citas` | Solo sus citas, identidad tomada de `sub` | 403 hasta asociar la cuenta a un especialista | Todas o filtradas |
| `GET /api/citas/{id}` | Solo si es propietario | 403 hasta asociar la cuenta a un especialista | Cualquier cita |
| `POST /api/citas` | Sí; `usuarioEmail` se fuerza al `sub` del JWT y el estado inicial a `CONFIRMADA` | 403 | 403 |
| `PATCH /api/citas/{id}/estado` | 403 | 403 | Sí |

Los parámetros `usuarioEmail` enviados por un paciente se ignoran. Un paciente no puede consultar citas ajenas ni reservar a nombre de otro usuario.

## Pendiente para habilitar terapeutas

El modelo `Usuario` y el JWT actual no contienen un identificador que relacione una cuenta TERAPEUTA con `especialistaId`. Por seguridad, el acceso de terapeutas a citas queda denegado hasta implementar y verificar esa relación (por ejemplo, agregar `especialistaId` al usuario y como claim firmado en el JWT, o resolverla en el backend). No se debe confiar en un `especialistaId` enviado libremente por Android.

## Respuestas de seguridad

- `401 Unauthorized`: token ausente, expirado, firma inválida o emisor incorrecto.
- `403 Forbidden`: token válido, pero rol o propiedad de la cita insuficientes.
- `400 Bad Request`: datos o reglas de negocio inválidos.
- `404 Not Found`: identificador de cita inexistente.

## Android

Android continúa usando el endpoint de login existente y el mismo `Authorization: Bearer ...` a través del interceptor compartido. No se agrega un login separado para agenda-service.
