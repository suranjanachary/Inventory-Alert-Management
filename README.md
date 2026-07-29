# Inventory Alert Management

Production-oriented Spring Boot service for product catalog, stock mutations, and low-stock alerts with JWT RBAC.

## Architecture

```
Controllers → Services → Repositories → MySQL
                ↓
         Scheduler (alert email mock)
                ↓
         Cache (product reads)
```

Layered package layout under `com.inventory.alert` (controller / service / repository / entity / security / scheduler / config).

## Tech stack

- Java 17, Spring Boot 3.5.16
- Spring Web, Data JPA, Security (JWT), Validation, Cache, Actuator
- Flyway, MySQL 8, MapStruct, Lombok, springdoc OpenAPI
- Docker / Docker Compose

## Features

- Product CRUD (soft delete), inventory purchase/sale + ledger
- Low-stock alerts with daily 09:00 notification scheduler
- JWT auth + ADMIN / MANAGER / VIEWER RBAC
- Flyway migrations, Spring Cache, correlation-id request logging
- Actuator health/liveness/readiness + metrics (+ prometheus)

## How to run (local)

```bash
# Reset DB if upgrading from the old single V1 migration
docker compose down -v
docker compose up -d mysql

mvn spring-boot:run
# profile defaults to dev
```

## Docker (app + MySQL)

```bash
cp .env.example .env   # set JWT_SECRET for real use
docker compose up -d --build
```

- App: http://localhost:8080  
- MySQL volume: `inventory_alert_mysql_data`  
- App joins Docker network hostname `mysql`

## URLs

| Resource | URL |
|----------|-----|
| Swagger UI (dev) | http://localhost:8080/swagger-ui.html |
| OpenAPI | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |
| Liveness | http://localhost:8080/actuator/health/liveness |
| Readiness | http://localhost:8080/actuator/health/readiness |
| Metrics | http://localhost:8080/actuator/metrics |
| Prometheus | http://localhost:8080/actuator/prometheus |

## Sample credentials

| Email | Password | Role |
|-------|----------|------|
| `admin@inventory.local` | `AdminPass123!` | ADMIN (Flyway V5) |

Register MANAGER/VIEWER via `POST /api/v1/auth/register`.

## API flow

1. `POST /api/v1/auth/login` → Bearer token  
2. Authorize in Swagger or `Authorization: Bearer <token>`  
3. Create product → purchase/sale → pending LOW_STOCK alert when qty ≤ minimum  
4. Scheduler at 09:00 mocks email and marks alerts `SENT`  
5. `PUT /api/v1/alerts/{id}/resolve` when handled

## Folder structure

```
src/main/java/com/inventory/alert/
  config/ controller/ dto/ entity/ enums/ exception/
  mapper/ repository/ scheduler/ security/ service/
src/main/resources/
  application.yml / application-dev.yml / application-prod.yml
  db/migration/  logback-spring.xml
docs/  Dockerfile  docker-compose.yml
```

## Configuration

| Property | Purpose |
|----------|---------|
| `app.jwt.secret` / `JWT_SECRET` | Base64 HS256 key (required in prod) |
| `app.jwt.expiration-ms` | Access token TTL |
| `app.scheduler.alert-cron` | Cron for alert dispatch (`0 0 9 * * *`) |
| `SPRING_PROFILES_ACTIVE` | `dev` or `prod` |

## Documentation

- [`docs/phase-6-production.md`](docs/phase-6-production.md) — design decisions, ops notes, self-review, interview Qs  
- Earlier phases: `docs/phase-2` … `phase-5`

## Future improvements

- Refresh tokens + rate limiting  
- Redis cache / ShedLock for multi-instance schedulers  
- Real email provider  
- Kubernetes manifests + External Secrets  
- Integration/E2E test suite  
