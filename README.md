# Inventory Alert Management

Phase 1 scaffold: compile-ready Spring Boot skeleton with layered packages, config stubs, and Docker MySQL. No domain features yet.

## Phase 4 — REST API

Controllers, global exception handling, Springdoc. See [`docs/phase-4-api.md`](docs/phase-4-api.md).

Swagger UI: http://localhost:8080/swagger-ui.html

## Phase 3 — Services

Business layer (Product / Inventory / Alerts). See [`docs/phase-3-services.md`](docs/phase-3-services.md).

## Phase 2 — Persistence

Database design, Flyway schema, JPA entities, repositories, and DTOs (no services yet).

- Design notes: [`docs/database-design.md`](docs/database-design.md)
- Annotations & self-review: [`docs/phase-2-persistence.md`](docs/phase-2-persistence.md)
- Migration: `src/main/resources/db/migration/V1__create_inventory_schema.sql`

```bash
docker compose up -d
mvn -q compile
# After MySQL is healthy, Flyway runs on application start
mvn spring-boot:run
```

## Stack

| Item | Choice |
|------|--------|
| Java | 17 (LTS; Boot 3.5 supports 17–25) |
| Spring Boot | 3.5.16 (final OSS patch; **EOL for real production** — acceptable for this portfolio demo) |
| Build | Single Maven module |
| DB | MySQL 8 via Docker Compose (schema / Flyway in Phase 2) |

## Quick start (local)

```bash
# Start MySQL
docker compose up -d

# Compile (Phase 1 gate)
mvn -q compile

# Run (needs MySQL healthy; no business APIs yet)
mvn spring-boot:run
```

- Swagger UI (once running): http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator/health

## Package layout

```
com.inventory.alert/
  config/ controller/ dto/{request,response}/ entity/ exception/
  mapper/ repository/ security/ service/ scheduler/ util/ constants/
```

**Dependency rule:** Controllers never depend on entities; Repositories never depend on Controllers; Services own business rules.

## Phase 1 design decisions

1. **Single module (not multi-module)** — Matches the package layout, keeps the demo focused, faster review. Multi-module (`api` / `domain` / `infra`) is better for large teams but overkill here.
2. **Temporary `permitAll` SecurityConfig** — Security and JWT deps are on the classpath early so later phases do not thrash `pom.xml`. Without an open chain, Boot’s default security would block `./mvn spring-boot:run` from day one. **PHASE 9** replaces this with JWT + authorization. This is demo pragmatism, not a production pattern.
3. **Spring Boot 3.5.16 EOL** — Portfolio OK; do not treat as a production baseline without a supported Boot line.
4. **Java 17 instead of 21** — Still a valid LTS for Boot 3.5; system JDK available. Documented deviation if an original brief preferred 21.
5. **Lombok + MapStruct processor order** — Compiler annotation processor path is Lombok → lombok-mapstruct-binding → mapstruct-processor (common interview / build footgun).
6. **`ddl-auto: none`** — No Flyway/Liquibase yet; schema design is Phase 2. App datasource points at Compose MySQL but does not mutate schema.

## What Phase 1 deliberately excludes

Entities, repositories, DTOs, services, controllers (beyond stubs), JWT logic, scheduler, Flyway, and feature tests. Next gate after review: Phase 2 database design / ERD / indexes / optimistic locking.

## Later phases (reminder)

2 DB design → 3 Entities → 4 Repositories → 5 DTOs → 6 Mappers → 7 Services → 8 Controllers → 9 Security → 10 Scheduler → 11 Testing → 12 Code review
