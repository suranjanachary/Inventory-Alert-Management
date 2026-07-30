# Phase 7 — Testing

## Strategy

| Layer | Tooling | Purpose |
|-------|---------|---------|
| Unit | JUnit 5 + Mockito | Fast service/JWT rules without DB |
| Repository / API IT | Testcontainers MySQL + `@SpringBootTest` + MockMvc | Real Flyway schema + security filter chain |

Scheduler disabled in `application-test.yml` (`app.scheduler.enabled=false`).

## How to run

```bash
# Unit + integration (Docker required for Testcontainers)
mvn test

# Unit only (no Docker)
mvn test -Dtest='*Test'

# Integration only
mvn test -Dtest='*IT'
```

`@Testcontainers(disabledWithoutDocker = true)` skips IT classes cleanly when Docker is unavailable.

## Coverage highlights

- `InventoryServiceImplTest` — purchase, insufficient stock, inactive product, low-stock alert trigger
- `ProductServiceImplTest` — duplicate SKU, create, soft delete, not found
- `AuthenticationServiceImplTest` — register/login rules
- `InventoryAlertServiceImplTest` — SENT transition idempotency
- `JwtServiceTest` — issue/parse + expiry
- `AuthAndProductApiIT` — login, RBAC 403, sale/history, validation 400
- `ProductRepositoryIT` — SKU lookup + below-minimum query
