# Checklist — PulsePass E2E manual

La bitácora adjunta registra pruebas ya ejecutadas. Esta lista representa las solicitudes de la colección; una casilla sin marcar no invalida una ejecución documentada. Contrastar por ID y variante antes de marcar; no atribuir PASS a casos con resultado FAIL, REVISAR o NOT RUN.

**Balance documentado:** 22/22 rutas base PASS; seguridad 25 PASS y TC-SEC-017 REVISAR; HTTP 7 PASS y TC-HTTP-007 FAIL; negocio 16/16 principales PASS, con subescenarios de aforo y edad NOT RUN.

## 00 Preparación / conectividad

- [ ] TC-PREP-01 — Verificar conectividad con API (ANONYMOUS) — `GET /api/venues/active`

## 01 Venues

- [ ] TC-E2E-01 — Consultar venues activos (ANONYMOUS) — `GET /api/venues/active`
- [ ] TC-E2E-02 — Consultar venue por código (ANONYMOUS) — `GET /api/venues/{{venueActiveCode}}`

## 02 Artists

- [ ] TC-E2E-03 — Listar artistas activos (ANONYMOUS) — `GET /api/artists/active`
- [ ] TC-E2E-04 — Consultar artista por ID (ANONYMOUS) — `GET /api/artists/{{artistId}}`
- [ ] TC-E2E-05 — Consultar artista por nombre artístico (ANONYMOUS) — `GET /api/artists/by-stage-name?stageName={{artistStageName}}`

## 03 Auth / usuarios

- [ ] TC-AUTH-001 / TC-E2E-06 — Registrar USER_A válido (ANONYMOUS) — `POST /api/users`
- [ ] TC-AUTH-002 / TC-E2E-07 — Login de USER_A por username — `POST /api/auth/login`
- [ ] TC-AUTH-006 / TC-E2E-08 — Consultar /api/auth/me con USER_A — `GET /api/auth/me`
- [ ] TC-AUTH-004 — Login de USER_A con contraseña incorrecta — `POST /api/auth/login`
- [ ] TC-AUTH-005 — Login con identificador inexistente — `POST /api/auth/login`
- [ ] TC-E2E-09 — Registrar USER_B (ANONYMOUS) — `POST /api/users`
- [ ] TC-E2E-10 — Iniciar sesión USER_B (ANONYMOUS) — `POST /api/auth/login`
- [ ] TC-E2E-11 — Consultar usuario autenticado (USER_B) — `GET /api/auth/me`
- [ ] PREP — Iniciar sesión ADMIN — `POST /api/auth/login`
- [ ] PREP — Verificar identidad ADMIN — `GET /api/auth/me`
- [ ] PREP — Iniciar sesión STAFF — `POST /api/auth/login`
- [ ] PREP — Verificar identidad STAFF — `GET /api/auth/me`
- [ ] PREP — Registrar INACTIVEUSER — `POST /api/users`
- [ ] PREP — iniciar sesión INACTIVEUSER — `POST /api/auth/login`
- [ ] PREP — Verificar identidad INACTIVEUSER — `GET /api/auth/me`

## 04 Usuarios / consulta y propiedad (2 rutas)

- [ ] TC-E2E-12 — Consultar USER_A por email propio — `GET /api/users/by-email?email={{userAEmail}}`
- [ ] TC-E2E-13 — Consultar USER_A por username propio — `GET /api/users/by-username?username={{userAUsername}}`

## 05 Events / administración y visibilidad (6 rutas)

- [ ] TC-E2E-14 — Crear evento (ADMIN) — `POST /api/events`
- [ ] TC-E2E-15 — Consultar evento por código (ADMIN) — `GET /api/events/{{newEventCode}}`
- [ ] TC-E2E-16 — Listar eventos publicados (ANONYMOUS) — `GET /api/events/published`
- [ ] TC-E2E-18 — Asociar artista al evento (ADMIN) — `POST /api/events/{{newEventCode}}/artists/{{artistId}}`
- [ ] TC-E2E-17 — Publicar evento (ADMIN) — `PATCH /api/events/{{newEventCode}}/publish`
- [ ] TC-E2E-19 — Consultar eventos por artista (ANONYMOUS) — `GET /api/events/by-artist?stageName={{artistStageName}}`

## 06 Tickets / compra, consulta y ciclo de vida (6 rutas base + preparación)

