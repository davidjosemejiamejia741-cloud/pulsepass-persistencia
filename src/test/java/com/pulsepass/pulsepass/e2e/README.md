# PulsePass — Pruebas E2E manuales (Fase 5)

## Propósito y alcance

Verificar manualmente, mediante Postman, la integración de las **20 rutas de negocio de Fase 3** y las **2 rutas de autenticación de Fase 4**, conforme al PRD de Fase 5. La colección incluye pruebas de rutas base, escenarios negativos de seguridad, validaciones HTTP y flujos de negocio.

La ejecución está documentada en `registro-ejecucion-manual.md`. La existencia de una solicitud en la colección no significa que haya sido aprobada: cada resultado se determina a partir de las verificaciones realizadas.

## Requisitos previos

- API PulsePass iniciada y conectada a una base de datos PostgreSQL exclusiva para E2E (por ejemplo, `pulsepass_e2e`), con las migraciones Flyway aplicadas.
- Postman Desktop o Web con acceso a la API.
- Venue y artista activos disponibles en la base de pruebas.
- Cuentas `USER_A` y `USER_B` registradas o listas para registrarse; cuentas `STAFF` y `ADMIN` preparadas mediante un procedimiento controlado; cuenta inactiva para los escenarios que la requieran.
- Política de acceso a recursos ajenos revisada, incluidos los códigos `403` y `404` y la variable `expectedForeignResourceStatus`, cuando corresponda.
- Instrucciones de `fixtures/README.md` revisadas antes de preparar datos. No ejecutar operaciones de preparación sobre producción.

## Importación y configuración

1. Importar `postman/PulsePass-Manual-E2E.postman_collection.json` en Postman.
2. Importar `postman/PulsePass-local.postman_environment.json` y seleccionar el environment `PulsePass-local`.
3. Configurar `baseUrl` (por defecto, `http://localhost:8080`) y asignar un `runId` que identifique la ejecución.
4. Consultar los recursos disponibles y establecer `venueActiveCode`, `artistId` y `artistStageName` según las respuestas reales.
5. Configurar las identidades y contraseñas de prueba en variables locales de Postman. No incluir secretos en los archivos exportados.
6. Ejecutar las solicitudes manualmente con **Send**, siguiendo las descripciones de cada request. No utilizar Collection Runner, Newman ni scripts Pre-request o Tests.
7. Después de cada login, copiar el `accessToken` a la variable local correspondiente: `userAToken`, `userBToken`, `staffToken` o `adminToken`. Si un JWT expira, iniciar sesión nuevamente.
8. Tras crear eventos o tickets, guardar los códigos devueltos por la API en las variables que correspondan. No asumir que existen los recursos indicados por valores de ejemplo.
9. Comparar el **HTTP observado**, el **JSON devuelto** y, cuando corresponda, el **estado persistido** con el resultado esperado en la documentación de la solicitud. Una respuesta `2xx` por sí sola no garantiza un PASS.
10. Registrar los resultados reales en `registro-ejecucion-manual.md`, diferenciando `PASS`, `FAIL`, `BLOCKED`, `NOT RUN` o `REVISAR` según corresponda.

## Secuencia sugerida de ejecución

| Carpeta | Actividad y verificaciones |
|---|---|
| 00 Preparación / conectividad | Comprobamos que la API responde y que existe un venue activo para las pruebas. Esta preparación no añade una ruta base. |
| 01 Venues | Consultamos los venues activos y recuperamos un venue por código; verificamos que ambos resultados son coherentes. |
| 02 Artists | Listamos artistas activos y consultamos un artista por ID y nombre artístico; conservamos sus identificadores para los eventos. |
| 03 Auth / usuarios | Registramos cuentas de prueba, iniciamos sesión y verificamos identidad y roles con `/api/auth/me`. Preparamos los tokens para las siguientes carpetas. |
| 04 Usuarios / consulta y propiedad | Consultamos a un usuario por email y username, y comprobamos que los datos corresponden al actor autorizado. |
| 05 Events / administración y visibilidad | Creamos un evento en `DRAFT`, lo consultamos, asociamos un artista, lo publicamos y verificamos su visibilidad pública. |
| 06 Tickets / compra, consulta y ciclo de vida | Compramos tickets, consultamos sus datos e historial, listamos tickets pagados con un rol autorizado y verificamos las transiciones `PAID → CANCELLED` y `PAID → USED`. |
| 07 Seguridad / escenarios negativos | Probamos solicitudes sin autenticación, tokens inválidos o expirados, restricciones por roles, propiedad de recursos y revocación de permisos. |
| 08 Validaciones y errores HTTP | Verificamos solicitudes inválidas, recursos inexistentes, registros duplicados y rechazos de autenticación, comparando los códigos HTTP esperados y observados. |
| 09 Flujos de negocio / secuencia recomendada | Ejecutamos las secuencias integradas de descubrimiento, autenticación, gestión de eventos, compra, cancelación, uso y regresión de errores. |

