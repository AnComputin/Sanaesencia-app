# Beneficiarios

- **Responsable:** Alejandra Núñez
- **Actualizado:** 22-11-2026
- **Versión:** 1.0.0
- **Estado:** Implementado

## Objetivo y datos

Permitir que el titular autenticado administre menores/personas a cargo para seleccionarlos en una cita. Campos: nombre, RUT, fecha de nacimiento y parentesco. Datos persistidos en Room mediante `BeneficiarioEntity`; no son una relación clínica con el Terapeuta.

## Pantallas y navegación

`FichaBeneficiarioScreen` presenta listado/resumen, formulario de alta/edición, desactivación y reactivación al registrar nuevamente un RUT desactivado. Se abre desde Agenda para registrar un beneficiario y vuelve a la ruta anterior. No hay pantalla de detalle separada.

## Archivos relacionados

- `data/repository/BeneficiarioRepository.kt`, `viewmodel/BeneficiarioViewModel.kt`, `viewmodel/BeneficiarioViewModelModule.kt`.
- `ui/views/auth/FichaBeneficiarioScreen.kt`, `navigation/AuthGraph.kt`.
- Consultas de `BeneficiarioDao` (infraestructura compartida de Anaís, solo dependencia); consumo desde `AgendaGraph` (Valentina).

## ViewModel y repositorio

`BeneficiarioViewModel` expone un estado StateFlow, observa cambios de sesión y limpia datos al cambiar/cerrar sesión. `BeneficiarioRepository` obtiene identidad de `SesionRepository`; no recibe propietario desde la UI. `BeneficiarioDao` filtra operaciones por propietario además del filtrado de estado.

## Estados y validaciones

Carga, vacío, sesión requerida, contenido y error de persistencia están diferenciados; error permite reintentar lectura. Validaciones: `validarNombre`, `validarRut`, `validarFechaNacimiento`, `validarParentesco`. Una operación guardando deshabilita envíos/cambios duplicados en UI y ViewModel.

## Reglas de acceso

El correo propietario deriva exclusivamente de sesión autenticada y se normaliza. Consultas por ID, actualización, desactivación/reactivación filtran por ID y correo propietario. Cerrar sesión vacía el estado visible. Agenda muestra los beneficiarios de la cuenta actual; al reservar guarda `beneficiarioId` y el nombre histórico del paciente.

## Dependencias y pruebas

Depende de `BeneficiarioRepository`, `BeneficiarioViewModel`, `SesionRepository`, consultas existentes de `BeneficiarioDao`, `AppState` y Agenda. AL-03 ejecutó compile, 196 unit tests y assemble correctamente; pruebas cubren alta/validación, consulta, edición, aislamiento entre cuentas, cierre de sesión y reintento de error.

## Limitaciones

La sesión se mantiene en memoria. La relación con cita se guarda por ID, pero una cita conserva además snapshot de nombre. La UI muestra resumen en listado en vez de detalle independiente.
