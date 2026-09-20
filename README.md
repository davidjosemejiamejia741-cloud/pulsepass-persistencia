# PulsePass

MVP académico de persistencia para gestionar venues, eventos, artistas, usuarios, perfiles y tickets. Está construido con Java 21, Spring Boot, JPA, PostgreSQL y Flyway.

## Modelo

- `Venue` 1:N `Event`.
- `Event` N:M `Artist`, mediante `event_artists`.
- `User` 1:1 `UserProfile`.
- `User` 1:N `Ticket` y `Event` 1:N `Ticket`.

Los enums se almacenan por nombre (`EnumType.STRING`). Los identificadores de negocio son únicos y PostgreSQL valida capacidad positiva, precio no negativo y los valores permitidos para enums.

## Migraciones

Flyway administra el esquema desde `src/main/resources/db/migration`:

1. `V1__create_schema.sql`: tablas, claves, relaciones y constraints básicos.
2. `V2__insert_initial_artists.sql`: artistas de referencia.
3. `V3__add_streaming_url_to_event.sql`: URL de streaming opcional.
4. `V4__add_enum_constraints_and_indexes.sql`: constraints para enums e índices de claves foráneas.

No se deben modificar migraciones ya aplicadas; los cambios estructurales se agregan en una nueva versión.

## Consultas

Los repositories incluyen Query Methods para búsquedas simples (por código, email, estado y relaciones) y JPQL para búsquedas por artista, ciudad, recomendaciones y conteo de tickets pagados.

## Ejecutar

Para arrancar la aplicación se requiere PostgreSQL, configurable mediante `DB_URL`, `DB_USER` y `DB_PASSWORD`.

```bash
./mvnw spring-boot:run
```

Las pruebas de integración utilizan PostgreSQL mediante Testcontainers; se necesita Docker disponible:

```bash
./mvnw clean test
```
