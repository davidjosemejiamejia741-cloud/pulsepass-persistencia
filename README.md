# PulsePass

## Endpoints principales

| Método | Endpoint | Descripción |
| --- | --- | --- |
| `GET` | `/api/venues/{code}` | Consultar venue por código |
| `GET` | `/api/venues/active` | Listar venues activos |
| `POST` | `/api/events` | Crear evento |
| `GET` | `/api/events/{eventCode}` | Consultar evento por código |
| `GET` | `/api/events/published` | Listar eventos publicados |
| `PATCH` | `/api/events/{eventCode}/publish` | Publicar evento |
| `POST` | `/api/events/{eventCode}/artists/{artistId}` | Asociar artista a un evento |
| `GET` | `/api/events/by-artist?stageName={stageName}` | Buscar eventos por artista |
| `GET` | `/api/artists/{id}` | Consultar artista por ID |
| `GET` | `/api/artists/by-stage-name?stageName={stageName}` | Consultar artista por nombre artístico |
| `GET` | `/api/artists/active` | Listar artistas activos |
| `POST` | `/api/users` | Registrar usuario |
| `GET` | `/api/users/by-email?email={email}` | Consultar usuario por email |
| `GET` | `/api/users/by-username?username={username}` | Consultar usuario por username |
| `POST` | `/api/tickets` | Comprar ticket |
| `GET` | `/api/tickets/{ticketCode}` | Consultar ticket por código |
| `GET` | `/api/tickets/by-user?email={email}` | Listar tickets de un usuario |
| `GET` | `/api/events/{eventCode}/tickets/paid` | Listar tickets pagados de un evento |
| `PATCH` | `/api/tickets/{ticketCode}/cancel` | Cancelar ticket |
| `PATCH` | `/api/tickets/{ticketCode}/use` | Marcar ticket como usado |

## Ejecutar las pruebas

Desde la raíz del proyecto:

```bash
./mvnw clean test
```

En PowerShell:

```powershell
.\mvnw.cmd clean test
```

Las pruebas de integración con Testcontainers requieren Docker en ejecución. Los resultados de las pruebas deben comprobarse después de ejecutar el comando; los reportes existentes en `target/` no sustituyen una ejecución actual.

# Fase 4 — Seguridad (Spring Security / JJWT)

## Requisitos y ejecución

- Java 21 y Maven Wrapper incluido.
- PostgreSQL con la base de datos creada previamente.
- Docker para las pruebas de integración con Testcontainers.
- Clave JWT Base64 válida de al menos 32 bytes.

El archivo `application.yml` configura por defecto la conexión a `jdbc:postgresql://localhost:5432/pulsepass`, usuario `postgres` y contraseña `admin`. Estos valores pueden sobrescribirse con variables de entorno. Flyway aplica las migraciones V1–V5 dentro de la base existente; no crea la base de datos.

Linux / WSL:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/pulsepass_e2e
export DB_USER=postgres
export DB_PASSWORD='TU_PASSWORD_LOCAL'
export PULSEPASS_JWT_SECRET_BASE64="$(openssl rand -base64 32)"
./mvnw spring-boot:run
```

PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/pulsepass_e2e"
$env:DB_USER="postgres"
$env:DB_PASSWORD="TU_PASSWORD_LOCAL"
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
$rng.Dispose()
.\mvnw.cmd spring-boot:run
```

No compartas la clave ni la incluyas en Git. La clave debe mantenerse estable entre reinicios si se pretende que los JWT emitidos anteriormente continúen siendo válidos hasta su vencimiento. Una clave ausente, inválida o insuficiente impide inicializar correctamente la seguridad.

## Variables de entorno

| Variable | Función | Valor predeterminado |
| --- | --- | --- |
| `DB_URL` | Conexión JDBC | `jdbc:postgresql://localhost:5432/pulsepass_e2e` |
| `DB_USER` | Usuario PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña PostgreSQL | `admin` (solo configuración local) |
| `PULSEPASS_JWT_SECRET_BASE64` | Clave HS256 en Base64 | Obligatoria |
| `PULSEPASS_JWT_ISSUER` | Emisor JWT | `pulsepass-api` |
| `PULSEPASS_JWT_TTL` | Vigencia del JWT | `PT15M` |
| `PULSEPASS_CORS_ORIGINS` | Orígenes CORS separados por comas | Vacío |
| `PULSEPASS_REQUIRE_HTTPS` | Requisito de HTTPS | `false` |


## Endpoints nuevos de autenticación

| Método | Endpoint | Acceso | Resultado |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Público | `200 AuthResponse` |
| `GET` | `/api/auth/me` | Autenticado | `200 AuthenticatedUserResponse` |

## Autenticación

### Registro público

`POST /api/users` conserva su endpoint y ahora exige `password`, de al menos 12 caracteres y máximo 72 bytes UTF-8. El registro público asigna `USER` y no permite elegir roles privilegiados. La respuesta es `201 Created` con `UserResponse`, sin contraseña, hash ni token.

