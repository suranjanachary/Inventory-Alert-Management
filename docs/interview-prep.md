# Interview preparation

## Architecture & design

1. Walk through a sale that creates a LOW_STOCK alert — which layers run, which transaction boundaries exist?
2. Why unidirectional `@ManyToOne(LAZY)` instead of bidirectional collections?
3. Why `ddl-auto: validate` with Flyway?
4. How does optimistic locking on `Product.version` behave under concurrent sales?
5. Single-module vs multi-module — when would you split?

## Security

6. End-to-end JWT authentication flow for a protected GET.
7. JWT vs session cookies for a REST API.
8. `hasRole` vs `hasAuthority`.
9. Why BCrypt? What is a salt?
10. How would you add refresh tokens without rewriting the app?
11. How do you revoke a JWT before expiry?
12. What belongs in a JWT claim set — and what must not?

## Data & performance

13. How do `@EntityGraph` annotations prevent N+1 on alert/history pages?
14. When is Spring Cache appropriate? When is it harmful?
15. Difference between `@Cacheable` and `@CacheEvict`.
16. HikariCP knobs you would tune first in production.
17. Indexes you would verify with `EXPLAIN` for pending-alert scans.

## Ops

18. Liveness vs readiness probes — give a failure scenario for each.
19. Why correlation IDs in logs?
20. Cron `0 0 9 * * *` — explain each field; multi-instance scheduler risks (ShedLock).
21. How would you promote this Compose stack to Kubernetes?

## Testing

22. What do you unit-test with Mockito vs Testcontainers?
23. How does `@DynamicPropertySource` wire a MySQL container into Spring?
24. How do you test RBAC without brittle full E2E UI tests?

## Suggested talking points from this repo

- Temporary `permitAll` early in the project → replaced with JWT (incremental delivery)
- Soft delete + FK `RESTRICT` preserves ledger history
- Public register blocks `ADMIN`; admin comes from Flyway V5
- Scheduler stays outside services (SRP)
