# Bitácora consolidada de ejecución manual E2E — PulsePass

**Proyecto:** PulsePass

**Ambiente:** PulsePass-local

**Run ID:** `e2e-20261017-01`

**Fecha:** 2026-10-18

**Equipo ejecutor:** 2 integrantes

**Base URL:** `http://localhost:8080`

## Resumen ejecutivo

La ejecución manual cubrió las 22 rutas base de las carpetas 01–06 y los escenarios adicionales de seguridad, errores HTTP y flujos de negocio. La bitácora diferencia los resultados confirmados en Postman de las observaciones que requieren revisión técnica.

| Área | Cobertura | Resultado consolidado |
|---|---|---|
| Rutas base (carpetas 01–06) | 22 rutas únicas | **22 PASS** |
| Seguridad / escenarios negativos (carpeta 07) | Casos de autenticación, roles, propiedad y revocación | **25 PASS; 1 caso en revisión técnica** |
| Validaciones y errores HTTP (carpeta 08) | 8 casos principales | **7 PASS; 1 FAIL** (`TC-HTTP-007`) |
| Flujos de negocio (carpeta 09) | `TC-BIZ-001` a `TC-BIZ-016` | **16 PASS** en los escenarios principales |
| Flujos integrados definidos por el PRD | A–E | **A, B, C y D PASS; E parcial** |

### Hallazgos importantes

1. **TC-HTTP-007 — FAIL:** USER_A recibe `403 Forbidden` al consultar un código de ticket inexistente; el resultado esperado era `404 Not Found`. Con ADMIN, el mismo código sí devuelve `404`.

2. **TC-SEC-017 — revisión técnica:** después de retirar la asignación `STAFF` al usuario `id=4`, la misma petición privilegiada dejó de funcionar y devolvió `401 Unauthorized`; al restaurar el rol, la petición volvió a `200 OK`. Se conserva el resultado observado sin marcar la prueba como PASS definitivo, porque el HTTP fue `401` y debe confirmarse la política esperada.

3. **TC-SEC-011 — PASS en la repetición válida:** USER_B recibió `403` al intentar leer/cancelar el ticket de USER_A (`id=7`). Después USER_A pudo cancelar ese mismo ticket con `200 OK`, pasando de `PAID` a `CANCELLED`. El primer intento histórico se conserva como no concluyente porque usó un ticket que ya estaba cancelado.

4. **Flujo D — PASS:** se verificaron compra, propiedad, cancelación, rechazo de segunda cancelación, uso por STAFF, rechazo del segundo uso y consultas posteriores de integridad.

5. Continúan fuera de la cobertura ejecutada los subescenarios de aforo agotado y edad insuficiente, que requieren fixtures específicos.

## Índice