- [ ] TC-E2E-20 — Comprar ticket (USER_A) — `POST /api/tickets`
- [ ] TC-E2E-21 — Consultar ticket por código (USER_A) — `GET /api/tickets/{{ticketCode}}`
- [ ] TC-E2E-22 — Listar tickets del usuario (USER_A) — `GET /api/tickets/by-user?email={{userAEmail}}`
- [ ] TC-E2E-23 — Consultar tickets pagados del evento (STAFF) — `GET /api/events/{{newEventCode}}/tickets/paid`
- [ ] TC-E2E-24 — Cancelar ticket (USER_A) — `PATCH /api/tickets/{{ticketCode}}/cancel`
- [ ] PREP — Comprar ticket independiente para TC-E2E-25 — `POST /api/tickets`
- [ ] TC-E2E-25 — Marcar ticket como usado (STAFF) — `PATCH /api/tickets/{{ticketCodeForUse}}/use`
- [ ] PREP — Verificar persistencia del ticket usado (TC-E2E-25) — `GET /api/tickets/{{ticketCodeForUse}}`
- [ ] PREP — Comprar ticket independiente para TC-SEC-012 y TC-BIZ-012/013/017 — `POST /api/tickets`
- [ ] PREP — Comprar ticket independiente para TC-SEC-014 — `POST /api/tickets`
- [ ] PREP — Comprar ticket independiente para TC-SEC-009/011 — `POST /api/tickets`

## 07 Seguridad / escenarios negativos

- [ ] TC-AUTH-007 — Ruta protegida sin token — `GET /api/auth/me`
- [ ] TC-AUTH-008 — Token inválido o malformado (ANONYMOUS) — `GET /api/auth/me`
- [ ] TC-AUTH-009 — JWT alterado (ANONYMOUS) — `GET /api/auth/me`
- [ ] TC-AUTH-010 — JWT expirado — `GET /api/auth/me`
- [ ] TC-AUTH-011 — JWT firmado por otra clave — `GET /api/auth/me`
- [ ] TC-AUTH-012 — Token por query string en lugar de Bearer — `GET /api/auth/me?access_token={{userAToken}}`
- [ ] TC-AUTH-013 — Usuario inactivo con token previo — `GET /api/auth/me`
- [ ] TC-AUTH-014 — Token de USER para endpoint ADMIN — `GET /api/events/{{newEventCode}}/tickets/paid`
- [ ] TC-AUTH-015 — JWT inválido en endpoint público — `GET /api/venues/active`
- [ ] TC-SEC-001 — Crear evento (ANONYMOUS) — `POST /api/events`
- [ ] TC-SEC-001 — Crear evento (USER_A) — `POST /api/events`
- [ ] TC-SEC-002 — Publicar evento (USER_A) — `PATCH /api/events/{{newEventCode}}/publish`
- [ ] TC-SEC-002 — Publicar evento (STAFF) — `PATCH /api/events/{{newEventCode}}/publish`
- [ ] TC-SEC-003 — Asociar artista (USER_A) — `POST /api/events/{{newEventCode}}/artists/{{artistId}}`
- [ ] TC-SEC-003B — Verificar evento tras mutaciones denegadas (ADMIN) — `GET /api/events/{{newEventCode}}`
- [ ] PREP — Crear evento DRAFT (ADMIN) — `POST /api/events`
- [ ] PREP — Asociar artista al DRAFT (ADMIN) — `POST /api/events/{{draftEventCode}}/artists/{{artistId}}`
- [ ] TC-SEC-004 — Consultar DRAFT por código (ANONYMOUS) — `GET /api/events/{{draftEventCode}}`
- [ ] TC-SEC-005 — Buscar eventos de artista con DRAFT (ANONYMOUS) — `GET /api/events/by-artist?stageName={{artistStageName}}`
- [ ] TC-SEC-006 — Consultar USER_A por email como USER_B — `GET /api/users/by-email?email={{userAEmail}}`
- [ ] TC-SEC-007 — Consultar USER_A por username como USER_B — `GET /api/users/by-username?username={{userAUsername}}`
- [ ] TC-SEC-008 — Comprar ticket usando email de USER_B (USER_A) — `POST /api/tickets`
- [ ] PREP — Verificar integridad de TC-SEC-008 (USER_B) — `GET /api/tickets/by-user?email={{userBEmail}}`
- [ ] TC-SEC-009 — Leer ticket de USER_A como USER_B — `GET /api/tickets/{{ticketCancelableCode}}`
- [ ] TC-SEC-010 — Listar tickets de USER_A como USER_B — `GET /api/tickets/by-user?email={{userAEmail}}`
- [ ] TC-SEC-011 — Cancelar ticket de USER_A como USER_B — `PATCH /api/tickets/{{ticketCancelableCode}}/cancel`
- [ ] TC-SEC-011B — Verificar ticket tras cancelación denegada (USER_A) — `GET /api/tickets/{{ticketCancelableCode}}`
- [ ] TC-SEC-012 — Marcar ticket de USER_A como usado por USER_B — `PATCH /api/tickets/{{ticketUsableCode}}/use`
- [ ] TC-SEC-012B — Verificar ticket tras uso denegado (USER_A) — `GET /api/tickets/{{ticketUsableCode}}`
- [ ] TC-SEC-013 — Consultar tickets pagados con USER_A — `GET /api/events/{{publishedEventCode}}/tickets/paid`
- [ ] TC-SEC-014 — Marcar ticket elegible como usado (STAFF) — `PATCH /api/tickets/{{ticketStaffUseCode}}/use`
- [ ] TC-SEC-015 — ADMIN consulta borrador — `GET /api/events/{{draftEventCode}}`
- [ ] TC-SEC-016 — Intentar registrar usuario con role=ADMIN — `POST /api/users`
- [ ] TC-SEC-016B — Login de usuario con role=ADMIN inyectado — `POST /api/auth/login`
- [ ] TC-SEC-016C — Usuario con role=ADMIN inyectado intenta ver tickets pagados — `GET /api/events/{{publishedEventCode}}/tickets/paid`
- [ ] TC-SEC-017 — Verificar revocación de privilegio con JWT previo — `GET /api/events/{{publishedEventCode}}/tickets/paid`

