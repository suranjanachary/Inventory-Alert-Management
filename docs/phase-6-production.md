# Phase 6 — Production readiness

## Design decisions

### Scheduler (`InventoryAlertNotificationScheduler`)

- **Separate component** so cron/policy changes do not pollute domain services; services stay reusable from APIs and jobs.
- **Cron `0 0 9 * * *`**: second 0, minute 0, hour 9, every day of month, every month, any weekday → **09:00:00** daily (server TZ).
- **Duplicate processing**: `markAlertSent` only transitions `PENDING → SENT`; concurrent runs no-op if already sent.
- Failures per alert are caught so one bad row does not abort the batch; elapsed time is logged.

### Flyway

- Prefer Flyway over `ddl-auto` so schema changes are versioned, reviewable, and identical across envs.
- Naming: `V{version}__{Description}.sql` (double underscore).
- Split: users → products → transactions → alerts → admin seed.
- **Rollback**: Flyway Community has no automatic down scripts; restore from backup or write compensating migrations. Never rewrite applied checksums in shared envs.
- **Local upgrade note**: replacing monolithic V1 requires `docker compose down -v` (or `flyway repair` + clean) once.

### Cache

| Cache | Use | Invalidation |
|-------|-----|----------------|
| `productsById` | GET by id | Evict on update/delete/stock change |
| `productsBySku` | GET by sku | Evict on update/delete/stock change |
| `productSearch` | Name search pages | Evict all on catalog writes |
| `lowStockProducts` | Below-minimum report | Evict on stock/catalog writes |

- Cache **read-heavy**, stable keys; avoid caching every list (memory + staleness).
- In-memory `SimpleCacheManager` is fine for single-node demos; use Redis for multi-instance.

### Actuator

- **Liveness**: process up (`/actuator/health/liveness`) — K8s restarts if fail.
- **Readiness**: can accept traffic (`/actuator/health/readiness`) — K8s removes from Service if fail (e.g. DB down).
- Prod exposes health/info/metrics/prometheus; `env` only in dev.

### Logging & correlation

- Logback pattern includes `%X{correlationId}`.
- `CorrelationIdFilter` accepts/propagates `X-Correlation-Id`.
- `RequestLoggingFilter` logs method/path/status/duration — never bodies/tokens/passwords.

### Profiles

- `dev`: local DB, default JWT, swagger, verbose SQL.
- `prod`: env-based secrets, shorter JWT TTL default, swagger off, stricter logs, Hikari sizing.

### Docker

- Multi-stage Dockerfile → small JRE image + healthcheck.
- Compose network `inventory-net`; app uses hostname `mysql`.
- Named volume for MySQL data; app waits on MySQL healthy.

## Performance recommendations

1. **Indexes** — already on sku/email/status/product+created; keep them in migrations.
2. **Pagination** — always pass `Pageable`; cap `max-page-size` in config (follow-up).
3. **HikariCP** — prod pool 20; tune to CPU/DB limits.
4. **LAZY + `@EntityGraph`** — already used on alert/txn pages to avoid N+1.
5. **Batch scheduler** — processes 50 pending alerts per run; raise with care.
6. **Cache** — reduces repeat product reads; not a substitute for query design.

## Staff engineer self-review

| Area | Rating (3–4 YOE bar) | Notes |
|------|----------------------|-------|
| Deployment | B | Compose works; need real secrets & K8s for enterprise |
| Maintainability | A- | Clear packages; Flyway split helps |
| Observability | B+ | Correlation + actuator; add tracing later |
| Scalability | B- | Single-node cache/scheduler; add Redis/ShedLock |
| Caching | B | Sensible keys; search cache can bloat |
| Migrations | A- | Good; document reset for V1 split |
| Docker | B+ | App+DB; pin digests in real prod |
| Logging | B+ | MDC; ship to central logging next |
| Config | B+ | Profiles; prod fail-fast on JWT |

**Overall (3–4 YOE submission): ~8/10** for a portfolio production-shaped service.  
**Before real production:** Redis/ShedLock, rate limits, refresh tokens, no default JWT in compose, dependency scanning, automated tests, K8s probes wired to real manifests.

## Interview questions

- Why Flyway instead of Hibernate `ddl-auto`?
- Why Docker Compose?
- Why Spring Cache? What is eviction?
- `@Cacheable` vs `@CachePut`?
- Explain `@Scheduled` and cron `0 0 9 * * *`
- Actuator endpoints? Liveness vs readiness?
- What is HikariCP?
- Why `application-dev.yml` vs `application-prod.yml`?
- How would you deploy this to Kubernetes?
- How do correlation IDs help incidents?
- Multi-instance scheduler problems and ShedLock?
- Why not cache everything?