- [Criterios de registro](#criterios-de-registro)

- [Resumen por carpetas](#resumen-por-carpetas)

- [00 Preparación / conectividad](#00-preparación--conectividad)

- [01 Venues](#01-venues-2-rutas)

- [02 Artists](#02-artists-3-rutas)

- [03 Auth / usuarios](#03-auth--usuarios-registro-login-me)

- [04 Usuarios / consulta y propiedad](#04-usuarios--consulta-y-propiedad-2-rutas)

- [05 Events / administración y visibilidad](#05-events--administración-y-visibilidad-6-rutas)

- [06 Tickets / compra, consulta y ciclo de vida](#06-tickets--compra-consulta-y-ciclo-de-vida-6-rutas)

- [07 Seguridad / escenarios negativos](#07-seguridad--escenarios-negativos)

- [08 Validaciones y errores HTTP](#08-validaciones-y-errores-http)

- [09 Flujos de negocio](#09-flujos-de-negocio--secuencia-recomendada)

- [Defectos y puntos de revisión](#defectos-y-bloqueos)

- [Cierre de ejecución](#cierre-de-ejecución)

## Criterios de registro

- Las secciones siguen el mismo orden y los mismos nombres de carpetas de la colección de Postman. Las operaciones temporales de preparación se distinguen de las 22 rutas base.

- Registramos los envíos relevantes y las verificaciones posteriores, distinguiendo las ejecuciones iniciales de las repeticiones cuando aportan resultados distintos.

- Documentamos los resultados de nuestras ejecuciones sin reproducir encabezados sensibles, credenciales, tokens ni el cuerpo completo de las peticiones.

- No incluimos contraseñas, JWT ni datos personales sensibles; utilizamos identificadores de prueba cuando necesitamos referenciar recursos.

- Utilizamos `PASS`, `FAIL`, `BLOCKED`, `NOT RUN` o `REVISAR`, según la evidencia disponible. Cuando una precondición no se cumple, se indica expresamente.

- No agregamos columnas de fecha/hora, request ni ID de colección, porque no forman parte del formato solicitado por el PRD. Las fechas técnicas que aparecían dentro de respuestas de tickets se omiten de las tablas cuando no son necesarias para comprobar el resultado.

## Resumen por carpetas

| Carpeta de la colección | Cobertura | PASS | FAIL | BLOCKED / NOT RUN | Revisión / observaciones |
|---|---|---:|---:|---:|---|
| 00 Preparación / conectividad | 1 comprobación previa; no suma a las 22 rutas base | 1 | 0 | 0 | API disponible y venue E2E activo. |
| 01 Venues | 2 rutas base | 2 | 0 | 0 | `TC-E2E-01` y `TC-E2E-02` confirmados. |
| 02 Artists | 3 rutas base | 3 | 0 | 0 | `TC-E2E-03` a `TC-E2E-05`; artista elegido: Solar Beat (`id=1`). |
| 03 Auth / usuarios | 3 rutas base | 3 | 0 | 0 | Registro, login y `/me` de las cuentas de prueba; la variante de login por email de USER_A no se registra como una ruta adicional. |
| 04 Usuarios / consulta y propiedad | 2 rutas base | 2 | 0 | 0 | `TC-E2E-12` y `TC-E2E-13` confirmados. |
| 05 Events / administración y visibilidad | 6 rutas base | 6 | 0 | 0 | Creación, lectura de DRAFT, asociación de Solar Beat, publicación y visibilidad pública comprobadas. |
| 06 Tickets / compra, consulta y ciclo de vida | 6 rutas base | 6 | 0 | 0 | Compra, consulta, cancelación y uso; verificaciones posteriores de estados `CANCELLED` y `USED` completadas. |
| 07 Seguridad / escenarios negativos | Casos adicionales | 25 | 0 | 0 | `TC-SEC-011` aprobado en la repetición con ticket `PAID`; `TC-SEC-017` queda en revisión por respuesta `401` después de revocar `STAFF`. |
| 08 Validaciones y errores HTTP | 8 casos principales | 7 | 1 | 0 | `TC-HTTP-007` permanece FAIL: USER_A recibe `403` en lugar del `404` esperado. |
| 09 Flujos de negocio | `TC-BIZ-001` a `TC-BIZ-016` | 16 | 0 | 0 | Los escenarios principales quedaron PASS. Subescenarios de aforo agotado y edad insuficiente no ejecutados por falta de fixtures. |
| **Total rutas base 01–06** | **22 rutas únicas** | **22** | **0** | **0** | Las pruebas adicionales reutilizan las rutas base; no aumentan el total de rutas. |

## 00 Preparación / conectividad

Utilizamos `GET /api/venues/active` para comprobar conectividad, como indica el PRD. Esta ejecución preliminar no cuenta entre las 22 rutas base; repetimos la consulta en `01 Venues` para comprobar la cobertura de la ruta.

| Caso | Actor | Método y ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Verificación realizada |
| --- | --- | --- | ---: | --- | --- | --- | --- |
| Preparación | ANONYMOUS | GET `/api/venues/active` | 200 | **200 OK** | JSON válido: venue `id=1`, `code=E2E-LOCAL-001`, `active=true`, `capacity=500`. | **PASS** | Comprobamos la conectividad con la API y confirmamos que el venue de prueba está activo. |
| Revisión previa | Estado (OK / BLOCKED) | Observación |
|---|---|---|
| Aplicación PulsePass iniciada | OK | Las peticiones de Postman recibieron respuestas HTTP. |
| Base de datos E2E disponible y migraciones aplicadas | OK | Se consultaron y modificaron únicamente datos de prueba en DBeaver. |
| Ambiente `PulsePass-local` seleccionado en Postman | OK | Las solicitudes usaron `{{baseUrl}}` con `http://localhost:8080`. |
| Variables de ambiente necesarias preparadas | OK | Se utilizaron variables locales de entorno/colección para códigos y tokens; no se registraron JWT ni contraseñas. |
| Cuentas/fixtures requeridos disponibles | OK | USER_A, USER_B, ADMIN, STAFF, cuenta inactiva y fixtures de eventos/tickets disponibles. |

### Preparación adicional de cuenta privilegiada (fixture ADMIN)

Realizamos estas operaciones auxiliares exclusivamente en la base de pruebas. No las contabilizamos como rutas base adicionales ni como pruebas de autorización de ADMIN.

| Operación | HTTP / comprobación | Resultado | Observación |
| --- | --- | --- | --- |
| Registro temporal de `e2e_admin_01` mediante `POST /api/users` | **201 Created** | **PASS** | Registramos la cuenta ADMIN (`id=3`, `active=true`) y comprobamos que la respuesta no incluye contraseña ni hash. |
| Asignación controlada de rol en `user_roles` y consulta de verificación SQL | `user_id=3`, `role=ADMIN`, usuario activo | **PASS** | Asignamos el rol `ADMIN` en la base de pruebas y comprobamos que los roles de USER_A y USER_B no cambiaron. |
| Inicio de sesión temporal ADMIN | **200 OK** | **PASS** | Iniciamos sesión y obtuvimos un JWT Bearer con `expiresIn=900`; registramos la ejecución en la sección 03. |
| Comprobación de autoridad con `GET /api/auth/me` | **200 OK** | **PASS** | Consultamos `/me` y confirmamos `id=3`, `username=e2e_admin_01` y `ROLE_ADMIN`, sin exposición de datos sensibles. |

### Preparación adicional de cuenta privilegiada (fixture STAFF)

| Operación | HTTP / comprobación | Resultado | Observación |
| --- | --- | --- | --- |
| Registro temporal de `e2e_staff_01` mediante `POST /api/users` | **201 Created** | **PASS** | Registramos la cuenta STAFF (`id=4`, `active=true`) y comprobamos que la respuesta no incluye contraseña ni hash. |
| Asignación controlada del rol y verificación SQL | `user_id=4`, `role=STAFF`, usuario activo | **PASS** | Asignamos el rol `STAFF` y comprobamos en la base de pruebas que no cambiaron los demás usuarios. |
| Inicio de sesión temporal STAFF | **200 OK** | **PASS** | Iniciamos sesión con STAFF y obtuvimos un JWT Bearer con `expiresIn=900`; no registramos el token. |
| Verificación de identidad con `GET /api/auth/me` | **200 OK** | **PASS** | Consultamos `/me` y confirmamos `id=4`, `username=e2e_staff_01` y `ROLE_STAFF`. |

## 01 Venues (2 rutas)

| Caso PRD | Actor | Método y ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Verificación realizada |
| --- | --- | --- | ---: | --- | --- | --- | --- |
| E2E-VEN-02 | ANONYMOUS | GET `/api/venues/active` | 200 | **200 OK** | Lista JSON con venue `id=1`, `code=E2E-LOCAL-001`, `name=Venue de prueba E2E`, `active=true`. | **PASS** | Consultamos los venues activos y confirmamos que aparece el venue de prueba esperado. |
| E2E-VEN-01 | ANONYMOUS | GET `/api/venues/{code}` | 200 | **200 OK** | Objeto JSON del venue `id=1`, `code=E2E-LOCAL-001`, `active=true`, `capacity=500`. | **PASS** | Consultamos el venue por código y comprobamos que coincide con el venue activo. |

## 02 Artists (3 rutas)

| Caso PRD | Actor | Método y ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Verificación realizada |
| --- | --- | --- | ---: | --- | --- | --- | --- |
| E2E-ART-03 | ANONYMOUS | GET `/api/artists/active` | 200 | **200 OK** | Lista JSON de artistas activos; se seleccionó `id=1`, `stageName=Solar Beat`, `active=true`. | **PASS** | Consultamos los artistas activos y seleccionamos a Solar Beat (`id=1`) para las siguientes pruebas. |
| E2E-ART-01 | ANONYMOUS | GET `/api/artists/{id}` | 200 | **200 OK** | `id=1`, `stageName=Solar Beat`, `country=Colombia`, `genre=Electronic`, `active=true`. | **PASS** | Consultamos el artista por ID y confirmamos que corresponde a Solar Beat. |
| E2E-ART-02 | ANONYMOUS | GET `/api/artists/by-stage-name?stageName=...` | 200 | **200 OK** | `id=1`, `stageName=Solar Beat`, `country=Colombia`, `genre=Electronic`, `active=true`. | **PASS** | Consultamos por nombre artístico y obtuvimos el artista esperado. |

## 03 Auth / usuarios (registro, login, me)

En esta carpeta documentamos el registro, el inicio de sesión y la consulta de `/me` de cada actor como ejecuciones independientes. Las credenciales incorrectas se rechazaron con `401`; los JWT no se incluyen en la bitácora. No incluimos contraseñas ni tokens.

| Caso PRD | Actor | Método y ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Verificación realizada |
| --- | --- | --- | ---: | --- | --- | --- | --- |
| E2E-USR-01 / TC-AUTH-001 | ANONYMOUS | POST `/api/users` | 201 | **201 Created** | `id=1`, `username=e2e_user_a_01`, `email=e2e_user_a_01@example.test`, `active=true`; sin contraseña/hash. | **PASS** | Registramos a USER_A y posteriormente confirmamos su rol USER mediante `/me`. |
| E2E-USR-01 | ANONYMOUS | POST `/api/users` | 201 | **201 Created** | `id=2`, `username=e2e_user_b_02`, `email=e2e_user_b_02@example.test`, `active=true`; sin contraseña/hash. | **PASS** | Registramos a USER_B con datos distintos de USER_A. |
| E2E-AUTH-01 / TC-AUTH-002 | ANONYMOUS | POST `/api/auth/login` | 200 | **200 OK** | `accessToken` no vacío, `tokenType=Bearer`, `expiresIn=900`. | **PASS** | Iniciamos sesión con USER_A y comprobamos que se generó un JWT Bearer; no lo registramos en la bitácora. |
| E2E-AUTH-01 / TC-AUTH-003 | ANONYMOUS | POST `/api/auth/login` | 200 |  |  | NOT RUN |  |
| E2E-AUTH-01 | ANONYMOUS | POST `/api/auth/login` | 200 | **200 OK** | `accessToken` no vacío, `tokenType=Bearer`, `expiresIn=900`. | **PASS** | Iniciamos sesión con USER_B y comprobamos que se generó un JWT Bearer; no lo registramos en la bitácora. |
| E2E-AUTH-01 (preparación de actor) | ANONYMOUS | POST `/api/auth/login` | 200 | **200 OK** | `accessToken` no vacío, `tokenType=Bearer`, `expiresIn=900`. | **PASS** | Iniciamos sesión con STAFF y comprobamos que se emitió un JWT Bearer; no lo registramos en la bitácora. |
| E2E-AUTH-01 (preparación de actor) | ANONYMOUS | POST `/api/auth/login` | 200 | **200 OK** | `accessToken` no vacío, `tokenType=Bearer`, `expiresIn=900`. | **PASS** | Iniciamos sesión con ADMIN y comprobamos que se emitió un JWT Bearer; posteriormente confirmamos su rol con `/api/auth/me`. |
| E2E-AUTH-02 / TC-AUTH-006 | USER_A | GET `/api/auth/me` | 200 | **200 OK** | `id=1`, `username=e2e_user_a_01`, `roles=[ROLE_USER]`; sin contraseña/hash. | **PASS** | Consultamos `/me` con USER_A y confirmamos su identidad y rol sin exposición de contraseñas. |
| E2E-AUTH-02 | USER_B | GET `/api/auth/me` | 200 | **200 OK** | `id=2`, `username=e2e_user_b_02`, `roles=[ROLE_USER]`; sin contraseña/hash. | **PASS** | Consultamos `/me` con USER_B y confirmamos su identidad y rol sin exposición de contraseñas. |
| E2E-AUTH-02 (preparación de actor) | STAFF | GET `/api/auth/me` | 200 | **200 OK** | `id=4`, `username=e2e_staff_01`, `roles=[ROLE_STAFF]`. | **PASS** | Consultamos `/me` con STAFF y confirmamos su identidad y autoridad `ROLE_STAFF`. |
| E2E-AUTH-02 (preparación de actor) | ADMIN | GET `/api/auth/me` | 200 | **200 OK** | `id=3`, `username=e2e_admin_01`, `roles=[ROLE_ADMIN]`. | **PASS** | Consultamos `/me` con ADMIN y confirmamos su identidad y autoridad `ROLE_ADMIN`. |

## 04 Usuarios / consulta y propiedad (2 rutas)

| Caso PRD | Actor | Método y ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Verificación realizada |
| --- | --- | --- | ---: | --- | --- | --- | --- |
| E2E-USR-02 | USER_A | GET `/api/users/by-email?email=...` | 200 | **200 OK** | `id=1`, `username=e2e_user_a_01`, `email=e2e_user_a_01@example.test`, `active=true`; sin contraseña/hash. | **PASS** | Consultamos los datos de USER_A por email y confirmamos que corresponden a la cuenta autenticada. |
| E2E-USR-03 | USER_A | GET `/api/users/by-username?username=...` | 200 | **200 OK** | `id=1`, `username=e2e_user_a_01`, `email=e2e_user_a_01@example.test`, `active=true`, `firstName=Usuario`, `lastName=Prueba A`, `phone=null`, `city=Santa Marta`, `birthDate=1995-05-10`; sin contraseña/hash. | **PASS** | Consultamos los datos de USER_A por username y confirmamos que corresponden a la cuenta autenticada. |

## 05 Events / administración y visibilidad (6 rutas)

| Caso PRD | Actor | Método y ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Verificación realizada |
| --- | --- | --- | ---: | --- | --- | --- | --- |
| E2E-EVT-01 / TC-BIZ-003 | ADMIN | POST `/api/events` | 201 | **201 Created** | `id=1`, `eventCode=E2E-EVENT-20261018-01`, `category=MUSIC`, `status=DRAFT`, `eventDate=2027-02-15T19:00:00`, `minimumAge=0`, `venueCode=E2E-LOCAL-001`, `artists=[]`. | **PASS con observación** | Creamos el evento y detectamos que `{{runId}}` no se sustituyó en el nombre. Decidimos eliminar ese evento para repetir la prueba con la variable corregida. |
| E2E-EVT-01 / TC-BIZ-003 | ADMIN | POST `/api/events` | 201 | **201 Created** | `id=2`, `eventCode=E2E-EVENT-20261018-01`, `name=PulsePass E2E e2e-20261017-01`, `description=Evento de prueba manual`, `category=MUSIC`, `status=DRAFT`, `eventDate=2027-02-15T19:00:00`, `minimumAge=0`, `venueCode=E2E-LOCAL-001`, `venueName=Venue de prueba E2E`, `artists=[]`. | **PASS** | Repetimos la creación tras corregir `runId` y comprobamos que el nombre, código y venue son los esperados. |
| E2E-EVT-02 | ADMIN | GET `/api/events/{{newEventCode}}` | 200 | **200 OK** | `id=2`, `eventCode=E2E-EVENT-20261018-01`, `name=PulsePass E2E e2e-20261017-01`, `category=MUSIC`, `status=DRAFT`, `eventDate=2027-02-15T19:00:00`, `minimumAge=0`, `venueCode=E2E-LOCAL-001`, `venueName=Venue de prueba E2E`, `artists=[]`. | **PASS** | Consultamos el evento con ADMIN y comprobamos que sus datos coinciden con la creación corregida y que permanece en `DRAFT`. |
| E2E-EVT-03 | ANONYMOUS | GET `/api/events/published` | 200 | **200 OK** | JSON válido: `[]` (lista vacía). El evento `E2E-EVENT-20261018-01` sigue en `DRAFT` según TC-E2E-15 y no aparece en la lista pública. | **PASS** | Consultamos los eventos publicados y obtuvimos una lista vacía; confirmamos que el borrador no estaba expuesto públicamente. |
| E2E-EVT-04 / TC-BIZ-004 (transición y consulta pública posterior verificadas) | ADMIN | PATCH `/api/events/{{newEventCode}}/publish` | 200 | **200 OK** | Evento `id=2`, `eventCode=E2E-EVENT-20261018-01`, `name=PulsePass E2E e2e-20261017-01`, `status=PUBLISHED`, `category=MUSIC`, `eventDate=2027-02-15T19:00:00`, `minimumAge=0`, `venueCode=E2E-LOCAL-001`, `artists=[{id:1, stageName:Solar Beat, country:Colombia, genre:Electronic, active:true}]`. | **PASS** | Publicamos el evento con ADMIN, comprobamos el estado `PUBLISHED` y verificamos su visibilidad pública en consultas posteriores. |
| E2E-EVT-05 / TC-BIZ-005 (asociación y consulta pública por artista verificadas) | ADMIN | POST `/api/events/{{newEventCode}}/artists/{{artistId}}` | 200 | **200 OK** | Evento `id=2`, `eventCode=E2E-EVENT-20261018-01`, `name=PulsePass E2E e2e-20261017-01`, `status=DRAFT`, `venueCode=E2E-LOCAL-001`, `artists=[{id:1, stageName:Solar Beat, country:Colombia, genre:Electronic, active:true}]`. | **PASS** | Asociamos a Solar Beat con el evento y comprobamos que este conservó su estado `DRAFT`. |
| E2E-EVT-03 / TC-BIZ-004 (verificación posterior) | ANONYMOUS | GET `/api/events/published` | 200 | **200 OK** | Lista JSON con el evento `id=2`, `eventCode=E2E-EVENT-20261018-01`, `name=PulsePass E2E e2e-20261017-01`, `category=MUSIC`, `status=PUBLISHED`, `eventDate=2027-02-15T19:00:00`, `venueName=Venue de prueba E2E`. | **PASS** | Repetimos la consulta pública después de publicar y confirmamos que ahora aparece el evento `PUBLISHED`. Verificamos la asociación del artista en TC-E2E-18 y TC-E2E-19. |
| E2E-EVT-06 / TC-BIZ-005 (verificación de consulta por artista) | ANONYMOUS | GET `/api/events/by-artist?stageName={{artistStageName}}` | 200 | **200 OK** | Lista JSON con evento `id=2`, `eventCode=E2E-EVENT-20261018-01`, `name=PulsePass E2E e2e-20261017-01`, `category=MUSIC`, `status=PUBLISHED`, `eventDate=2027-02-15T19:00:00`, `venueName=Venue de prueba E2E`. | **PASS** | Consultamos eventos por Solar Beat y confirmamos que aparece el evento publicado; ya había verificado la asociación en TC-E2E-18. |

## 06 Tickets / compra, consulta y ciclo de vida (6 rutas)

| Caso PRD | Actor | Método y ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Verificación realizada |
| --- | --- | --- | ---: | --- | --- | --- | --- |
| E2E-TKT-01 / TC-BIZ-007 (compra y consulta posterior realizadas) | USER_A | POST `/api/tickets` | 201 | **201 Created** | JSON válido: `id=1`, `ticketCode=TKT-d2570dd4-b5c9-418d-8e20-9eea85cbd38b`, `type=GENERAL`, `price=100000.00`, `status=PAID`, `userEmail=e2e_user_a_01@example.test`, `eventCode=E2E-EVENT-20261018-01`, `eventName=PulsePass E2E e2e-20261017-01`. | **PASS** | Compramos un ticket con USER_A y confirmamos que se creó en `PAID`. Guardamos su código para consultas posteriores; la tarifa independiente todavía no está verificada. |
| E2E-TKT-02 / TC-BIZ-008 (consulta por código) | USER_A | GET `/api/tickets/{{ticketCode}}` | 200 | **200 OK** | JSON válido: `id=1`, `ticketCode=TKT-d2570dd4-b5c9-418d-8e20-9eea85cbd38b`, `type=GENERAL`, `price=100000.00`, `status=PAID`, `userEmail=e2e_user_a_01@example.test`, `eventCode=E2E-EVENT-20261018-01`, `eventName=PulsePass E2E e2e-20261017-01`. | **PASS** | Consultamos el ticket por código con USER_A y confirmamos que coincide con la compra de TC-E2E-20 y no expone credenciales. |
| E2E-TKT-03 | USER_A | GET `/api/tickets/by-user?email={{userAEmail}}` | 200 | **200 OK** | Lista JSON con 1 ticket: `id=1`, `ticketCode=TKT-d2570dd4-b5c9-418d-8e20-9eea85cbd38b`, `type=GENERAL`, `price=100000.00`, `status=PAID`, `userEmail=e2e_user_a_01@example.test`, `eventCode=E2E-EVENT-20261018-01`, `eventName=PulsePass E2E e2e-20261017-01`. | **PASS** | Consultamos los tickets de USER_A y confirmamos que aparece el ticket comprado. Las denegaciones de acceso de USER_B al ticket y al historial de USER_A se documentan en TC-SEC-009 y TC-SEC-010. |
| E2E-TKT-04 / TC-BIZ-009 (variante ADMIN; STAFF verificado en carpeta 09) | ADMIN | GET `/api/events/{{newEventCode}}/tickets/paid` | 200 | **200 OK** | Lista JSON con 1 ticket pagado: `id=1`, `ticketCode=TKT-d2570dd4-b5c9-418d-8e20-9eea85cbd38b`, `type=GENERAL`, `price=100000.00`, `status=PAID`, `userEmail=e2e_user_a_01@example.test`, `eventCode=E2E-EVENT-20261018-01`, `eventName=PulsePass E2E e2e-20261017-01`. | **PASS** | Consultamos los tickets pagados con ADMIN y confirmamos que aparece el ticket pagado. STAFF obtuvo 200 en TC-BIZ-009; USER_A recibió 403 en TC-AUTH-014 al intentar consultar esta ruta privilegiada. |
| E2E-TKT-05 / TC-BIZ-010 (GET posterior comprobado durante seguridad) | USER_A | PATCH `/api/tickets/{{ticketCode}}/cancel` | 200 | **200 OK** | JSON válido: `id=1`, `ticketCode=TKT-d2570dd4-b5c9-418d-8e20-9eea85cbd38b`, `type=GENERAL`, `price=100000.00`, `status=CANCELLED`, `userEmail=e2e_user_a_01@example.test`, `eventCode=E2E-EVENT-20261018-01`, `eventName=PulsePass E2E e2e-20261017-01`. | **PASS** | Cancelamos el ticket de USER_A y confirmamos la transición `PAID` → `CANCELLED`. Confirmamos posteriormente su persistencia al consultar el ticket con USER_A: `200 OK`, estado `CANCELLED`. |
| E2E-TKT-06 (variante ADMIN; variante STAFF completada en TC-SEC-014) | ADMIN | PATCH `/api/tickets/{{ticketCodeForUse}}/use` | 200 | **200 OK** | JSON válido: `id=2`, `ticketCode=TKT-3cc31ed9-2749-441f-af2f-3bb275ebe0fb`, `type=GENERAL`, `price=100000.00`, `status=USED`, `userEmail=e2e_user_a_01@example.test`, `eventCode=E2E-EVENT-20261018-01`, `eventName=PulsePass E2E e2e-20261017-01`. | **PASS** | Marcamos como usado con ADMIN un ticket distinto del cancelado (`id=2`) y confirmamos su estado `USED` con un GET posterior. La autorización de STAFF se verificó además con el ticket `id=6` en TC-SEC-014; el ticket `id=2` se conserva como comprobación de la ruta base. |
| E2E-TKT-06 (comprobación posterior; no es ruta nueva) | USER_A | GET `/api/tickets/{{ticketCodeForUse}}` | 200 | **200 OK** | JSON válido: `id=2`, `ticketCode=TKT-3cc31ed9-2749-441f-af2f-3bb275ebe0fb`, `type=GENERAL`, `price=100000.00`, `status=USED`, `userEmail=e2e_user_a_01@example.test`, `eventCode=E2E-EVENT-20261018-01`, `eventName=PulsePass E2E e2e-20261017-01`. | **PASS** | Consultamos nuevamente el ticket `id=2` con USER_A y confirmamos que sigue en `USED` después de TC-E2E-25. No contabilizamos esta comprobación como ruta nueva. |

## 07 Seguridad / escenarios negativos

Aquí documentamos las pruebas negativas y las comprobaciones de roles/propiedad adicionales a las rutas base. La repetición válida de TC-SEC-011 se registra por separado del primer intento, cuya precondición no se cumplía. TC-SEC-017 también se registra con el resultado HTTP real y su observación técnica.

| Caso | Actor | Método y ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Verificación realizada; ¿hubo mutación? |
| --- | --- | --- | --- | --- | --- | --- | --- |
| TC-AUTH-007 | ANONYMOUS | GET `/api/auth/me` | 401 | **401 Unauthorized** | ErrorResponse JSON con `status=401`, `error=Unauthorized`, mensaje genérico de autenticación. | **PASS** | Enviamos la petición sin encabezado Authorization y comprobamos que el acceso fue rechazado con 401. |
| TC-AUTH-008 | ANONYMOUS (token inválido) | GET `/api/auth/me` | 401 | **401 Unauthorized** | ErrorResponse JSON con `status=401`, `error=Unauthorized`, mensaje genérico de autenticación. | **PASS** | Enviamos un Bearer inventado o malformado y comprobamos que no pudimos acceder al recurso protegido. |
| TC-AUTH-009 | ANONYMOUS (JWT alterado) | GET `/api/auth/me` | 401 | **401 Unauthorized** | ErrorResponse JSON con `status=401`, `error=Unauthorized`, mensaje genérico de autenticación. | **PASS** | Enviamos un JWT alterado y obtuvimos 401. El cuerpo de la respuesta no especifica la causa técnica del rechazo. |
| TC-AUTH-010 | ANONYMOUS (JWT expirado) | GET `/api/auth/me` | 401 | **401 Unauthorized** | ErrorResponse JSON con `status=401`, `error=Unauthorized`, `message=Credenciales inválidas o autenticación requerida`, `details={}`. | **PASS** | Utilizamos `{{expiredToken}}`, obtenido previamente de un login válido. Esperamos más de 15 minutos sin renovarlo, enviamos la petición y confirmamos el rechazo con 401 sin datos de identidad. |
| TC-AUTH-011 | ANONYMOUS (JWT con firma externa) | GET `/api/auth/me` | 401 | **401 Unauthorized** | ErrorResponse JSON: `status=401`, `error=Unauthorized`, `message=Credenciales inválidas o autenticación requerida`, `details={}`. | **PASS** | Enviamos Bearer `{{foreignToken}}`, un JWT HS256 de prueba no expirado firmado con una clave diferente de la del servidor. Confirmamos el rechazo con 401 y una respuesta JSON de error; no registramos el JWT. |
| TC-AUTH-012 | ANONYMOUS (token solo en query) | GET `/api/auth/me?access_token=...` | 401 | **401 Unauthorized** | ErrorResponse JSON: `status=401`, `error=Unauthorized`, `message=Credenciales inválidas o autenticación requerida`, `details={}`. | **PASS** | Enviamos el JWT de USER_A como parámetro `access_token` de la URL, con la opción `No Auth` en Postman. Comprobamos que la API rechazó el acceso con 401 y no devolvió información de identidad. No registramos el token. |
| Preparación de TC-AUTH-013 | ANONYMOUS | POST `/api/users` | 201 | **201 Created** | JSON con `id=5`, `username=e2e_inactive_01`, `email=e2e_inactive_01@example.test`, `active=true`, `firstName=Usuario`, `lastName=Inactivo`, `city=Santa Marta`, `birthDate=1995-05-10`; sin contraseña en respuesta. | **PASS (preparación)** | Registramos una cuenta exclusiva para la prueba y verificamos que se creó activa. Esta preparación se completó antes del login y la desactivación. |
| Preparación de TC-AUTH-013 | ANONYMOUS | POST `/api/auth/login` | 200 | **200 OK** | JSON con `accessToken` emitido, `tokenType=Bearer`, `expiresIn=900`; sin registrar el JWT. | **PASS (preparación)** | Iniciamos sesión con `e2e_inactive_01` mientras la cuenta estaba activa y comprobamos que la API emitió un JWT válido por 900 segundos. Conservamos el token para comprobar `/api/auth/me` antes y después de desactivar la cuenta. |
| Preparación de TC-AUTH-013 | INACTIVE_USER | GET `/api/auth/me` | 200 | **200 OK** | JSON con `id=5`, `username=e2e_inactive_01`, `roles=[ROLE_USER]`; sin datos sensibles. | **PASS (preparación)** | Enviamos Bearer `{{inactiveUserToken}}` mientras la cuenta seguía activa y comprobamos que el servidor reconoció correctamente la identidad y el rol del usuario. Conservamos el JWT para la prueba posterior a la desactivación. |
| Preparación de TC-AUTH-013 | Operación local en base E2E | Verificación de `users.active` en DBeaver | `active=false` | **`active=false` visualizado** | En la consulta de `users` observamos `id=5`, `username=e2e_inactive_01` y la casilla `active` desmarcada. | **PASS (preparación)** | Desactivamos la cuenta de prueba y verificamos `active=false`; al consultar la API con el JWT anterior obtuvimos 401. |
| TC-AUTH-013 | INACTIVE_USER | GET `/api/auth/me` | 401 o denegación configurada | **401 Unauthorized** | ErrorResponse JSON: `status=401`, `error=Unauthorized`, `message=Credenciales inválidas o autenticación requerida`, `details={}`. | **PASS** | Ejecutamos la consulta con el mismo Bearer `{{inactiveUserToken}}` que previamente devolvió 200 mientras la cuenta estaba activa. Tras desactivar `id=5`, comprobamos que el servidor rechazó el acceso y no expuso información de identidad. |
| TC-AUTH-014 | USER_A | GET `/api/events/{{newEventCode}}/tickets/paid` | 403 | **403 Forbidden** | ErrorResponse JSON: `status=403`, `error=Forbidden`, `message=No tiene permisos para realizar esta operación`, `details={}`. | **PASS** | Consultamos con Bearer `{{userAToken}}` la ruta de tickets pagados del evento, reservada para STAFF/ADMIN. Verificamos que la API denegó la operación por permisos insuficientes y no devolvió información de los tickets. Al ser una consulta GET, no solicitamos ninguna modificación de datos. |
| TC-AUTH-015 | ANONYMOUS (Bearer inválido presente) | GET `/api/venues/active` | Según política configurada: sin autenticar el JWT inválido | **401 Unauthorized** | ErrorResponse JSON: `status=401`, `error=Unauthorized`, `message=Credenciales inválidas o autenticación requerida`, `details={}`. | **PASS** | Ejecutamos la solicitud al endpoint público con la configuración prevista de Bearer `{{foreignToken}}` y comprobamos que la API rechazó la autenticación con HTTP 401 sin exponer información. Confirmamos que configuramos Authorization como Bearer Token con `{{foreignToken}}`; verificamos que el JWT inválido no fue autenticado. |
| TC-SEC-001 (variante ANONYMOUS) | ANONYMOUS | POST `/api/events` | 401 | **401 Unauthorized** | ErrorResponse JSON: `status=401`, `error=Unauthorized`, `message=Credenciales inválidas o autenticación requerida`, `details={}`. | **PASS (variante ANONYMOUS)** | Enviamos la solicitud sin autenticación y comprobamos que PulsePass denegó la creación con 401. No produjo cambios observables: la API rechazó la creación antes de entregar un recurso nuevo. Utilizamos el código ya existente `E2E-EVENT-20261018-01`; no verificamos directamente la ausencia de inserciones en la base de datos. |
| TC-SEC-001 (variante USER_A) | USER_A | POST `/api/events` | 403 | **403 Forbidden** | ErrorResponse JSON: `status=403`, `error=Forbidden`, `message=No tiene permisos para realizar esta operación`, `details={}`. | **PASS (denegación)** | Enviamos la solicitud con Bearer `{{userAToken}}` y comprobamos que PulsePass denegó la creación del evento por permisos insuficientes. No produjo cambios observables en la respuesta ni se entregó un evento nuevo. El Body utilizó el código ya existente `E2E-EVENT-20261018-01`; no realizamos una comprobación posterior de integridad en la base de datos. |
| TC-SEC-002 (variante USER_A) | USER_A | PATCH `/api/events/{{newEventCode}}/publish` | 403 | **403 Forbidden** | ErrorResponse JSON: `status=403`, `error=Forbidden`, `message=No tiene permisos para realizar esta operación`, `details={}`. | **PASS** | Intentamos publicar el evento con USER_A y comprobamos la denegación por permisos insuficientes. Posteriormente consultamos el evento con ADMIN y verificamos que conservaba `status=PUBLISHED` y la asociación con Solar Beat (`id=1`). |
| TC-SEC-002 (variante STAFF) | STAFF | PATCH `/api/events/{{newEventCode}}/publish` | 403 | **403 Forbidden** | ErrorResponse JSON: `status=403`, `error=Forbidden`, `message=No tiene permisos para realizar esta operación`, `details={}`. | **PASS** | Intentamos publicar el evento con STAFF y comprobamos la denegación por permisos insuficientes. Después consultamos el evento con ADMIN y confirmamos que conservaba `status=PUBLISHED` y la asociación con Solar Beat (`id=1`). |
| TC-SEC-002 (comprobación posterior) | ADMIN | GET `/api/events/{{newEventCode}}` | 200 | **200 OK** | `id=2`, `eventCode=E2E-EVENT-20261018-01`, `status=PUBLISHED`, `artists=[Solar Beat (id=1)]`. | **PASS (integridad observable)** | Volvimos a consultar el evento con ADMIN tras las dos denegaciones y comprobamos que permanecía publicado y conservaba el artista asociado. El evento ya estaba publicado antes de los intentos, por lo que esta lectura confirma la conservación del estado visible, no una comparación de cambios internos. |
| TC-SEC-003 | USER_A | POST `/api/events/{{newEventCode}}/artists/{{artistId}}` | 403 | **403 Forbidden** | ErrorResponse JSON: `status=403`, `error=Forbidden`, `message=No tiene permisos para realizar esta operación`, `details={}`. | **PASS** | Intentamos asociar un artista al evento con USER_A y comprobamos que la API denegó la operación por permisos insuficientes. Repetimos la consulta del evento con ADMIN y confirmamos que su lista de artistas permaneció igual: Solar Beat (`id=1`), sin nuevas asociaciones observables. |
| TC-SEC-003 (comprobación posterior) | ADMIN | GET `/api/events/{{newEventCode}}` | 200 | **200 OK** (confirmado durante nuestra ejecución) | La lista de artistas permaneció igual: Solar Beat (`id=1`), sin nuevas asociaciones visibles. | **PASS (integridad observable)** | Consultamos nuevamente el evento con un actor autorizado y comprobamos que la lista de artistas no cambió después del intento rechazado de USER_A. |
| Preparación de TC-SEC-004 | ADMIN | POST `/api/events` | 201 | **201 Created** | Evento `id=3`, código `E2E-SEC-DRAFT-20261018-01`, estado `DRAFT`, venue `E2E-LOCAL-001`, `artists=[]`. | **PASS (preparación)** | El evento quedó creado en estado DRAFT y no se publicó. La preparación está lista para ejecutar la consulta anónima. |
| TC-SEC-004 | ANONYMOUS | GET `/api/events/{{draftEventCode}}` | `403` o `404`, sin exponer datos del DRAFT | **404 Not Found** | ErrorResponse JSON: `status=404`, `error=Not Found`, `message=Event not found`, `details={}`. | **PASS** | Enviamos la consulta sin autorización y el servicio respondió `404 Not Found`; no devolvió el objeto ni los datos del evento DRAFT. El resultado es compatible con la política de ocultar borradores. |
| Preparación de TC-SEC-005 | ADMIN | POST `/api/events/{{draftEventCode}}/artists/{{artistId}}` | 200 | **200 OK** | Evento `id=3`, código `E2E-SEC-DRAFT-20261018-01`, estado `DRAFT`, artistas `[Solar Beat (id=1)]`. | **PASS (preparación)** | Asociamos Solar Beat (`artistId=1`) al evento y la respuesta confirmó que el evento permaneció en `DRAFT` con el artista asociado. Configuramos `artistId=1` y `artistStageName=Solar Beat` en el entorno para la prueba siguiente. |
| TC-SEC-005 | ANONYMOUS | GET `/api/events/by-artist?stageName={{artistStageName}}` | 200 | **200 OK** | Respuesta JSON válida; no aparece el evento `E2E-SEC-DRAFT-20261018-01`, que permanece en `DRAFT`. | **PASS** | Consultamos los eventos asociados a Solar Beat sin enviar token. Confirmamos que la respuesta pública no incluye el evento en borrador. |
| TC-SEC-006 | USER_B | GET `/api/users/by-email?email={{userAEmail}}` | `403` o `404`, sin exponer datos personales | **403 Forbidden** | ErrorResponse JSON: `status=403`, `error=Forbidden`, `message=No tiene permisos para realizar esta operación`, `details={}`. | **PASS** | Repetimos la petición con Authorization Bearer `{{userBToken}}` y el servidor respondió 403 por permisos insuficientes. La respuesta no incluyó información personal de USER_A. El resultado cumple la política de denegación prevista para esta prueba. |
| TC-SEC-007 | USER_B | GET `/api/users/by-username?username={{userAUsername}}` | `403` o `404`, sin exponer datos personales | **403 Forbidden** | ErrorResponse JSON: `status=403`, `error=Forbidden`, `message=No tiene permisos para realizar esta operación`, `details={}`. | **PASS** | Enviamos la petición con Bearer `{{userBToken}}` para consultar a USER_A por username. El servidor denegó el acceso con 403 y no devolvió el perfil ni datos personales de USER_A. |
| TC-SEC-008 | USER_A | POST `/api/tickets` | `403` o `400`, según el contrato de propiedad | **403 Forbidden** | ErrorResponse JSON: `status=403`, `error=Forbidden`, `message=No tiene permisos para realizar esta operación`, `details={}`. | **PASS** | Enviamos la compra con el token de USER_A indicando el correo de USER_B y obtuvimos 403. La API denegó la operación y no devolvió un ticket. La consulta posterior del historial propio de USER_B respondió `200 OK` con lista vacía `[]`; no observamos tickets a nombre de USER_B tras el intento rechazado. |
| Verificación de integridad de TC-SEC-008 | USER_B | GET `/api/tickets/by-user?email={{userBEmail}}` | `200 OK` | **200 OK** | Lista vacía `[]`. | **PASS (integridad)** | Consultamos el historial propio de USER_B después del intento de compra rechazado. La respuesta fue una lista vacía; no observamos tickets a nombre de USER_B como consecuencia del intento de TC-SEC-008. |
| TC-SEC-009 | USER_B | GET `/api/tickets/{{ticketCancelableCode}}` | `403` o `404`, sin exponer datos del ticket | **403 Forbidden** | Ticket de USER_A; en la repetición más reciente, `ticketCancelableCode` corresponde al ticket `id=7`. | **PASS** | USER_B intentó consultar el ticket de USER_A y la API denegó el acceso con 403 sin devolver sus datos. La consulta corresponde a una cuenta distinta de la propietaria. |
| TC-SEC-010 | USER_B | GET `/api/tickets/by-user?email={{userAEmail}}` | `403` o `404`; sin datos de USER_A | **403 Forbidden** | ErrorResponse JSON: `status=403`, `error=Forbidden`, `message=No tiene permisos para realizar esta operación`, `details={}`. | **PASS** | Consultamos el historial de USER_A con el JWT de USER_B; comprobamos el rechazo 403 y la ausencia de información sobre sus tickets. |
| TC-SEC-011 (primer intento, no concluyente) | USER_B | PATCH `/api/tickets/{{ticketCancelableCode}}/cancel` | `403` o `404`; ticket inicialmente `PAID` | **403 Forbidden** | En aquel intento se usó el ticket `id=1`, que ya estaba `CANCELLED` antes de la petición. | **No concluyente (precondición incorrecta)** | La denegación fue correcta, pero ese envío no comprobó por sí solo que un ticket `PAID` conservara su estado. La repetición válida se registra en la fila siguiente. |
| TC-SEC-011 (repetición válida) | USER_B | PATCH `/api/tickets/{{ticketCancelableCode}}/cancel` | `403` o `404`; sin mutación del ticket ajeno | **403 Forbidden** | Ticket `id=7`, código `TKT-c3a1e15d-fa26-4610-ae05-f03ea463ce51`, propietario USER_A, comprado en `PAID`. | **PASS** | Con el ticket recién comprado en estado `PAID`, USER_B intentó cancelarlo y recibió 403. Después, USER_A pudo cancelar ese mismo ticket con `200 OK` y quedó `CANCELLED`; esto confirma que el intento no autorizado no consumió la transición de cancelación. |
| TC-SEC-012 | USER_B | PATCH `/api/tickets/{{ticketUsableCode}}/use` | `403`; sin transición a USED | **403 Forbidden** | ErrorResponse JSON: `status=403`, `error=Forbidden`, `message=No tiene permisos para realizar esta operación`, `details={}`. | **PASS** | Enviamos el intento de uso con el JWT de USER_B sobre el ticket `id=3` perteneciente a USER_A. La API denegó la operación. Verificamos después que el estado seguía PAID. |
| TC-SEC-012 (integridad) | USER_A | GET `/api/tickets/{{ticketUsableCode}}` | `200 OK`; `status=PAID` | **200 OK** | Ticket `id=3`, código `TKT-453f8390-a5e5-42d7-97a6-d3ebfd75d10e`, `type=GENERAL`, `status=PAID`, propietario USER_A, evento `E2E-EVENT-20261018-01`. | **PASS (integridad)** | Consultamos el ticket con USER_A después de la denegación de USER_B y comprobamos que permanecía PAID, sin uso indebido. |
| TC-SEC-013 | USER_A | GET `/api/events/{{publishedEventCode}}/tickets/paid` | `403`; sin lista de compradores | **403 Forbidden** | ErrorResponse JSON: `status=403`, `error=Forbidden`, `message=No tiene permisos para realizar esta operación`, `details={}`. | **PASS** | Consultamos la ruta privilegiada con USER_A y comprobamos que no se reveló ninguna lista de tickets ni datos de compradores. |
| TC-SEC-014 | STAFF | PATCH `/api/tickets/{{ticketUsableCode}}/use` | `200 OK`; `status=USED` | **200 OK** | Ticket `id=6`, código `TKT-00c4e9b6-629c-454d-a2da-a372f287d98c`, estado `USED`, propietario USER_A, evento `E2E-BIZ-20261018-01`. | **PASS** | STAFF marcó como usado un ticket que estaba `PAID`. La respuesta confirmó `USED`; posteriormente, el segundo intento sobre el mismo ticket devolvió `409` y USER_A pudo consultarlo con `200 OK`, manteniendo `USED`. |
| TC-SEC-014 (segundo intento e integridad) | STAFF y luego USER_A | PATCH `/api/tickets/{{ticketUsableCode}}/use`; GET `/api/tickets/{{ticketUsableCode}}` | Segundo uso `409`; consulta posterior `200`, estado `USED` | **409 Conflict; después 200 OK** | Ticket `id=6`, código `TKT-00c4e9b6-629c-454d-a2da-a372f287d98c`, `status=USED`. | **PASS** | El segundo intento fue rechazado con `Only PAID tickets can be marked as used.`. La consulta posterior como USER_A confirmó que el estado siguió siendo `USED`. |
| TC-SEC-015 | ADMIN | GET `/api/events/{{draftEventCode}}` | `200 OK`; `status=DRAFT` | **200 OK** | Evento `id=3`, código `E2E-SEC-DRAFT-20261018-01`, `status=DRAFT`, artista Solar Beat (`id=1`). | **PASS** | Consultamos como ADMIN el evento no publicado y comprobamos que se devolvió el borrador con su artista, sin modificar su estado. |
| TC-SEC-016 | ANONYMOUS | POST `/api/users` | `201` o `400`; nunca conceder rol ADMIN | **201 Created** | Usuario `id=6`, username `e2e_role_injection_01`, `active=true`; la respuesta de registro no incluía roles. | **PASS (verificación posterior)** | Registramos un usuario incluyendo `role=ADMIN`. El servidor lo creó, pero la respuesta no mostraba roles; comprobamos posteriormente mediante `/api/auth/me` que recibió únicamente `ROLE_USER`. |
| TC-SEC-016 (verificación de privilegios) | Usuario de prueba (e2e_role_injection_01) | GET `/api/auth/me` | `200 OK`; `roles=[ROLE_USER]` | **200 OK** | `id=6`, `username=e2e_role_injection_01`, `roles=[ROLE_USER]` (sin `ROLE_ADMIN`). | **PASS (integridad de roles)** | Iniciamos sesión con la cuenta recién creada y consultamos `/me` con su propio JWT. Comprobamos que el atributo inyectado no concedió privilegios administrativos. |
| TC-SEC-017 | STAFF / usuario `id=4` | Revocación controlada de `STAFF` y repetición de GET `/api/events/{{eventToPublishCode}}/tickets/paid` con el mismo `{{staffToken}}` | Acceso denegado después de retirar el rol; confirmar código conforme a política de seguridad | **200 OK → 401 Unauthorized → 200 OK tras restaurar STAFF** | Antes de la revocación la respuesta fue `200 OK` (`[]`). Tras eliminar `user_roles(user_id=4, role=STAFF)` en DBeaver con Auto-commit, la misma petición devolvió `401`. Al restaurar la asignación STAFF, el endpoint volvió a responder `200 OK`. | **REVISAR (no PASS definitivo)** | La revocación impidió el acceso con el token anterior, pero se observó `401` y no `403`. Se registra el comportamiento real sin repetir la prueba; revisar si la carga del usuario sin roles debe producir `401` o si el contrato requiere autenticar y responder `403`. El rol `STAFF` quedó restaurado al terminar. |

## 08 Validaciones y errores HTTP

Resultados registrados a partir de las respuestas observadas en Postman el 2026-10-18. El estado de cada TC principal se determina contra el HTTP esperado definido en la colección. Las peticiones complementarias se registran por separado para conservar trazabilidad.

| Caso | Actor | Método y ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación del equipo |
| --- | --- | --- | ---: | ---: | --- | --- | --- |
| TC-HTTP-001 | ADMIN | POST `/api/events` | 400 | 400 | `Validation failed`; category, eventCode, eventDate, minimumAge y venueCode son obligatorios. | PASS | Comprobamos que se rechazó el body incompleto con ErrorResponse; no se informó creación de evento. |
| TC-HTTP-002 | ADMIN | POST `/api/events` | 400 | 400 | `Malformed JSON or invalid request body`. | PASS | JSON malformado rechazado; no se obtuvo HTTP 500. |
| TC-HTTP-003 | ADMIN | POST `/api/events` | 400 | 400 | `Malformed JSON or invalid request body`; categoría enviada: `NOT_A_CATEGORY`. | PASS | El valor de categoría inválido fue rechazado con 400. Verificamos después que el evento no se creó. |
| TC-HTTP-003 (verificación) | ADMIN | GET `/api/events/E2E-HTTP-ENUM` | 404 | 404 | `Event not found with code: E2E-HTTP-ENUM`. | PASS | Comprobación complementaria: confirma que el POST inválido no creó el evento. No es un TC principal adicional. |
| TC-HTTP-004 | ANONYMOUS | GET `/api/events/E2E-HTTP-NOT-FOUND` | 404 | 404 | `Event not found with code: E2E-HTTP-NOT-FOUND`. | PASS | La consulta devolvió el ErrorResponse esperado sin exponer información interna. |
| TC-HTTP-005 | ANONYMOUS | POST `/api/users` | 400 | 400 | `Validation failed`; `email: Email must be valid`. | PASS | El email inválido fue rechazado por validación. |
| TC-HTTP-006 | ANONYMOUS | POST `/api/users` | 409 | 409 | `Username already exists: e2e_user_a_01`. | PASS | El registro duplicado fue rechazado con conflicto. |
| TC-HTTP-007 | USER_A | GET `/api/tickets/TKT-E2E-NOT-FOUND` | 404 | 403 | `Forbidden`; `No tiene permisos para realizar esta operación`. | FAIL | No coincide con el HTTP esperado para esta prueba. Con USER_A el control de permisos devuelve 403 antes de obtener el 404 esperado. Revisar política de autorización/ocultamiento del recurso; no marcar PASS. |
| Diagnóstico TC-HTTP-007 | USER_A | GET `/api/tickets/{{ticketUsableCode}}` | 200 | 200 | Ticket `TKT-453f8390-a5e5-42d7-97a6-d3ebfd75d10e`; estado `USED`; propietario `e2e_user_a_01@example.test`. | PASS (diagnóstico) | Confirma que el token de USER_A funciona y que puede consultar su ticket propio. No sustituye TC-HTTP-007. |
| Diagnóstico TC-HTTP-007 | ADMIN | GET `/api/tickets/TKT-E2E-NOT-FOUND` | 404 | 404 | `Ticket not found: TKT-E2E-NOT-FOUND`. | PASS (diagnóstico) | ADMIN obtiene 404 con el mismo código inexistente; el resultado principal de USER_A permanece FAIL por diferencia de autorización. |
| TC-HTTP-008 | ANONYMOUS | GET `/api/auth/me` | 401 | 401 | `Unauthorized`; `Credenciales inválidas o autenticación requerida`. | PASS | Ruta protegida denegada sin token; no se devolvieron datos de identidad. |

### Resumen de la carpeta 08

- **Casos principales ejecutados:** 8 de 8.

- **PASS:** 7 (`TC-HTTP-001` a `TC-HTTP-006`, y `TC-HTTP-008`).

- **FAIL:** 1 (`TC-HTTP-007` con USER_A: esperado 404, observado 403).

- **Comprobaciones complementarias:** 3 envíos registrados (verificación de no creación en TC-HTTP-003; consulta de ticket propio con USER_A; consulta del ticket inexistente con ADMIN). Estos diagnósticos no reemplazan ni alteran el resultado del caso principal.

- **Pendiente:** revisar por qué `GET /api/tickets/{ticketCode}` con USER_A devuelve 403 para un código inexistente, mientras ADMIN recibe 404. No cambiar el resultado de TC-HTTP-007 a PASS hasta resolver o justificar la política de forma consistente.

## 09 Flujos de negocio / secuencia recomendada

Resultados de los casos `TC-BIZ-001` a `TC-BIZ-015` ejecutados manualmente en Postman. Cuando confirmamos PASS sin compartir el detalle del HTTP exacto, se indica en observaciones; no se registran JWT ni contraseñas. Los casos con varias peticiones se desglosan por envío cuando el resultado se conoce.

| Caso | Actor | Método y ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación del equipo |
| --- | --- | --- | ---: | --- | --- | --- | --- |
| TC-BIZ-001 | ANONYMOUS | GET `/api/venues/active` | 200 | 200 OK | Venue `id=1`, código `E2E-LOCAL-001`, nombre `Venue de prueba E2E`, ciudad Santa Marta, `active=true`. | PASS | Comprobamos que el venue activo aparece en el listado público. |
| TC-BIZ-001 | ANONYMOUS | GET `/api/venues/{{venueActiveCode}}` | 200 | 200 OK | Venue `id=1`, código `E2E-LOCAL-001`, nombre `Venue de prueba E2E`, `active=true`. | PASS | Confirmamos que la segunda consulta también funcionó y los datos corresponden al fixture. |
| TC-BIZ-002 | ANONYMOUS | GET `/api/artists/active` | 200 | 200 OK | Artista activo identificado: `id=1`, `stageName=Solar Beat`. | PASS | Confirmamos que las consultas del caso fueron correctas. |
| TC-BIZ-002 | ANONYMOUS | GET `/api/artists/{{artistId}}` | 200 | 200 OK | `artistId=1`, `stageName=Solar Beat`, activo. | PASS | El ID corresponde al artista identificado en el listado. |
| TC-BIZ-002 | ANONYMOUS | GET `/api/artists/by-stage-name?stageName={{artistStageName}}` | 200 | 200 OK | Coincide con `Solar Beat`, `id=1`. | PASS | La consulta por nombre artístico devuelve el mismo artista. |
| TC-BIZ-003 | ADMIN | POST `/api/events` | 201 | 201 Created (confirmado durante nuestra ejecución) | Código `E2E-BIZ-20261018-01`; nombre `PulsePass BIZ e2e-20261017-01`; categoría `MUSIC`; estado inicial `DRAFT`; venue `E2E-LOCAL-001`. | PASS | Confirmamos PASS y el ticket comprado posteriormente referencia este evento. |
| TC-BIZ-004 | ADMIN | PATCH `/api/events/{{eventToPublishCode}}/publish` | 200 | PASS confirmado; HTTP exacto no se volvió a compartir en esta conversación | Evento `E2E-BIZ-20261018-01` publicado; estado esperado `PUBLISHED`. | PASS | Confirmamos PASS. La consulta pública del evento por código quedó comprobada posteriormente en TC-BIZ-016; su inclusión en `/api/events/published` también se verificó en las pruebas de visibilidad pública. |
| TC-BIZ-005 | ADMIN | POST `/api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 200 | 200 OK (confirmado durante nuestra ejecución) | Artista `Solar Beat` (`id=1`) asociado al evento `E2E-BIZ-20261018-01`. | PASS | Confirmamos que la asociación devolvió 200 OK antes de publicar el evento. |
| TC-BIZ-006 | ANONYMOUS / actores de prueba | POST `/api/users` (no repetido: se reutilizaron resultados previos) | 201 para registros nuevos; no duplicar existentes | Registros previos existentes | USER_A y USER_B ya estaban registrados y sus identidades/roles se habían comprobado en la sección 03. | PASS (resultado previo reutilizado) | Se siguió la instrucción de la colección de no crear duplicados. No se enviaron POST nuevos para este caso. |
| TC-BIZ-007 | USER_A | POST `/api/tickets` | 201 | 201 Created | Ticket `id=4`, código `TKT-61471d9f-8cef-46a2-97b7-8b66c3112935`, `type=GENERAL`, precio `100000.00`, estado `PAID`, propietario `e2e_user_a_01@example.test`, evento `E2E-BIZ-20261018-01`. | PASS | Comprobamos la compra y la relación entre ticket, propietario y evento. Se guardó el código en `ticketCancelableCode`. |
| TC-BIZ-007 (segunda compra) | USER_A | POST `/api/tickets` | 201 | **201 Created** | Ticket `id=7`, código `TKT-c3a1e15d-fa26-4610-ae05-f03ea463ce51`, `type=GENERAL`, precio `100000.00`, estado `PAID`, propietario USER_A, evento `E2E-BIZ-20261018-01`. | **PASS** | Creamos un ticket adicional para disponer de un ticket `PAID` en la prueba de propiedad y cancelación. No se repitió la prueba de compra DRAFT. |
| TC-BIZ-008 | USER_A | GET `/api/tickets/{{ticketCancelableCode}}` | 200 | 200 OK | Ticket `id=4`, código `TKT-61471d9f-8cef-46a2-97b7-8b66c3112935`, estado `PAID`, propietario USER_A, evento `E2E-BIZ-20261018-01`. | PASS | La consulta coincidió con la compra de TC-BIZ-007. |
| TC-BIZ-008 (integridad tras segunda cancelación) | USER_A | GET `/api/tickets/{{ticketCancelableCode}}` | 200 | **200 OK** | Ticket `id=7`, código `TKT-c3a1e15d-fa26-4610-ae05-f03ea463ce51`, estado `CANCELLED`. | **PASS** | La consulta posterior confirmó que el segundo intento no alteró el estado final. |
| TC-BIZ-009 | STAFF | GET `/api/events/{{eventToPublishCode}}/tickets/paid` | 200 | 200 OK | La lista contiene el ticket `id=4`, código `TKT-61471d9f-8cef-46a2-97b7-8b66c3112935`, propietario USER_A, estado `PAID`. | PASS | Comprobamos que STAFF puede listar el ticket pagado del evento. |
| TC-BIZ-010 | USER_A | PATCH `/api/tickets/{{ticketCancelableCode}}/cancel` | 200 | 200 OK | Ticket `id=4`, estado `CANCELLED`. | PASS | Comprobamos la transición `PAID → CANCELLED`. |
| TC-BIZ-010 (segunda ejecución) | USER_A | PATCH `/api/tickets/{{ticketCancelableCode}}/cancel` | 200 | **200 OK** | Ticket `id=7`, código `TKT-c3a1e15d-fa26-4610-ae05-f03ea463ce51`, estado `CANCELLED`. | **PASS** | USER_A canceló el ticket que USER_B no había podido cancelar en TC-SEC-011. La transición fue `PAID → CANCELLED`. |
| TC-BIZ-011 | USER_A | PATCH `/api/tickets/{{ticketCancelableCode}}/cancel` | 409 | 409 Conflict | Mensaje: `Only PAID tickets can be cancelled.` | PASS | El segundo intento fue rechazado porque el ticket ya estaba cancelado. |
| TC-BIZ-011 (segunda ejecución) | USER_A | PATCH `/api/tickets/{{ticketCancelableCode}}/cancel` | 409 | **409 Conflict** | Mensaje: `Only PAID tickets can be cancelled.`; ticket `id=7` permanece `CANCELLED`. | **PASS** | El segundo intento de cancelación fue rechazado. |
| TC-BIZ-011 (integridad) | USER_A | GET `/api/tickets/{{ticketCancelableCode}}` | 200 | 200 OK | Ticket `id=4`, estado `CANCELLED`. | PASS (integridad) | Confirmamos que el ticket continuó en `CANCELLED`; no se observó una segunda transición. |
| TC-BIZ-012 | STAFF | PATCH `/api/tickets/{{ticketUsableCode}}/use` | 200 | 200 OK | Ticket `id=5`, código `TKT-cbf90dd1-89cf-4961-9bbb-6890017301c3`, estado `USED`, propietario USER_A, evento `E2E-EVENT-20261018-01`. | PASS | Comprobamos que STAFF marcó como usado el ticket elegible. Se actualizó `ticketUsableCode` con este ticket independiente del cancelado. |
| TC-BIZ-013 | STAFF | PATCH `/api/tickets/{{ticketUsableCode}}/use` | 409 | 409 Conflict | Mensaje: `Only PAID tickets can be marked as used.` | PASS | La API rechazó el segundo intento con `409 Conflict`. La consulta posterior de USER_A quedó registrada como TC-BIZ-013 (integridad) y confirmó que el ticket seguía `USED`. |
| TC-BIZ-013 (integridad tras segundo uso) | USER_A | GET `/api/tickets/{{ticketUsableCode}}` | 200 | **200 OK** | Ticket `id=5`, código `TKT-cbf90dd1-89cf-4961-9bbb-6890017301c3`, estado `USED`. | **PASS** | Después de que el segundo intento de uso devolviera `409`, la consulta confirmó que el ticket continuaba en `USED`. |
| TC-BIZ-014 | USER_A | POST `/api/tickets` | Error de negocio; nunca 201 | 409 Conflict | Mensaje: `Tickets can only be purchased for PUBLISHED events.` | PASS | La compra fue rechazada correctamente con 409. La verificación de integridad se registra en la fila siguiente. Los subescenarios de aforo agotado y edad insuficiente no se ejecutaron por falta de fixtures dedicados. |
| TC-BIZ-014 (integridad) | USER_A | GET `/api/tickets/by-user?email={{userAEmail}}` | 200 | 200 OK | Lista con 5 tickets; los códigos de evento observados son `E2E-EVENT-20261018-01` y `E2E-BIZ-20261018-01`. No aparece `E2E-SEC-DRAFT-20261018-01`. | PASS (integridad) | La consulta posterior confirma que no hay ticket de USER_A para el evento DRAFT probado. Esto valida el subescenario de compra DRAFT; no valida aforo agotado ni edad insuficiente. |
| TC-BIZ-015 | ADMIN | POST `/api/events` | 400 | 400 Bad Request | `Validation failed`; `eventCode: Event code is required`; `minimumAge: Minimum age must be zero or greater`; `name: Name is required`. | PASS | El JSON válido fue rechazado por las reglas de validación. En este reintento se usó `category=MUSIC`; la validación específica de `INVALID_ENUM` no quedó comprobada en la respuesta final. |
| TC-BIZ-015 | ADMIN | GET `/api/events/E2E-BIZ-NOT-FOUND` | 404 | 404 Not Found (confirmado durante nuestra ejecución; respuesta JSON no compartida) | Evento inexistente; no se informó creación de recurso. | PASS | Confirmamos PASS después de configurar `{{adminToken}}`; se registra el 404 esperado. |
| TC-BIZ-015 (intento corregido) | ADMIN | POST `/api/events` | 400 | 400 Bad Request | `Malformed JSON or invalid request body`. | PASS (diagnóstico de formato) | El primer envío no permitió validar los campos; se corrigió el Body a `raw` + `JSON` y se volvió a enviar. |
| TC-BIZ-015 (intento de autorización) | ANONYMOUS | GET `/api/events/E2E-BIZ-NOT-FOUND` | No corresponde al actor ADMIN configurado | 401 Unauthorized | `Credenciales inválidas o autenticación requerida`. | PASS (diagnóstico) | El endpoint exigió autenticación en ese intento; se repitió la consulta con `{{adminToken}}` según la configuración del caso. |
| TC-BIZ-016 | ANONYMOUS | GET `/api/events/{{eventToPublishCode}}` | 200 | **200 OK** | Evento `id=4`, código `E2E-BIZ-20261018-01`, nombre `PulsePass BIZ e2e-20261017-01`, estado `PUBLISHED`, categoría `MUSIC`, venue `E2E-LOCAL-001`, artista Solar Beat (`id=1`). | **PASS** | Consultamos el evento publicado por código sin Bearer y recibimos el objeto completo con estado `PUBLISHED` y artista asociado. La prueba se agregó a la carpeta 09 para respetar el máximo de seis rutas de la carpeta base. |

### Resumen de la carpeta 09

- **Casos TC-BIZ principales:** 16 (`TC-BIZ-001` a `TC-BIZ-016`).

- **PASS:** 16 casos principales; `TC-BIZ-006` reutiliza las cuentas ya creadas, y `TC-BIZ-016` confirma el acceso público al evento BIZ publicado por código.

- **BLOCKED / FAIL:** 0 casos principales de la carpeta 09.

- **Integridad verificada:** el ticket `id=5` permanece `USED` después del segundo intento; el ticket `id=7` permanece `CANCELLED` después de la segunda cancelación.

- **Subescenarios pendientes:** aforo agotado y edad insuficiente dentro de TC-BIZ-014, porque faltan fixtures dedicados. Estos subescenarios no cambian el PASS del escenario DRAFT ejecutado.

### Estado de los flujos

| Flujo PRD | Secuencia | Estado | Observaciones del equipo |
|---|---|---|---|
| A — Descubrimiento público | Venues → Artists → eventos publicados y consulta por código | **PASS** | `TC-BIZ-001`, `TC-BIZ-002` y `TC-BIZ-016`; el evento publicado se consultó sin autenticación. |
| B — Registro y JWT | Registro USER_A/USER_B → login → `/api/auth/me` → escenarios de credenciales inválidas | **PASS** | Se verificaron identidad, roles y rechazos de tokens/credenciales inválidos; los tokens no se incluyen en el documento. |
| C — Gestión del catálogo | Crear DRAFT → consultar como ADMIN → asociar artista → publicar → validar visibilidad | **PASS** | `TC-BIZ-003`, `TC-BIZ-004`, `TC-BIZ-005` y `TC-BIZ-016`; el evento BIZ quedó publicado con Solar Beat asociado. |
| D — Compra, propiedad, cancelación y uso | Comprar → consultar → intentar acceso ajeno → cancelar → repetir cancelación → usar → repetir uso → consultar estados | **PASS** | `TC-BIZ-007`–`TC-BIZ-013` y `TC-SEC-009`–`TC-SEC-014`. USER_B recibió 403; USER_A pudo cancelar su ticket; las transiciones repetidas devolvieron 409; los estados finales `CANCELLED` y `USED` se confirmaron por GET. |
| E — Regresión de errores | Validaciones 400, recursos inexistentes 404, duplicados/transiciones 409, autenticación 401 y permisos 403 | **PARCIAL** | Siete casos HTTP PASS; `TC-HTTP-007` sigue FAIL (403 observado frente a 404 esperado). `TC-SEC-017` requiere revisión por el 401 observado tras revocar STAFF. |

## Defectos y puntos de revisión

| Caso | Estado | HTTP esperado / observado | Hallazgo | Acción recomendada |
|---|---|---|---|---|
| `TC-HTTP-007` — consultar ticket inexistente con USER_A | **FAIL** | `404` esperado / `403` observado con USER_A; ADMIN recibe `404` | La autorización devuelve un código distinto para el mismo código inexistente según el actor. | Revisar o justificar la política de autorización/ocultamiento de recursos. No marcar PASS hasta que el contrato quede definido y consistente. |
| `TC-SEC-017` — revocación del rol STAFF con JWT anterior | **REVISAR** | `200 OK` antes de revocar → `401 Unauthorized` después de retirar el rol → `200 OK` después de restaurarlo | La eliminación de `user_roles(user_id=4, role=STAFF)` impidió el acceso con el token anterior, pero el servidor respondió 401 en vez de un 403 de privilegios insuficientes. La cuenta `e2e_staff_01` siguió existiendo y el rol quedó restaurado. | Confirmar el comportamiento esperado con la implementación de seguridad; no repetir el procedimiento manual. |
| `TC-BIZ-014` — subescenarios de aforo y edad | **NOT RUN (subescenarios)** | Requieren rechazos de negocio específicos; no se ejecutaron | No se contaba con fixtures dedicados para aforo agotado ni edad insuficiente. El subescenario de compra para evento DRAFT sí fue PASS (`409`) y la consulta posterior confirmó que no se creó ticket para ese evento. | Preparar fixtures exclusivos si se exige cubrir esos subescenarios en una ejecución posterior. |
| `TC-SEC-011` — primer intento con ticket ya cancelado | **No concluyente; repetición PASS** | `403` observado en ambos intentos de USER_B | El primer intento usó un ticket que ya estaba `CANCELLED`; la repetición posterior utilizó el ticket `id=7` en `PAID`, y USER_A pudo cancelarlo después de la denegación. | La repetición válida se considera PASS; conservar la nota sobre el primer intento para explicar la trazabilidad. |

## Cierre de ejecución

### Resultado consolidado

- **Rutas base de las carpetas 01–06:** 22/22 PASS. Las pruebas adicionales reutilizan estas rutas y no aumentan el total de rutas base del PRD.

- **Seguridad / carpeta 07:** 25 escenarios con resultado PASS, incluida la repetición válida de `TC-SEC-011`. `TC-SEC-017` queda en **REVISAR**, con secuencia observada `200 → 401 → 200` tras retirar y restaurar el rol STAFF.

- **Validaciones HTTP / carpeta 08:** 7 PASS y 1 FAIL (`TC-HTTP-007`).

- **Flujos de negocio / carpeta 09:** 16/16 casos principales PASS (`TC-BIZ-001` a `TC-BIZ-016`).

- **Flujos PRD:** A, B, C y D PASS; E parcial por `TC-HTTP-007` y la revisión técnica de `TC-SEC-017`.

- **Subescenarios sin ejecutar:** aforo agotado y edad insuficiente en `TC-BIZ-014`, por falta de fixtures específicos.

### Estado final de datos de prueba relevantes

| Recurso | Código / referencia | Estado final confirmado |
|---|---|---|
| Venue de prueba | `E2E-LOCAL-001` (`id=1`) | Activo |
| Artista | Solar Beat (`id=1`) | Activo y asociado al evento publicado |
| Evento de flujo BIZ | `E2E-BIZ-20261018-01` (`id=4`) | `PUBLISHED` |
| Ticket cancelable | `TKT-c3a1e15d-fa26-4610-ae05-f03ea463ce51` (`id=7`) | `CANCELLED`; segundo intento de cancelación rechazado con 409 |
| Ticket usado de flujo E2E | `TKT-cbf90dd1-89cf-4961-9bbb-6890017301c3` (`id=5`) | `USED`; segundo intento de uso rechazado con 409 y estado verificado por GET |
| Ticket usado de flujo BIZ | `TKT-00c4e9b6-629c-454d-a2da-a372f287d98c` (`id=6`) | `USED`; segundo intento de uso rechazado con 409 y estado verificado por GET |
| Cuenta STAFF | `e2e_staff_01` (`user_id=4`) | Cuenta conservada y rol `STAFF` restaurado tras TC-SEC-017 |

### Observaciones de cierre

- Los resultados corresponden a las respuestas durante la ejecución manual. No se guardaron contraseñas ni JWT en esta bitácora.

- La carpeta 09 contiene `TC-BIZ-016` para la consulta pública por código del evento publicado; no se agregó una ruta base adicional a las 22 del PRD.

- El flujo D se considera PASS con las verificaciones ejecutadas: la denegación de acceso por otro usuario fue 403; la cancelación por el propietario fue 200; la segunda cancelación fue 409; el uso por STAFF fue 200; el segundo uso fue 409; las consultas posteriores confirmaron los estados finales.

- La revocación de STAFF no se marca como PASS definitivo. La petición dejó de estar autorizada después de retirar el rol, pero se observó 401; se requiere confirmar el código esperado según la política de seguridad. El rol se restauró al terminar.

- `TC-HTTP-007` permanece FAIL hasta revisar la diferencia entre el 403 de USER_A y el 404 de ADMIN para un mismo ticket inexistente.

> **Criterio de integridad del reporte:** se conserva el HTTP realmente observado. Un resultado no se cambia a PASS solo para igualarlo al esperado; las diferencias y las limitaciones de precondiciones se explican en observaciones. Las tablas no incluyen columnas de fecha/hora, request ni ID de colección. Mantener credenciales y JWT únicamente en variables locales de Postman.