## 08 Validaciones y errores HTTP

- [ ] TC-HTTP-001 — Crear evento sin campos obligatorios — `POST /api/events`
- [ ] TC-HTTP-002 — Crear evento con JSON malformado — `POST /api/events`
- [ ] TC-HTTP-003 — Crear evento con categoría inválida — `POST /api/events`
- [ ] TC-HTTP-004 — Consultar evento inexistente — `GET /api/events/E2E-HTTP-NOT-FOUND`
- [ ] TC-HTTP-005 — Registrar usuario con email mal formado — `POST /api/users`
- [ ] TC-HTTP-006 — Registrar usuario duplicado — `POST /api/users`
- [ ] TC-HTTP-007 — Consultar ticket inexistente — `GET /api/tickets/TKT-E2E-NOT-FOUND`
- [ ] TC-HTTP-008 — Ruta protegida sin token — `GET /api/auth/me`

## 09 Flujos de negocio / secuencia recomendada

- [ ] TC-BIZ-001 — Descubrimiento público de venues — `GET /api/venues/active`
- [ ] TC-BIZ-002 — Descubrimiento público de artistas — `GET /api/artists/active`
- [ ] TC-BIZ-003 — Crear evento válido (ADMIN) — `POST /api/events`
- [ ] TC-BIZ-004 — Publicar evento (ADMIN) — `PATCH /api/events/{{eventToPublishCode}}/publish`
- [ ] TC-BIZ-005 — Asociar artista a evento (ADMIN) — `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}`
- [ ] TC-BIZ-006 — Registrar USER_A y USER_B — `POST /api/users`
- [ ] TC-BIZ-007 — Comprar ticket de evento publicado (USER_A) — `POST /api/tickets`
- [ ] TC-BIZ-008 — Consultar ticket por código y propietario (USER_A) — `GET /api/tickets/{{ticketCancelableCode}}`
- [ ] TC-BIZ-009 — Listar tickets pagados (STAFF) — `GET /api/events/{{eventToPublishCode}}/tickets/paid`
- [ ] TC-BIZ-010 — Cancelar ticket elegible por propietario (USER_A) — `PATCH /api/tickets/{{ticketCancelableCode}}/cancel`
- [ ] TC-BIZ-011 — Cancelar el mismo ticket por segunda vez — `PATCH /api/tickets/{{ticketCancelableCode}}/cancel`
- [ ] TC-BIZ-012 — Marcar otro ticket como usado (STAFF) — `PATCH /api/tickets/{{ticketUsableCode}}/use`
- [ ] TC-BIZ-013 — Marcar usado por segunda vez — `PATCH /api/tickets/{{ticketUsableCode}}/use`
- [ ] TC-BIZ-014 — Compra no elegible — `POST /api/tickets`
- [ ] TC-BIZ-015 — Rechazar evento con categoría inválida — `POST /api/events`
- [ ] TC-BIZ-016 — Consultar evento publicado por código (ANONYMOUS) — `GET /api/events/{{eventToPublishCode}}`
- [ ] TC-BIZ-017 — USER_A intenta marcar su ticket como usado — `PATCH /api/tickets/{{ticketUsableCode}}/use`

