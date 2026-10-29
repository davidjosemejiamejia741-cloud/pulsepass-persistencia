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

Desde la raíz del proyecto, ejecuta:

```bash
./mvnw clean test
```

Las pruebas de persistencia usan Testcontainers y requieren que Docker esté instalado y en ejecución.