```json
{
  "username": "nuevo_usuario",
  "email": "nuevo@example.com",
  "firstName": "Andrea",
  "lastName": "Pérez",
  "phone": null,
  "city": "Santa Marta",
  "birthDate": "1990-01-01",
  "password": "EjemploSoloDocumentacion2026!"
}
```

### Inicio de sesión

`POST /api/auth/login` es público y acepta el identificador de usuario o correo y la contraseña:

```json
{
  "identifier": "nuevo_usuario",
  "password": "EjemploSoloDocumentacion2026!"
}
```

Una autenticación correcta devuelve `accessToken`, `tokenType` (`Bearer`) y `expiresIn` (900 segundos por defecto). Credenciales incorrectas generan `401 Unauthorized`.

### Usuario autenticado

`GET /api/auth/me` requiere:

```http
Authorization: Bearer <token>
```

La API usa sesiones `STATELESS`, valida firma HS256, emisor, sujeto y expiración del JWT, y consulta la identidad y permisos vigentes del usuario.

## Roles de prueba (escenario del PRD)

El PRD utiliza los siguientes actores para verificar la matriz de permisos. Son **identidades de demostración**, no credenciales predeterminadas ni cuentas creadas automáticamente por la aplicación.

| Actor | Rol de prueba | Escenario |
| --- | --- | --- |
| Visitante | ANONYMOUS | Consultar recursos públicos, registrarse e iniciar sesión. |
| Andrea | USER | Comprar y consultar tickets propios; intentar acceso a tickets de Carlos. |
| Carlos | USER | Titular de recursos que Andrea no debe poder consultar ni modificar. |
| Laura | STAFF | Marcar tickets como utilizados y consultar operaciones autorizadas. |
| Miguel | ADMIN | Crear y publicar eventos y asociar artistas. |

Para ejecutar los escenarios se necesitan cuentas activas con contraseñas individuales y roles asignados de forma controlada. El registro público crea únicamente cuentas USER. El PRD no proporciona contraseñas compartidas para estas identidades.

## Roles y autorización

- **ANONYMOUS:** acceso a venues, artistas, eventos publicados, registro y login.
- **USER:** consulta de su información, compra de tickets propios y operaciones permitidas sobre sus entradas.
- **STAFF:** operaciones de control de acceso y consultas operativas autorizadas.
- **ADMIN:** creación y publicación de eventos, asociación de artistas y operaciones administrativas autorizadas.

Las rutas de eventos por código y por artista deben ocultar eventos no publicados a usuarios sin privilegios. La consulta, compra y cancelación de tickets deben validar propiedad; conocer el código o correo de otro usuario no concede acceso.

Los usuarios existentes antes de la migración de seguridad no reciben automáticamente contraseñas ni roles privilegiados. Su activación y la asignación de `STAFF` o `ADMIN` requieren un procedimiento administrativo controlado. **Este repositorio no incluye `scripts/provision_identity.py`**, por lo que no se documenta como comando ejecutable.

## Migraciones Flyway

```text
src/main/resources/db/migration/
├── V1__create_schema.sql
├── V2__insert_initial_artists.sql
├── V3__add_streaming_url_to_event.sql
├── V4__add_enum_constraints_and_indexes.sql
└── V5__security_credentials_roles.sql
```

La migración V5 incorpora credenciales y roles. Hibernate utiliza `ddl-auto: validate`, por lo que valida el esquema sin reemplazar las migraciones.

## Verificación de seguridad

Suite completa:

```bash
./mvnw clean verify
```

Pruebas de seguridad seleccionadas:

```bash
./mvnw -Dtest=JwtServiceTest,AuthServiceTest,AuthorizationServiceTest,CustomUserDetailsServiceTest,JwtAuthenticationFilterTest,SecurityHttpTest,MethodSecurityTest,EventVisibilityTest test
```

En Windows, sustituir `./mvnw` por `.\mvnw.cmd`.

El repositorio contiene pruebas de JWT, autenticación, autorización, filtro Bearer, seguridad HTTP, seguridad por métodos, visibilidad de eventos y pruebas de integración/migración. No se afirma que la suite completa esté aprobada sin una ejecución actual exitosa.

## Respuestas de error

La API mantiene su contrato `ErrorResponse` para validación y errores de negocio. Los errores de autenticación y autorización se representan en JSON mediante los handlers de Spring Security:

- `400`: solicitud inválida.
- `401`: credenciales o token ausentes, inválidos o vencidos.
- `403`: usuario autenticado sin permisos.
- `404`: recurso no encontrado.
- `409`: conflicto de reglas de negocio.
- `500`: error interno sin exposición de detalles sensibles.

## Limitaciones del MVP

No incluye MFA, refresh tokens, recuperación de contraseña, SSO ni revocación instantánea por `jti`. La autorización global de `STAFF` para ciertas operaciones de tickets deberá restringirse por evento en una evolución posterior. El despliegue fuera de un entorno local debe asegurar HTTPS, manejo de secretos y controles operativos adicionales.
