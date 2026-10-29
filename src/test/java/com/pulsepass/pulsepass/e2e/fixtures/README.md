# Fixtures E2E — preparación controlada

Los datos deben existir exclusivamente en una base PostgreSQL aislada para pruebas. No se incluye `seed-e2e.sql` porque es opcional y no se ha validado un SQL reproducible contra el esquema y las migraciones del proyecto.

## Preparación manual

1. Aplicar migraciones Flyway y comprobar conectividad de la API.
2. Verificar un venue activo y un artista activo; copiar sus identificadores reales al environment.
3. Registrar USER_A y USER_B mediante la API y guardar contraseñas únicamente de forma local.
4. Provisionar STAFF, ADMIN e INACTIVE_USER mediante el procedimiento seguro permitido por la Fase 4, sin escalamiento público de roles.
5. Preparar eventos de prueba con estados y capacidad/edad apropiados para escenarios de publicación, compra, cancelación, uso y agotamiento; registrar sus códigos reales.
6. Para pruebas de propiedad, usar recursos creados por un usuario y consultar con el token del otro.
7. Repetir una ejecución con un `runId` nuevo o limpiar datos exclusivamente de la base E2E con un procedimiento aprobado; no borrar datos de producción.

Registrar cualquier fixture faltante como BLOCKED en la bitácora, no como PASS.