Las carpetas 07–09 reutilizan rutas de las carpetas base; sus solicitudes no incrementan el total de **22 rutas únicas**. Para conocer el orden exacto y las precondiciones de cada caso, consultar los Docs de la colección.

## Verificación y criterios de cierre

- En cada caso comprobamos actor, autorización, precondiciones, respuesta HTTP, estructura del DTO o `ErrorResponse` y, cuando aplica, el estado final mediante una consulta posterior o verificación en la base de pruebas.
- En los escenarios de propiedad verificamos que un usuario no pueda consultar ni modificar recursos ajenos.
- Registramos los resultados de las validaciones `400`, `401`, `403`, `404` y `409`, según cada contrato. Un `500` inesperado en un escenario nominal se considera fallo.
- Actualizamos el checklist, la matriz de cobertura y la bitácora con los resultados reales. Los casos pendientes, fallidos o en revisión se mantienen identificados; no se declara cumplimiento total del DoD mientras existan criterios sin satisfacer.
- No versionamos contraseñas, JWT, secretos ni datos personales reales.

## Archivos de la entrega

| Archivo | Propósito |
|---|---|
| `postman/PulsePass-Manual-E2E.postman_collection.json` | Colección Postman v2.1 con solicitudes, descripciones y ejemplos, sin scripts. |
| `postman/PulsePass-local.postman_environment.json` | Plantilla de variables sin valores sensibles. |
| `checklist-e2e-manual.md` | Seguimiento de rutas y escenarios. |
| `registro-ejecucion-manual.md` | Bitácora con HTTP esperado y observado, estados y verificaciones realizadas. |
| `matriz-cobertura.md` | Trazabilidad de las 22 rutas base y los roles. |
| `fixtures/README.md` | Preparación y consideraciones sobre los datos de prueba. |
| `.gitignore` | Exclusión de archivos locales o sensibles que no deben versionarse. |

`fixtures/seed-e2e.sql` es **opcional** y no forma parte de esta entrega. Los datos se prepararon mediante el procedimiento manual documentado.

## Consideraciones para nuevas ejecuciones

Los valores de `runId`, los códigos de eventos y tickets y las credenciales de prueba deben revisarse antes de iniciar una nueva ejecución. Los resultados **no se trasladan automáticamente a ejecuciones posteriores**: cada repetición debe verificarse y registrarse por separado.

## Resultado de la ejecución 

| Área | Resultado | Observación |
|---|---|---|
| Rutas base | **22/22 PASS** | Se verificaron las 20 rutas de negocio y las 2 rutas de autenticación. |
| Seguridad | **25 PASS; 1 REVISAR** | `TC-SEC-017`: después de revocar `STAFF`, observamos la secuencia `200 → 401 → 200` tras restaurar el rol. |
| Validaciones HTTP | **7 PASS; 1 FAIL** | `TC-HTTP-007`: con `USER_A` obtuvimos `403` cuando el caso esperaba `404`; con `ADMIN` se obtuvo `404`. |
| Flujos de negocio | **16/16 PASS** en los escenarios principales | Los subescenarios de aforo agotado y edad insuficiente quedaron **NOT RUN** por falta de datos específicos. |
| Flujos integrados A–E | **A, B, C y D PASS; E PARCIAL** | El flujo E conserva el fallo HTTP y la revisión de seguridad pendientes. |

El detalle de las solicitudes, los resultados observados y las limitaciones está en `registro-ejecucion-manual.md`. **No cambiamos un resultado FAIL o REVISAR a PASS sin una nueva verificación que lo justifique.**
